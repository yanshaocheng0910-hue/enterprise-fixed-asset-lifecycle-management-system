package com.example.asset.depreciation.controller;

import com.example.asset.common.Result;
import com.example.asset.depreciation.service.DepreciationReportService;
import com.example.asset.depreciation.vo.*;
import com.example.asset.permission.annotation.RequirePermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/depreciation")
public class DepreciationReportController {

    private final DepreciationReportService depreciationReportService;

    public DepreciationReportController(DepreciationReportService depreciationReportService) {
        this.depreciationReportService = depreciationReportService;
    }

    @GetMapping("/report/summary")
    @RequirePermission("depreciation:view")
    public Result<MonthlyReportSummaryVO> monthlySummary(@RequestParam String month) {
        return Result.success(depreciationReportService.monthlySummary(month));
    }

    @GetMapping("/report/monthly")
    @RequirePermission("depreciation:view")
    public Result<List<MonthlyDepreciationItemVO>> monthlyItems(@RequestParam String month) {
        return Result.success(depreciationReportService.monthlyItems(month));
    }

    @GetMapping("/statistics/department")
    @RequirePermission("depreciation:view")
    public Result<List<DepartmentStatVO>> departmentStats() {
        return Result.success(depreciationReportService.departmentStats());
    }

    @GetMapping("/statistics/category")
    @RequirePermission("depreciation:view")
    public Result<List<CategoryStatVO>> categoryStats() {
        return Result.success(depreciationReportService.categoryStats());
    }

    @GetMapping("/trend")
    @RequirePermission("depreciation:view")
    public Result<List<DepreciationTrendVO>> trend() {
        return Result.success(depreciationReportService.trend());
    }

    @GetMapping("/summary")
    @RequirePermission("depreciation:view")
    public Result<DepreciationSummaryVO> summary() {
        return Result.success(depreciationReportService.getSummary());
    }

    @GetMapping("/low-value-assets")
    @RequirePermission("depreciation:view")
    public Result<List<LowValueAssetVO>> lowValueAssets() {
        return Result.success(depreciationReportService.getLowValueAssets());
    }

    @GetMapping("/near-end-assets")
    @RequirePermission("depreciation:view")
    public Result<List<NearEndAssetVO>> nearEndAssets() {
        return Result.success(depreciationReportService.getNearEndAssets());
    }
}
