package com.tech.ian.order.model.dto;

import java.math.BigDecimal;

public record ProductResponseDto(String product, BigDecimal price) {
}
