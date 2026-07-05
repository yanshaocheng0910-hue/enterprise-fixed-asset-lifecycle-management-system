package com.example.asset.finance.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {

    public Map<String, Object> syncDepreciation() {
        return Map.of(
                "success", true,
                "message", "第一阶段为模拟财务同步，未接入真实财务系统",
                "syncTime", LocalDateTime.now().toString()
        );
    }

    public List<Map<String, Object>> syncRecords() {
        return List.of(
                Map.of(
                        "recordId", 1,
                        "syncType", "DEPRECIATION",
                        "status", "MOCK_SUCCESS",
                        "message", "第一阶段模拟同步记录",
                        "syncTime", LocalDateTime.now().minusDays(1).toString()
                )
        );
    }
}
