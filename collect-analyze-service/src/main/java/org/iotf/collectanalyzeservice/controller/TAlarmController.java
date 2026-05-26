package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iotf.collectanalyzeservice.service.ITAlarmService;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.TAlarm;
import org.iotf.entity.collect_analyze.TDevice;
import org.iotf.requestFormation.collect_analyze.acknowledgeRequest;
import org.iotf.requestFormation.collect_analyze.alarmListRequest;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 告警记录表 前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class TAlarmController {

    private final ITDeviceService deviceService;
    private final ITAlarmService alarmService;

    @PostMapping("/acknowledge")
    @Operation(summary = "用户确认预警", description = "")
    public ApiResponse<?> acknowledge(
            @Valid @RequestBody acknowledgeRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        if (
                !alarmService.lambdaQuery()
                        .eq(TAlarm::getAlarm_id, request.alarm_id())
                        .eq(TAlarm::getUser_id, payload.getUser_id())
                        .exists()
        ) {
            return ApiResponse.fail(404, "该告警不存在");
        }

        if (alarmService.acknowledgeAlarm(request, payload)) {
            return ApiResponse.success("确认成功");
        }

        return ApiResponse.error("确认失败");
    }

    @PostMapping("/alarmList")
    @Operation(summary = "告警列表", description = "")
    public ApiResponse<?> alarmList(
            @Valid @RequestBody alarmListRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        return ApiResponse.success(alarmService.alarmList(request, payload));
    }


}
