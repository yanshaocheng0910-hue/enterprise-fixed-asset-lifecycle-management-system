package com.example.asset.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.inventory.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {

    @Select("SELECT * FROM inventory_record WHERE task_id = #{taskId} ORDER BY id ASC")
    List<InventoryRecord> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM inventory_record WHERE task_id = #{taskId} AND result = 'NORMAL'")
    long countNormal(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM inventory_record WHERE task_id = #{taskId} AND result = 'LOCATION_MISMATCH'")
    long countLocationMismatch(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM inventory_record WHERE task_id = #{taskId} AND result = 'KEEPER_MISMATCH'")
    long countKeeperMismatch(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM inventory_record WHERE task_id = #{taskId} AND result = 'MISSING'")
    long countMissing(@Param("taskId") Long taskId);
}