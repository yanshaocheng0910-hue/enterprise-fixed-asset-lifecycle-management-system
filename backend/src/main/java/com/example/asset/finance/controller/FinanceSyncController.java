package com.example.asset.finance.controller;

import com.example.asset.common.Result;
import com.example.asset.finance.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceSyncController {

    private final FinanceService financeService;

    public FinanceSyncController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/sync-depreciation")
    public Result<Map<String, Object>> syncDepreciation() {
        return Result.success(financeService.syncDepreciation());
    }

    @GetMapping("/sync-records")
    public Result<List<Map<String, Object>>> syncRecords() {
        return Result.success(financeService.syncRecords());
    }
}
