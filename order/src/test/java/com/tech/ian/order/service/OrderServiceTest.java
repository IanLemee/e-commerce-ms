package com.tech.ian.order.service;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.order.client.StockApiClient;
import com.tech.ian.order.commons.BuildFactory;
import com.tech.ian.order.config.kafka.dto.OrderSendEventDto;
import com.tech.ian.order.model.dto.ProductResponseDto;
import com.tech.ian.order.repository.OrderRepository;
import com.tech.ian.order.utils.OrderFactory;
import com.tech.ian.order.utils.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository repository;
    @Mock
    private StockApiClient stockApiClient;
    @Mock
    private KafkaTemplate<String, OrderSendEventDto> kafkaTemplate;
    @Mock
    private UserContext userContext;
    @Mock
    private OrderFactory orderFactory;
    
    @InjectMocks
    private OrderService orderService;

    // TODO
    //  create test
    //  Create Order
    //  Get product
    //  Refresh payment

    @Nested
    class create {
        @Test
        @DisplayName("Should create an order when successful")
        void shouldCreateAnOrderWhenSuccessful() {
            var price = BigDecimal.valueOf(1000.0);
            when(stockApiClient.getProductInfo("Phone", 1)).thenReturn(Optional.of(BuildFactory.productResponseBuild()));
            when(userContext.getCurrentUserId()).thenReturn("1");
            when(orderFactory.buildOrder(BuildFactory.orderReqBuild(), BuildFactory.orderEntityBuild().getId(), price, price)).thenReturn(BuildFactory.orderEntityBuild());
            when(repository.save(BuildFactory.orderEntityBuild())).thenReturn(BuildFactory.orderEntityBuild());
            when(kafkaTemplate.send("topic-payment-processor", null)).thenReturn(new CompletableFuture<>());

            var order = orderService.createOrder(BuildFactory.orderReqBuild());
            assertNotNull(order);
        }

        @Test
        @DisplayName("Should throw Product Out Of Stock Ex")
        void shouldThrowProductOutOfStockException() {
            when(stockApiClient.getProductInfo("Phone", 1)).thenReturn(Optional.empty());
            assertThrows(ProductOutOfStockException.class, () -> orderService.createOrder(BuildFactory.orderReqBuild()));
        }
    }

    @Nested
    class getProduct {
        @Test
        void p() {
            when(stockApiClient.getProductInfo(anyString(), anyInt())).thenReturn(Optional.of(BuildFactory.productResponseBuild()));
            var iphone = orderService.getProduct("Iphone", 1);
            assertNotNull(iphone);
            verify(stockApiClient, times(1)).getProductInfo("Iphone", 1);
        }

        @Test
        @DisplayName("Should throw Product Out Of Stock Ex")
        void shouldThrowProductOutOfStockException() {
            when(stockApiClient.getProductInfo("Phone", 1)).thenReturn(Optional.empty());
            assertThrows(ProductOutOfStockException.class, () -> orderService.createOrder(BuildFactory.orderReqBuild()));
        }
    }
}