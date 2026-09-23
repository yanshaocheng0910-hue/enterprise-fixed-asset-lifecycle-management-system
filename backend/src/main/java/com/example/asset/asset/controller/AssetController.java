package com.example.asset.asset.controller;

import com.example.asset.asset.dto.AssetCreateRequest;
import com.example.asset.asset.dto.AssetQueryRequest;
import com.example.asset.asset.dto.AssetUpdateRequest;
import com.example.asset.asset.vo.AssetDetailVO;
import com.example.asset.asset.vo.AssetPageVO;
import com.example.asset.asset.vo.StatusOptionVO;
import com.example.asset.asset.service.AssetService;
import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.asset.permission.annotation.RequirePermission;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/page")
    @RequirePermission("asset:view")
    public Result<PageResult<AssetPageVO>> page(@Valid AssetQueryRequest query) {
        return Result.success(assetService.page(query));
    }

    @GetMapping("/status-options")
    @RequirePermission("asset:view")
    public Result<List<StatusOptionVO>> statusOptions() {
        return Result.success(assetService.statusOptions());
    }

    @GetMapping("/{id}")
    @RequirePermission("asset:view")
    public Result<AssetDetailVO> detail(@PathVariable Long id) {
        return Result.success(assetService.detail(id));
    }

    @PostMapping
    @RequirePermission("asset:create")
    public Result<Long> create(@Valid @RequestBody AssetCreateRequest request) {
        return Result.success(assetService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("asset:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AssetUpdateRequest request) {
        assetService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("asset:delete")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return Result.success();
    }
}
