package com.myapp.similarproducts.infrastructure.outbound.productapi;

import com.myapp.similarproducts.application.port.ProductApiClient;
import com.myapp.similarproducts.domain.model.Product;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ProductApiRestClient implements ProductApiClient {

    private static final ParameterizedTypeReference<List<Object>> SIMILAR_IDS_TYPE =
        new ParameterizedTypeReference<>() {
        };

    private final RestClient restClient;

    public ProductApiRestClient(@Qualifier("productApiHttpClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<String> getSimilarIds(String productId) {
        try {
            List<Object> similarIds = restClient.get()
                .uri("/product/{productId}/similarids", productId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ProductNotFoundException("Product " + productId + " not found");
                })
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    throw new ProductApiException("Downstream 4xx while fetching similar IDs for product " + productId);
                })
                .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                    throw new ProductApiException("Downstream 5xx while fetching similar IDs for product " + productId);
                })
                .body(SIMILAR_IDS_TYPE);

            if (similarIds == null) {
                return List.of();
            }

            return similarIds.stream()
                .map(ProductApiRestClient::normalizeSimilarId)
                .toList();
        } catch (ProductNotFoundException | ProductApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ProductApiException("Timeout or I/O error while fetching similar IDs for product " + productId, ex);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ProductNotFoundException("Product " + productId + " not found", ex);
            }
            throw new ProductApiException("Downstream error while fetching similar IDs for product " + productId, ex);
        } catch (RuntimeException ex) {
            throw new ProductApiException("Unexpected error while fetching similar IDs for product " + productId, ex);
        }
    }

    @Override
    public Product getProduct(String productId) {
        try {
            Product product = restClient.get()
                .uri("/product/{productId}", productId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ProductNotFoundException("Product " + productId + " not found");
                })
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    throw new ProductApiException("Downstream 4xx while fetching product detail for product " + productId);
                })
                .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                    throw new ProductApiException("Downstream 5xx while fetching product detail for product " + productId);
                })
                .body(Product.class);

            if (product == null) {
                throw new ProductApiException("Downstream returned an empty body for product " + productId);
            }

            return product;
        } catch (ProductNotFoundException | ProductApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ProductApiException("Timeout or I/O error while fetching product detail for product " + productId, ex);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ProductNotFoundException("Product " + productId + " not found", ex);
            }
            throw new ProductApiException("Downstream error while fetching product detail for product " + productId, ex);
        } catch (RuntimeException ex) {
            throw new ProductApiException("Unexpected error while fetching product detail for product " + productId, ex);
        }
    }

    private static String normalizeSimilarId(Object rawId) {
        if (rawId == null) {
            throw new ProductApiException("Received null similar ID from downstream");
        }
        return String.valueOf(rawId);
    }
}
