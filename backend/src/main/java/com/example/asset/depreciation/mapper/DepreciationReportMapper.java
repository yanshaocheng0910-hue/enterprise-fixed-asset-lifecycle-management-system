package com.example.asset.depreciation.mapper;

import com.example.asset.depreciation.vo.CategoryStatVO;
import com.example.asset.depreciation.vo.DepartmentStatVO;
import com.example.asset.depreciation.vo.DepreciationTrendVO;
import com.example.asset.depreciation.vo.MonthlyDepreciationItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepreciationReportMapper {

    List<MonthlyDepreciationItemVO> selectMonthlyDepreciationItems(@Param("month") String month);

    List<DepartmentStatVO> selectDepartmentStats();

    List<CategoryStatVO> selectCategoryStats();

    List<DepreciationTrendVO> selectDepreciationTrend(@Param("months") List<String> months);
}
