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

    @Transactional(rollbackFor = Exception.class)
    public Long createReceive(ReceiveCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!"IDLE".equals(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅闲置资产可领用，当前状态:" + beforeStatus);
        }
        asset.setStatus("IN_USE");
        asset.setDepartment(request.getReceiverDepartment());
        asset.setKeeper(request.getReceiver());
        assetMapper.updateById(asset);

        ReceiveOrder order = new ReceiveOrder();
        order.setOrderCode(generateOrderCode("RE"));
        order.setAssetId(request.getAssetId());
        order.setReceiver(request.getReceiver());
        order.setReceiverDepartment(request.getReceiverDepartment());
        order.setReceiveDate(request.getReceiveDate());
        order.setUsagePurpose(request.getUsagePurpose());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("IN_USE");
        order.setStatus("COMPLETED");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        receiveOrderMapper.insert(order);

        recordLog(request.getAssetId(), "RECEIVE", "资产领用", beforeStatus, "IN_USE",
                "领用单:" + order.getOrderCode());
        return order.getId();
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

    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(TransferCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!TRANSFERABLE_STATUSES.contains(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许调拨，状态:" + beforeStatus);
        }
        asset.setDepartment(request.getToDepartment());
        asset.setLocation(request.getToLocation());
        asset.setKeeper(request.getToKeeper());
        asset.setStatus("IN_USE");
        assetMapper.updateById(asset);

        TransferOrder order = new TransferOrder();
        order.setOrderCode(generateOrderCode("TR"));
        order.setAssetId(request.getAssetId());
        order.setFromDepartment(request.getFromDepartment());
        order.setToDepartment(request.getToDepartment());
        order.setFromLocation(request.getFromLocation());
        order.setToLocation(request.getToLocation());
        order.setFromKeeper(request.getFromKeeper());
        order.setToKeeper(request.getToKeeper());
        order.setTransferDate(request.getTransferDate());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("IN_USE");
        order.setStatus("COMPLETED");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        transferOrderMapper.insert(order);

        recordLog(request.getAssetId(), "TRANSFER", "资产调拨", beforeStatus, "IN_USE",
                "调拨单:" + order.getOrderCode());
        return order.getId();
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

    @Transactional(rollbackFor = Exception.class)
    public Long createRepair(RepairCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!REPAIRABLE_STATUSES.contains(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许维修，状态:" + beforeStatus);
        }
        asset.setStatus("REPAIRING");
        assetMapper.updateById(asset);

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

        recordLog(request.getAssetId(), "REPAIR_START", "资产送修", beforeStatus, "REPAIRING",
                "维修单:" + order.getOrderCode());
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id, RepairCompleteRequest request) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修单不存在");
        }
        if (!"DRAFT".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "维修单状态不是草稿，无法完成");
        }
        Asset asset = requireAsset(order.getAssetId());
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
        order.setBeforeStatus(order.getBeforeStatus());
        order.setAfterStatus(afterStatus);
        order.setStatus("COMPLETED");
        order.setRemark(request.getRemark());
        repairOrderMapper.updateById(order);

        recordLog(asset.getId(), "REPAIR_COMPLETE", "维修完成", "REPAIRING", afterStatus,
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

    @Transactional(rollbackFor = Exception.class)
    public Long createScrap(ScrapCreateRequest request) {
        Asset asset = requireAsset(request.getAssetId());
        String beforeStatus = asset.getStatus();
        if (!SCRAPPABLE_STATUSES.contains(beforeStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许报废，状态:" + beforeStatus);
        }
        asset.setStatus("SCRAPPED");
        assetMapper.updateById(asset);

        ScrapOrder order = new ScrapOrder();
        order.setOrderCode(generateOrderCode("SC"));
        order.setAssetId(request.getAssetId());
        order.setScrapReason(request.getScrapReason());
        order.setScrapDate(request.getScrapDate());
        order.setDisposalMethod(request.getDisposalMethod());
        order.setResidualValue(request.getResidualValue());
        order.setBeforeStatus(beforeStatus);
        order.setAfterStatus("SCRAPPED");
        order.setStatus("COMPLETED");
        order.setRemark(request.getRemark());
        order.setCreatedBy(UserContext.getUserId());
        scrapOrderMapper.insert(order);

        recordLog(request.getAssetId(), "SCRAP", "资产报废", beforeStatus, "SCRAPPED",
                "报废单:" + order.getOrderCode());
        return order.getId();
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

    private void fillAssetInfo(InboundOrderPageVO vo, Long assetId) {
        if (assetId != null) {
            Asset asset = assetMapper.selectById(assetId);
            if (asset != null) {
                vo.setAssetName(asset.getAssetName());
                vo.setAssetCode(asset.getAssetCode());
            }
        }
    }

    private void fillAssetInfo(ReceiveOrderPageVO vo, Long assetId) {
        if (assetId != null) {
            Asset asset = assetMapper.selectById(assetId);
            if (asset != null) {
                vo.setAssetName(asset.getAssetName());
                vo.setAssetCode(asset.getAssetCode());
            }
        }
    }

    private void fillAssetInfo(TransferOrderPageVO vo, Long assetId) {
        if (assetId != null) {
            Asset asset = assetMapper.selectById(assetId);
            if (asset != null) {
                vo.setAssetName(asset.getAssetName());
                vo.setAssetCode(asset.getAssetCode());
            }
        }
    }

    private void fillAssetInfo(RepairOrderPageVO vo, Long assetId) {
        if (assetId != null) {
            Asset asset = assetMapper.selectById(assetId);
            if (asset != null) {
                vo.setAssetName(asset.getAssetName());
                vo.setAssetCode(asset.getAssetCode());
            }
        }
    }

    private void fillAssetInfo(ScrapOrderPageVO vo, Long assetId) {
        if (assetId != null) {
            Asset asset = assetMapper.selectById(assetId);
            if (asset != null) {
                vo.setAssetName(asset.getAssetName());
                vo.setAssetCode(asset.getAssetCode());
            }
        }
    }
}
