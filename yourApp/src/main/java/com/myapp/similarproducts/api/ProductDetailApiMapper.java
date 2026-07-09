package com.myapp.similarproducts.api;

import com.myapp.similarproducts.model.Product;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDetailApiMapper {

    com.example.similarproducts.generated.model.ProductDetail toApi(Product product);

    List<com.example.similarproducts.generated.model.ProductDetail> toApiList(List<Product> products);
}
