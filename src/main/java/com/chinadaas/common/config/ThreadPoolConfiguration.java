package com.chinadaas.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liubc
 * @version 1.0.0
 * @description 线程池配置
 * @createTime 2021.07.01
 */
@Slf4j
@Configuration
public class ThreadPoolConfiguration {
    private final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        log.info("creating thread pool, available core pool size: [{}]", CORE_POOL_SIZE);
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                CORE_POOL_SIZE,
                0L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                Executors.defaultThreadFactory()
        );
    }

}
