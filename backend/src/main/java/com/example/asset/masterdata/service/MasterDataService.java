package com.example.asset.masterdata.service;

import com.example.asset.masterdata.mapper.MasterDataMapper;
import com.example.asset.masterdata.vo.MasterDataVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterDataService {

    private final MasterDataMapper masterDataMapper;

    public MasterDataService(MasterDataMapper masterDataMapper) {
        this.masterDataMapper = masterDataMapper;
    }

    public List<MasterDataVO> getDepartments() {
        return masterDataMapper.selectDepartments();
    }

    public List<MasterDataVO> getLocations() {
        return masterDataMapper.selectLocations();
    }

    public List<MasterDataVO> getKeepers() {
        return masterDataMapper.selectKeepers();
    }
}
