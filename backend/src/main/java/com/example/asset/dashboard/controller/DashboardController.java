package com.example.asset.dashboard.controller;

import com.example.asset.common.Result;
import com.example.asset.dashboard.service.DashboardService;
import com.example.asset.dashboard.vo.DashboardStatsVO;
import com.example.asset.dashboard.vo.DepartmentRankingVO;
import com.example.asset.dashboard.vo.NameValueVO;
import com.example.asset.dashboard.vo.TrendPointVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.stats());
    }

    @GetMapping("/category-distribution")
    public Result<List<NameValueVO>> categoryDistribution() {
        return Result.success(dashboardService.categoryDistribution());
    }

    @GetMapping("/department-ranking")
    public Result<List<DepartmentRankingVO>> departmentRanking() {
        return Result.success(dashboardService.departmentRanking());
    }

    @GetMapping("/depreciation-trend")
    public Result<List<TrendPointVO>> depreciationTrend() {
        return Result.success(dashboardService.depreciationTrend());
    }
}
