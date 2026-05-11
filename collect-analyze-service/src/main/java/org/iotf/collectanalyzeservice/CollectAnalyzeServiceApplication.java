package org.iotf.collectanalyzeservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.iotf")
@MapperScan({"org.iotf.mapper"})
public class CollectAnalyzeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectAnalyzeServiceApplication.class, args);
    }

}
