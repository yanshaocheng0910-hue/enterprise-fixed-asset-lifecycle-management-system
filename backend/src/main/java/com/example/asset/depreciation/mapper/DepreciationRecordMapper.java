package com.example.asset.depreciation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.depreciation.entity.DepreciationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface DepreciationRecordMapper extends BaseMapper<DepreciationRecord> {

    @Select("SELECT COALESCE(SUM(monthly_depreciation), 0) FROM depreciation_record WHERE depreciation_month = #{month}")
    BigDecimal sumMonthlyDepreciationByMonth(@Param("month") String month);

    @Select("SELECT COUNT(*) FROM depreciation_record WHERE depreciation_month = #{month}")
    Integer countByMonth(@Param("month") String month);
}