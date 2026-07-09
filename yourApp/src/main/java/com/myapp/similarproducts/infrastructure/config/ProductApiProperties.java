package com.myapp.similarproducts.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.product-api")
public record ProductApiProperties(
    String baseUrl,
    Duration connectTimeout,
    Duration readTimeout
) {
}
