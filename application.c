spring:
  datasource:
    url: jdbc:postgresql://localhost:45434/iotf?currentSchema=main
    username: root
    password: W2316195243
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
  data:
    redis:
      host: localhost
      port: 46375
      #      password: your-password
      timeout: 2000
  security:
    user:
      name: admin
      password: p[];'\,./
      roles: ADMIN

springdoc:
  api-docs:
    path: /api-docs  # 配置 OpenAPI 文档的路径
    enabled: true
  swagger-ui:
    path: /swagger-ui.html  # 配置 Swagger UI 的路径
    enabled: true  # 启用 Swagger UI

jwt:
  secret:
    []

logging:
  level:
    root: INFO  # 根日志级别
    org.springframework.web: DEBUG  # Web 相关的日志级别
    com.baomidou.mybatisplus.core.executor.MybatisSimpleExecutor: debug  # MyBatis-Plus
    com.example.mapper: debug  # 你自己的 Mapper 包路径

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

aliyunService:
  bucket:
    []

  accessKeyId:
    []

  accessKeySecret:
    []

snowflake:
  worker-id: 1
  datacenter-id: 1