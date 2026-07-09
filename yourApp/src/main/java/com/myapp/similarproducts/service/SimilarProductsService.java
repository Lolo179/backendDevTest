package com.myapp.similarproducts.service;

import com.myapp.similarproducts.client.ProductApiClient;
import com.myapp.similarproducts.client.ProductApiException;
import com.myapp.similarproducts.client.ProductNotFoundException;
import com.myapp.similarproducts.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SimilarProductsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarProductsService.class);

    private final ProductApiClient productApiClient;
    private final Executor executor;

    public SimilarProductsService(
        ProductApiClient productApiClient,
        @Qualifier("similarProductsExecutor") Executor executor
    ) {
        this.productApiClient = productApiClient;
        this.executor = executor;
    }

    public List<Product> getSimilarProducts(String productId) {
        List<String> similarIds = productApiClient.getSimilarIds(productId);
        if (similarIds.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<Product>> futures = similarIds.stream()
            .map(similarId -> CompletableFuture.supplyAsync(
                () -> productApiClient.getProduct(similarId),
                executor
            ))
            .toList();

        List<Product> products = new ArrayList<>();
        for (int i = 0; i < similarIds.size(); i++) {
            String similarId = similarIds.get(i);
            try {
                products.add(futures.get(i).join());
            } catch (CompletionException ex) {
                if (isBestEffortFailure(ex.getCause())) {
                    LOGGER.warn("Skipping product {} due to downstream error", similarId);
                    continue;
                }
                throw ex;
            }
        }

        return products;
    }

    private static boolean isBestEffortFailure(Throwable throwable) {
        return throwable instanceof ProductNotFoundException || throwable instanceof ProductApiException;
    }
}
