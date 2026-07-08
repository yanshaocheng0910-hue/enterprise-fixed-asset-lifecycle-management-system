package com.example.asset.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.common.PageResult;
import com.example.asset.user.dto.RoleCreateRequest;
import com.example.asset.user.dto.RolePermissionRequest;
import com.example.asset.user.dto.RoleUpdateRequest;
import com.example.asset.user.entity.SysPermission;
import com.example.asset.user.entity.SysRole;
import com.example.asset.user.entity.SysRolePermission;
import com.example.asset.user.entity.SysUserRole;
import com.example.asset.user.mapper.SysPermissionMapper;
import com.example.asset.user.mapper.SysRoleMapper;
import com.example.asset.user.mapper.SysRolePermissionMapper;
import com.example.asset.user.mapper.SysUserRoleMapper;
import com.example.asset.user.vo.PermissionTreeVO;
import com.example.asset.user.vo.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public RoleService(SysRoleMapper sysRoleMapper,
                       SysPermissionMapper sysPermissionMapper,
                       SysRolePermissionMapper sysRolePermissionMapper,
                       SysUserRoleMapper sysUserRoleMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    public List<SysRole> all() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    public PageResult<RoleVO> page() {
        IPage<SysRole> page = sysRoleMapper.selectPage(
                new Page<>(1, 100), new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));

        List<RoleVO> voList = new ArrayList<>();
        for (SysRole role : page.getRecords()) {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
            vo.setDescription(role.getDescription());
            vo.setCreatedAt(role.getCreatedAt());

            List<SysRolePermission> rps = sysRolePermissionMapper.selectList(
                    new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, role.getId()));
            vo.setPermissionIds(rps.stream().map(SysRolePermission::getPermissionId).toList());

            Long userCount = sysUserRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, role.getId()));
            vo.setUserCount(userCount);

            voList.add(vo);
        }
        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public RoleVO getById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setCreatedAt(role.getCreatedAt());

        List<SysRolePermission> rps = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        vo.setPermissionIds(rps.stream().map(SysRolePermission::getPermissionId).toList());
        return vo;
    }

    @Transactional
    public Long create(RoleCreateRequest request) {
        SysRole exist = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, request.getRoleCode()));
        if (exist != null) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        sysRoleMapper.insert(role);
        return role.getId();
    }

    @Transactional
    public void update(Long id, RoleUpdateRequest request) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        sysRoleMapper.updateById(role);
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        Long userCount = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new IllegalArgumentException("该角色下有" + userCount + "个用户被分配，请先取消分配");
        }
        sysRoleMapper.deleteById(id);
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
    }

    @Transactional
    public void assignPermissions(Long id, RolePermissionRequest request) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<SysRolePermission> rps = request.getPermissionIds().stream().map(permId -> {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permId);
                return rp;
            }).toList();
            for (SysRolePermission rp : rps) {
                sysRolePermissionMapper.insert(rp);
            }
        }
    }

    public List<PermissionTreeVO> permissionTree() {
        List<SysPermission> all = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getId));

        Map<String, List<SysPermission>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysPermission::getModule, LinkedHashMap::new, Collectors.toList()));

        List<PermissionTreeVO> tree = new ArrayList<>();
        for (Map.Entry<String, List<SysPermission>> entry : grouped.entrySet()) {
            PermissionTreeVO node = new PermissionTreeVO();
            node.setModule(entry.getKey());
            node.setPermissions(entry.getValue().stream().map(p -> {
                PermissionTreeVO.PermissionItemVO item = new PermissionTreeVO.PermissionItemVO();
                item.setId(p.getId());
                item.setPermissionCode(p.getPermissionCode());
                item.setPermissionName(p.getPermissionName());
                return item;
            }).toList());
            tree.add(node);
        }
        return tree;
    }
}
