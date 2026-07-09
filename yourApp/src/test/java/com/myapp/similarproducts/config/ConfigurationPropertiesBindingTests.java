package com.myapp.similarproducts.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.myapp.similarproducts.SimilarProductsApplication;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SimilarProductsApplication.class)
class ConfigurationPropertiesBindingTests {

    @Autowired
    private ProductApiProperties productApiProperties;

    @Autowired
    private SimilarProductsProperties similarProductsProperties;

    @Test
    void loadsConfiguredProperties() {
        assertThat(productApiProperties.baseUrl()).isEqualTo("http://localhost:3001");
        assertThat(productApiProperties.connectTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(productApiProperties.readTimeout()).isEqualTo(Duration.ofMillis(1500));
        assertThat(similarProductsProperties.maxConcurrency()).isEqualTo(100);
    }
}
