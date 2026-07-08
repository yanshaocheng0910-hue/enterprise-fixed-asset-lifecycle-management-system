package com.example.asset.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private List<Long> permissionIds;
    private Long userCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
