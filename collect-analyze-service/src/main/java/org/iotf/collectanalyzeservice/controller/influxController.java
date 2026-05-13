package org.iotf.collectanalyzeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.requestFormation.collect_analyze.reportQueryRequest;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/influx")
public class influxController {

    @PostMapping("/reportQuery")
    @Operation(summary = "报表查询", description = "")
    public ApiResponse<?> query(
            @Valid @RequestBody reportQueryRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        return null;
    }

}
