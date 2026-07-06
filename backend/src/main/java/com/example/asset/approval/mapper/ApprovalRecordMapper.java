package com.example.asset.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.approval.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    @Select("SELECT * FROM approval_record WHERE instance_id = #{instanceId} ORDER BY created_at ASC")
    List<ApprovalRecord> findByInstanceIdOrderByCreatedAt(@Param("instanceId") Long instanceId);

}
