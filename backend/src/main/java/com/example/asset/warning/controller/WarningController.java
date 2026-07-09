package com.example.asset.warning.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.warning.service.WarningService;
import com.example.asset.warning.vo.WarningItemVO;
import com.example.asset.warning.vo.WarningSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warnings")
public class WarningController {

    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping("/summary")
    public Result<WarningSummaryVO> summary() {
        return Result.success(warningService.getSummary());
    }

    @GetMapping("/items")
    public Result<PageResult<WarningItemVO>> items(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(warningService.getItems(type, level, pageNum, pageSize));
    }
}
