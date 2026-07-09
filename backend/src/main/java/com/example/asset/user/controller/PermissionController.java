package com.example.asset.user.controller;

import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import com.example.asset.user.mapper.SysPermissionMapper;
import com.example.asset.user.service.RoleService;
import com.example.asset.user.vo.PermissionTreeVO;
import com.example.asset.context.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    private final RoleService roleService;
    private final SysPermissionMapper sysPermissionMapper;

    public PermissionController(RoleService roleService, SysPermissionMapper sysPermissionMapper) {
        this.roleService = roleService;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @GetMapping("/tree")
    @RequirePermission("role:view")
    public Result<List<PermissionTreeVO>> tree() {
        return Result.success(roleService.permissionTree());
    }

    @GetMapping("/my")
    public Result<List<String>> my() {
        Long userId = UserContext.getUserId();
        List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(userId);
        return Result.success(permissions);
    }
}
