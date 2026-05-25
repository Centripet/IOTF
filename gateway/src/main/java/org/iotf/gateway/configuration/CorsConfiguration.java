package org.iotf.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 网关跨域配置
 * 仅拦截 OPTIONS 预检请求直接返回，避免转发到下游服务。
 * 实际请求的 CORS 响应头由下游服务（auth-service 等）通过 Spring Security CORS 配置添加，
 * 网关不做干预，防止出现重复的 Access-Control-Allow-Origin 头。
 */
@Configuration
public class CorsConfiguration {

    private static final String ALLOWED_HEADERS =
        "Authorization,Content-Type,X-Requested-With,accept,Origin,"
            + "Access-Control-Request-Method,Access-Control-Request-Headers";
    private static final String ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS,PATCH";
    private static final String MAX_AGE = "3600";

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 仅处理 OPTIONS 预检，实际请求的 CORS 头由下游服务负责
            if (CorsUtils.isCorsRequest(request)
                && request.getMethod() == HttpMethod.OPTIONS) {

                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();

                String origin = request.getHeaders().getOrigin();
                if (origin != null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                }
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            return chain.filter(exchange);
        };
    }
}
