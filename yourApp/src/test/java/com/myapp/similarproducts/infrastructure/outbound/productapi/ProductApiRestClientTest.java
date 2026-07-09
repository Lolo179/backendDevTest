package com.myapp.similarproducts.infrastructure.outbound.productapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.myapp.similarproducts.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ProductApiRestClientTest {

    private static final String BASE_URL = "http://localhost:3001";

    private MockRestServiceServer mockServer;
    private ProductApiRestClient productApiRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        productApiRestClient = new ProductApiRestClient(builder.build());
    }

    @Test
    void shouldNormalizeNumericSimilarIdsToStringValues() {
        // given
        mockServer.expect(requestTo(BASE_URL + "/product/1/similarids"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("[2,3,4]", MediaType.APPLICATION_JSON));

        // when
        List<String> result = productApiRestClient.getSimilarIds("1");

        // then
        assertThat(result).containsExactly("2", "3", "4");
        mockServer.verify();
    }

    @Test
    void shouldMapProductFieldsFromDownstreamResponse() {
        // given
        mockServer.expect(requestTo(BASE_URL + "/product/2"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                """
                    {
                      "id": "2",
                      "name": "product-2",
                      "price": 10.25,
                      "availability": true
                    }
                    """,
                MediaType.APPLICATION_JSON
            ));

        // when
        Product result = productApiRestClient.getProduct("2");

        // then
        assertThat(result.id()).isEqualTo("2");
        assertThat(result.name()).isEqualTo("product-2");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("10.25"));
        assertThat(result.availability()).isTrue();
        mockServer.verify();
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenDownstreamReturns404() {
        // given
        mockServer.expect(requestTo(BASE_URL + "/product/404"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // when
        var thrown = assertThatThrownBy(() -> productApiRestClient.getProduct("404"));

        // then
        thrown
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("404");
        mockServer.verify();
    }

    @Test
    void shouldThrowProductApiExceptionWhenDownstreamReturns500() {
        // given
        mockServer.expect(requestTo(BASE_URL + "/product/2"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("boom"));

        // when
        var thrown = assertThatThrownBy(() -> productApiRestClient.getProduct("2"));

        // then
        thrown
            .isInstanceOf(ProductApiException.class)
            .hasMessageContaining("5xx");
        mockServer.verify();
    }

    @Test
    void shouldThrowProductApiExceptionWhenDownstreamRequestFailsWithIoError() {
        // given
        mockServer.expect(requestTo(BASE_URL + "/product/1/similarids"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(request -> {
                throw new java.io.IOException("connection reset");
            });

        // when
        var thrown = assertThatThrownBy(() -> productApiRestClient.getSimilarIds("1"));

        // then
        thrown
            .isInstanceOf(ProductApiException.class)
            .hasMessageContaining("product 1");
        mockServer.verify();
    }
}
