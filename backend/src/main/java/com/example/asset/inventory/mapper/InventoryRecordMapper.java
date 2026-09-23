package com.example.asset.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.inventory.entity.InventoryRecord;
import com.example.asset.inventory.vo.InventoryRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {

    @Select("SELECT r.id, r.task_id AS taskId, r.asset_id AS assetId, " +
            "a.asset_code AS assetCode, a.asset_name AS assetName, " +
            "c.category_name AS categoryName, " +
            "r.expected_location AS expectedLocation, r.actual_location AS actualLocation, " +
            "r.expected_keeper AS expectedKeeper, r.actual_keeper AS actualKeeper, " +
            "r.result, r.scanned_at AS scannedAt, r.remark " +
            "FROM inventory_record r " +
            "LEFT JOIN asset a ON a.id = r.asset_id " +
            "LEFT JOIN asset_category c ON c.id = a.category_id " +
            "WHERE r.task_id = #{taskId} " +
            "ORDER BY r.id")
    List<InventoryRecordVO> selectRecordsByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM inventory_record WHERE task_id = #{taskId} AND result IS NULL")
    int countUnrecorded(@Param("taskId") Long taskId);
}
