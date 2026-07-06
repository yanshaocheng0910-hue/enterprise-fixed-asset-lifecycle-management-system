package com.example.asset.approval.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.asset.approval.dto.ApprovalActionRequest;
import com.example.asset.approval.dto.ApprovalPageRequest;
import com.example.asset.approval.dto.ApprovalSubmitRequest;
import com.example.asset.approval.entity.*;
import com.example.asset.approval.mapper.*;
import com.example.asset.approval.vo.*;
import com.example.asset.common.BusinessException;
import com.example.asset.common.PageResult;
import com.example.asset.common.ResultCode;
import com.example.asset.context.UserContext;
import com.example.asset.user.entity.SysUser;
import com.example.asset.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private static final List<String> SUPPORTED_BUSINESS_TYPES = List.of("RECEIVE", "TRANSFER", "REPAIR", "SCRAP");

    private final ApprovalFlowMapper approvalFlowMapper;
    private final ApprovalNodeMapper approvalNodeMapper;
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final SysUserMapper sysUserMapper;

    public ApprovalService(ApprovalFlowMapper approvalFlowMapper,
                           ApprovalNodeMapper approvalNodeMapper,
                           ApprovalInstanceMapper approvalInstanceMapper,
                           ApprovalRecordMapper approvalRecordMapper,
                           SysUserMapper sysUserMapper) {
        this.approvalFlowMapper = approvalFlowMapper;
        this.approvalNodeMapper = approvalNodeMapper;
        this.approvalInstanceMapper = approvalInstanceMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.sysUserMapper = sysUserMapper;
    }

    // ======================== Submit ========================

    /**
     * 提交审批
     * 创建审批实例和首条审批记录（SUBMIT），不修改资产状态，不执行业务流转。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApprovalSubmitRequest req) {
        if (!SUPPORTED_BUSINESS_TYPES.contains(req.getBusinessType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "不支持的业务类型: " + req.getBusinessType() + "，支持: " + String.join(", ", SUPPORTED_BUSINESS_TYPES));
        }

        // 查找审批模板
        ApprovalFlow flow = approvalFlowMapper.findByBusinessType(req.getBusinessType());
        if (flow == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "未找到 " + req.getBusinessType() + " 对应的审批配置");
        }

        // 校验是否已有未完成的审批实例
        ApprovalInstance existing = approvalInstanceMapper.findByBusiness(req.getBusinessType(), req.getBusinessId());
        if (existing != null && ("SUBMITTED".equals(existing.getStatus()) || "APPROVING".equals(existing.getStatus()))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该单据已在审批中，不能重复提交");
        }

        // 获取审批节点列表
        List<ApprovalNode> nodes = approvalNodeMapper.findByFlowIdOrderBySort(flow.getId());
        if (nodes.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审批流程未配置审批节点");
        }

        Long currentUserId = UserContext.getUserId();
        String currentUsername = UserContext.getUsername();

        // 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setBusinessType(req.getBusinessType());
        instance.setBusinessId(req.getBusinessId());
        instance.setFlowId(flow.getId());
        instance.setCurrentNodeId(nodes.get(0).getId()); // 第一个审批节点
        instance.setStatus("APPROVING");
        instance.setStartedBy(currentUserId);
        instance.setStartedAt(LocalDateTime.now());
        approvalInstanceMapper.insert(instance);

        // 创建 SUBMIT 记录
        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(instance.getId());
        record.setNodeId(0L); // 0 表示提交节点
        record.setApproverId(null);
        record.setApproverName(null);
        record.setAction("SUBMIT");
        record.setComment(req.getRemark());
        record.setStatus("APPROVING");
        record.setApprovedAt(null);
        approvalRecordMapper.insert(record);

        return instance.getId();
    }

    // ======================== Approve / Reject (stubs) ========================

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long instanceId, ApprovalActionRequest req) {
        throw new BusinessException(ResultCode.BAD_REQUEST,
                "审批通过功能将在 3.3 版本实现，当前为审批基础骨架阶段");
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long instanceId, ApprovalActionRequest req) {
        throw new BusinessException(ResultCode.BAD_REQUEST,
                "审批驳回功能将在 3.3 版本实现，当前为审批基础骨架阶段");
    }

    // ======================== Todo Page ========================

    /**
     * 我的待办
     * 根据当前用户角色匹配审批节点，返回待审批的审批实例列表。
     */
    public PageResult<ApprovalTodoVO> todoPage(ApprovalPageRequest req) {
        List<String> userRoles = UserContext.get().getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            return new PageResult<>();
        }

        Page<ApprovalTodoVO> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<ApprovalTodoVO> result = approvalInstanceMapper.selectTodoPage(page, userRoles);

        // 填充申请人姓名
        for (ApprovalTodoVO vo : result.getRecords()) {
            vo.setApplicantName(getUserNameById(vo.getStartedBy()));
        }

        return PageResult.of(result);
    }

    // ======================== Done Page ========================

    /**
     * 我的已办
     * 查询当前用户已处理的审批记录。
     */
    public PageResult<ApprovalDoneVO> donePage(ApprovalPageRequest req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return new PageResult<>();
        }

        Page<ApprovalDoneVO> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<ApprovalDoneVO> result = approvalInstanceMapper.selectDonePage(page, userId, req.getBusinessType());

        return PageResult.of(result);
    }

    // ======================== Records ========================

    /**
     * 查询审批记录
     * 根据业务类型和业务 ID 查找审批实例下的所有审批记录。
     */
    public List<ApprovalRecordVO> getRecords(String businessType, Long businessId) {
        // 查找审批实例
        ApprovalInstance instance = approvalInstanceMapper.findByBusiness(businessType, businessId);
        if (instance == null) {
            return new ArrayList<>();
        }

        // 查询审批记录
        List<ApprovalRecord> records = approvalRecordMapper.findByInstanceIdOrderByCreatedAt(instance.getId());

        // 转换为 VO
        return records.stream().map(r -> {
            ApprovalRecordVO vo = new ApprovalRecordVO();
            vo.setAction(r.getAction());
            vo.setComment(r.getComment());
            vo.setApprovedAt(r.getApprovedAt());
            // 如果是 SUBMIT 记录，用"提交人"代替
            if ("SUBMIT".equals(r.getAction())) {
                vo.setNodeName("提交申请");
                vo.setApproverName(getUserNameById(instance.getStartedBy()));
            } else {
                // 查找节点名称
                if (r.getNodeId() != null && r.getNodeId() > 0) {
                    ApprovalNode node = approvalNodeMapper.selectById(r.getNodeId());
                    vo.setNodeName(node != null ? node.getNodeName() : "未知节点");
                } else {
                    vo.setNodeName("未知节点");
                }
                vo.setApproverName(r.getApproverName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // ======================== Detail ========================

    /**
     * 查询审批详情
     * 返回审批实例信息、当前节点信息、审批流程信息和审批记录列表。
     */
    public ApprovalDetailVO getDetail(Long instanceId) {
        ApprovalInstance instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审批实例不存在");
        }

        ApprovalDetailVO vo = new ApprovalDetailVO();
        vo.setInstanceId(instance.getId());
        vo.setBusinessType(instance.getBusinessType());
        vo.setBusinessId(instance.getBusinessId());
        vo.setStatus(instance.getStatus());
        vo.setStartedAt(instance.getStartedAt());
        vo.setCompletedAt(instance.getCompletedAt());

        // 查询审批流名称
        ApprovalFlow flow = approvalFlowMapper.selectById(instance.getFlowId());
        vo.setFlowName(flow != null ? flow.getFlowName() : "未知流程");

        // 查询当前节点名称
        if (instance.getCurrentNodeId() != null && instance.getCurrentNodeId() > 0) {
            ApprovalNode currentNode = approvalNodeMapper.selectById(instance.getCurrentNodeId());
            vo.setCurrentNodeName(currentNode != null ? currentNode.getNodeName() : null);
        }

        // 申请人姓名
        vo.setApplicantName(getUserNameById(instance.getStartedBy()));

        // 审批记录
        List<ApprovalRecord> records = approvalRecordMapper.findByInstanceIdOrderByCreatedAt(instanceId);
        List<ApprovalRecordVO> recordVOs = records.stream().map(r -> {
            ApprovalRecordVO rvo = new ApprovalRecordVO();
            rvo.setAction(r.getAction());
            rvo.setComment(r.getComment());
            rvo.setApprovedAt(r.getApprovedAt());

            if ("SUBMIT".equals(r.getAction())) {
                rvo.setNodeName("提交申请");
                rvo.setApproverName(vo.getApplicantName());
            } else {
                if (r.getNodeId() != null && r.getNodeId() > 0) {
                    ApprovalNode node = approvalNodeMapper.selectById(r.getNodeId());
                    rvo.setNodeName(node != null ? node.getNodeName() : "未知节点");
                } else {
                    rvo.setNodeName("未知节点");
                }
                rvo.setApproverName(r.getApproverName());
            }
            return rvo;
        }).collect(Collectors.toList());
        vo.setRecords(recordVOs);

        return vo;
    }

    // ======================== Helpers ========================

    private String getUserNameById(Long userId) {
        if (userId == null) return "未知";
        try {
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null && user.getRealName() != null) {
                return user.getRealName();
            }
        } catch (Exception ignored) {
        }
        return "用户" + userId;
    }
}
