package org.iotf.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Tag(name = "登录验证", description = "登录注册鉴权等相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class test {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/test")
    String testMethod(@Valid @RequestParam String str) {
        return str;
    }

}
