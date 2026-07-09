package com.example.asset.audit.controller;

import com.example.asset.audit.dto.AuditLogQueryRequest;
import com.example.asset.audit.service.AuditService;
import com.example.asset.audit.vo.AuditLogVO;
import com.example.asset.audit.vo.AuditSummaryVO;
import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志 Controller
 */
@RestController
@RequestMapping("/api/audit/logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 审计统计
     */
    @GetMapping("/summary")
    @RequirePermission("approval:audit")
    public Result<AuditSummaryVO> summary() {
        return Result.success(auditService.summary());
    }

    /**
     * 分页查询审计日志
     */
    @GetMapping("/page")
    @RequirePermission("approval:audit")
    public Result<PageResult<AuditLogVO>> page(AuditLogQueryRequest req) {
        return Result.success(auditService.page(req));
    }

    /**
     * 日志详情（复合 ID）
     */
    @GetMapping("/{id}")
    @RequirePermission("approval:audit")
    public Result<AuditLogVO> detail(@PathVariable String id) {
        return Result.success(auditService.getDetail(id));
    }
}
