package com.tech.ian.stock.model.dto;

import java.math.BigDecimal;

public record StockResponseDto(String product, BigDecimal price) {
}
