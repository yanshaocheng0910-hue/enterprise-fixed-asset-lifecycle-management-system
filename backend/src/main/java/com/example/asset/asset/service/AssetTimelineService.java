package com.example.asset.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.asset.approval.entity.ApprovalInstance;
import com.example.asset.approval.entity.ApprovalRecord;
import com.example.asset.approval.mapper.ApprovalInstanceMapper;
import com.example.asset.approval.mapper.ApprovalRecordMapper;
import com.example.asset.asset.dto.AssetTimelineQueryRequest;
import com.example.asset.asset.entity.Asset;
import com.example.asset.asset.entity.AssetOperationLog;
import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.asset.mapper.AssetOperationLogMapper;
import com.example.asset.asset.vo.AssetTimelineEventVO;
import com.example.asset.common.BusinessException;
import com.example.asset.common.ResultCode;
import com.example.asset.lifecycle.entity.InboundOrder;
import com.example.asset.lifecycle.entity.ReceiveOrder;
import com.example.asset.lifecycle.entity.RepairOrder;
import com.example.asset.lifecycle.entity.ScrapOrder;
import com.example.asset.lifecycle.entity.TransferOrder;
import com.example.asset.lifecycle.mapper.InboundOrderMapper;
import com.example.asset.lifecycle.mapper.ReceiveOrderMapper;
import com.example.asset.lifecycle.mapper.RepairOrderMapper;
import com.example.asset.lifecycle.mapper.ScrapOrderMapper;
import com.example.asset.lifecycle.mapper.TransferOrderMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetTimelineService {

    private final AssetMapper assetMapper;
    private final AssetOperationLogMapper assetOperationLogMapper;
    private final InboundOrderMapper inboundOrderMapper;
    private final ReceiveOrderMapper receiveOrderMapper;
    private final TransferOrderMapper transferOrderMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ScrapOrderMapper scrapOrderMapper;
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ApprovalRecordMapper approvalRecordMapper;

    public List<AssetTimelineEventVO> getTimeline(Long assetId, AssetTimelineQueryRequest query) {
        // 1. 校验资产存在
        Asset asset = assetMapper.selectById(assetId);
        if (asset == null || (asset.getDeleted() != null && asset.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资产不存在");
        }

        List<AssetTimelineEventVO> events = new ArrayList<>();

        // 2. 查操作日志
        events.addAll(queryOperationLogs(assetId));

        // 3. 查5类生命周期单据
        List<ReceiveOrder> receiveOrders = getReceiveOrderList(assetId);
        List<TransferOrder> transferOrders = getTransferOrderList(assetId);
        List<RepairOrder> repairOrders = getRepairOrderList(assetId);
        List<ScrapOrder> scrapOrders = getScrapOrderList(assetId);

        events.addAll(queryInboundOrders(assetId));
        events.addAll(mapReceiveOrders(receiveOrders));
        events.addAll(mapTransferOrders(transferOrders));
        events.addAll(mapRepairOrders(repairOrders));
        events.addAll(mapScrapOrders(scrapOrders));

        // 4. 审批事件聚合
        events.addAll(queryApprovalEvents(receiveOrders, transferOrders, repairOrders, scrapOrders));

        // 5. 过滤
        String eventType = query != null ? query.getEventType() : null;
        if (eventType != null && !eventType.isEmpty()) {
            events = events.stream()
                    .filter(e -> eventType.equals(e.getEventType()))
                    .collect(Collectors.toList());
        }

        // 6. 按时间倒序
        events.sort(Comparator.comparing(
                AssetTimelineEventVO::getEventTime,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return events;
    }

    // ===== 操作日志 =====

    private List<AssetTimelineEventVO> queryOperationLogs(Long assetId) {
        List<AssetOperationLog> logs = assetOperationLogMapper.selectList(
                new LambdaQueryWrapper<AssetOperationLog>()
                        .eq(AssetOperationLog::getAssetId, assetId));

        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (AssetOperationLog log : logs) {
            AssetTimelineEventVO vo = new AssetTimelineEventVO();
            vo.setId("OPERATION_LOG-" + log.getId());
            vo.setAssetId(assetId);
            vo.setEventType("OPERATION_LOG");
            vo.setEventTypeName("操作日志");
            vo.setTitle(log.getOperationName() != null ? log.getOperationName() : "操作日志");
            vo.setOperatorName(log.getOperatorName());
            vo.setEventTime(log.getOperationTime());
            vo.setBeforeStatus(log.getBeforeStatus());
            vo.setAfterStatus(log.getAfterStatus());
            vo.setSource("asset_operation_log");
            vo.setRemark(log.getRemark());

            StringBuilder desc = new StringBuilder();
            if (log.getOperationName() != null) desc.append("操作：").append(log.getOperationName());
            if (log.getBeforeStatus() != null || log.getAfterStatus() != null) {
                desc.append("，状态变化：").append(log.getBeforeStatus()).append(" → ").append(log.getAfterStatus());
            }
            if (log.getRemark() != null && !log.getRemark().isEmpty()) {
                desc.append("，备注：").append(log.getRemark());
            }
            vo.setDescription(desc.toString());
            events.add(vo);
        }
        return events;
    }

    // ===== 入库单 =====

    private List<AssetTimelineEventVO> queryInboundOrders(Long assetId) {
        List<InboundOrder> orders = inboundOrderMapper.selectList(
                new LambdaQueryWrapper<InboundOrder>()
                        .eq(InboundOrder::getAssetId, assetId));

        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (InboundOrder order : orders) {
            AssetTimelineEventVO vo = new AssetTimelineEventVO();
            vo.setId("INBOUND-" + order.getId());
            vo.setAssetId(assetId);
            vo.setEventType("INBOUND");
            vo.setEventTypeName("入库");
            vo.setTitle("资产入库");
            vo.setOrderCode(order.getOrderCode());
            vo.setBusinessType("INBOUND");
            vo.setBusinessId(order.getId());
            vo.setStatus(order.getStatus());
            vo.setBeforeStatus(order.getBeforeStatus());
            vo.setAfterStatus(order.getAfterStatus());
            vo.setEventTime(order.getCreatedAt());
            vo.setSource("asset_inbound_order");
            vo.setRemark(order.getRemark());

            StringBuilder desc = new StringBuilder();
            desc.append("入库单：").append(order.getOrderCode());
            if (order.getInboundType() != null) desc.append("，入库类型：").append(order.getInboundType());
            if (order.getSupplier() != null) desc.append("，供应商：").append(order.getSupplier());
            if (order.getHandler() != null) desc.append("，经办人：").append(order.getHandler());
            if (order.getStatus() != null) desc.append("，状态：").append(order.getStatus());
            vo.setDescription(desc.toString());
            events.add(vo);
        }
        return events;
    }

    // ===== 领用单 =====

    private List<ReceiveOrder> getReceiveOrderList(Long assetId) {
        return receiveOrderMapper.selectList(
                new LambdaQueryWrapper<ReceiveOrder>()
                        .eq(ReceiveOrder::getAssetId, assetId));
    }

    private List<AssetTimelineEventVO> mapReceiveOrders(List<ReceiveOrder> orders) {
        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (ReceiveOrder order : orders) {
            AssetTimelineEventVO vo = new AssetTimelineEventVO();
            vo.setId("RECEIVE-" + order.getId());
            vo.setAssetId(order.getAssetId());
            vo.setEventType("RECEIVE");
            vo.setEventTypeName("领用");
            vo.setTitle("资产领用");
            vo.setOrderCode(order.getOrderCode());
            vo.setBusinessType("RECEIVE");
            vo.setBusinessId(order.getId());
            vo.setStatus(order.getStatus());
            vo.setBeforeStatus(order.getBeforeStatus());
            vo.setAfterStatus(order.getAfterStatus());
            vo.setEventTime(order.getCreatedAt());
            vo.setSource("asset_receive_order");
            vo.setRemark(order.getRemark());

            StringBuilder desc = new StringBuilder();
            desc.append("领用单：").append(order.getOrderCode());
            if (order.getReceiver() != null) desc.append("，领用人：").append(order.getReceiver());
            if (order.getReceiverDepartment() != null) desc.append("，领用部门：").append(order.getReceiverDepartment());
            if (order.getStatus() != null) desc.append("，状态：").append(order.getStatus());
            vo.setDescription(desc.toString());
            events.add(vo);
        }
        return events;
    }

    // ===== 调拨单 =====

    private List<TransferOrder> getTransferOrderList(Long assetId) {
        return transferOrderMapper.selectList(
                new LambdaQueryWrapper<TransferOrder>()
                        .eq(TransferOrder::getAssetId, assetId));
    }

    private List<AssetTimelineEventVO> mapTransferOrders(List<TransferOrder> orders) {
        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (TransferOrder order : orders) {
            AssetTimelineEventVO vo = new AssetTimelineEventVO();
            vo.setId("TRANSFER-" + order.getId());
            vo.setAssetId(order.getAssetId());
            vo.setEventType("TRANSFER");
            vo.setEventTypeName("调拨");
            vo.setTitle("资产调拨");
            vo.setOrderCode(order.getOrderCode());
            vo.setBusinessType("TRANSFER");
            vo.setBusinessId(order.getId());
            vo.setStatus(order.getStatus());
            vo.setBeforeStatus(order.getBeforeStatus());
            vo.setAfterStatus(order.getAfterStatus());
            vo.setEventTime(order.getCreatedAt());
            vo.setSource("asset_transfer_order");
            vo.setRemark(order.getRemark());

            StringBuilder desc = new StringBuilder();
            desc.append("调拨单：").append(order.getOrderCode());
            if (order.getFromDepartment() != null && order.getToDepartment() != null) {
                desc.append("，").append(order.getFromDepartment()).append(" → ").append(order.getToDepartment());
            }
            if (order.getStatus() != null) desc.append("，状态：").append(order.getStatus());
            vo.setDescription(desc.toString());
            events.add(vo);
        }
        return events;
    }

    // ===== 维修单 =====

    private List<RepairOrder> getRepairOrderList(Long assetId) {
        return repairOrderMapper.selectList(
                new LambdaQueryWrapper<RepairOrder>()
                        .eq(RepairOrder::getAssetId, assetId));
    }

    private List<AssetTimelineEventVO> mapRepairOrders(List<RepairOrder> orders) {
        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (RepairOrder order : orders) {
            AssetTimelineEventVO vo = new AssetTimelineEventVO();
            vo.setId("REPAIR-" + order.getId());
            vo.setAssetId(order.getAssetId());
            vo.setEventType("REPAIR");
            vo.setEventTypeName("维修");
            vo.setTitle("资产维修");
            vo.setOrderCode(order.getOrderCode());
            vo.setBusinessType("REPAIR");
            vo.setBusinessId(order.getId());
            vo.setStatus(order.getStatus());
            vo.setBeforeStatus(order.getBeforeStatus());
            vo.setAfterStatus(order.getAfterStatus());
            vo.setEventTime(order.getCreatedAt());
            vo.setSource("asset_repair_order");
            vo.setRemark(order.getRemark());

            StringBuilder desc = new StringBuilder();
            desc.append("维修单：").append(order.getOrderCode());
            if (order.getFaultDescription() != null) desc.append("，故障：").append(order.getFaultDescription());
            if (order.getRepairVendor() != null) desc.append("，维修商：").append(order.getRepairVendor());
            if (order.getStatus() != null) desc.append("，状态：").append(order.getStatus());
            vo.setDescription(desc.toString());
            events.add(vo);
        }
        return events;
    }

    // ===== 报废单 =====

    private List<ScrapOrder> getScrapOrderList(Long assetId) {
        return scrapOrderMapper.selectList(
                new LambdaQueryWrapper<ScrapOrder>()
                        .eq(ScrapOrder::getAssetId, assetId));
    }

    private List<AssetTimelineEventVO> mapScrapOrders(List<ScrapOrder> orders) {
        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (ScrapOrder order : orders) {
            AssetTimelineEventVO vo = new AssetTimelineEventVO();
            vo.setId("SCRAP-" + order.getId());
            vo.setAssetId(order.getAssetId());
            vo.setEventType("SCRAP");
            vo.setEventTypeName("报废");
            vo.setTitle("资产报废");
            vo.setOrderCode(order.getOrderCode());
            vo.setBusinessType("SCRAP");
            vo.setBusinessId(order.getId());
            vo.setStatus(order.getStatus());
            vo.setBeforeStatus(order.getBeforeStatus());
            vo.setAfterStatus(order.getAfterStatus());
            vo.setEventTime(order.getCreatedAt());
            vo.setSource("asset_scrap_order");
            vo.setRemark(order.getRemark());

            StringBuilder desc = new StringBuilder();
            desc.append("报废单：").append(order.getOrderCode());
            if (order.getScrapReason() != null) desc.append("，报废原因：").append(order.getScrapReason());
            if (order.getDisposalMethod() != null) desc.append("，处置方式：").append(order.getDisposalMethod());
            if (order.getStatus() != null) desc.append("，状态：").append(order.getStatus());
            vo.setDescription(desc.toString());
            events.add(vo);
        }
        return events;
    }

    // ===== 审批事件聚合 =====

    private List<AssetTimelineEventVO> queryApprovalEvents(
            List<ReceiveOrder> receiveOrders,
            List<TransferOrder> transferOrders,
            List<RepairOrder> repairOrders,
            List<ScrapOrder> scrapOrders) {

        List<AssetTimelineEventVO> events = new ArrayList<>();

        // 领用单审批
        for (ReceiveOrder order : receiveOrders) {
            events.addAll(queryApprovalByBusiness("RECEIVE", order.getId(), order.getOrderCode()));
        }
        // 调拨单审批
        for (TransferOrder order : transferOrders) {
            events.addAll(queryApprovalByBusiness("TRANSFER", order.getId(), order.getOrderCode()));
        }
        // 维修单审批
        for (RepairOrder order : repairOrders) {
            events.addAll(queryApprovalByBusiness("REPAIR", order.getId(), order.getOrderCode()));
        }
        // 报废单审批
        for (ScrapOrder order : scrapOrders) {
            events.addAll(queryApprovalByBusiness("SCRAP", order.getId(), order.getOrderCode()));
        }

        return events;
    }

    private List<AssetTimelineEventVO> queryApprovalByBusiness(String businessType, Long businessId, String orderCode) {
        // 查询审批实例
        List<ApprovalInstance> instances = approvalInstanceMapper.selectList(
                new LambdaQueryWrapper<ApprovalInstance>()
                        .eq(ApprovalInstance::getBusinessType, businessType)
                        .eq(ApprovalInstance::getBusinessId, businessId));

        if (instances.isEmpty()) {
            return new ArrayList<>();
        }

        List<AssetTimelineEventVO> events = new ArrayList<>();
        for (ApprovalInstance instance : instances) {
            // 查询审批记录
            List<ApprovalRecord> records = approvalRecordMapper.selectList(
                    new LambdaQueryWrapper<ApprovalRecord>()
                            .eq(ApprovalRecord::getInstanceId, instance.getId()));

            for (ApprovalRecord record : records) {
                AssetTimelineEventVO vo = new AssetTimelineEventVO();
                vo.setId("APPROVAL-" + record.getId());
                vo.setAssetId(null); // approval_record 没有 asset_id
                vo.setEventType("APPROVAL");
                vo.setEventTypeName("审批");
                vo.setOrderCode(orderCode);
                vo.setBusinessType(businessType);
                vo.setBusinessId(businessId);
                vo.setStatus(record.getStatus());
                vo.setOperatorName(record.getApproverName() != null ? record.getApproverName() : "系统");
                vo.setSource("approval_record");
                vo.setRemark(record.getComment());

                // eventTime: 优先 approvedAt，空则 createdAt
                LocalDateTime eventTime = record.getApprovedAt();
                if (eventTime == null) {
                    eventTime = record.getCreatedAt();
                }
                vo.setEventTime(eventTime);

                // title: 按 action 映射
                String action = record.getAction();
                String title;
                if ("SUBMIT".equals(action)) {
                    title = "提交审批";
                } else if ("APPROVED".equals(action)) {
                    title = "审批通过";
                } else if ("REJECTED".equals(action)) {
                    title = "审批驳回";
                } else {
                    title = "审批记录";
                }
                vo.setTitle(title);

                // description
                StringBuilder desc = new StringBuilder();
                desc.append(title);
                if (orderCode != null) desc.append("，单据：").append(orderCode);
                if (record.getComment() != null && !record.getComment().isEmpty()) {
                    desc.append("，审批意见：").append(record.getComment());
                }
                if (record.getStatus() != null) desc.append("，状态：").append(record.getStatus());
                vo.setDescription(desc.toString());

                events.add(vo);
            }
        }
        return events;
    }
}
