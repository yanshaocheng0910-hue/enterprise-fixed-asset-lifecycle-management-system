package com.example.asset.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.approval.entity.ApprovalInstance;
import com.example.asset.approval.vo.ApprovalDoneVO;
import com.example.asset.approval.vo.ApprovalTodoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApprovalInstanceMapper extends BaseMapper<ApprovalInstance> {

    @Select("SELECT * FROM approval_instance WHERE business_type = #{businessType} AND business_id = #{businessId} ORDER BY id DESC LIMIT 1")
    ApprovalInstance findByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    @Select("<script>"
            + "SELECT ai.id AS instanceId, ai.business_type AS businessType, ai.business_id AS businessId, "
            + "af.flow_name AS flowName, an.node_name AS nodeName, ai.status, "
            + "ai.started_by AS startedBy, ai.started_at AS startedAt "
            + "FROM approval_instance ai "
            + "JOIN approval_node an ON ai.current_node_id = an.id "
            + "JOIN approval_flow af ON ai.flow_id = af.id "
            + "WHERE ai.status = 'APPROVING' "
            + "<if test='roles != null and roles.size > 0'>"
            + "AND an.approver_role IN "
            + "<foreach item='role' collection='roles' open='(' separator=',' close=')'>#{role}</foreach>"
            + "</if>"
            + "ORDER BY ai.started_at DESC"
            + "</script>")
    IPage<ApprovalTodoVO> selectTodoPage(Page<ApprovalTodoVO> page, @Param("roles") List<String> roles);

    @Select("<script>"
            + "SELECT ar.instance_id AS instanceId, ai.business_type AS businessType, ai.business_id AS businessId, "
            + "ar.action, ar.comment, ai.status, ar.approver_name AS approverName, ar.approved_at AS approvedAt "
            + "FROM approval_record ar "
            + "JOIN approval_instance ai ON ar.instance_id = ai.id "
            + "WHERE ar.approver_id = #{userId} "
            + "AND ar.action IN ('APPROVED', 'REJECTED') "
            + "<if test='businessType != null and businessType != \"\"'>"
            + "AND ai.business_type = #{businessType} "
            + "</if>"
            + "ORDER BY ar.created_at DESC"
            + "</script>")
    IPage<ApprovalDoneVO> selectDonePage(Page<ApprovalDoneVO> page,
                                          @Param("userId") Long userId,
                                          @Param("businessType") String businessType);

    @Select("<script>"
            + "SELECT ai.id AS instanceId, ai.business_type AS businessType, ai.business_id AS businessId, "
            + "af.flow_name AS flowName, ai.status, ai.started_at AS startedAt, "
            + "ai.completed_at AS completedAt, ai.started_by AS startedBy "
            + "FROM approval_instance ai "
            + "JOIN approval_flow af ON ai.flow_id = af.id "
            + "WHERE 1=1 "
            + "<if test='businessType != null and businessType != \"\"'>"
            + "AND ai.business_type = #{businessType} "
            + "</if>"
            + "<if test='businessId != null'>"
            + "AND ai.business_id = #{businessId} "
            + "</if>"
            + "ORDER BY ai.created_at DESC "
            + "<if test='limit != null'>LIMIT #{limit}</if>"
            + "</script>")
    List<ApprovalTodoVO> selectByBusiness(@Param("businessType") String businessType,
                                           @Param("businessId") Long businessId,
                                           @Param("limit") Integer limit);
}
