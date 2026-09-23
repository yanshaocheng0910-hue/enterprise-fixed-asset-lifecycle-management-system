package com.example.asset.asset.enums;

import lombok.Getter;

@Getter
public enum AssetStatusEnum {

    IDLE("IDLE", "闲置"),
    IN_USE("IN_USE", "使用中"),
    TRANSFERRING("TRANSFERRING", "调拨中"),
    REPAIRING("REPAIRING", "维修中"),
    WAITING_SCRAP("WAITING_SCRAP", "待报废"),
    SCRAPPED("SCRAPPED", "已报废"),
    INVENTORY_ABNORMAL("INVENTORY_ABNORMAL", "盘点异常");

    private final String code;
    private final String label;

    AssetStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
