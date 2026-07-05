package com.example.asset.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String realName;
    private String department;
    private List<String> roles;
}
