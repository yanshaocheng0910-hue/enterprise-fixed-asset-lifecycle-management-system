package com.example.asset.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String department;
    private String phone;
    private String email;
}
