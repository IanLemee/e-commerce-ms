package com.tech.ian.exception;

public class ProductOutOfStockException extends BaseAppException {

    public ProductOutOfStockException(String message) {
        super(message);
    }

    public ProductOutOfStockException() {
        super("Product out of stock");
    }
}
