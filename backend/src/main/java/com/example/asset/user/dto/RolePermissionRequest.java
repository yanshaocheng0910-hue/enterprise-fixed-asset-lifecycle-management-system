package com.example.asset.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionRequest {
    @NotNull(message = "权限不能为空")
    private List<Long> permissionIds;
}
