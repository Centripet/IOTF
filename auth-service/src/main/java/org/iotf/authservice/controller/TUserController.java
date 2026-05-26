package org.iotf.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.authservice.service.ITUserService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.auth.dao.TUser;
import org.iotf.requestFormation.auth.resetPasswordRequest;
import org.iotf.requestFormation.auth.userSelfModifyRequest;
import org.iotf.requestFormation.test.testRequest;
import org.iotf.wrapper.operationLogHandle.OperationLog;
import org.iotf.wrapper.permissionHandle.PermissionType;
import org.iotf.wrapper.permissionHandle.RequirePermission;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Slf4j
@Tag(name = "用户相关接口", description = "登录后")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class TUserController {

    private final ITUserService userService;
    private final RedisTemplate<String, String> redisTemplate;

//    @PostMapping("/test")
//    @Operation(summary = "test", description = "test")
//    @RequirePermission(permission = PermissionType.ADMIN)
//    @OperationLog(operation = "test", targetType = "user", targetIdField = "user_id")
//    public ApiResponse<?> test(
//            @Valid @RequestBody testRequest request,
//            HttpServletResponse response,
//            @AuthenticationPrincipal JwtPayload payload
//    ) {
//
//        return ApiResponse.success(request);
//    }

    @PostMapping("/resetPassword")
    @Operation(summary = "登录后重置密码", description = "")
    public ApiResponse<?> resetPassword(
            @Valid @RequestBody resetPasswordRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {

        Optional<TUser> oUser = userService.lambdaQuery()
                .eq(TUser::getUser_id, payload.getUser_id())
                .oneOpt();

        String phone;
        if (oUser.isPresent()) {
            TUser user = oUser.get();
            phone = user.getPhone();

            if (!Objects.equals(request.passwordHash(), request.passwordHashRe())) {
                return ApiResponse.fail(400,"密码不一致");
            }

            String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$";
            if (!request.passwordHash().matches(regex)) {
                return ApiResponse.fail(400,"密码必须包含大小写字母");
            }

            if (!userService.verifyPassword(user.getUser_id(), request.passwordHashOld())) {
                return ApiResponse.fail(400, "原密码不正确");
            }

            if (!userService.resetPassword(user.getUser_id(), request.passwordHash())) {
                return ApiResponse.fail(500, "重置失败");
            }

            redisTemplate.delete("phoneCode:" + phone);

            return ApiResponse.success("重置密码成功");
        } else {
            return ApiResponse.fail(401, "重置失败:用户不存在");
        }
    }

    @GetMapping("/userSelfDetail")
    @Operation(summary = "用户信息", description = "")
    public ApiResponse<?> userSelfDetail(
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        if (
                !userService.lambdaQuery()
                        .eq(TUser::getUser_id, payload.getUser_id())
                        .exists()
        ) {
            return ApiResponse.fail(404, "该用户不存在");
        }

        return ApiResponse.success(userService.userSelfDetail(payload.getUser_id()));
    }

    @PostMapping("/userSelfModify")
    @Operation(summary = "用户信息修改", description = "")
    public ApiResponse<?> userSelfModify(
            @Valid @RequestBody userSelfModifyRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal JwtPayload payload
    ) {
        if (
                !userService.lambdaQuery()
                        .eq(TUser::getUser_id, payload.getUser_id())
                        .exists()
        ) {
            return ApiResponse.fail(404, "该用户不存在");
        }

        if (!userService.userSelfModify(payload.getUser_id(), request)) {
            return ApiResponse.fail(500, "修改失败");
        }

        return ApiResponse.success("修改成功");
    }

}
