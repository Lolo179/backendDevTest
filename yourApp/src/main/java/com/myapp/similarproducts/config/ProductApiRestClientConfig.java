package com.myapp.similarproducts.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ProductApiRestClientConfig {

    @Bean("productApiHttpClient")
    RestClient productApiHttpClient(ProductApiProperties productApiProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(toMillis(productApiProperties.connectTimeout()));
        requestFactory.setReadTimeout(toMillis(productApiProperties.readTimeout()));

        return RestClient.builder()
            .baseUrl(productApiProperties.baseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    private static int toMillis(Duration duration) {
        return (int) Math.min(Integer.MAX_VALUE, duration.toMillis());
    }
}
