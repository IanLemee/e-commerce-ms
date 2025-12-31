package com.tech.ian.stock.model.dto;

import java.math.BigDecimal;

public record StockCreateDto(String product, int quantity, BigDecimal price) {
}
