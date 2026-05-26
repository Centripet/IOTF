package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.TDevice;
import org.iotf.requestFormation.collect_analyze.*;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class TDeviceController {

    private final ITDeviceService deviceService;

    @PostMapping("/deviceSubmit")
    @Operation(summary = "注册新设备", description = "")
    public ApiResponse<?> deviceSubmit(
            @Valid @RequestBody deviceSubmitRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt

        return ApiResponse.success(deviceService.deviceSubmit(payload, request));

    }

    @PostMapping("/reportModify")
    @Operation(summary = "修改设备上报", description = "")
    public ApiResponse<?> reportModify(
            @Valid @RequestBody reportModifyRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt
        if (
                !deviceService.lambdaQuery()
                        .eq(TDevice::getUser_id, payload.getUser_id())
                        .eq(TDevice::getDevice_id, request.device_id())
                        .eq(TDevice::getDeleted, 0)
                        .exists()
        ) {
            return ApiResponse.fail(404, "该设备不存在");
        }

        if (deviceService.reportModify(payload, request)) {
            return ApiResponse.success("修改成功");
        }

        return ApiResponse.error("修改失败");

    }

//    @PostMapping("/reportSwitch")
//    @Operation(summary = "开始/暂停 上报", description = "")
//    public ApiResponse<?> reportSwitch(
//            @Valid @RequestBody reportSwitchRequest request,
//            HttpServletResponse response,
//            @AuthenticationPrincipal JwtPayload payload
//    ) {
////submit-mqtt
//        if (
//                !deviceService.lambdaQuery()
//                        .eq(TDevice::getUser_id, payload.getUser_id())
//                        .eq(TDevice::getDevice_id, request.device_id())
//                        .eq(TDevice::getDeleted, 0)
//                        .exists()
//        ) {
//            return ApiResponse.fail(404, "该设备不存在");
//        }
//
//        if (deviceService.reportSwitch(payload, request)) {
//            return ApiResponse.success("修改成功");
//        }
//
//        return ApiResponse.error("修改失败");
//
//    }

    @PostMapping("/deviceList")
    @Operation(summary = "设备列表搜索", description = "")
    public ApiResponse<?> deviceList(
            @Valid @RequestBody deviceListRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        return ApiResponse.success(deviceService.deviceList(payload, request));

    }

    @PostMapping("/deviceDetail")
    @Operation(summary = "设备详情", description = "")
    public ApiResponse<?> deviceDetail(
            @Valid @RequestBody deviceDetailRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        if (
                !deviceService.lambdaQuery()
                        .eq(TDevice::getUser_id, payload.getUser_id())
                        .eq(TDevice::getDevice_id, request.device_id())
                        .eq(TDevice::getDeleted, 0)
                        .exists()
        ) {
            return ApiResponse.fail(404, "该设备不存在");
        }

        return ApiResponse.success(deviceService.deviceDetail(payload, request));

    }

    @PostMapping("/deviceDelete")
    @Operation(summary = "删除设备", description = "")
    public ApiResponse<?> deviceDelete(
            @Valid @RequestBody deviceDeleteRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
//submit-mqtt
        if (
                !deviceService.lambdaQuery()
                        .eq(TDevice::getUser_id, payload.getUser_id())
                        .eq(TDevice::getDevice_id, request.device_id())
                        .eq(TDevice::getDeleted, 0)
                        .exists()
        ) {
            return ApiResponse.fail(404, "该设备不存在");
        }

        if (deviceService.deviceDelete(payload, request)) {
            return ApiResponse.success("删除成功");
        }

        return ApiResponse.error("删除失败");

    }

}
