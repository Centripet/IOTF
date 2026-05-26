package org.iotf.authservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.authservice.service.AliSmsService;
import org.iotf.authservice.service.ITRolePermissionService;
import org.iotf.authservice.service.ITUserService;
import org.iotf.service.common.JwtService;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.auth.dao.TRolePermission;
import org.iotf.entity.auth.dao.TUser;
import org.iotf.requestFormation.auth.*;
import org.iotf.util.SnowflakeIdGenerator;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.iotf.util.CommonGenerator.generateNumericCaptcha;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Centripet
 * @since 2025-10-30
 */
@Slf4j
@Tag(name = "登录验证", description = "登录注册鉴权等相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ITUserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    private final AliSmsService smsService;
    private final ITRolePermissionService rolePermissionService;
    private final SnowflakeIdGenerator idGenerator;

    @PostMapping("/login")
    @Operation(summary = "登录", description = "账号密码登录")
    public ApiResponse<?> login(
            @Valid @RequestBody loginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) throws JsonProcessingException {
        TUser user = userService.loginVerification(request);

        if (user != null) {

            if (user.getUser_status() != 1) {
                return ApiResponse.fail(400, "登录失败:账户异常或已注销");
            }

            Optional<TRolePermission> oRp = rolePermissionService.getOptById(user.getRole_id());
            if (oRp.isEmpty()) {
                return ApiResponse.fail(500, "登录失败:用户权限异常");
            }

            TRolePermission rolePermission = oRp.get();

            Long jti = idGenerator.nextId();
            String clientType = userService.resolveClientType(httpRequest);
            String deviceId = userService.resolveDeviceId(httpRequest);
            JwtPayload payload = JwtPayload.builder()
                    .user_id(user.getUser_id())
                    .role(rolePermission.getRole())
                    .permission(rolePermission.jsonPermissionList())
                    .jti(jti)
                    .clientType(clientType)
                    .deviceId(deviceId)
                    .build();

            String accessToken = jwtService.generateAccessToken(payload);
            String refreshToken = jwtService.generateRefreshToken(payload);

            jwtService.setTokenForUserId(redisTemplate, user.getUser_id(), clientType, deviceId, refreshToken, accessToken, response);

            return ApiResponse.success(Collections.singletonMap("user_id", user.getUser_id()));
        } else {
            return ApiResponse.fail(401, "登录失败:用户名或密码错误");
        }
    }

    @PostMapping("/login-Captcha")
    public ApiResponse<?> loginCaptcha(
            @Valid @RequestBody loginCaptchaRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) throws JsonProcessingException {
        List<TUser> users = userService.findUsersByUserName(request.account());
        String phone;
        if (!users.isEmpty()) {
            TUser user = users.get(0);
            phone = user.getPhone();
            String code = redisTemplate.opsForValue().get("phoneCode:" + phone);

            if (code == null) {
                return ApiResponse.fail(400, "验证码已失效");
            }

            if (!code.equals(request.verificationCode())) {
                return ApiResponse.fail(400, "验证码错误");
            }

            if (user.getUser_status() != 1) {
                return ApiResponse.fail(400, "登录失败:账户异常或已注销");
            }

            Optional<TRolePermission> oRp = rolePermissionService.getOptById(user.getRole_id());
            if (oRp.isEmpty()) {
                return ApiResponse.fail(500, "登录失败:用户权限异常");
            }

            TRolePermission rolePermission = oRp.get();

            Long jti = idGenerator.nextId();
            String clientType = userService.resolveClientType(httpRequest);
            String deviceId = userService.resolveDeviceId(httpRequest);
            JwtPayload payload = JwtPayload.builder()
                    .user_id(user.getUser_id())
                    .role(rolePermission.getRole())
                    .permission(rolePermission.jsonPermissionList())
                    .jti(jti)
                    .clientType(clientType)
                    .deviceId(deviceId)
                    .build();

            String accessToken = jwtService.generateAccessToken(payload);
            String refreshToken = jwtService.generateRefreshToken(payload);

            jwtService.setTokenForUserId(redisTemplate, user.getUser_id(), clientType, deviceId, refreshToken, accessToken, response);

            redisTemplate.delete("phoneCode:" + phone);

            return ApiResponse.success(Collections.singletonMap("user_id", user.getUser_id()));
        } else {
            return ApiResponse.fail(401, "登录失败:用户不存在");
        }

    }

    @PostMapping("/forgetPassword")
    public ApiResponse<?> forgetPassword(
            @Valid @RequestBody forgetPasswordRequest request,
            HttpServletResponse response
    ) {

        List<TUser> users = userService.findUsersByUserName(request.account());
        String phone;
        if (!users.isEmpty()) {
            TUser user = users.get(0);
            phone = user.getPhone();
            String code = redisTemplate.opsForValue().get("phoneCode:" + phone);

            if (code == null) {
                return ApiResponse.fail(400,"验证码已失效");
            }

            if (!code.equals(request.verificationCode())) {
                return ApiResponse.fail(400,"验证码错误");
            }

            String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$";
            if (!request.passwordHash().matches(regex)) {
                return ApiResponse.fail(400,"密码必须包含大小写字母");
            }
            if (!userService.forgetAndResetPassword(request, user)) {
                return ApiResponse.fail(500, "重置失败");
            }

            redisTemplate.delete("phoneCode:" + phone);

            return ApiResponse.success("重置密码成功");
        } else {
            return ApiResponse.fail(401, "重置失败:用户不存在");
        }
    }

    @PostMapping("/sendCaptcha")
    public ApiResponse<?> sendCode(@Valid @RequestBody sendCodeRequest request) {
        String phone = "";

        if (request.method().equals("user_name")) {
            List<TUser> users = userService.findUsersByUserName(request.str());
            if (!users.isEmpty()) {
                phone = users.get(0).getPhone();
            } else {
                return ApiResponse.fail(401, "发送失败:用户不存在");
            }
        }

        if (request.method().equals("phone")) {
            phone = request.str();
        }

        if (phone.isEmpty()) {
            return ApiResponse.fail(401, "未知发送方式");
        }

        String regex = "^1[3-9]\\d{9}$";

        if (!phone.matches(regex)) {
            return ApiResponse.fail(400,"手机号码格式不正确");
        }

//        String code = redisTemplate.opsForValue().get("phoneCode:" + phone);
//        if (code != null) {
//            return ApiResponse.fail(400,"5分钟内只能发送一次验证码");
//        }

        Long ttl = redisTemplate.getExpire("phoneCode:" + phone, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            long secondsPassed = 300 - ttl; // 已过去的秒数（300 是 5 分钟）
            if (secondsPassed < 60) {
                // 不允许再次发送
                return ApiResponse.fail(400,"验证码发送过于频繁，请稍后再试");
            }
        }

        String code = generateNumericCaptcha(6);
        redisTemplate.opsForValue().set("phoneCode:" + phone, code, 5, TimeUnit.MINUTES);

        // 验证码发送服务 * sendCodeService
        smsService.sendCodeForAliYun(phone,code);

        return ApiResponse.success("验证码已发送,5分钟有效");
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody registerRequest request) {

        if (!request.agree_policy()) {
            return ApiResponse.fail(400,"未勾选用户协议");
        }

        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$";
        if (!request.passwordHash().matches(regex)) {
            return ApiResponse.fail(400,"密码必须包含大小写字母");
        }

        String code = redisTemplate.opsForValue().get("phoneCode:" + request.phone());

        if (code == null) {
            return ApiResponse.fail(400,"验证码已失效");
        }

        if (!code.equals(request.verificationCode())) {
            return ApiResponse.fail(400,"验证码错误");
        }

        if (userService.userExists(request)) {
            return ApiResponse.fail(400,"注册失败:用户名或手机号码已存在");
        }

        try {
            if (!userService.registerService(request)) {
                return ApiResponse.fail(500,"注册失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.fail(500,"注册失败");
        }

        redisTemplate.delete("phoneCode:" + request.phone());

        return ApiResponse.success("注册成功");
    }

    @PostMapping("/refresh")
    public ApiResponse<?> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ApiResponse.fail(401,"未提供 refresh_token");
        }

        JwtPayload payload;
        try {
            // 从 refresh_token 中解析
            payload = jwtService.extractPayload(refreshToken);
        } catch (Exception e) {
            return ApiResponse.fail(401,"无效的 refresh_token");
        }

        // 从 Redis 中获取用户对应的 refresh_token
        Long redisJti = Long.valueOf(Objects.requireNonNull(redisTemplate.opsForValue().get(jwtService.generateSessionKey(payload.getUser_id(), payload.getClientType()))));
        if (redisJti == null) {
            return ApiResponse.fail(401,"未找到该用户的 refresh_token");
        }

        // 比对 Redis 中存储的 jti，注意 Redis 存的是 String
        if (!redisJti.equals(payload.getJti())) {
            return ApiResponse.fail(401,"refresh_token 不匹配");
        }

        // 验证 refresh_token 是否有效
        if (!jwtService.isTokenValid(refreshToken)) {
            return ApiResponse.fail(401,"refresh_token 已过期");
        }

        // 生成新的 access_token 和 refresh_token
        String newAccessToken = jwtService.generateAccessTokenByUserId(payload);
        String newRefreshToken = jwtService.generateRefreshTokenByUserId(payload);

        jwtService.setTokenForUserId(redisTemplate, payload.getUser_id(), payload.getClientType(), payload.getDeviceId(), newRefreshToken, newAccessToken, response);

        return ApiResponse.success("Token 已刷新");
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ApiResponse.fail(401,"未提供 refresh_token");
        }

        JwtPayload payload;
        try {
            payload = jwtService.extractPayload(refreshToken);
        } catch (Exception e) {
            return ApiResponse.fail(401,"无效的 refresh_token");
        }

        // 从 Redis 中删除该 refresh_token
        redisTemplate.delete(jwtService.generateSessionKey(payload.getUser_id(), payload.getClientType()));

        // 清除 Cookie
        ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)  // 立即失效
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ApiResponse.success("已成功登出");
    }

    @PostMapping("/refreshTokenVerify")
    public ApiResponse<?> refreshTokenVerify(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ApiResponse.fail(401,"未提供 refresh_token");
        }

        JwtPayload payload;
        try {
            payload = jwtService.extractPayload(refreshToken);
        } catch (Exception e) {
            return ApiResponse.fail(401,"无效的 refresh_token");
        }

        // 从 Redis 中获取用户对应的 refresh_token
        Long redisJti = Long.valueOf(Objects.requireNonNull(redisTemplate.opsForValue().get(jwtService.generateSessionKey(payload.getUser_id(), payload.getClientType()))));
        if (redisJti == null) {
            return ApiResponse.fail(401,"未找到该用户的 refresh_token");
        }

        // 比对 Redis 中存储的 jti，注意 Redis 存的是 String
        if (!redisJti.equals(payload.getJti())) {
            return ApiResponse.fail(401,"refresh_token 不匹配");
        }

        // 验证 refresh_token 是否有效
        if (!jwtService.isTokenValid(refreshToken)) {
            return ApiResponse.fail(401,"refresh_token 已过期");
        }

        return ApiResponse.success("OK");
    }
    
    
}
