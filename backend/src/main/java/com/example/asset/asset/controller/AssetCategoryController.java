package com.example.asset.asset.controller;

import com.example.asset.asset.entity.AssetCategory;
import com.example.asset.asset.service.AssetCategoryService;
import com.example.asset.asset.vo.AssetCategoryTreeVO;
import com.example.asset.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/asset-categories")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    public AssetCategoryController(AssetCategoryService assetCategoryService) {
        this.assetCategoryService = assetCategoryService;
    }

    @GetMapping("/list")
    public Result<List<AssetCategory>> list() {
        return Result.success(assetCategoryService.list());
    }

    @GetMapping("/tree")
    public Result<List<AssetCategoryTreeVO>> tree() {
        return Result.success(assetCategoryService.tree());
    }
}
