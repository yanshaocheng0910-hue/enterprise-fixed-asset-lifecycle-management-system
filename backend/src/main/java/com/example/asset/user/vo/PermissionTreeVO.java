package com.example.asset.user.vo;

import lombok.Data;

import java.util.List;

@Data
public class PermissionTreeVO {
    private String module;
    private List<PermissionItemVO> permissions;

    @Data
    public static class PermissionItemVO {
        private Long id;
        private String permissionCode;
        private String permissionName;
    }
}
