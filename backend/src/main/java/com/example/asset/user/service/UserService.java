package com.example.asset.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.common.PageResult;
import com.example.asset.context.UserContext;
import com.example.asset.user.dto.ChangePasswordRequest;
import com.example.asset.user.dto.UserCreateRequest;
import com.example.asset.user.dto.UserPageRequest;
import com.example.asset.user.dto.UserRoleRequest;
import com.example.asset.user.dto.UserStatusRequest;
import com.example.asset.user.dto.UserUpdateRequest;
import com.example.asset.user.entity.SysRole;
import com.example.asset.user.entity.SysUser;
import com.example.asset.user.entity.SysUserRole;
import com.example.asset.user.mapper.SysRoleMapper;
import com.example.asset.user.mapper.SysUserMapper;
import com.example.asset.user.mapper.SysUserRoleMapper;
import com.example.asset.user.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserMapper sysUserMapper,
                       SysRoleMapper sysRoleMapper,
                       SysUserRoleMapper sysUserRoleMapper,
                       PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<UserVO> page(UserPageRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getUsername()), SysUser::getUsername, request.getUsername())
                .like(StringUtils.hasText(request.getRealName()), SysUser::getRealName, request.getRealName())
                .eq(request.getStatus() != null, SysUser::getStatus, request.getStatus())
                .orderByDesc(SysUser::getCreatedAt);

        IPage<SysUser> page = sysUserMapper.selectPage(
                new Page<>(request.getPageNum(), request.getPageSize()), wrapper);

        List<SysUser> users = page.getRecords();
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<UserVO> voList = new ArrayList<>();

        if (!userIds.isEmpty()) {
            List<SysUserRole> allUserRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
            Map<Long, List<Long>> userRoleMap = allUserRoles.stream()
                    .collect(Collectors.groupingBy(SysUserRole::getUserId,
                            Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));

            List<Long> allRoleIds = allUserRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
            final Map<Long, String> roleNameMap;
            if (!allRoleIds.isEmpty()) {
                roleNameMap = sysRoleMapper.selectList(
                                new LambdaQueryWrapper<SysRole>().in(SysRole::getId, allRoleIds))
                        .stream().collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
            } else {
                roleNameMap = Map.of();
            }

            for (SysUser user : users) {
                UserVO vo = buildUserVO(user);
                List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), List.of());
                vo.setRoleIds(roleIds);
                vo.setRoleNames(roleIds.stream().map(rid -> roleNameMap.getOrDefault(rid, "")).filter(s -> !s.isEmpty()).toList());
                voList.add(vo);
            }
        }

        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public UserVO getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        UserVO vo = buildUserVO(user);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        vo.setRoleIds(roleIds);

        if (!roleIds.isEmpty()) {
            List<String> roleNames = sysRoleMapper.selectList(
                            new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                    .stream().map(SysRole::getRoleName).toList();
            vo.setRoleNames(roleNames);
        }
        return vo;
    }

    @Transactional
    public Long create(UserCreateRequest request) {
        SysUser exist = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (exist != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user.getId();
    }

    @Transactional
    public void update(Long id, UserUpdateRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setRealName(request.getRealName());
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        sysUserMapper.updateById(user);
    }

    @Transactional
    public void updateStatus(Long id, UserStatusRequest request) {
        Long currentUserId = UserContext.getUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不允许禁用自己的账号");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不允许删除自己的账号");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new IllegalArgumentException("系统管理员账号不可删除");
        }
        sysUserMapper.deleteById(id);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    @Transactional
    public void assignRoles(Long id, UserRoleRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<SysUserRole> userRoles = request.getRoleIds().stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                return ur;
            }).toList();
            for (SysUserRole ur : userRoles) {
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = UserContext.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    private UserVO buildUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setDepartment(user.getDepartment());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
