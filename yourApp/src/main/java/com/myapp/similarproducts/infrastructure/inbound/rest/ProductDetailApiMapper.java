package com.myapp.similarproducts.infrastructure.inbound.rest;

import com.myapp.similarproducts.domain.model.Product;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDetailApiMapper {

    com.example.similarproducts.generated.model.ProductDetail toApi(Product product);

    List<com.example.similarproducts.generated.model.ProductDetail> toApiList(List<Product> products);
}
