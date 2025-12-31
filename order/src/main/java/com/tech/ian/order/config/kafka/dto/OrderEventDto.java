package com.tech.ian.order.config.kafka.dto;

import com.tech.ian.order.model.PaymentStatus;

public record OrderEventDto(Long id, String customerId, PaymentStatus paymentStatus) {
}
