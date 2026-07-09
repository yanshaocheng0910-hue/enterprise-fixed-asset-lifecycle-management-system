package com.example.asset.ai.controller;

import com.example.asset.ai.service.AiAnalysisService;
import com.example.asset.ai.vo.*;
import com.example.asset.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/summary")
    public Result<AiSummaryVO> summary() {
        return Result.success(aiAnalysisService.getSummary());
    }

    @GetMapping("/alerts")
    public Result<AiAlertVO> alerts() {
        return Result.success(aiAnalysisService.getAlerts());
    }

    @GetMapping("/suggestions")
    public Result<AiSuggestionVO> suggestions() {
        return Result.success(aiAnalysisService.getSuggestions());
    }

    @GetMapping("/report")
    public Result<AiReportVO> report() {
        return Result.success(aiAnalysisService.getReport());
    }
}
