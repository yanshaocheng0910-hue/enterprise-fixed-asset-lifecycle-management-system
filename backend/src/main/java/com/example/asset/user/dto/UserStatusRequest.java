package com.example.asset.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
