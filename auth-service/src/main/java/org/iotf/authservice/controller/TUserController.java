package org.iotf.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.requestFormation.test.testRequest;
import org.iotf.wrapper.operationLogHandle.OperationLog;
import org.iotf.wrapper.permissionHandle.PermissionType;
import org.iotf.wrapper.permissionHandle.RequirePermission;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Slf4j
@Tag(name = "", description = "")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class TUserController {

    @PostMapping("/test")
    @Operation(summary = "test", description = "test")
    @RequirePermission(permission = PermissionType.ADMIN)
    @OperationLog(operation = "test", targetType = "user", targetIdField = "user_id")
    public ApiResponse<?> test(
            @Valid @RequestBody testRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        return ApiResponse.success(request);
    }

}
