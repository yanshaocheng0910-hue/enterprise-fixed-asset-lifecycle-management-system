package com.example.asset.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.asset.finance.entity.FinanceSyncRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FinanceSyncRecordMapper extends BaseMapper<FinanceSyncRecord> {

    @Select("SELECT COUNT(*) FROM finance_sync_record WHERE sync_month = #{month}")
    Integer countByMonth(@Param("month") String month);

    @Select("SELECT * FROM finance_sync_record WHERE sync_month = #{month} ORDER BY created_at DESC LIMIT 1")
    FinanceSyncRecord selectByMonth(@Param("month") String month);
}