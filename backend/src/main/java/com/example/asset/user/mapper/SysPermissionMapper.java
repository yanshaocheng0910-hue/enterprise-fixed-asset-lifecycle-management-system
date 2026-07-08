package com.example.asset.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.user.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
