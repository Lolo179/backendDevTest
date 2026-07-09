package com.myapp.similarproducts.shared.error;

import com.myapp.similarproducts.infrastructure.outbound.productapi.ProductApiException;
import com.myapp.similarproducts.infrastructure.outbound.productapi.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Product not found");
    }

    @ExceptionHandler(ProductApiException.class)
    public ProblemDetail handleProductApiException(ProductApiException ex) {
        LOGGER.warn("Downstream API error: {}", ex.getMessage());
        return problem(HttpStatus.BAD_GATEWAY, "Downstream service error");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        LOGGER.error("Unexpected error while processing request", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(detail);
        return problemDetail;
    }
}
