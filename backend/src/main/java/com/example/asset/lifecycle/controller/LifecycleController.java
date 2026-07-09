package com.example.asset.lifecycle.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.lifecycle.dto.*;
import com.example.asset.lifecycle.service.LifecycleService;
import com.example.asset.lifecycle.vo.*;
import com.example.asset.permission.annotation.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: 缺少 inbound:view/receive:view/transfer:view/repair:view/scrap:view 权限码，GET接口暂用对应的 *:create 权限兜底
@Validated
@RestController
@RequestMapping("/api/lifecycle")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    // ==================== Asset Select ====================

    @GetMapping("/asset-select-options")
    @RequirePermission("asset:view")
    public Result<List<AssetSelectVO>> assetSelectOptions(
            @RequestParam(required = false) String status) {
        return Result.success(lifecycleService.getAssetSelectOptions(status));
    }

    // ==================== Inbound ====================

    @GetMapping("/inbound/page")
    @RequirePermission("inbound:create")
    public Result<PageResult<InboundOrderPageVO>> inboundPage(@Valid LifecyclePageRequest request) {
        return Result.success(lifecycleService.inboundPage(request));
    }

    @GetMapping("/inbound/{id}")
    @RequirePermission("inbound:create")
    public Result<InboundOrderPageVO> inboundDetail(@PathVariable Long id) {
        return Result.success(lifecycleService.inboundDetail(id));
    }

    @PostMapping("/inbound")
    @RequirePermission("inbound:create")
    public Result<Long> createInbound(@Valid @RequestBody InboundCreateRequest request) {
        return Result.success(lifecycleService.createInbound(request));
    }

    // ==================== Receive ====================

    @GetMapping("/receive/page")
    public Result<PageResult<ReceiveOrderPageVO>> receivePage(@Valid LifecyclePageRequest request) {
        return Result.success(lifecycleService.receivePage(request));
    }

    @GetMapping("/receive/{id}")
    @RequirePermission("receive:create")
    public Result<ReceiveOrderPageVO> receiveDetail(@PathVariable Long id) {
        return Result.success(lifecycleService.receiveDetail(id));
    }

    @PostMapping("/receive")
    @RequirePermission("receive:create")
    public Result<Long> createReceive(@Valid @RequestBody ReceiveCreateRequest request) {
        return Result.success(lifecycleService.createReceive(request));
    }

    // ==================== Transfer ====================

    @GetMapping("/transfer/page")
    @RequirePermission("transfer:create")
    public Result<PageResult<TransferOrderPageVO>> transferPage(@Valid LifecyclePageRequest request) {
        return Result.success(lifecycleService.transferPage(request));
    }

    @GetMapping("/transfer/{id}")
    @RequirePermission("transfer:create")
    public Result<TransferOrderPageVO> transferDetail(@PathVariable Long id) {
        return Result.success(lifecycleService.transferDetail(id));
    }

    @PostMapping("/transfer")
    @RequirePermission("transfer:create")
    public Result<Long> createTransfer(@Valid @RequestBody TransferCreateRequest request) {
        return Result.success(lifecycleService.createTransfer(request));
    }

    // ==================== Repair ====================

    @GetMapping("/repair/page")
    @RequirePermission("repair:create")
    public Result<PageResult<RepairOrderPageVO>> repairPage(@Valid LifecyclePageRequest request) {
        return Result.success(lifecycleService.repairPage(request));
    }

    @GetMapping("/repair/{id}")
    @RequirePermission("repair:create")
    public Result<RepairOrderPageVO> repairDetail(@PathVariable Long id) {
        return Result.success(lifecycleService.repairDetail(id));
    }

    @PostMapping("/repair")
    @RequirePermission("repair:create")
    public Result<Long> createRepair(@Valid @RequestBody RepairCreateRequest request) {
        return Result.success(lifecycleService.createRepair(request));
    }

    @PutMapping("/repair/{id}/complete")
    @RequirePermission("repair:create")
    public Result<Void> completeRepair(@PathVariable Long id,
                                       @Valid @RequestBody RepairCompleteRequest request) {
        lifecycleService.completeRepair(id, request);
        return Result.success();
    }

    // ==================== Scrap ====================

    @GetMapping("/scrap/page")
    @RequirePermission("scrap:create")
    public Result<PageResult<ScrapOrderPageVO>> scrapPage(@Valid LifecyclePageRequest request) {
        return Result.success(lifecycleService.scrapPage(request));
    }

    @GetMapping("/scrap/{id}")
    public Result<ScrapOrderPageVO> scrapDetail(@PathVariable Long id) {
        return Result.success(lifecycleService.scrapDetail(id));
    }

    @PostMapping("/scrap")
    @RequirePermission("scrap:create")
    public Result<Long> createScrap(@Valid @RequestBody ScrapCreateRequest request) {
        return Result.success(lifecycleService.createScrap(request));
    }
}
