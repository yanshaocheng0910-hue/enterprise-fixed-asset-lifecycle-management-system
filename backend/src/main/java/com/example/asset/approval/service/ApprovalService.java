package com.example.asset.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.example.asset.lifecycle.service.LifecycleService;
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
    private final LifecycleService lifecycleService;

    public ApprovalService(ApprovalFlowMapper approvalFlowMapper,
                           ApprovalNodeMapper approvalNodeMapper,
                           ApprovalInstanceMapper approvalInstanceMapper,
                           ApprovalRecordMapper approvalRecordMapper,
                           SysUserMapper sysUserMapper,
                           LifecycleService lifecycleService) {
        this.approvalFlowMapper = approvalFlowMapper;
        this.approvalNodeMapper = approvalNodeMapper;
        this.approvalInstanceMapper = approvalInstanceMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.sysUserMapper = sysUserMapper;
        this.lifecycleService = lifecycleService;
    }

    // ======================== Submit ========================

    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApprovalSubmitRequest req) {
        if (!SUPPORTED_BUSINESS_TYPES.contains(req.getBusinessType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "不支持的业务类型: " + req.getBusinessType() + "，支持: " + String.join(", ", SUPPORTED_BUSINESS_TYPES));
        }

        ApprovalFlow flow = approvalFlowMapper.findByBusinessType(req.getBusinessType());
        if (flow == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "未找到 " + req.getBusinessType() + " 对应的审批配置");
        }

        ApprovalInstance existing = approvalInstanceMapper.findByBusiness(req.getBusinessType(), req.getBusinessId());
        if (existing != null && ("SUBMITTED".equals(existing.getStatus()) || "APPROVING".equals(existing.getStatus()))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该单据已在审批中，不能重复提交");
        }

        List<ApprovalNode> nodes = approvalNodeMapper.findByFlowIdOrderBySort(flow.getId());
        if (nodes.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审批流程未配置审批节点");
        }

        Long currentUserId = UserContext.getUserId();

        ApprovalInstance instance = new ApprovalInstance();
        instance.setBusinessType(req.getBusinessType());
        instance.setBusinessId(req.getBusinessId());
        instance.setFlowId(flow.getId());
        instance.setCurrentNodeId(nodes.get(0).getId());
        instance.setStatus("APPROVING");
        instance.setStartedBy(currentUserId);
        instance.setStartedAt(LocalDateTime.now());
        approvalInstanceMapper.insert(instance);

        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(instance.getId());
        record.setNodeId(0L);
        record.setAction("SUBMIT");
        record.setComment(req.getRemark());
        record.setStatus("APPROVING");
        approvalRecordMapper.insert(record);

        // 更新单据状态为审批中
        lifecycleService.updateOrderStatus(req.getBusinessType(), req.getBusinessId(), "APPROVING");

        return instance.getId();
    }

    // ======================== Approve ========================

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long instanceId, ApprovalActionRequest req) {
        ApprovalInstance instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审批实例不存在");
        }
        if (!"APPROVING".equals(instance.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审批状态不是审批中，无法审批通过");
        }

        // Check role matches current node
        ApprovalNode currentNode = approvalNodeMapper.selectById(instance.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审批节点不存在");
        }
        List<String> userRoles = UserContext.get().getRoles();
        if (userRoles == null || (!userRoles.contains(currentNode.getApproverRole()) && !userRoles.contains("ADMIN"))) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前用户没有该节点的审批权限");
        }

        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String comment = req.getComment() != null ? req.getComment() : "";

        // Get next node
        List<ApprovalNode> allNodes = approvalNodeMapper.findByFlowIdOrderBySort(instance.getFlowId());
        ApprovalNode nextNode = null;
        for (int i = 0; i < allNodes.size() - 1; i++) {
            if (allNodes.get(i).getId().equals(instance.getCurrentNodeId())) {
                nextNode = allNodes.get(i + 1);
                break;
            }
        }

        if (nextNode != null) {
            // Still has next approval node
            instance.setCurrentNodeId(nextNode.getId());
            instance.setStatus("APPROVING");
            approvalInstanceMapper.updateById(instance);

            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instanceId);
            record.setNodeId(currentNode.getId());
            record.setApproverId(userId);
            record.setApproverName(getUserNameById(userId));
            record.setAction("APPROVED");
            record.setComment(comment);
            record.setStatus("APPROVING");
            record.setApprovedAt(LocalDateTime.now());
            approvalRecordMapper.insert(record);
        } else {
            // Last node approved — complete flow and execute business logic
            instance.setStatus("COMPLETED");
            instance.setCurrentNodeId(null);
            instance.setCompletedAt(LocalDateTime.now());
            approvalInstanceMapper.updateById(instance);

            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instanceId);
            record.setNodeId(currentNode.getId());
            record.setApproverId(userId);
            record.setApproverName(getUserNameById(userId));
            record.setAction("APPROVED");
            record.setComment(comment);
            record.setStatus("COMPLETED");
            record.setApprovedAt(LocalDateTime.now());
            approvalRecordMapper.insert(record);

            // Execute lifecycle flow
            executeBusinessFlow(instance.getBusinessType(), instance.getBusinessId());
        }
    }

    // ======================== Reject ========================

    @Transactional(rollbackFor = Exception.class)
    public void reject(Long instanceId, ApprovalActionRequest req) {
        ApprovalInstance instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审批实例不存在");
        }
        if (!"APPROVING".equals(instance.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审批状态不是审批中，无法驳回");
        }

        ApprovalNode currentNode = approvalNodeMapper.selectById(instance.getCurrentNodeId());
        if (currentNode == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审批节点不存在");
        }
        List<String> userRoles = UserContext.get().getRoles();
        if (userRoles == null || (!userRoles.contains(currentNode.getApproverRole()) && !userRoles.contains("ADMIN"))) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前用户没有该节点的审批权限");
        }

        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String comment = req.getComment() != null ? req.getComment() : "";

        // Reject instance
        instance.setStatus("REJECTED");
        instance.setCurrentNodeId(null);
        instance.setCompletedAt(LocalDateTime.now());
        approvalInstanceMapper.updateById(instance);

        // Write record
        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(instanceId);
        record.setNodeId(currentNode.getId());
        record.setApproverId(userId);
        record.setApproverName(getUserNameById(userId));
        record.setAction("REJECTED");
        record.setComment(comment);
        record.setStatus("REJECTED");
        record.setApprovedAt(LocalDateTime.now());
        approvalRecordMapper.insert(record);

        // Do NOT modify asset status — rejection leaves asset untouched
        // 单据状态恢复为草稿，允许修改后重新提交
        lifecycleService.updateOrderStatus(instance.getBusinessType(), instance.getBusinessId(), "DRAFT");
    }

    // ======================== Todo Page ========================

    public PageResult<ApprovalTodoVO> todoPage(ApprovalPageRequest req) {
        List<String> userRoles = UserContext.get().getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            return new PageResult<>();
        }

        // ADMIN 角色可以看到所有待办
        if (userRoles.contains("ADMIN")) {
            userRoles = new ArrayList<>();
        }

        Page<ApprovalTodoVO> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<ApprovalTodoVO> result = approvalInstanceMapper.selectTodoPage(page, userRoles);

        for (ApprovalTodoVO vo : result.getRecords()) {
            vo.setApplicantName(getUserNameById(vo.getStartedBy()));
        }

        return PageResult.of(result);
    }

    // ======================== Done Page ========================

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

    public List<ApprovalRecordVO> getRecords(String businessType, Long businessId) {
        ApprovalInstance instance = approvalInstanceMapper.findByBusiness(businessType, businessId);
        if (instance == null) {
            return new ArrayList<>();
        }

        List<ApprovalRecord> records = approvalRecordMapper.findByInstanceIdOrderByCreatedAt(instance.getId());

        return records.stream().map(r -> {
            ApprovalRecordVO vo = new ApprovalRecordVO();
            vo.setAction(r.getAction());
            vo.setComment(r.getComment());
            vo.setApprovedAt(r.getApprovedAt());
            if ("SUBMIT".equals(r.getAction())) {
                vo.setNodeName("提交申请");
                vo.setApproverName(getUserNameById(instance.getStartedBy()));
            } else {
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

        ApprovalFlow flow = approvalFlowMapper.selectById(instance.getFlowId());
        vo.setFlowName(flow != null ? flow.getFlowName() : "未知流程");

        if (instance.getCurrentNodeId() != null && instance.getCurrentNodeId() > 0) {
            ApprovalNode currentNode = approvalNodeMapper.selectById(instance.getCurrentNodeId());
            vo.setCurrentNodeName(currentNode != null ? currentNode.getNodeName() : null);
        }

        vo.setApplicantName(getUserNameById(instance.getStartedBy()));

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

    // ======================== Private ========================

    private void executeBusinessFlow(String businessType, Long businessId) {
        switch (businessType) {
            case "RECEIVE":
                lifecycleService.executeReceiveFlow(businessId);
                break;
            case "TRANSFER":
                lifecycleService.executeTransferFlow(businessId);
                break;
            case "REPAIR":
                lifecycleService.executeRepairStartFlow(businessId);
                break;
            case "SCRAP":
                lifecycleService.executeScrapFlow(businessId);
                break;
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的审批业务类型: " + businessType);
        }
    }

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
