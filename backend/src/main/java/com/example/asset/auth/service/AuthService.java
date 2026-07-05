package com.example.asset.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.asset.auth.dto.LoginRequest;
import com.example.asset.auth.dto.LoginResponse;
import com.example.asset.auth.util.JwtUtil;
import com.example.asset.common.BusinessException;
import com.example.asset.common.ResultCode;
import com.example.asset.context.LoginUser;
import com.example.asset.context.UserContext;
import com.example.asset.user.entity.SysUser;
import com.example.asset.user.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper sysUserMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roles(roles)
                .build();
        return LoginResponse.builder()
                .token(jwtUtil.generateToken(loginUser))
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .department(user.getDepartment())
                .roles(roles)
                .build();
    }

    public LoginResponse getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "当前用户不存在");
        }
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        return LoginResponse.builder()
                .token(null)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .department(user.getDepartment())
                .roles(roles)
                .build();
    }
}
