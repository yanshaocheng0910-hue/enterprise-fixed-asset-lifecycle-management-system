package com.example.asset.masterdata.mapper;

import com.example.asset.masterdata.vo.MasterDataVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MasterDataMapper {

    List<MasterDataVO> selectDepartments();

    List<MasterDataVO> selectLocations();

    List<MasterDataVO> selectKeepers();
}
