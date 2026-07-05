package com.example.asset.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TrendPointVO {

    private String month;
    private BigDecimal value;
}
