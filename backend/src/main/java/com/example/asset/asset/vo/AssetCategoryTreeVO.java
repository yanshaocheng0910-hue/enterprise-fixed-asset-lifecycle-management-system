package com.example.asset.asset.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssetCategoryTreeVO {

    private Long id;
    private String categoryCode;
    private String categoryName;
    private Long parentId;
    private Integer depreciationYears;
    private String remark;
    private List<AssetCategoryTreeVO> children = new ArrayList<>();
}
