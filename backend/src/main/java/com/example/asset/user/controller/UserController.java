package com.example.asset.user.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import com.example.asset.user.dto.ChangePasswordRequest;
import com.example.asset.user.dto.UserCreateRequest;
import com.example.asset.user.dto.UserPageRequest;
import com.example.asset.user.dto.UserRoleRequest;
import com.example.asset.user.dto.UserStatusRequest;
import com.example.asset.user.dto.UserUpdateRequest;
import com.example.asset.user.service.UserService;
import com.example.asset.user.vo.UserVO;
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

@Validated
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    @RequirePermission("user:view")
    public Result<PageResult<UserVO>> page(@Valid UserPageRequest request) {
        return Result.success(userService.page(request));
    }

    @GetMapping("/{id}")
    @RequirePermission("user:view")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    @RequirePermission("user:create")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("user:status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        userService.updateStatus(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("user:role")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        userService.assignRoles(id, request);
        return Result.success();
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success();
    }
}
