package com.tech.ian.order.model.dto;

import com.tech.ian.order.model.PaymentStatus;

import java.math.BigDecimal;

public record OrderResponseDto(String product, int quantity, BigDecimal price, BigDecimal totalPrice, PaymentStatus status) {
}
