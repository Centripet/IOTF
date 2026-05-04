package org.iotf.test_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class test {

    @Value("${test.testStr}")
    private String testStr;

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

}
