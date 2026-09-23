package com.example.asset.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.asset.dto.AssetCreateRequest;
import com.example.asset.asset.dto.AssetQueryRequest;
import com.example.asset.asset.dto.AssetUpdateRequest;
import com.example.asset.asset.entity.Asset;
import com.example.asset.asset.entity.AssetCategory;
import com.example.asset.asset.entity.AssetOperationLog;
import com.example.asset.asset.enums.AssetStatusEnum;
import com.example.asset.asset.mapper.AssetCategoryMapper;
import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.asset.mapper.AssetOperationLogMapper;
import com.example.asset.asset.vo.AssetDetailVO;
import com.example.asset.asset.vo.AssetPageVO;
import com.example.asset.asset.vo.StatusOptionVO;
import com.example.asset.common.BusinessException;
import com.example.asset.common.PageResult;
import com.example.asset.common.ResultCode;
import com.example.asset.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class AssetService {

    private static final DateTimeFormatter ASSET_CODE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DEFAULT_DEPRECIATION_METHOD = "straight_line";

    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final AssetOperationLogMapper assetOperationLogMapper;

    public AssetService(AssetMapper assetMapper, AssetCategoryMapper assetCategoryMapper, AssetOperationLogMapper assetOperationLogMapper) {
        this.assetMapper = assetMapper;
        this.assetCategoryMapper = assetCategoryMapper;
        this.assetOperationLogMapper = assetOperationLogMapper;
    }

    public PageResult<AssetPageVO> page(AssetQueryRequest query) {
        Page<AssetPageVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(assetMapper.selectAssetPage(page, query));
    }

    public AssetDetailVO detail(Long id) {
        AssetDetailVO detailVO = assetMapper.selectAssetDetail(id);
        if (detailVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资产不存在");
        }
        return detailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(AssetCreateRequest request) {
        AssetCategory category = requireCategory(request.getCategoryId());
        Asset asset = new Asset();
        asset.setAssetCode(generateAssetCode());
        asset.setAssetName(request.getAssetName());
        asset.setCategoryId(category.getId());
        asset.setSpecification(request.getSpecification());
        asset.setBrand(request.getBrand());
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setOriginalValue(request.getOriginalValue());
        asset.setUsefulLife(request.getUsefulLife());
        asset.setResidualRate(request.getResidualRate());
        asset.setDepreciationMethod(DEFAULT_DEPRECIATION_METHOD);
        asset.setDepartment(request.getDepartment());
        asset.setKeeper(request.getKeeper());
        asset.setLocation(request.getLocation());
        asset.setStatus(AssetStatusEnum.IDLE.getCode());
        asset.setQrCode(request.getQrCode());
        asset.setRfidCode(request.getRfidCode());
        asset.setPhotoUrl(request.getPhotoUrl());
        asset.setRemark(request.getRemark());
        fillDepreciation(asset);
        assetMapper.insert(asset);
        recordLog(asset.getId(), "CREATE", "新增资产", null, asset.getStatus(), "新增资产台账");
        return asset.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AssetUpdateRequest request) {
        Asset existing = requireAsset(id);
        requireCategory(request.getCategoryId());
        String beforeStatus = existing.getStatus();
        existing.setAssetName(request.getAssetName());
        existing.setCategoryId(request.getCategoryId());
        existing.setSpecification(request.getSpecification());
        existing.setBrand(request.getBrand());
        existing.setPurchaseDate(request.getPurchaseDate());
        existing.setOriginalValue(request.getOriginalValue());
        existing.setUsefulLife(request.getUsefulLife());
        existing.setResidualRate(request.getResidualRate());
        existing.setDepreciationMethod(DEFAULT_DEPRECIATION_METHOD);
        existing.setDepartment(request.getDepartment());
        existing.setKeeper(request.getKeeper());
        existing.setLocation(request.getLocation());
        existing.setStatus(request.getStatus());
        existing.setQrCode(request.getQrCode());
        existing.setRfidCode(request.getRfidCode());
        existing.setPhotoUrl(request.getPhotoUrl());
        existing.setRemark(request.getRemark());
        fillDepreciation(existing);
        assetMapper.updateById(existing);
        String afterStatus = existing.getStatus();
        String remark = beforeStatus.equals(afterStatus) ? "编辑资产信息" : "编辑资产信息并变更状态";
        recordLog(existing.getId(), "UPDATE", "编辑资产", beforeStatus, afterStatus, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Asset asset = requireAsset(id);
        assetMapper.deleteById(id);
        recordLog(asset.getId(), "DELETE", "删除资产", asset.getStatus(), asset.getStatus(), "逻辑删除资产");
    }

    public List<StatusOptionVO> statusOptions() {
        return Arrays.stream(AssetStatusEnum.values())
                .map(item -> new StatusOptionVO(item.getCode(), item.getLabel()))
                .toList();
    }

    private AssetCategory requireCategory(Long categoryId) {
        AssetCategory category = assetCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资产分类不存在");
        }
        return category;
    }

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

    private String generateAssetCode() {
        String prefix = "FA" + LocalDate.now().format(ASSET_CODE_MONTH_FORMATTER);
        String maxAssetCode = assetMapper.selectMaxAssetCodeByPrefix(prefix);
        int nextSequence = 1;
        if (maxAssetCode != null && maxAssetCode.length() >= prefix.length() + 4) {
            String sequencePart = maxAssetCode.substring(prefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }
        return prefix + String.format("%04d", nextSequence);
    }

    private void fillDepreciation(Asset asset) {
        BigDecimal originalValue = asset.getOriginalValue();
        BigDecimal residualRate = asset.getResidualRate();
        BigDecimal residualAmount = originalValue.multiply(residualRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal yearlyDepreciation = originalValue.multiply(BigDecimal.ONE.subtract(residualRate))
                .divide(BigDecimal.valueOf(asset.getUsefulLife()), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyDepreciation = yearlyDepreciation.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        int months = calculateUsedMonths(asset.getPurchaseDate(), asset.getUsefulLife());
        BigDecimal accumulated = monthlyDepreciation.multiply(BigDecimal.valueOf(months)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxDepreciation = originalValue.subtract(residualAmount).setScale(2, RoundingMode.HALF_UP);
        if (accumulated.compareTo(maxDepreciation) > 0) {
            accumulated = maxDepreciation;
        }
        BigDecimal netValue = originalValue.subtract(accumulated).setScale(2, RoundingMode.HALF_UP);
        if (netValue.compareTo(residualAmount) < 0) {
            netValue = residualAmount;
        }
        asset.setAccumulatedDepreciation(accumulated);
        asset.setNetValue(netValue);
    }

    private int calculateUsedMonths(LocalDate purchaseDate, Integer usefulLife) {
        if (purchaseDate == null || usefulLife == null || usefulLife <= 0) {
            return 0;
        }
        YearMonth purchaseMonth = YearMonth.from(purchaseDate);
        YearMonth currentMonth = YearMonth.now();
        if (purchaseMonth.isAfter(currentMonth)) {
            return 0;
        }
        long months = purchaseMonth.until(currentMonth, ChronoUnit.MONTHS);
        return (int) Math.min(months, usefulLife * 12L);
    }

    private void recordLog(Long assetId, String operationType, String operationName, String beforeStatus, String afterStatus, String remark) {
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
        assetOperationLogMapper.insert(log);
    }
}
