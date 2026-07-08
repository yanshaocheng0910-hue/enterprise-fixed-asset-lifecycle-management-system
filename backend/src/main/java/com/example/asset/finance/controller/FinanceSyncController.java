package com.example.asset.finance.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.finance.entity.FinanceSyncRecord;
import com.example.asset.finance.service.FinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceSyncController {

    private final FinanceService financeService;

    public FinanceSyncController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/sync-depreciation")
    public Result<Map<String, Object>> syncDepreciation(@RequestParam String month) {
        return Result.success(financeService.syncDepreciation(month));
    }

    @GetMapping("/sync-records")
    public Result<PageResult<FinanceSyncRecord>> syncRecords(@RequestParam(defaultValue = "1") Long pageNum,
                                                              @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(financeService.syncRecords(pageNum, pageSize));
    }
}