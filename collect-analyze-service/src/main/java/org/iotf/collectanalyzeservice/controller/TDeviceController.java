package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.requestFormation.collect_analyze.*;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

/**
 * <p>
 *  设备管理
 *  功能演示 仅模拟真实设备
 *  IOT<-VUE->SPRING
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Controller
@RequestMapping("/api/device")
public class TDeviceController {

    @PostMapping("/deviceSubmit")
    @Operation(summary = "注册新设备", description = "")
    public ApiResponse<?> deviceSubmit(
            @Valid @RequestBody deviceSubmitRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt
        return null;
    }

    @PostMapping("/reportModify")
    @Operation(summary = "修改设备上报", description = "")
    public ApiResponse<?> reportModify(
            @Valid @RequestBody reportModifyRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt
        return null;
    }

    @PostMapping("/reportSwitch")
    @Operation(summary = "开始/暂停 上报", description = "")
    public ApiResponse<?> reportSwitch(
            @Valid @RequestBody reportSwitchRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt
        return null;
    }

    @PostMapping("/deviceList")
    @Operation(summary = "设备列表", description = "")
    public ApiResponse<?> deviceList(
            @Valid @RequestBody deviceListRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        return null;
    }

    @PostMapping("/deviceDetail")
    @Operation(summary = "设备详情", description = "")
    public ApiResponse<?> deviceDetail(
            @Valid @RequestBody deviceDetailRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        return null;
    }

    @PostMapping("/deviceDelete")
    @Operation(summary = "删除设备", description = "")
    public ApiResponse<?> deviceDelete(
            @Valid @RequestBody deviceDeleteRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt
        return null;
    }

}
