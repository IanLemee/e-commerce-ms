package com.tech.ian.stock.utils;

import com.tech.ian.stock.model.StockEntity;
import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockFactory {
    public StockEntity buildStockEntity(String product, int quantity, BigDecimal price) {
        return StockEntity.builder().product(product).quantity(quantity).price(price).build();
    }

    public StockCreateDto buildStockCreate(String product, int quantity, BigDecimal price) {
        return new StockCreateDto(product, quantity, price);
    }

    public StockResponseDto buildStockResponse(String product, BigDecimal price) {
        return new StockResponseDto(product, price);
    }
}
