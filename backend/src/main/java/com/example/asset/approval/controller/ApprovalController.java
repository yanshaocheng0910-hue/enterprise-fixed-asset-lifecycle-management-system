package com.example.asset.approval.controller;

import com.example.asset.approval.dto.ApprovalActionRequest;
import com.example.asset.approval.dto.ApprovalPageRequest;
import com.example.asset.approval.dto.ApprovalSubmitRequest;
import com.example.asset.approval.service.ApprovalService;
import com.example.asset.approval.vo.*;
import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/submit")
    @RequirePermission("approval:todo")
    public Result<Long> submit(@Valid @RequestBody ApprovalSubmitRequest req) {
        return Result.success(approvalService.submit(req));
    }

    @PostMapping("/{instanceId}/approve")
    public Result<Void> approve(@PathVariable Long instanceId,
                                @Valid @RequestBody ApprovalActionRequest req) {
        approvalService.approve(instanceId, req);
        return Result.success();
    }

    @PostMapping("/{instanceId}/reject")
    @RequirePermission("approval:todo")
    public Result<Void> reject(@PathVariable Long instanceId,
                               @Valid @RequestBody ApprovalActionRequest req) {
        approvalService.reject(instanceId, req);
        return Result.success();
    }

    @GetMapping("/todo/page")
    @RequirePermission("approval:todo")
    public Result<PageResult<ApprovalTodoVO>> todoPage(@Valid ApprovalPageRequest req) {
        return Result.success(approvalService.todoPage(req));
    }

    @GetMapping("/done/page")
    @RequirePermission("approval:done")
    public Result<PageResult<ApprovalDoneVO>> donePage(@Valid ApprovalPageRequest req) {
        return Result.success(approvalService.donePage(req));
    }

    @GetMapping("/records")
    @RequirePermission("approval:done")
    public Result<List<ApprovalRecordVO>> records(
            @RequestParam String businessType,
            @RequestParam Long businessId) {
        return Result.success(approvalService.getRecords(businessType, businessId));
    }

    @GetMapping("/{instanceId}")
    @RequirePermission("approval:todo")
    public Result<ApprovalDetailVO> detail(@PathVariable Long instanceId) {
        return Result.success(approvalService.getDetail(instanceId));
    }
}
