package com.example.asset.lifecycle.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetSelectVO {

    private Long id;
    private String assetCode;
    private String assetName;
    private String department;
    private String keeper;
    private String location;
    private String status;
}
