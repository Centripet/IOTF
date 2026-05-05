package org.iotf.test_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iotf.requestFormation.test.testRequest;
import org.iotf.test_service.feignClient.testClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class test {

    @Value("${test.testStr}")
    private String testStr;

    final private testClient testFeignClient;

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", "用户" + id);
        result.put("message", "Hello from user-service");
        return result;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/confTest")
    public String confTest() {
        return testStr;
    }

    @GetMapping("/feignTest")
    public String feignTest(@Valid @RequestParam String str) {
        return testFeignClient.testMethod(str);
    }

    @PostMapping("/commonTest")
    public Object commonTest(@Valid @RequestBody testRequest testRequest) {
        return testRequest;
    }

}
