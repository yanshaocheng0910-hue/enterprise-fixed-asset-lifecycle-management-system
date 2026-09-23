package com.example.asset.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetCategoryUpdateRequest {
    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;
    private Long parentId = 0L;
    @NotNull(message = "折旧年限不能为空")
    private Integer depreciationYears;
    private String remark;
}
