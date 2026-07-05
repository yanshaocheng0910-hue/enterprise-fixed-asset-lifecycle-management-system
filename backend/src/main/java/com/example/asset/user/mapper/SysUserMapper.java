package com.example.asset.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId} ORDER BY r.id")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
