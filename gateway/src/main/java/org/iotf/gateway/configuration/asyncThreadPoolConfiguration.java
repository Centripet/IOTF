//package org.iotf.gateway.configuration;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.AsyncConfigurer;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
///**
// * <p>
// *  异步线程池配置类
// *  用于配置 @Async 注解的线程池
// * </p>
// *
// * @author Centripet
// * @since 2026-03-20
// */
//@Slf4j
//@Configuration
//@EnableAsync
//public class asyncThreadPoolConfiguration implements AsyncConfigurer {
//
//    /**
//     * 核心线程数
//     */
//    @Value("${async.thread-pool.core-pool-size:5}")
//    private int corePoolSize;
//
//    /**
//     * 最大线程数
//     */
//    @Value("${async.thread-pool.max-pool-size:20}")
//    private int maxPoolSize;
//
//    /**
//     * 队列容量
//     */
//    @Value("${async.thread-pool.queue-capacity:100}")
//    private int queueCapacity;
//
//    /**
//     * 线程名前缀
//     */
//    @Value("${async.thread-pool.thread-name-prefix:async-task-}")
//    private String threadNamePrefix;
//
//    /**
//     * 线程空闲时间（秒）
//     */
//    @Value("${async.thread-pool.keep-alive-seconds:60}")
//    private int keepAliveSeconds;
//
//    /**
//     * 等待任务完成时间（秒）
//     */
//    @Value("${async.thread-pool.await-termination-seconds:60}")
//    private int awaitTerminationSeconds;
//
//    /**
//     * 配置默认的异步执行器（线程池）
//     * Bean 名称为 "taskExecutor"，这是 Spring @Async 的默认 Bean 名称
//     *
//     * @return 线程池执行器
//     */
//    @Bean(name = "taskExecutor")
//    @Override
//    public Executor getAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//
//        // 核心线程数
//        executor.setCorePoolSize(corePoolSize);
//
//        // 最大线程数
//        executor.setMaxPoolSize(maxPoolSize);
//
//        // 队列容量
//        executor.setQueueCapacity(queueCapacity);
//
//        // 线程名前缀
//        executor.setThreadNamePrefix(threadNamePrefix);
//
//        // 线程空闲时间（秒）
//        executor.setKeepAliveSeconds(keepAliveSeconds);
//
//        // 拒绝策略：当线程池和队列都满时，使用调用者线程来执行任务
//        // 这样可以保证任务一定被执行，但可能会阻塞调用者线程
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//
//        // 等待所有任务结束后再关闭线程池
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//
//        // 等待时间（秒）
//        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
//
//        // 初始化线程池
//        executor.initialize();
//
//        log.info("异步线程池配置完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}, 线程名前缀: {}",
//                corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);
//
//        return executor;
//    }
//
//    /**
//     * 配置异步任务异常处理器
//     *
//     * @return 异常处理器
//     */
//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return (throwable, method, params) -> {
//            log.error("异步任务执行异常 - 方法: {}, 参数: {}",
//                    method.getName(), params, throwable);
//        };
//    }
//}
