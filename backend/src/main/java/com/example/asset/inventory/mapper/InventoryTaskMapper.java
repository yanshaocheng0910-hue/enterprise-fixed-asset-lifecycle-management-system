package com.example.asset.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.inventory.entity.InventoryTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryTaskMapper extends BaseMapper<InventoryTask> {
}