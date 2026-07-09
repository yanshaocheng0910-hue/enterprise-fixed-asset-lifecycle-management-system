package com.example.asset.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.asset.common.PageResult;
import com.example.asset.common.ResultCode;
import com.example.asset.common.BusinessException;
import com.example.asset.context.UserContext;
import com.example.asset.asset.entity.Asset;
import com.example.asset.asset.mapper.AssetMapper;
import com.example.asset.asset.mapper.AssetCategoryMapper;
import com.example.asset.inventory.dto.CreateInventoryTaskRequest;
import com.example.asset.inventory.dto.UpdateInventoryRecordRequest;
import com.example.asset.inventory.entity.InventoryRecord;
import com.example.asset.inventory.entity.InventoryTask;
import com.example.asset.inventory.mapper.InventoryRecordMapper;
import com.example.asset.inventory.mapper.InventoryTaskMapper;
import com.example.asset.inventory.vo.InventoryRecordVO;
import com.example.asset.inventory.vo.InventoryReportVO;
import com.example.asset.inventory.vo.InventoryTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryTaskMapper inventoryTaskMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;

    public InventoryService(InventoryTaskMapper inventoryTaskMapper,
                            InventoryRecordMapper inventoryRecordMapper,
                            AssetMapper assetMapper,
                            AssetCategoryMapper assetCategoryMapper) {
        this.inventoryTaskMapper = inventoryTaskMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.assetMapper = assetMapper;
        this.assetCategoryMapper = assetCategoryMapper;
    }

    public PageResult<InventoryTaskVO> page(Long pageNum, Long pageSize, String status, String keyword) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;

        LambdaQueryWrapper<InventoryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InventoryTask::getCreatedAt);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(InventoryTask::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(InventoryTask::getTaskName, keyword)
                    .or().like(InventoryTask::getTaskCode, keyword);
        }

        Page<InventoryTask> page = new Page<>(current, size);
        Page<InventoryTask> result = inventoryTaskMapper.selectPage(page, wrapper);

        List<InventoryTaskVO> voList = result.getRecords().stream().map(this::toTaskVO).collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public Long createTask(CreateInventoryTaskRequest req) {
        String code = "INV" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        InventoryTask task = new InventoryTask();
        task.setTaskCode(code);
        task.setTaskName(req.getTaskName());
        task.setScopeType(req.getScopeType());
        task.setDepartment(req.getDepartment());
        task.setLocation(req.getLocation());
        task.setStatus("DRAFT");
        task.setCreatedBy(UserContext.getUserId());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        inventoryTaskMapper.insert(task);
        return task.getId();
    }

    @Transactional
    public void startTask(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ResultCode.NOT_FOUND, "Inventory task not found");
        if (!"DRAFT".equals(task.getStatus())) throw new BusinessException(ResultCode.BAD_REQUEST, "Only DRAFT tasks can be started");

        List<Asset> assets = queryAssetsByScope(task.getScopeType(), task.getDepartment(), task.getLocation());
        if (assets.isEmpty()) throw new BusinessException(ResultCode.BAD_REQUEST, "No assets in scope");

        for (Asset asset : assets) {
            InventoryRecord record = new InventoryRecord();
            record.setTaskId(taskId);
            record.setAssetId(asset.getId());
            record.setExpectedLocation(asset.getLocation());
            record.setExpectedKeeper(asset.getKeeper());
            record.setResult("PENDING");
            inventoryRecordMapper.insert(record);
        }

        task.setStatus("IN_PROGRESS");
        task.setStartTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        inventoryTaskMapper.updateById(task);
    }

    @Transactional
    public void scanRecord(UpdateInventoryRecordRequest req) {
        InventoryRecord record = inventoryRecordMapper.selectById(req.getRecordId());
        if (record == null) throw new BusinessException(ResultCode.NOT_FOUND, "盘点记录不存在");

        InventoryTask task = inventoryTaskMapper.selectById(record.getTaskId());
        if (task == null || !"IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "盘点任务不在执行中状态");
        }

        if (req.getActualLocation() != null) record.setActualLocation(req.getActualLocation());
        if (req.getActualKeeper() != null) record.setActualKeeper(req.getActualKeeper());

        if (req.getResult() != null) {
            record.setResult(req.getResult());
        } else {
            String expLoc = record.getExpectedLocation();
            String expKeeper = record.getExpectedKeeper();
            String actLoc = req.getActualLocation();
            String actKeeper = req.getActualKeeper();

            boolean locationChecked = actLoc != null && !actLoc.isEmpty();
            boolean keeperChecked = actKeeper != null && !actKeeper.isEmpty();
            boolean locationOk = !locationChecked || actLoc.trim().equals(expLoc == null ? "" : expLoc.trim());
            boolean keeperOk = !keeperChecked || actKeeper.trim().equals(expKeeper == null ? "" : expKeeper.trim());

            if (locationOk && keeperOk) {
                record.setResult("NORMAL");
            } else if (!locationOk && !keeperOk) {
                record.setResult("MISMATCH");
            } else if (!locationOk) {
                record.setResult("LOCATION_MISMATCH");
            } else {
                record.setResult("KEEPER_MISMATCH");
            }
        }

        record.setScannedAt(LocalDateTime.now());
        record.setRemark(req.getRemark());
        inventoryRecordMapper.updateById(record);
    }

    @Transactional
    public void completeTask(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ResultCode.NOT_FOUND, "Inventory task not found");
        if (!"IN_PROGRESS".equals(task.getStatus())) throw new BusinessException(ResultCode.BAD_REQUEST, "Only IN_PROGRESS tasks can be completed");

        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getTaskId, taskId)
                .eq(InventoryRecord::getResult, "PENDING");
        List<InventoryRecord> pendingRecords = inventoryRecordMapper.selectList(wrapper);
        for (InventoryRecord r : pendingRecords) {
            r.setResult("MISSING");
            r.setRemark("Not scanned, marked as missing");
            inventoryRecordMapper.updateById(r);
        }

        task.setStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        inventoryTaskMapper.updateById(task);
    }

    public InventoryTaskVO getTaskDetail(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ResultCode.NOT_FOUND, "Task not found");
        return toTaskVO(task);
    }

    public List<InventoryRecordVO> getRecords(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ResultCode.NOT_FOUND, "Task not found");

        List<InventoryRecord> records = inventoryRecordMapper.findByTaskId(taskId);
        List<InventoryRecordVO> voList = new ArrayList<>();
        for (InventoryRecord r : records) {
            InventoryRecordVO vo = new InventoryRecordVO();
            vo.setId(r.getId());
            vo.setTaskId(r.getTaskId());
            vo.setAssetId(r.getAssetId());
            vo.setExpectedLocation(r.getExpectedLocation());
            vo.setActualLocation(r.getActualLocation());
            vo.setExpectedKeeper(r.getExpectedKeeper());
            vo.setActualKeeper(r.getActualKeeper());
            vo.setResult(r.getResult());
            vo.setScannedAt(r.getScannedAt());
            vo.setRemark(r.getRemark());

            if (r.getAssetId() != null) {
                Asset asset = assetMapper.selectById(r.getAssetId());
                if (asset != null) {
                    vo.setAssetCode(asset.getAssetCode());
                    vo.setAssetName(asset.getAssetName());
                    if (asset.getCategoryId() != null) {
                        var cat = assetCategoryMapper.selectById(asset.getCategoryId());
                        if (cat != null) vo.setCategoryName(cat.getCategoryName());
                    }
                }
            }
            voList.add(vo);
        }
        return voList;
    }

    public InventoryReportVO getReport(Long taskId) {
        InventoryTaskVO taskVO = getTaskDetail(taskId);
        List<InventoryRecordVO> records = getRecords(taskId);

        InventoryReportVO report = new InventoryReportVO();
        report.setTask(taskVO);
        report.setTotalCount(records.size());
        report.setScannedCount(records.stream().filter(r -> !"PENDING".equals(r.getResult())).count());
        report.setNormalCount(records.stream().filter(r -> "NORMAL".equals(r.getResult())).count());
        report.setLocationMismatchCount(records.stream().filter(r -> "LOCATION_MISMATCH".equals(r.getResult()) || "MISMATCH".equals(r.getResult())).count());
        report.setKeeperMismatchCount(records.stream().filter(r -> "KEEPER_MISMATCH".equals(r.getResult()) || "MISMATCH".equals(r.getResult())).count());
        report.setMissingCount(records.stream().filter(r -> "MISSING".equals(r.getResult())).count());
        report.setDetails(records);
        return report;
    }

    public void deleteTask(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ResultCode.NOT_FOUND, "Task not found");
        if (!"DRAFT".equals(task.getStatus())) throw new BusinessException(ResultCode.BAD_REQUEST, "Only DRAFT tasks can be deleted");

        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getTaskId, taskId);
        inventoryRecordMapper.delete(wrapper);

        inventoryTaskMapper.deleteById(taskId);
    }


    @Transactional
    public void batchScanPending(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(ResultCode.NOT_FOUND, "盘点任务不存在");
        if (!"IN_PROGRESS".equals(task.getStatus())) throw new BusinessException(ResultCode.BAD_REQUEST, "仅执行中的任务可以批量扫描");

        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getTaskId, taskId)
                .eq(InventoryRecord::getResult, "PENDING");
        List<InventoryRecord> pending = inventoryRecordMapper.selectList(wrapper);

        for (InventoryRecord r : pending) {
            r.setActualLocation(r.getExpectedLocation());
            r.setActualKeeper(r.getExpectedKeeper());
            r.setResult("NORMAL");
            r.setScannedAt(LocalDateTime.now());
            r.setRemark("批量扫描 - 自动确认");
            inventoryRecordMapper.updateById(r);
        }
    }

    public List<String> getDistinctDepartments() {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Asset::getDepartment)
                .isNotNull(Asset::getDepartment)
                .ne(Asset::getDepartment, "")
                .eq(Asset::getDeleted, 0)
                .groupBy(Asset::getDepartment);
        List<Asset> assets = assetMapper.selectList(wrapper);
        List<String> result = new ArrayList<>();
        for (Asset a : assets) {
            if (a.getDepartment() != null && !a.getDepartment().isEmpty()) {
                result.add(a.getDepartment());
            }
        }
        return result;
    }

    public List<String> getDistinctLocations() {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Asset::getLocation)
                .isNotNull(Asset::getLocation)
                .ne(Asset::getLocation, "")
                .eq(Asset::getDeleted, 0)
                .groupBy(Asset::getLocation);
        List<Asset> assets = assetMapper.selectList(wrapper);
        List<String> result = new ArrayList<>();
        for (Asset a : assets) {
            if (a.getLocation() != null && !a.getLocation().isEmpty()) {
                result.add(a.getLocation());
            }
        }
        return result;
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
        vo.setCreatedAt(task.getCreatedAt());

        LambdaQueryWrapper<InventoryRecord> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(InventoryRecord::getTaskId, task.getId());
        long total = inventoryRecordMapper.selectCount(countWrapper);
        vo.setTotalCount(total);

        long scanned = inventoryRecordMapper.countNormal(task.getId())
                + inventoryRecordMapper.countLocationMismatch(task.getId())
                + inventoryRecordMapper.countKeeperMismatch(task.getId())
                + inventoryRecordMapper.countMissing(task.getId());
        vo.setScannedCount(scanned);
        vo.setNormalCount(inventoryRecordMapper.countNormal(task.getId()));
        vo.setAbnormalCount(scanned - inventoryRecordMapper.countNormal(task.getId()));

        return vo;
    }

    private List<Asset> queryAssetsByScope(String scopeType, String department, String location) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getDeleted, 0);

        if (scopeType != null) {
            switch (scopeType) {
                case "DEPARTMENT":
                    if (department != null && !department.isEmpty()) {
                        wrapper.eq(Asset::getDepartment, department);
                    }
                    break;
                case "LOCATION":
                    if (location != null && !location.isEmpty()) {
                        wrapper.eq(Asset::getLocation, location);
                    }
                    break;
                case "ALL":
                    break;
            }
        }
        return assetMapper.selectList(wrapper);
    }
}