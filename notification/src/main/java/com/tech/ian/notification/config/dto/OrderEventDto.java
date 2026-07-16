package com.tech.ian.notification.config.dto;

import java.math.BigDecimal;

public record OrderEventDto(String orderId, String customerId, BigDecimal totalPrice) {
}
