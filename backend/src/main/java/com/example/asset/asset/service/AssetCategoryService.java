package com.example.asset.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.asset.asset.entity.AssetCategory;
import com.example.asset.asset.mapper.AssetCategoryMapper;
import com.example.asset.asset.vo.AssetCategoryTreeVO;
import org.springframework.stereotype.Service;

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
