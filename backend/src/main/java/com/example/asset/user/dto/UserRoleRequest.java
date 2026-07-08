package com.example.asset.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleRequest {
    @NotNull(message = "角色不能为空")
    private List<Long> roleIds;
}
