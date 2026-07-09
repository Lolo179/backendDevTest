package com.myapp.similarproducts.client;

import com.myapp.similarproducts.model.Product;
import java.util.List;

public interface ProductApiClient {

    List<String> getSimilarIds(String productId);

    Product getProduct(String productId);
}
