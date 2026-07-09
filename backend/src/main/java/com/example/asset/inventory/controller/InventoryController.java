package com.example.asset.inventory.controller;

import com.example.asset.common.PageResult;
import com.example.asset.common.Result;
import com.example.asset.inventory.dto.InventoryRecordUpdateRequest;
import com.example.asset.inventory.dto.InventoryTaskCreateRequest;
import com.example.asset.inventory.dto.InventoryTaskQueryRequest;
import com.example.asset.inventory.service.InventoryService;
import com.example.asset.inventory.vo.InventoryRecordVO;
import com.example.asset.inventory.vo.InventoryTaskVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/tasks/page")
    public Result<PageResult<InventoryTaskVO>> page(@Valid InventoryTaskQueryRequest query) {
        return Result.success(inventoryService.page(query));
    }

    @PostMapping("/tasks")
    public Result<Long> create(@Valid @RequestBody InventoryTaskCreateRequest request) {
        return Result.success(inventoryService.create(request));
    }

    @GetMapping("/tasks/{id}")
    public Result<InventoryTaskVO> detail(@PathVariable Long id) {
        return Result.success(inventoryService.detail(id));
    }

    @GetMapping("/tasks/{id}/records")
    public Result<List<InventoryRecordVO>> records(@PathVariable Long id) {
        return Result.success(inventoryService.getRecords(id));
    }

    @PutMapping("/records/{recordId}")
    public Result<Void> updateRecord(@PathVariable Long recordId,
                                     @Valid @RequestBody InventoryRecordUpdateRequest request) {
        inventoryService.updateRecord(recordId, request);
        return Result.success();
    }

    @PutMapping("/tasks/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        inventoryService.complete(id);
        return Result.success();
    }
}
