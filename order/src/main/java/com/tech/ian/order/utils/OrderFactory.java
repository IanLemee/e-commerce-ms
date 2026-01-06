package com.tech.ian.order.utils;

import com.tech.ian.order.config.kafka.dto.OrderSendEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.OrderItem;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.model.dto.OrderRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderFactory {

    public OrderSendEventDto buildOrderEvent(OrderEntity orderEntity, String token) {
        return new OrderSendEventDto(orderEntity.getId(), orderEntity.getCustomerId(), orderEntity.getTotalPrice(), orderEntity.getPaymentStatus(), token);
    }

    public OrderEntity buildOrder(OrderRequestDto req, String userId, BigDecimal price, BigDecimal totalPrice) {
        OrderItem orderItem = OrderItem.builder().product(req.product()).quantity(req.quantity()).price(price).build();
        return OrderEntity.builder()
                .customerId(userId)
                .paymentStatus(PaymentStatus.PENDING)
                .totalPrice(totalPrice)
                .item(orderItem)
                .build();
    }
}
