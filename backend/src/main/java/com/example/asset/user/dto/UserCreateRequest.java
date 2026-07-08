package com.example.asset.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度为6-64个字符")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String department;
    private String phone;
    private String email;
}
