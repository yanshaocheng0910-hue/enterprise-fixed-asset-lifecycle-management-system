package com.example.asset.lifecycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.asset.entity.Asset;
import com.example.asset.asset.entity.AssetOperationLog;
import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.asset.mapper.AssetOperationLogMapper;
import com.example.asset.common.BusinessException;
import com.example.asset.common.PageResult;
import com.example.asset.common.ResultCode;
import com.example.asset.context.UserContext;
import com.example.asset.lifecycle.dto.*;
import com.example.asset.lifecycle.entity.*;
import com.example.asset.lifecycle.mapper.*;
import com.example.asset.lifecycle.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LifecycleService {

    private static final DateTimeFormatter ORDER_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<String> REPAIRABLE_STATUSES = Arrays.asList("IDLE", "IN_USE");
    private static final List<String> TRANSFERABLE_STATUSES = Arrays.asList("IDLE", "IN_USE");
    private static final List<String> SCRAPPABLE_STATUSES = Arrays.asList("IDLE", "IN_USE", "REPAIRING", "WAITING_SCRAP");

    private final AssetMapper assetMapper;
    private final AssetOperationLogMapper operationLogMapper;
    private final InboundOrderMapper inboundOrderMapper;
    private final ReceiveOrderMapper receiveOrderMapper;
    private final TransferOrderMapper transferOrderMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ScrapOrderMapper scrapOrderMapper;

    public LifecycleService(AssetMapper assetMapper,
                            AssetOperationLogMapper operationLogMapper,
                            InboundOrderMapper inboundOrderMapper,
                            ReceiveOrderMapper receiveOrderMapper,
                            TransferOrderMapper transferOrderMapper,
                            RepairOrderMapper repairOrderMapper,
                            ScrapOrderMapper scrapOrderMapper) {
        this.assetMapper = assetMapper;
        this.operationLogMapper = operationLogMapper;
        this.inboundOrderMapper = inboundOrderMapper;
        this.receiveOrderMapper = receiveOrderMapper;
        this.transferOrderMapper = transferOrderMapper;
        this.repairOrderMapper = repairOrderMapper;
        this.scrapOrderMapper = scrapOrderMapper;
    }

    // ==================== Update Order Status (for approval flow) ====================

    @Transactional
    public void updateOrderStatus(String businessType, Long businessId, String status) {
        switch (businessType) {
            case "RECEIVE":
                ReceiveOrder ro = new ReceiveOrder();
                ro.setId(businessId);
                ro.setStatus(status);
                receiveOrderMapper.updateById(ro);
                break;
            case "TRANSFER":
                TransferOrder to = new TransferOrder();
                to.setId(businessId);
                to.setStatus(status);
                transferOrderMapper.updateById(to);
                break;
            case "REPAIR":
                RepairOrder ro2 = new RepairOrder();
                ro2.setId(businessId);
                ro2.setStatus(status);
                repairOrderMapper.updateById(ro2);
                break;
            case "SCRAP":
                ScrapOrder so = new ScrapOrder();
                so.setId(businessId);
                so.setStatus(status);
                scrapOrderMapper.updateById(so);
                break;
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的业务类型: " + businessType);
        }
    }

    // ==================== Asset Select Options ====================

    public List<AssetSelectVO> getAssetSelectOptions(String status) {
        LambdaQueryWrapper<Asset> qw = new LambdaQueryWrapper<Asset>()
                .eq(Asset::getDeleted, 0);
        if (status != null && !status.isBlank()) {
            qw.eq(Asset::getStatus, status);
        }
        return assetMapper.selectList(qw).stream()
                .map(a -> new AssetSelectVO(a.getId(), a.getAssetCode(), a.getAssetName(),
                        a.getDepartment(), a.getKeeper(), a.getLocation(), a.getStatus()))
                .collect(Collectors.toList());
    }

    // ==================== Inbound ====================

    public PageResult<InboundOrderPageVO> inboundPage(LifecyclePageRequest request) {
        Page<InboundOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<InboundOrder> qw = new LambdaQueryWrapper<InboundOrder>()
                .orderByDesc(InboundOrder::getCreatedAt);
        if (request.getOrderCode() != null && !request.getOrderCode().isBlank()) {
            qw.like(InboundOrder::getOrderCode, request.getOrderCode());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            qw.eq(InboundOrder::getStatus, request.getStatus());
        }
        Page<InboundOrder> p = inboundOrderMapper.selectPage(page, qw);
        List<InboundOrderPageVO> records = p.getRecords().stream().map(order -> {
            InboundOrderPageVO vo = new InboundOrderPageVO();
            copyBaseFields(vo, order);
            vo.setInboundType(order.getInboundType());
            vo.setSupplier(order.getSupplier());
            vo.setPurchaseOrderNo(order.getPurchaseOrderNo());
            vo.setInboundDate(order.getInboundDate());
            vo.setHandler(order.getHandler());
            fillAssetInfo(vo, order.getAssetId());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(new Page<InboundOrderPageVO>()
                .setRecords(records)
                .setTotal(p.getTotal())
                .setCurrent(p.getCurrent())
                .setSize(p.getSize()));
    }

    public InboundOrderPageVO inboundDetail(Long id) {
        InboundOrder order = inboundOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "入库单不存在");
        }
        InboundOrderPageVO vo = new InboundOrderPageVO();
        copyBaseFields(vo, order);
        vo.setInboundType(order.getInboundType());
        vo.setSupplier(order.getSupplier());
        vo.setPurchaseOrderNo(order.getPurchaseOrderNo());
        vo.setInboundDate(order.getInboundDate());
        vo.setHandler(order.getHandler());
        fillAssetInfo(vo, order.getAssetId());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(InboundCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if ("SCRAPPED".equals(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已报废资产不能入库");
        }
        // Phase 3: Inbound has no approval, so still auto-completes
        asset.setStatus("IDLE");
        assetMapper.updateById(asset);

        InboundOrder order = new InboundOrder();
        order.setOrderCode(generateOrderCode("IN"));
        order.setAssetId(request.getAssetId());
        order.setInboundType(request.getInboundType());
        order.setSupplier(request.getSupplier());
        order.setPurchaseOrderNo(request.getPurchaseOrderNo());
        order.setInboundDate(request.getInboundDate());
        order.setHandler(request.getHandler());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("IDLE");
        order.setStatus("COMPLETED");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        inboundOrderMapper.insert(order);

        recordLog(request.getAssetId(), "INBOUND", "资产入库", beforeStatus, "IDLE",
                "入库单:" + order.getOrderCode());
        return order.getId();
    }

    // ==================== Receive ====================

    public PageResult<ReceiveOrderPageVO> receivePage(LifecyclePageRequest request) {
        Page<ReceiveOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<ReceiveOrder> qw = new LambdaQueryWrapper<ReceiveOrder>()
                .orderByDesc(ReceiveOrder::getCreatedAt);
        if (request.getOrderCode() != null && !request.getOrderCode().isBlank()) {
            qw.like(ReceiveOrder::getOrderCode, request.getOrderCode());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            qw.eq(ReceiveOrder::getStatus, request.getStatus());
        }
        Page<ReceiveOrder> p = receiveOrderMapper.selectPage(page, qw);
        List<ReceiveOrderPageVO> records = p.getRecords().stream().map(order -> {
            ReceiveOrderPageVO vo = new ReceiveOrderPageVO();
            copyBaseFields(vo, order);
            vo.setReceiver(order.getReceiver());
            vo.setReceiverDepartment(order.getReceiverDepartment());
            vo.setReceiveDate(order.getReceiveDate());
            vo.setUsagePurpose(order.getUsagePurpose());
            fillAssetInfo(vo, order.getAssetId());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(new Page<ReceiveOrderPageVO>()
                .setRecords(records)
                .setTotal(p.getTotal())
                .setCurrent(p.getCurrent())
                .setSize(p.getSize()));
    }

    public ReceiveOrderPageVO receiveDetail(Long id) {
        ReceiveOrder order = receiveOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "领用单不存在");
        }
        ReceiveOrderPageVO vo = new ReceiveOrderPageVO();
        copyBaseFields(vo, order);
        vo.setReceiver(order.getReceiver());
        vo.setReceiverDepartment(order.getReceiverDepartment());
        vo.setReceiveDate(order.getReceiveDate());
        vo.setUsagePurpose(order.getUsagePurpose());
        fillAssetInfo(vo, order.getAssetId());
        return vo;
    }

    /**
     * Create receive order as DRAFT (Phase 3 approval mode).
     * Does NOT change asset status. Approval must pass first.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createReceive(ReceiveCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!"IDLE".equals(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅闲置资产可领用，当前状态:" + beforeStatus);
        }

        ReceiveOrder order = new ReceiveOrder();
        order.setOrderCode(generateOrderCode("RE"));
        order.setAssetId(request.getAssetId());
        order.setReceiver(request.getReceiver());
        order.setReceiverDepartment(request.getReceiverDepartment());
        order.setReceiveDate(request.getReceiveDate());
        order.setUsagePurpose(request.getUsagePurpose());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("IN_USE");
        order.setStatus("DRAFT");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        receiveOrderMapper.insert(order);
        return order.getId();
    }

    /**
     * Execute receive flow after approval.
     * Called by ApprovalService when the final approval node is approved.
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeReceiveFlow(Long orderId) {
        ReceiveOrder order = receiveOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "领用单不存在");
        }
        Asset asset = requireAsset(order.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!"IDLE".equals(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅闲置资产可领用，当前状态:" + beforeStatus);
        }
        asset.setStatus("IN_USE");
        asset.setDepartment(order.getReceiverDepartment());
        asset.setKeeper(order.getReceiver());
        assetMapper.updateById(asset);

        order.setStatus("COMPLETED");
        receiveOrderMapper.updateById(order);

        recordLog(asset.getId(), "RECEIVE", "资产领用(审批通过)", beforeStatus, "IN_USE",
                "领用单:" + order.getOrderCode());
    }

    // ==================== Transfer ====================

    public PageResult<TransferOrderPageVO> transferPage(LifecyclePageRequest request) {
        Page<TransferOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<TransferOrder> qw = new LambdaQueryWrapper<TransferOrder>()
                .orderByDesc(TransferOrder::getCreatedAt);
        if (request.getOrderCode() != null && !request.getOrderCode().isBlank()) {
            qw.like(TransferOrder::getOrderCode, request.getOrderCode());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            qw.eq(TransferOrder::getStatus, request.getStatus());
        }
        Page<TransferOrder> p = transferOrderMapper.selectPage(page, qw);
        List<TransferOrderPageVO> records = p.getRecords().stream().map(order -> {
            TransferOrderPageVO vo = new TransferOrderPageVO();
            copyBaseFields(vo, order);
            vo.setFromDepartment(order.getFromDepartment());
            vo.setToDepartment(order.getToDepartment());
            vo.setFromLocation(order.getFromLocation());
            vo.setToLocation(order.getToLocation());
            vo.setFromKeeper(order.getFromKeeper());
            vo.setToKeeper(order.getToKeeper());
            vo.setTransferDate(order.getTransferDate());
            fillAssetInfo(vo, order.getAssetId());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(new Page<TransferOrderPageVO>()
                .setRecords(records)
                .setTotal(p.getTotal())
                .setCurrent(p.getCurrent())
                .setSize(p.getSize()));
    }

    public TransferOrderPageVO transferDetail(Long id) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "调拨单不存在");
        }
        TransferOrderPageVO vo = new TransferOrderPageVO();
        copyBaseFields(vo, order);
        vo.setFromDepartment(order.getFromDepartment());
        vo.setToDepartment(order.getToDepartment());
        vo.setFromLocation(order.getFromLocation());
        vo.setToLocation(order.getToLocation());
        vo.setFromKeeper(order.getFromKeeper());
        vo.setToKeeper(order.getToKeeper());
        vo.setTransferDate(order.getTransferDate());
        fillAssetInfo(vo, order.getAssetId());
        return vo;
    }

    /**
     * Create transfer order as DRAFT (Phase 3 approval mode).
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(TransferCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!TRANSFERABLE_STATUSES.contains(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许调拨，状态:" + beforeStatus);
        }

        TransferOrder order = new TransferOrder();
        order.setOrderCode(generateOrderCode("TR"));
        order.setAssetId(request.getAssetId());
        order.setFromDepartment(asset.getDepartment());
        order.setToDepartment(request.getToDepartment());
        order.setFromLocation(asset.getLocation());
        order.setToLocation(request.getToLocation());
        order.setFromKeeper(asset.getKeeper());
        order.setToKeeper(request.getToKeeper());
        order.setTransferDate(request.getTransferDate());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("IN_USE");
        order.setStatus("DRAFT");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        transferOrderMapper.insert(order);
        return order.getId();
    }

    /**
     * Execute transfer flow after approval.
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeTransferFlow(Long orderId) {
        TransferOrder order = transferOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "调拨单不存在");
        }
        Asset asset = requireAsset(order.getAssetId());
        if (!TRANSFERABLE_STATUSES.contains(asset.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许调拨，状态:" + asset.getStatus());
        }
        asset.setDepartment(order.getToDepartment());
        asset.setLocation(order.getToLocation());
        asset.setKeeper(order.getToKeeper());
        asset.setStatus("IN_USE");
        assetMapper.updateById(asset);

        order.setStatus("COMPLETED");
        transferOrderMapper.updateById(order);

        recordLog(asset.getId(), "TRANSFER", "资产调拨(审批通过)", order.getBeforeStatus(), "IN_USE",
                "调拨单:" + order.getOrderCode());
    }

    // ==================== Repair ====================

    public PageResult<RepairOrderPageVO> repairPage(LifecyclePageRequest request) {
        Page<RepairOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<RepairOrder> qw = new LambdaQueryWrapper<RepairOrder>()
                .orderByDesc(RepairOrder::getCreatedAt);
        if (request.getOrderCode() != null && !request.getOrderCode().isBlank()) {
            qw.like(RepairOrder::getOrderCode, request.getOrderCode());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            qw.eq(RepairOrder::getStatus, request.getStatus());
        }
        Page<RepairOrder> p = repairOrderMapper.selectPage(page, qw);
        List<RepairOrderPageVO> records = p.getRecords().stream().map(order -> {
            RepairOrderPageVO vo = new RepairOrderPageVO();
            copyBaseFields(vo, order);
            vo.setFaultDescription(order.getFaultDescription());
            vo.setRepairVendor(order.getRepairVendor());
            vo.setRepairCost(order.getRepairCost());
            vo.setRepairStartDate(order.getRepairStartDate());
            vo.setRepairEndDate(order.getRepairEndDate());
            vo.setRepairResult(order.getRepairResult());
            fillAssetInfo(vo, order.getAssetId());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(new Page<RepairOrderPageVO>()
                .setRecords(records)
                .setTotal(p.getTotal())
                .setCurrent(p.getCurrent())
                .setSize(p.getSize()));
    }

    public RepairOrderPageVO repairDetail(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修单不存在");
        }
        RepairOrderPageVO vo = new RepairOrderPageVO();
        copyBaseFields(vo, order);
        vo.setFaultDescription(order.getFaultDescription());
        vo.setRepairVendor(order.getRepairVendor());
        vo.setRepairCost(order.getRepairCost());
        vo.setRepairStartDate(order.getRepairStartDate());
        vo.setRepairEndDate(order.getRepairEndDate());
        vo.setRepairResult(order.getRepairResult());
        fillAssetInfo(vo, order.getAssetId());
        return vo;
    }

    /**
     * Create repair order as DRAFT. Does NOT change asset status.
     * Approval must pass before asset enters REPAIRING.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRepair(RepairCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!REPAIRABLE_STATUSES.contains(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许维修，状态:" + beforeStatus);
        }

        RepairOrder order = new RepairOrder();
        order.setOrderCode(generateOrderCode("RP"));
        order.setAssetId(request.getAssetId());
        order.setFaultDescription(request.getFaultDescription());
        order.setRepairVendor(request.getRepairVendor());
        order.setRepairStartDate(request.getRepairStartDate());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("REPAIRING");
        order.setStatus("DRAFT");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        repairOrderMapper.insert(order);
        return order.getId();
    }

    /**
     * Execute repair start flow after approval.
     * Changes asset status to REPAIRING.
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeRepairStartFlow(Long orderId) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修单不存在");
        }
        Asset asset = requireAsset(order.getAssetId());
        if (!REPAIRABLE_STATUSES.contains(asset.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许维修，状态:" + asset.getStatus());
        }
        asset.setStatus("REPAIRING");
        assetMapper.updateById(asset);

        order.setStatus("COMPLETED");
        repairOrderMapper.updateById(order);

        recordLog(asset.getId(), "REPAIR_START", "资产送修(审批通过)", order.getBeforeStatus(), "REPAIRING",
                "维修单:" + order.getOrderCode());
    }

    /**
     * Complete repair (Phase 2 mode, used after repair work is done).
     * Not affected by approval flow - this is a separate step.
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id, RepairCompleteRequest request) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修单不存在");
        }
        if (!"COMPLETED".equals(order.getStatus()) && !"DRAFT".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "维修单状态不允许完成");
        }
        Asset asset = requireAsset(order.getAssetId());
        if (!"REPAIRING".equals(asset.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资产状态不是维修中，无法完成");
        }
        String afterStatus;
        if ("REPAIRED".equals(request.getRepairResult())) {
            afterStatus = "IN_USE";
        } else if ("SCRAP_SUGGESTED".equals(request.getRepairResult())) {
            afterStatus = "WAITING_SCRAP";
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的维修结果: " + request.getRepairResult());
        }
        asset.setStatus(afterStatus);
        assetMapper.updateById(asset);

        order.setRepairResult(request.getRepairResult());
        order.setRepairVendor(request.getRepairVendor());
        order.setRepairCost(request.getRepairCost());
        order.setRepairEndDate(request.getRepairEndDate());
        order.setAfterStatus(afterStatus);
        order.setStatus("COMPLETED");
        order.setRemark(request.getRemark());
        repairOrderMapper.updateById(order);

        recordLog(asset.getId(), "REPAIR_COMPLETE", "维修完成", "REPAIRING", afterStatus,
                "维修单:" + order.getOrderCode());
    }

    /**
     * Execute repair complete via approval (if needed).
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeRepairCompleteFlow(Long orderId, String repairResult) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修单不存在");
        }
        Asset asset = requireAsset(order.getAssetId());
        if (!"REPAIRING".equals(asset.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资产状态不是维修中，无法完成");
        }
        String afterStatus;
        if ("REPAIRED".equals(repairResult)) {
            afterStatus = "IN_USE";
        } else if ("SCRAP_SUGGESTED".equals(repairResult)) {
            afterStatus = "WAITING_SCRAP";
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的维修结果: " + repairResult);
        }
        asset.setStatus(afterStatus);
        assetMapper.updateById(asset);

        order.setRepairResult(repairResult);
        order.setAfterStatus(afterStatus);
        order.setStatus("COMPLETED");
        repairOrderMapper.updateById(order);

        recordLog(asset.getId(), "REPAIR_COMPLETE", "维修完成(审批通过)", "REPAIRING", afterStatus,
                "维修单:" + order.getOrderCode());
    }

    // ==================== Scrap ====================

    public PageResult<ScrapOrderPageVO> scrapPage(LifecyclePageRequest request) {
        Page<ScrapOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<ScrapOrder> qw = new LambdaQueryWrapper<ScrapOrder>()
                .orderByDesc(ScrapOrder::getCreatedAt);
        if (request.getOrderCode() != null && !request.getOrderCode().isBlank()) {
            qw.like(ScrapOrder::getOrderCode, request.getOrderCode());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            qw.eq(ScrapOrder::getStatus, request.getStatus());
        }
        Page<ScrapOrder> p = scrapOrderMapper.selectPage(page, qw);
        List<ScrapOrderPageVO> records = p.getRecords().stream().map(order -> {
            ScrapOrderPageVO vo = new ScrapOrderPageVO();
            copyBaseFields(vo, order);
            vo.setScrapReason(order.getScrapReason());
            vo.setScrapDate(order.getScrapDate());
            vo.setDisposalMethod(order.getDisposalMethod());
            vo.setResidualValue(order.getResidualValue());
            fillAssetInfo(vo, order.getAssetId());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(new Page<ScrapOrderPageVO>()
                .setRecords(records)
                .setTotal(p.getTotal())
                .setCurrent(p.getCurrent())
                .setSize(p.getSize()));
    }

    public ScrapOrderPageVO scrapDetail(Long id) {
        ScrapOrder order = scrapOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报废单不存在");
        }
        ScrapOrderPageVO vo = new ScrapOrderPageVO();
        copyBaseFields(vo, order);
        vo.setScrapReason(order.getScrapReason());
        vo.setScrapDate(order.getScrapDate());
        vo.setDisposalMethod(order.getDisposalMethod());
        vo.setResidualValue(order.getResidualValue());
        fillAssetInfo(vo, order.getAssetId());
        return vo;
    }

    /**
     * Create scrap order as DRAFT. Does NOT change asset status.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createScrap(ScrapCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!SCRAPPABLE_STATUSES.contains(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许报废，状态:" + beforeStatus);
        }

        ScrapOrder order = new ScrapOrder();
        order.setOrderCode(generateOrderCode("SC"));
        order.setAssetId(request.getAssetId());
        order.setScrapReason(request.getScrapReason());
        order.setScrapDate(request.getScrapDate());
        order.setDisposalMethod(request.getDisposalMethod());
        order.setResidualValue(request.getResidualValue());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("SCRAPPED");
        order.setStatus("DRAFT");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        scrapOrderMapper.insert(order);
        return order.getId();
    }

    /**
     * Execute scrap flow after approval.
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeScrapFlow(Long orderId) {
        ScrapOrder order = scrapOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报废单不存在");
        }
        Asset asset = requireAsset(order.getAssetId());
        String beforeStatus = asset.getStatus();
        if ("SCRAPPED".equals(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已报废资产不能再次报废");
        }
        asset.setStatus("SCRAPPED");
        assetMapper.updateById(asset);

        order.setStatus("COMPLETED");
        scrapOrderMapper.updateById(order);

        recordLog(asset.getId(), "SCRAP", "资产报废(审批通过)", beforeStatus, "SCRAPPED",
                "报废单:" + order.getOrderCode());
    }

    // ==================== Private Helpers ====================

    private Asset requireAsset(Long id) {
        Asset asset = assetMapper.selectOne(new LambdaQueryWrapper<Asset>()
                .eq(Asset::getId, id)
                .eq(Asset::getDeleted, 0)
                .last("limit 1"));
        if (asset == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资产不存在");
        }
        return asset;
    }

    private String generateOrderCode(String prefix) {
        String datePart = LocalDate.now().format(ORDER_CODE_FORMATTER);
        String searchPrefix = prefix + datePart;
        String maxCode = null;
        String candidateIn = inboundOrderMapper.selectMaxOrderCodeByPrefix(searchPrefix);
        if (candidateIn != null && (maxCode == null || candidateIn.compareTo(maxCode) > 0)) {
            maxCode = candidateIn;
        }
        String candidateRe = receiveOrderMapper.selectMaxOrderCodeByPrefix(searchPrefix);
        if (candidateRe != null && (maxCode == null || candidateRe.compareTo(maxCode) > 0)) {
            maxCode = candidateRe;
        }
        String candidateTr = transferOrderMapper.selectMaxOrderCodeByPrefix(searchPrefix);
        if (candidateTr != null && (maxCode == null || candidateTr.compareTo(maxCode) > 0)) {
            maxCode = candidateTr;
        }
        String candidateRp = repairOrderMapper.selectMaxOrderCodeByPrefix(searchPrefix);
        if (candidateRp != null && (maxCode == null || candidateRp.compareTo(maxCode) > 0)) {
            maxCode = candidateRp;
        }
        String candidateSc = scrapOrderMapper.selectMaxOrderCodeByPrefix(searchPrefix);
        if (candidateSc != null && (maxCode == null || candidateSc.compareTo(maxCode) > 0)) {
            maxCode = candidateSc;
        }

        int nextSequence = 1;
        if (maxCode != null && maxCode.length() >= searchPrefix.length() + 4) {
            String sequencePart = maxCode.substring(searchPrefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return searchPrefix + String.format("%04d", nextSequence);
    }

    private void recordLog(Long assetId, String operationType, String operationName,
                           String beforeStatus, String afterStatus, String remark) {
        AssetOperationLog log = new AssetOperationLog();
        log.setAssetId(assetId);
        log.setOperationType(operationType);
        log.setOperationName(operationName);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setOperatorId(UserContext.getUserId());
        log.setOperatorName(UserContext.getUsername());
        log.setOperationTime(LocalDateTime.now());
        log.setRemark(remark);
        operationLogMapper.insert(log);
    }

    private void copyBaseFields(InboundOrderPageVO vo, InboundOrder order) {
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setAssetId(order.getAssetId());
        vo.setBeforeStatus(order.getBeforeStatus());
        vo.setAfterStatus(order.getAfterStatus());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedBy(order.getCreatedBy() != null ? String.valueOf(order.getCreatedBy()) : null);
        vo.setCreatedAt(order.getCreatedAt());
    }

    private void copyBaseFields(ReceiveOrderPageVO vo, ReceiveOrder order) {
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setAssetId(order.getAssetId());
        vo.setBeforeStatus(order.getBeforeStatus());
        vo.setAfterStatus(order.getAfterStatus());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedBy(order.getCreatedBy() != null ? String.valueOf(order.getCreatedBy()) : null);
        vo.setCreatedAt(order.getCreatedAt());
    }

    private void copyBaseFields(TransferOrderPageVO vo, TransferOrder order) {
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setAssetId(order.getAssetId());
        vo.setBeforeStatus(order.getBeforeStatus());
        vo.setAfterStatus(order.getAfterStatus());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedBy(order.getCreatedBy() != null ? String.valueOf(order.getCreatedBy()) : null);
        vo.setCreatedAt(order.getCreatedAt());
    }

    private void copyBaseFields(RepairOrderPageVO vo, RepairOrder order) {
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setAssetId(order.getAssetId());
        vo.setBeforeStatus(order.getBeforeStatus());
        vo.setAfterStatus(order.getAfterStatus());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedBy(order.getCreatedBy() != null ? String.valueOf(order.getCreatedBy()) : null);
        vo.setCreatedAt(order.getCreatedAt());
    }

    private void copyBaseFields(ScrapOrderPageVO vo, ScrapOrder order) {
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setAssetId(order.getAssetId());
        vo.setBeforeStatus(order.getBeforeStatus());
        vo.setAfterStatus(order.getAfterStatus());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedBy(order.getCreatedBy() != null ? String.valueOf(order.getCreatedBy()) : null);
        vo.setCreatedAt(order.getCreatedAt());
    }

    private void fillAssetInfo(Object vo, Long assetId) {
        try {
            Asset asset = assetMapper.selectById(assetId);
            if (asset != null) {
                if (hasMethod(vo, "setAssetName", String.class)) {
                    vo.getClass().getMethod("setAssetName", String.class).invoke(vo, asset.getAssetName());
                }
                if (hasMethod(vo, "setAssetCode", String.class)) {
                    vo.getClass().getMethod("setAssetCode", String.class).invoke(vo, asset.getAssetCode());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean hasMethod(Object obj, String name, Class<?> paramType) {
        try {
            obj.getClass().getMethod(name, paramType);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
