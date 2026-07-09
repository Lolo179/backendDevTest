package com.myapp.similarproducts.infrastructure.outbound.productapi;

public class ProductApiException extends RuntimeException {

    public ProductApiException(String message) {
        super(message);
    }

    public ProductApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
