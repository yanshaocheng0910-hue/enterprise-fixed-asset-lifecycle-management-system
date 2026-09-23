package com.example.asset.masterdata.vo;

import lombok.Data;

@Data
public class MasterDataVO {

    private Long id;
    private String code;
    private String name;
    private String label;
    private String value;
    private String extraInfo;
}
