package com.example.asset.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.asset.asset.dto.AssetCategoryCreateRequest;
import com.example.asset.asset.dto.AssetCategoryUpdateRequest;
import com.example.asset.asset.entity.AssetCategory;
import com.example.asset.asset.mapper.AssetCategoryMapper;
import com.example.asset.asset.vo.AssetCategoryTreeVO;
import com.example.asset.common.BusinessException;
import com.example.asset.common.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssetCategoryService {

    private final AssetCategoryMapper assetCategoryMapper;

    public AssetCategoryService(AssetCategoryMapper assetCategoryMapper) {
        this.assetCategoryMapper = assetCategoryMapper;
    }

    public List<AssetCategory> list() {
        return assetCategoryMapper.selectList(new LambdaQueryWrapper<AssetCategory>()
                .orderByAsc(AssetCategory::getParentId)
                .orderByAsc(AssetCategory::getId));
    }

    public List<AssetCategoryTreeVO> tree() {
        List<AssetCategory> categories = list();
        Map<Long, AssetCategoryTreeVO> nodeMap = categories.stream()
                .map(this::toTreeVO)
                .collect(Collectors.toMap(AssetCategoryTreeVO::getId, Function.identity()));
        List<AssetCategoryTreeVO> roots = new ArrayList<>();
        for (AssetCategory category : categories) {
            AssetCategoryTreeVO current = nodeMap.get(category.getId());
            Long parentId = category.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(current);
            } else {
                nodeMap.get(parentId).getChildren().add(current);
            }
        }
        return roots;
    }

    @Transactional
    public Long create(AssetCategoryCreateRequest req) {
        // 检查编码唯一性
        Long count = assetCategoryMapper.selectCount(new LambdaQueryWrapper<AssetCategory>()
                .eq(AssetCategory::getCategoryCode, req.getCategoryCode()));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分类编码已存在: " + req.getCategoryCode());
        }
        // 如果有父分类，检查父分类是否存在
        if (req.getParentId() != null && req.getParentId() > 0) {
            AssetCategory parent = assetCategoryMapper.selectById(req.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "父分类不存在");
            }
        }
        AssetCategory category = new AssetCategory();
        category.setCategoryCode(req.getCategoryCode());
        category.setCategoryName(req.getCategoryName());
        category.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        category.setDepreciationYears(req.getDepreciationYears());
        category.setRemark(req.getRemark());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        assetCategoryMapper.insert(category);
        return category.getId();
    }

    @Transactional
    public void update(Long id, AssetCategoryUpdateRequest req) {
        AssetCategory existing = assetCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        // 检查编码唯一性（排除自身）
        Long count = assetCategoryMapper.selectCount(new LambdaQueryWrapper<AssetCategory>()
                .eq(AssetCategory::getCategoryCode, req.getCategoryCode())
                .ne(AssetCategory::getId, id));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分类编码已存在: " + req.getCategoryCode());
        }
        // 不能将自身设为父分类
        if (req.getParentId() != null && req.getParentId().equals(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能将自身设为父分类");
        }
        existing.setCategoryCode(req.getCategoryCode());
        existing.setCategoryName(req.getCategoryName());
        existing.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        existing.setDepreciationYears(req.getDepreciationYears());
        existing.setRemark(req.getRemark());
        existing.setUpdatedAt(LocalDateTime.now());
        assetCategoryMapper.updateById(existing);
    }

    @Transactional
    public void delete(Long id) {
        AssetCategory existing = assetCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        // 检查是否有子分类
        Long childCount = assetCategoryMapper.selectCount(new LambdaQueryWrapper<AssetCategory>()
                .eq(AssetCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该分类下有子分类，不能删除");
        }
        assetCategoryMapper.deleteById(id);
    }

    private AssetCategoryTreeVO toTreeVO(AssetCategory category) {
        AssetCategoryTreeVO vo = new AssetCategoryTreeVO();
        vo.setId(category.getId());
        vo.setCategoryCode(category.getCategoryCode());
        vo.setCategoryName(category.getCategoryName());
        vo.setParentId(category.getParentId());
        vo.setDepreciationYears(category.getDepreciationYears());
        vo.setRemark(category.getRemark());
        return vo;
    }
}
