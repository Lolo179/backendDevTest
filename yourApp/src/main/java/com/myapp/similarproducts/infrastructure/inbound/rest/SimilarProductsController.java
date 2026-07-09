package com.myapp.similarproducts.infrastructure.inbound.rest;

import com.example.similarproducts.generated.api.ProductApi;
import com.example.similarproducts.generated.model.ProductDetail;
import com.myapp.similarproducts.application.service.SimilarProductsService;
import com.myapp.similarproducts.domain.model.Product;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimilarProductsController implements ProductApi {

    private final SimilarProductsService similarProductsService;
    private final ProductDetailApiMapper productDetailApiMapper;

    public SimilarProductsController(
        SimilarProductsService similarProductsService,
        ProductDetailApiMapper productDetailApiMapper
    ) {
        this.similarProductsService = similarProductsService;
        this.productDetailApiMapper = productDetailApiMapper;
    }

    @Override
    public ResponseEntity<Set<ProductDetail>> getProductSimilar(String productId) {
        List<Product> internalProducts = similarProductsService.getSimilarProducts(productId);
        List<ProductDetail> apiProducts = productDetailApiMapper.toApiList(internalProducts);
        return ResponseEntity.ok(new LinkedHashSet<>(apiProducts));
    }
}
