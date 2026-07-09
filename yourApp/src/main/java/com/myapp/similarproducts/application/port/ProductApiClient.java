package com.myapp.similarproducts.application.port;

import com.myapp.similarproducts.domain.model.Product;
import java.util.List;

public interface ProductApiClient {

    List<String> getSimilarIds(String productId);

    Product getProduct(String productId);
}
