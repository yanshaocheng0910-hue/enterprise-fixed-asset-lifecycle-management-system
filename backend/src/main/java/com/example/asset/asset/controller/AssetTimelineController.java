package com.example.asset.asset.controller;

import com.example.asset.asset.dto.AssetTimelineQueryRequest;
import com.example.asset.asset.service.AssetTimelineService;
import com.example.asset.asset.vo.AssetTimelineEventVO;
import com.example.asset.common.Result;
import com.example.asset.permission.annotation.RequirePermission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetTimelineController {

    private final AssetTimelineService assetTimelineService;

    @GetMapping("/{assetId}/timeline")
    @RequirePermission("asset:view")
    public Result<List<AssetTimelineEventVO>> getTimeline(
            @PathVariable Long assetId,
            AssetTimelineQueryRequest query) {
        return Result.success(assetTimelineService.getTimeline(assetId, query));
    }
}
