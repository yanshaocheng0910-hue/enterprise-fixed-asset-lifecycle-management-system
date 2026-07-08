package com.example.asset.user.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import com.example.asset.user.dto.RoleCreateRequest;
import com.example.asset.user.dto.RolePermissionRequest;
import com.example.asset.user.dto.RoleUpdateRequest;
import com.example.asset.user.entity.SysRole;
import com.example.asset.user.service.RoleService;
import com.example.asset.user.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.success(roleService.all());
    }

    @GetMapping("/page")
    @RequirePermission("role:view")
    public Result<PageResult<RoleVO>> page() {
        return Result.success(roleService.page());
    }

    @GetMapping("/{id}")
    @RequirePermission("role:view")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    @RequirePermission("role:create")
    public Result<Long> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("role:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission("role:permission")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody RolePermissionRequest request) {
        roleService.assignPermissions(id, request);
        return Result.success();
    }
}
