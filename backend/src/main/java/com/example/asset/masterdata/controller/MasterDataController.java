package com.example.asset.masterdata.controller;

import com.example.asset.common.Result;
import com.example.asset.masterdata.service.MasterDataService;
import com.example.asset.masterdata.vo.MasterDataVO;
import com.example.asset.permission.annotation.RequirePermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/departments")
    @RequirePermission("asset:view")
    public Result<List<MasterDataVO>> departments() {
        return Result.success(masterDataService.getDepartments());
    }

    @GetMapping("/locations")
    @RequirePermission("asset:view")
    public Result<List<MasterDataVO>> locations() {
        return Result.success(masterDataService.getLocations());
    }

    @GetMapping("/keepers")
    @RequirePermission("asset:view")
    public Result<List<MasterDataVO>> keepers() {
        return Result.success(masterDataService.getKeepers());
    }
}
