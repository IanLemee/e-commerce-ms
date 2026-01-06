package com.tech.ian.order.config.kafka.dto;

import com.tech.ian.order.model.PaymentStatus;

import java.math.BigDecimal;

public record OrderSendEventDto(String orderId, String customerId, BigDecimal totalPrice, PaymentStatus paymentStatus, String cardToken) {
}
