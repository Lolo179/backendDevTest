package com.myapp.similarproducts.infrastructure.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimilarProductsExecutorConfig {

    @Bean(name = "similarProductsExecutor", destroyMethod = "shutdown")
    ExecutorService similarProductsExecutor(SimilarProductsProperties properties) {
        int maxConcurrency = Math.max(1, properties.maxConcurrency());
        return Executors.newFixedThreadPool(maxConcurrency);
    }
}
