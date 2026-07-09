package com.myapp.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "similar-products.client")
public record SimilarProductsProperties(int maxConcurrency) {
}
