package com.example.asset.finance.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.finance.service.FinanceService;
import com.example.asset.finance.vo.FinanceSyncRecordVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
public class FinanceSyncController {

    private final FinanceService financeService;

    public FinanceSyncController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/sync/depreciation")
    public Result<FinanceSyncRecordVO> syncDepreciation(@RequestParam String month) {
        return Result.success(financeService.syncDepreciation(month));
    }

    @GetMapping("/sync/records")
    public Result<PageResult<FinanceSyncRecordVO>> syncRecords(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(financeService.syncRecords(pageNum, pageSize));
    }

    @GetMapping("/sync/records/{id}")
    public Result<FinanceSyncRecordVO> getSyncDetail(@PathVariable Long id) {
        return Result.success(financeService.getSyncDetail(id));
    }
}
