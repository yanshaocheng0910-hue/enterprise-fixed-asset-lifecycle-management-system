package com.example.asset.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.approval.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    @Select("SELECT * FROM approval_node WHERE flow_id = #{flowId} ORDER BY sort_order ASC")
    List<ApprovalNode> findByFlowIdOrderBySort(@Param("flowId") Long flowId);

}
