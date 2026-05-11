package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.controller.requestFormation.fluxQueryRequest;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.iotf.collectanalyzeservice.service.InfluxDBService;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "登录验证", description = "登录注册鉴权等相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class test {

    private final InfluxDBService influxDBService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/test")
    String testMethod(@Valid @RequestParam String str) {
        return str;
    }


    @PostMapping("/fluxAdd")
    public ApiResponse<?> fluxAdd(
            @Valid @RequestBody EnergyDataPoint point,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        influxDBService.writeData(point);
        return ApiResponse.success(point);
    }

    @PostMapping("/fluxQuery")
    public ApiResponse<?> fluxQuery(
            @Valid @RequestBody fluxQueryRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {

        return ApiResponse.success(
                influxDBService.queryDeviceData(request.deviceId(), request.start(), request.end())
        );
    }

}
