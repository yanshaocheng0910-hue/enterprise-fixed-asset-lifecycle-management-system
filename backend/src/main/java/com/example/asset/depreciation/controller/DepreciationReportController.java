package com.example.asset.depreciation.controller;

import com.example.asset.common.Result;
import com.example.asset.depreciation.service.DepreciationReportService;
import com.example.asset.depreciation.vo.*;
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
    public Result<MonthlyReportSummaryVO> monthlySummary(@RequestParam String month) {
        return Result.success(depreciationReportService.monthlySummary(month));
    }

    @GetMapping("/report/monthly")
    public Result<List<MonthlyDepreciationItemVO>> monthlyItems(@RequestParam String month) {
        return Result.success(depreciationReportService.monthlyItems(month));
    }

    @GetMapping("/statistics/department")
    public Result<List<DepartmentStatVO>> departmentStats() {
        return Result.success(depreciationReportService.departmentStats());
    }

    @GetMapping("/statistics/category")
    public Result<List<CategoryStatVO>> categoryStats() {
        return Result.success(depreciationReportService.categoryStats());
    }

    @GetMapping("/trend")
    public Result<List<DepreciationTrendVO>> trend() {
        return Result.success(depreciationReportService.trend());
    }
}
