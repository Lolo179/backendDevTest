package com.myapp.similarproducts.infrastructure.inbound.rest;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.similarproducts.generated.model.ProductDetail;
import com.myapp.similarproducts.application.service.SimilarProductsService;
import com.myapp.similarproducts.domain.model.Product;
import com.myapp.similarproducts.infrastructure.outbound.productapi.ProductApiException;
import com.myapp.similarproducts.infrastructure.outbound.productapi.ProductNotFoundException;
import com.myapp.similarproducts.shared.error.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SimilarProductsController.class)
@Import(GlobalExceptionHandler.class)
class SimilarProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimilarProductsService similarProductsService;

    @MockBean
    private ProductDetailApiMapper productDetailApiMapper;

    @Test
    void shouldReturnSimilarProductsWhenServiceReturnsProducts() throws Exception {
        // given
        List<Product> products = List.of(product("2"), product("3"));
        List<ProductDetail> apiProducts = List.of(apiProduct("2"), apiProduct("3"));

        when(similarProductsService.getSimilarProducts("1")).thenReturn(products);
        when(productDetailApiMapper.toApiList(products)).thenReturn(apiProducts);

        // when / then
        mockMvc.perform(get("/product/{productId}/similar", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("product-2"))
            .andExpect(jsonPath("$[0].price").value(10))
            .andExpect(jsonPath("$[0].availability").value(true))
            .andExpect(jsonPath("$[0].id").value("2"))
            .andExpect(jsonPath("$[1].id").value("3"));

        // then
        verify(similarProductsService).getSimilarProducts("1");
        verify(productDetailApiMapper).toApiList(products);
    }

    @Test
    void shouldReturnEmptyArrayWhenServiceReturnsNoProducts() throws Exception {
        // given
        List<Product> products = List.of();
        List<ProductDetail> apiProducts = List.of();

        when(similarProductsService.getSimilarProducts("1")).thenReturn(products);
        when(productDetailApiMapper.toApiList(products)).thenReturn(apiProducts);

        // when / then
        mockMvc.perform(get("/product/{productId}/similar", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));

        // then
        verify(similarProductsService).getSimilarProducts("1");
        verify(productDetailApiMapper).toApiList(products);
    }

    @Test
    void shouldDelegateProductIdToService() throws Exception {
        // given
        List<Product> products = List.of(product("9"));
        List<ProductDetail> apiProducts = List.of(apiProduct("9"));

        when(similarProductsService.getSimilarProducts("9")).thenReturn(products);
        when(productDetailApiMapper.toApiList(products)).thenReturn(apiProducts);

        // when
        mockMvc.perform(get("/product/{productId}/similar", "9"))
            .andExpect(status().isOk());

        // then
        verify(similarProductsService).getSimilarProducts("9");
        verify(productDetailApiMapper).toApiList(products);
    }

    @Test
    void shouldReturnNotFoundWhenServiceThrowsProductNotFoundException() throws Exception {
        // given
        when(similarProductsService.getSimilarProducts("404"))
            .thenThrow(new ProductNotFoundException("Product 404 not found"));

        // when / then
        mockMvc.perform(get("/product/{productId}/similar", "404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Product not found"));
    }

    @Test
    void shouldReturnBadGatewayWhenServiceThrowsProductApiException() throws Exception {
        // given
        when(similarProductsService.getSimilarProducts("1"))
            .thenThrow(new ProductApiException("Downstream 5xx"));

        // when / then
        mockMvc.perform(get("/product/{productId}/similar", "1"))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.status").value(502))
            .andExpect(jsonPath("$.detail").value("Downstream service error"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        // given
        when(similarProductsService.getSimilarProducts("1"))
            .thenThrow(new RuntimeException("boom"));

        // when / then
        mockMvc.perform(get("/product/{productId}/similar", "1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("Internal server error"))
            .andExpect(jsonPath("$.instance").value("/product/1/similar"))
            .andExpect(jsonPath("$.title", containsString("Internal")));
    }

    private static Product product(String id) {
        return new Product(id, "product-" + id, BigDecimal.TEN, true);
    }

    private static ProductDetail apiProduct(String id) {
        return new ProductDetail()
            .id(id)
            .name("product-" + id)
            .price(BigDecimal.TEN)
            .availability(true);
    }
}
