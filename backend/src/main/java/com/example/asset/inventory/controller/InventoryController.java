package com.example.asset.inventory.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.inventory.dto.CreateInventoryTaskRequest;
import com.example.asset.inventory.dto.UpdateInventoryRecordRequest;
import com.example.asset.inventory.service.InventoryService;
import com.example.asset.inventory.vo.InventoryRecordVO;
import com.example.asset.inventory.vo.InventoryReportVO;
import com.example.asset.inventory.vo.InventoryTaskVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/tasks/page")
    public Result<PageResult<InventoryTaskVO>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(inventoryService.page(pageNum, pageSize, status, keyword));
    }

    @PostMapping("/tasks")
    public Result<Long> createTask(@Valid @RequestBody CreateInventoryTaskRequest req) {
        return Result.success(inventoryService.createTask(req));
    }

    @GetMapping("/tasks/{id}")
    public Result<InventoryTaskVO> getTask(@PathVariable Long id) {
        return Result.success(inventoryService.getTaskDetail(id));
    }

    @PostMapping("/tasks/{id}/start")
    public Result<Void> startTask(@PathVariable Long id) {
        inventoryService.startTask(id);
        return Result.success();
    }

    @PostMapping("/tasks/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id) {
        inventoryService.completeTask(id);
        return Result.success();
    }

    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        inventoryService.deleteTask(id);
        return Result.success();
    }

    @GetMapping("/tasks/{id}/records")
    public Result<List<InventoryRecordVO>> getRecords(@PathVariable Long id) {
        return Result.success(inventoryService.getRecords(id));
    }

    @PutMapping("/records")
    public Result<Void> scanRecord(@Valid @RequestBody UpdateInventoryRecordRequest req) {
        inventoryService.scanRecord(req);
        return Result.success();
    }

    @PostMapping("/tasks/{id}/batch-scan")
    public Result<Void> batchScan(@PathVariable Long id) {
        inventoryService.batchScanPending(id);
        return Result.success();
    }

    @GetMapping("/departments")
    public Result<List<String>> departments() {
        return Result.success(inventoryService.getDistinctDepartments());
    }

    @GetMapping("/locations")
    public Result<List<String>> locations() {
        return Result.success(inventoryService.getDistinctLocations());
    }

    @GetMapping("/tasks/{id}/report")
    public Result<InventoryReportVO> getReport(@PathVariable Long id) {
        return Result.success(inventoryService.getReport(id));
    }
}