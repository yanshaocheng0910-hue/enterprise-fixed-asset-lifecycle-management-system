package com.example.asset.warning.mapper;

import com.example.asset.warning.vo.WarningItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WarningMapper {

    List<WarningItemVO> selectLowValueAssets();

    List<WarningItemVO> selectNearEndAssets();

    List<WarningItemVO> selectIdleLongTimeAssets(@Param("thresholdDate") LocalDateTime thresholdDate);

    List<WarningItemVO> selectRepairOverdueAssets(@Param("thresholdDate") LocalDate thresholdDate);

    List<WarningItemVO> selectInventoryAbnormalRecords();

    List<WarningItemVO> selectFinanceSyncAbnormalRecords();

    int countCurrentMonthSuccessSync(@Param("currentMonth") String currentMonth);
}
