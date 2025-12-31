package com.tech.ian.order.commons;

import com.tech.ian.order.config.kafka.dto.OrderSendEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.OrderItem;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.ProductResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class BuildFactory {

    private BuildFactory() {
    }

    public static OrderEntity orderEntityBuild() {
        BigDecimal totalPrice = BigDecimal.valueOf(1000.00);
        var item = OrderItem.builder().product("Phone").quantity(1).price(totalPrice).build();
        return OrderEntity.builder()
                .id("1")
                .customerId("test@mail.com")
                .totalPrice(totalPrice)
                .paymentStatus(PaymentStatus.PENDING)
                .item(item)
                .build();
    }

    public static ProductResponseDto productResponseBuild() {
        return new ProductResponseDto("Phone", BigDecimal.valueOf(1000.00));
    }

    public static OrderSendEventDto orderSendEventBuild() {
        var cardDetails = new CardDetailsDto("1111222233334444", YearMonth.now(), "123", "Test");
        return new OrderSendEventDto("1", "test@mail.com", BigDecimal.valueOf(1000.0), PaymentStatus.PENDING, cardDetails);
    }

    public static OrderRequestDto orderReqBuild() {
        var cardDetails = new CardDetailsDto("1111222233334444", YearMonth.now(), "123", "Test");
        return new OrderRequestDto("Phone", 1, cardDetails);
    }
}
