package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iotf.collectanalyzeservice.service.EnergyDataQueryService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.EnergyAggDTO;
import org.iotf.requestFormation.collect_analyze.energyAggByDeviceRequest;
import org.iotf.requestFormation.collect_analyze.energyAggByUserRequest;
import org.iotf.requestFormation.collect_analyze.energyAggListByDeviceRequest;
import org.iotf.requestFormation.energyAggListByUserRequest;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/influx")
@RequiredArgsConstructor
public class influxController {

    private final EnergyDataQueryService energyDataQueryService;

    @PostMapping("/energyAggByDevice")
    @Operation(summary = "能耗聚合-设备", description = "查询指定设备最近一次能耗聚合数据")
    public ApiResponse<?> energyAggByDevice(
            @Valid @RequestBody energyAggByDeviceRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        EnergyAggDTO result = energyDataQueryService.queryLatestDeviceAgg(
                payload.getUser_id(), request.device_id(), request.period());
        if (result == null) {
            return ApiResponse.fail(404, "未查询到该设备的能耗聚合数据");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/energyAggByUser")
    @Operation(summary = "能耗聚合-用户", description = "查询当前用户最近一次能耗聚合数据")
    public ApiResponse<?> energyAggByUser(
            @Valid @RequestBody energyAggByUserRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        EnergyAggDTO result = energyDataQueryService.queryLatestUserAgg(
                payload.getUser_id(), request.period());
        if (result == null) {
            return ApiResponse.fail(404, "未查询到该用户的能耗聚合数据");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/energyAggListByDevice")
    @Operation(summary = "能耗聚合列表-设备", description = "分页查询指定设备的能耗聚合历史数据，按时间正序排列")
    public ApiResponse<?> energyAggListByDevice(
            @Valid @RequestBody energyAggListByDeviceRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        Long userId = payload.getUser_id();
        List<EnergyAggDTO> list = energyDataQueryService.queryDeviceAggPage(
                userId, request.device_id(), request.period(), request.page(), request.size());
        Long total = energyDataQueryService.queryDeviceAggCount(
                userId, request.device_id(), request.period());

        return ApiResponse.success(Map.of(
                "records", list,
                "page", request.page(),
                "size", request.size(),
                "total", total
        ));
    }

    @PostMapping("/energyAggListByUser")
    @Operation(summary = "能耗聚合列表-用户", description = "分页查询当前用户的能耗聚合历史数据，按时间正序排列")
    public ApiResponse<?> energyAggListByUser(
            @Valid @RequestBody energyAggListByUserRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        Long userId = payload.getUser_id();
        List<EnergyAggDTO> list = energyDataQueryService.queryUserAggPage(
                userId, request.period(), request.page(), request.size());
        Long total = energyDataQueryService.queryUserAggCount(
                userId, request.period());

        return ApiResponse.success(Map.of(
                "records", list,
                "page", request.page(),
                "size", request.size(),
                "total", total
        ));
    }

}
