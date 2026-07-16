package com.tech.ian.order.utils;

import com.tech.ian.order.config.kafka.dto.OrderNotificationEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.OrderItem;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.OrderRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class OrderFactory {

    public OrderNotificationEventDto buildOrderEvent(OrderEntity orderEntity) {
        return new OrderNotificationEventDto(orderEntity.getId(), orderEntity.getCustomerId(), orderEntity.getTotalPrice(), orderEntity.getPaymentStatus());
    }

    public OrderEntity buildOrder(OrderRequestDto req, String userId, BigDecimal price, BigDecimal totalPrice) {
        OrderItem orderItem = OrderItem.builder().product(req.product()).quantity(req.quantity()).price(price).build();
        return OrderEntity.builder()
                .customerId(userId)
                .paymentStatus(PaymentStatus.PENDING)
                .totalPrice(totalPrice)
                .item(orderItem)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
