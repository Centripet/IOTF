package org.iotf.service.common;

import cn.hutool.core.bean.BeanUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.iotf.entity.auth.JwtPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 8; // 60分钟 * 8
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7天

    // 获取加密密钥
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 生成 Access Token（有效期 30分钟）
    public String generateAccessToken(JwtPayload payload) {
        Map<String, Object> claims = BeanUtil.beanToMap(payload);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 生成 Refresh Token（有效期 7天）
    public String generateRefreshToken(JwtPayload payload) {
        Map<String, Object> claims = BeanUtil.beanToMap(payload);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 Token，获取 user_id
    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("user_id", String.class);
    }

    // 验证 Token 是否有效
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token); // 如果解析成功，说明有效
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 如果验证失败（如过期、无效等），返回 false
            return false;
        }
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public JwtPayload extractPayload(String token) {
        Claims claims = parseToken(token);
        JwtPayload payload = new JwtPayload();

        payload.setUser_id(claims.get("user_id", Long.class));
        payload.setRole(claims.get("role", String.class));
        Object permissionObj = claims.get("permission");

        payload.setJti(claims.get("jti", Long.class));
        payload.setClientType(claims.get("clientType", String.class));
        payload.setDeviceId(claims.get("deviceId", String.class));

        if (permissionObj instanceof List<?>) {
            payload.setPermission(((List<?>) permissionObj).stream()
                    .map(String::valueOf)
                    .toList());
        }

        System.out.println(payload);

        return payload;
    }

    private Long extractJti(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("jti", Long.class);
    }

    public String generateSessionKey(Long userId, String clientType) {
        return "refresh:" + userId + ":" + clientType;
    }

    public void setTokenForUserId(RedisTemplate<String, String> redisTemplate,
                                  Long userId,
                                  String clientType, // 新增：web/app/h5/miniProgram
                                  String deviceId,   // 新增：设备唯一标识
                                  String refreshToken,
                                  String accessToken,
                                  HttpServletResponse response) {

        Long refreshJti = extractJti(refreshToken);

        redisTemplate.opsForValue().set(generateSessionKey(userId, clientType), String.valueOf(refreshJti), 7, TimeUnit.DAYS);

        response.addHeader("Authorization", "Bearer " + accessToken);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 根据 user_id 生成 Access Token
    public String generateAccessTokenByUserId(JwtPayload payload) {
        return generateAccessToken(payload);
    }

    // 根据 user_id 生成 Refresh Token
    public String generateRefreshTokenByUserId(JwtPayload payload) {
        return generateRefreshToken(payload);
    }
}