package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iotf.collectanalyzeservice.service.ITAlarmLogService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.requestFormation.collect_analyze.alarmListRequest;
import org.iotf.requestFormation.collect_analyze.alarmLogListRequest;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 告警状态变更日志表 前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@RestController
@RequestMapping("/api/alarmLog")
@RequiredArgsConstructor
public class TAlarmLogController {

    private final ITAlarmLogService alarmLogService;

    @PostMapping("/alarmList")
    @Operation(summary = "告警日志列表", description = "")
    public ApiResponse<?> alarmLogList(
            @Valid @RequestBody alarmLogListRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        return ApiResponse.success(alarmLogService.alarmLogList(request, payload));
    }


}
