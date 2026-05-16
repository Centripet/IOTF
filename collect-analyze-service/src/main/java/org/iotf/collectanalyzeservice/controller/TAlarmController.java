package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
//import org.iotf.collectanalyzeservice.service.AlarmService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.requestFormation.collect_analyze.acknowledgeRequest;
import org.iotf.requestFormation.collect_analyze.reportQueryRequest;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 告警记录表 前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Controller
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class TAlarmController {

//    private final AlarmService alarmService;
//
//    @PostMapping("/acknowledge")
//    @Operation(summary = "用户确认预警", description = "")
//    public ApiResponse<?> acknowledge(
//            @Valid @RequestBody acknowledgeRequest request,
//            HttpServletResponse response,
//            @AuthenticationPrincipal JwtPayload payload
//    ) {
//
//        alarmService.acknowledge(request.alarm_id(), payload.getUser_id());
//
//        return ApiResponse.success("OK");
//    }

}
