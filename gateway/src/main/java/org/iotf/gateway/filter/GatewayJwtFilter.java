package org.iotf.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.iotf.gateway.service.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GatewayJwtFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/**"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        // 获取 Authorization 头
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Empty Token");
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                return unauthorized(exchange, "Valid Token");
            }
        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, "Valid Token");
        } catch (JwtException e) {
            return unauthorized(exchange, "Verification failed");
        } catch (Exception e) {
            return unauthorized(exchange, "Verification Service Exception");
        }

        return chain.filter(exchange);
    }

    private boolean isWhitePath(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}