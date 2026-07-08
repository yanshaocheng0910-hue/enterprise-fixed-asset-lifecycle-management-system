package com.example.asset.permission.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.asset.context.UserContext;
import com.example.asset.permission.annotation.RequirePermission;
import com.example.asset.permission.exception.PermissionDeniedException;
import com.example.asset.user.entity.SysRolePermission;
import com.example.asset.user.entity.SysPermission;
import com.example.asset.user.mapper.SysPermissionMapper;
import com.example.asset.user.mapper.SysRolePermissionMapper;
import com.example.asset.user.mapper.SysUserRoleMapper;
import com.example.asset.user.entity.SysUserRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class PermissionAspect {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public PermissionAspect(SysUserRoleMapper sysUserRoleMapper,
                            SysRolePermissionMapper sysRolePermissionMapper,
                            SysPermissionMapper sysPermissionMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    @Pointcut("@annotation(com.example.asset.permission.annotation.RequirePermission)")
    public void permissionPointcut() {
    }

    @Around("permissionPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new PermissionDeniedException("未登录");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequirePermission annotation = signature.getMethod().getAnnotation(RequirePermission.class);
        String requiredPermission = annotation.value();

        List<Long> roleIds = sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();

        if (roleIds.isEmpty()) {
            throw new PermissionDeniedException("无操作权限");
        }

        List<Long> permissionIds = sysRolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermissionId).distinct().toList();

        if (permissionIds.isEmpty()) {
            throw new PermissionDeniedException("无操作权限");
        }

        boolean hasPermission = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permissionIds))
                .stream().anyMatch(p -> p.getPermissionCode().equals(requiredPermission));

        if (!hasPermission) {
            throw new PermissionDeniedException("无操作权限");
        }

        return joinPoint.proceed();
    }
}
