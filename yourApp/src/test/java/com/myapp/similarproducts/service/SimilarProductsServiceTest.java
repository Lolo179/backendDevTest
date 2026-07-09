package com.myapp.similarproducts.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.myapp.similarproducts.application.port.ProductApiClient;
import com.myapp.similarproducts.domain.model.Product;
import com.myapp.similarproducts.infrastructure.outbound.productapi.ProductApiException;
import com.myapp.similarproducts.infrastructure.outbound.productapi.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimilarProductsServiceTest {

    @Mock
    private ProductApiClient productApiClient;

    private ExecutorService executorService;
    private SimilarProductsService similarProductsService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(4);
        similarProductsService = new SimilarProductsService(productApiClient, executorService);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void shouldReturnProductsInSimilarIdsOrder() {
        // given
        List<Product> expected = products("3", "2", "4");
        when(productApiClient.getSimilarIds("1")).thenReturn(List.of("3", "2", "4"));
        when(productApiClient.getProduct("3")).thenReturn(expected.get(0));
        when(productApiClient.getProduct("2")).thenReturn(expected.get(1));
        when(productApiClient.getProduct("4")).thenReturn(expected.get(2));

        // when
        List<Product> result = similarProductsService.getSimilarProducts("1");

        // then
        assertThat(result)
            .extracting(Product::id)
            .containsExactly("3", "2", "4");

        InOrder inOrder = inOrder(productApiClient);
        inOrder.verify(productApiClient).getSimilarIds("1");
        inOrder.verify(productApiClient, atLeast(1)).getProduct(anyString());
    }

    @Test
    void shouldReturnEmptyListWhenNoSimilarIds() {
        // given
        when(productApiClient.getSimilarIds("1")).thenReturn(List.of());

        // when
        List<Product> result = similarProductsService.getSimilarProducts("1");

        // then
        assertThat(result).isEmpty();
        verify(productApiClient).getSimilarIds("1");
        verifyNoMoreInteractions(productApiClient);
    }

    @Test
    void shouldOmitProductWhenProductDetailReturnsNotFound() {
        // given
        List<Product> expected = products("2", "3");
        when(productApiClient.getSimilarIds("1")).thenReturn(List.of("2", "404", "3"));
        when(productApiClient.getProduct("2")).thenReturn(expected.get(0));
        when(productApiClient.getProduct("404")).thenThrow(new ProductNotFoundException("not found"));
        when(productApiClient.getProduct("3")).thenReturn(expected.get(1));

        // when
        List<Product> result = similarProductsService.getSimilarProducts("1");

        // then
        assertThat(result)
            .extracting(Product::id)
            .containsExactly("2", "3");
    }

    @Test
    void shouldOmitProductWhenProductDetailFails() {
        // given
        List<Product> expected = products("2", "3");
        when(productApiClient.getSimilarIds("1")).thenReturn(List.of("2", "err", "3"));
        when(productApiClient.getProduct("2")).thenReturn(expected.get(0));
        when(productApiClient.getProduct("err")).thenThrow(new ProductApiException("boom"));
        when(productApiClient.getProduct("3")).thenReturn(expected.get(1));

        // when
        List<Product> result = similarProductsService.getSimilarProducts("1");

        // then
        assertThat(result)
            .extracting(Product::id)
            .containsExactly("2", "3");
    }

    @Test
    void shouldPropagateExceptionWhenSimilarIdsFails() {
        // given
        when(productApiClient.getSimilarIds("1")).thenThrow(new ProductApiException("mandatory call failed"));

        // when / then
        assertThatThrownBy(() -> similarProductsService.getSimilarProducts("1"))
            .isInstanceOf(ProductApiException.class)
            .hasMessageContaining("mandatory call failed");

        verify(productApiClient).getSimilarIds("1");
        verifyNoMoreInteractions(productApiClient);
    }

    private static Product product(String id) {
        return new Product(id, "product-" + id, BigDecimal.ONE, true);
    }

    private static List<Product> products(String... ids) {
        return Arrays.stream(ids)
            .map(SimilarProductsServiceTest::product)
            .toList();
    }
}
