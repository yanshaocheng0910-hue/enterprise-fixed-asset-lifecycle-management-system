package com.example.asset.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.asset.entity.Asset;
import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.common.BusinessException;
import com.example.asset.common.PageResult;
import com.example.asset.common.ResultCode;
import com.example.asset.context.UserContext;
import com.example.asset.inventory.dto.InventoryRecordUpdateRequest;
import com.example.asset.inventory.dto.InventoryTaskCreateRequest;
import com.example.asset.inventory.dto.InventoryTaskQueryRequest;
import com.example.asset.inventory.entity.InventoryRecord;
import com.example.asset.inventory.entity.InventoryTask;
import com.example.asset.inventory.mapper.InventoryRecordMapper;
import com.example.asset.inventory.mapper.InventoryTaskMapper;
import com.example.asset.inventory.vo.InventoryRecordVO;
import com.example.asset.inventory.vo.InventoryTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final DateTimeFormatter TASK_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final InventoryTaskMapper inventoryTaskMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final AssetMapper assetMapper;

    public InventoryService(InventoryTaskMapper inventoryTaskMapper,
                            InventoryRecordMapper inventoryRecordMapper,
                            AssetMapper assetMapper) {
        this.inventoryTaskMapper = inventoryTaskMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.assetMapper = assetMapper;
    }

    public PageResult<InventoryTaskVO> page(InventoryTaskQueryRequest query) {
        Page<InventoryTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<InventoryTask> wrapper = new LambdaQueryWrapper<InventoryTask>()
                .eq(query.getStatus() != null && !query.getStatus().isEmpty(),
                        InventoryTask::getStatus, query.getStatus())
                .eq(query.getScopeType() != null && !query.getScopeType().isEmpty(),
                        InventoryTask::getScopeType, query.getScopeType())
                .orderByDesc(InventoryTask::getCreatedAt);
        Page<InventoryTask> result = inventoryTaskMapper.selectPage(page, wrapper);
        List<InventoryTaskVO> vos = result.getRecords().stream()
                .map(this::toTaskVO)
                .collect(Collectors.toList());
        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(InventoryTaskCreateRequest request) {
        validateScope(request);

        String prefix = "PD" + LocalDateTime.now().format(TASK_CODE_FORMATTER);
        String taskCode = generateTaskCode(prefix);

        InventoryTask task = new InventoryTask();
        task.setTaskCode(taskCode);
        task.setTaskName(request.getTaskName());
        task.setScopeType(request.getScopeType());
        task.setDepartment(request.getDepartment());
        task.setLocation(request.getLocation());
        task.setStatus("IN_PROGRESS");
        task.setStartTime(LocalDateTime.now());
        task.setCreatedBy(UserContext.getUserId());
        inventoryTaskMapper.insert(task);

        LambdaQueryWrapper<Asset> assetWrapper = new LambdaQueryWrapper<Asset>()
                .eq(Asset::getDeleted, 0);
        if ("DEPARTMENT".equals(request.getScopeType())) {
            assetWrapper.eq(Asset::getDepartment, request.getDepartment());
        } else if ("LOCATION".equals(request.getScopeType())) {
            assetWrapper.eq(Asset::getLocation, request.getLocation());
        }
        List<Asset> assets = assetMapper.selectList(assetWrapper);

        for (Asset asset : assets) {
            InventoryRecord record = new InventoryRecord();
            record.setTaskId(task.getId());
            record.setAssetId(asset.getId());
            record.setExpectedLocation(asset.getLocation());
            record.setExpectedKeeper(asset.getKeeper());
            inventoryRecordMapper.insert(record);
        }

        return task.getId();
    }

    public InventoryTaskVO detail(Long id) {
        InventoryTask task = inventoryTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "盘点任务不存在");
        }
        return toTaskVO(task);
    }

    public List<InventoryRecordVO> getRecords(Long taskId) {
        return inventoryRecordMapper.selectRecordsByTaskId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRecord(Long recordId, InventoryRecordUpdateRequest request) {
        InventoryRecord record = inventoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "盘点明细不存在");
        }
        record.setActualLocation(request.getActualLocation());
        record.setActualKeeper(request.getActualKeeper());
        record.setResult(request.getResult());
        record.setRemark(request.getRemark());
        record.setScannedAt(LocalDateTime.now());
        inventoryRecordMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "盘点任务不存在");
        }
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "任务状态不允许完成");
        }
        int unrecorded = inventoryRecordMapper.countUnrecorded(taskId);
        if (unrecorded > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "存在 " + unrecorded + " 条未录入结果的明细，无法完成");
        }
        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        inventoryTaskMapper.updateById(task);
    }

    private void validateScope(InventoryTaskCreateRequest request) {
        if ("DEPARTMENT".equals(request.getScopeType())
                && (request.getDepartment() == null || request.getDepartment().isEmpty())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "按部门盘点时部门不能为空");
        }
        if ("LOCATION".equals(request.getScopeType())
                && (request.getLocation() == null || request.getLocation().isEmpty())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "按地点盘点时地点不能为空");
        }
    }

    private InventoryTaskVO toTaskVO(InventoryTask task) {
        InventoryTaskVO vo = new InventoryTaskVO();
        vo.setId(task.getId());
        vo.setTaskCode(task.getTaskCode());
        vo.setTaskName(task.getTaskName());
        vo.setScopeType(task.getScopeType());
        vo.setDepartment(task.getDepartment());
        vo.setLocation(task.getLocation());
        vo.setStatus(task.getStatus());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());
        vo.setCreatedBy(task.getCreatedBy());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());

        LambdaQueryWrapper<InventoryRecord> totalWrapper = new LambdaQueryWrapper<InventoryRecord>()
                .eq(InventoryRecord::getTaskId, task.getId());
        Long total = inventoryRecordMapper.selectCount(totalWrapper);
        vo.setTotalRecords(total != null ? total.intValue() : 0);

        LambdaQueryWrapper<InventoryRecord> completedWrapper = new LambdaQueryWrapper<InventoryRecord>()
                .eq(InventoryRecord::getTaskId, task.getId())
                .isNotNull(InventoryRecord::getResult);
        Long completed = inventoryRecordMapper.selectCount(completedWrapper);
        vo.setCompletedRecords(completed != null ? completed.intValue() : 0);

        return vo;
    }

    private String generateTaskCode(String prefix) {
        LambdaQueryWrapper<InventoryTask> wrapper = new LambdaQueryWrapper<InventoryTask>()
                .likeRight(InventoryTask::getTaskCode, prefix)
                .orderByDesc(InventoryTask::getTaskCode)
                .last("LIMIT 1");
        InventoryTask latest = inventoryTaskMapper.selectOne(wrapper);
        int nextSequence = 1;
        if (latest != null && latest.getTaskCode() != null
                && latest.getTaskCode().length() >= prefix.length() + 4) {
            String sequencePart = latest.getTaskCode().substring(prefix.length());
            nextSequence = Integer.parseInt(sequencePart) + 1;
        }
        return prefix + String.format("%04d", nextSequence);
    }
}
