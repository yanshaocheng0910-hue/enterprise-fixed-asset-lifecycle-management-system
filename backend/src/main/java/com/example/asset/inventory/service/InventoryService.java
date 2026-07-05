package com.example.asset.inventory.service;

import com.example.asset.common.PageResult;
import com.example.asset.inventory.entity.InventoryTask;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class InventoryService {

    public PageResult<InventoryTask> page(Long pageNum, Long pageSize) {
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        return new PageResult<>(Collections.emptyList(), 0L, current, size);
    }
}
