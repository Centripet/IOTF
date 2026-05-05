package org.iotf.test_service.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface testClient {

    @GetMapping("/api/test/test")
    String testMethod(@RequestParam String str);

//    @SpringQueryMap
}
