package org.iotf.authservice.configuration;

import org.iotf.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class idGeneratorConfiguration {

    @Value("${snowflake.worker-id:1}")
    private long workerId;

    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(workerId, datacenterId);
    }
}
