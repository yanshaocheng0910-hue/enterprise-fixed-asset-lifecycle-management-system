package com.example.asset.asset.controller;

import com.example.asset.asset.dto.AssetCategoryCreateRequest;
import com.example.asset.asset.dto.AssetCategoryUpdateRequest;
import com.example.asset.asset.entity.AssetCategory;
import com.example.asset.asset.service.AssetCategoryService;
import com.example.asset.asset.vo.AssetCategoryTreeVO;
import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-categories")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    public AssetCategoryController(AssetCategoryService assetCategoryService) {
        this.assetCategoryService = assetCategoryService;
    }

    @GetMapping("/list")
    @RequirePermission("category:view")
    public Result<List<AssetCategory>> list() {
        return Result.success(assetCategoryService.list());
    }

    @GetMapping("/tree")
    @RequirePermission("category:view")
    public Result<List<AssetCategoryTreeVO>> tree() {
        return Result.success(assetCategoryService.tree());
    }

    @PostMapping
    @RequirePermission("category:create")
    public Result<Long> create(@Valid @RequestBody AssetCategoryCreateRequest req) {
        return Result.success(assetCategoryService.create(req));
    }

    @PutMapping("/{id}")
    @RequirePermission("category:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AssetCategoryUpdateRequest req) {
        assetCategoryService.update(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("category:delete")
    public Result<Void> delete(@PathVariable Long id) {
        assetCategoryService.delete(id);
        return Result.success();
    }
}
