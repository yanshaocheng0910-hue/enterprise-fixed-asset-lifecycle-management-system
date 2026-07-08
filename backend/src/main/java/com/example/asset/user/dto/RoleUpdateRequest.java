package com.example.asset.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;
}
