package org.iotf.comparerankservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.iotf")
@MapperScan({"org.iotf.mapper", "org.iotf.comparerankservice.mapper"})
public class CompareRankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompareRankServiceApplication.class, args);
    }

}
