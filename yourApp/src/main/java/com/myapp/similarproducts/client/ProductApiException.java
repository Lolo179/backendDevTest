package com.myapp.similarproducts.client;

public class ProductApiException extends RuntimeException {

    public ProductApiException(String message) {
        super(message);
    }

    public ProductApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
