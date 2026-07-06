package com.example.asset.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.approval.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {

    @Select("SELECT * FROM approval_flow WHERE business_type = #{businessType} AND enabled = 1 LIMIT 1")
    ApprovalFlow findByBusinessType(@Param("businessType") String businessType);

}
