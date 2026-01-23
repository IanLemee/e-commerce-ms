package com.tech.ian.order.commons;

import com.tech.ian.order.config.kafka.dto.OrderNotificationEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.OrderItem;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.ProductResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BuildFactory {

    private BuildFactory() {
    }

    public static OrderEntity orderEntityBuild() {
        BigDecimal totalPrice = BigDecimal.valueOf(1000.00);
        var item = OrderItem.builder().product("Phone").quantity(1).price(totalPrice).build();
        return OrderEntity.builder()
                .customerId("test@mail.com")
                .totalPrice(totalPrice)
                .paymentStatus(PaymentStatus.PENDING)
                .item(item)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ProductResponseDto productResponseBuild() {
        return new ProductResponseDto("Phone", BigDecimal.valueOf(1000.00));
    }

    public static OrderNotificationEventDto orderSendEventBuild() {
        return new OrderNotificationEventDto("1", "test@mail.com", BigDecimal.valueOf(1000.0), PaymentStatus.APPROVED);
    }

    public static OrderRequestDto orderReqBuild() {
        var cardDetails = new CardDetailsDto("pm_card_visa");
        return new OrderRequestDto("Phone", 1, cardDetails);
    }
}
