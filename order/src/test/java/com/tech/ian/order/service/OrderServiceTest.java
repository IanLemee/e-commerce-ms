package com.tech.ian.order.service;

import com.stripe.model.PaymentIntent;
import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.order.client.StockApiClient;
import com.tech.ian.order.commons.BuildFactory;
import com.tech.ian.order.config.kafka.dto.OrderBuyProductEventDto;
import com.tech.ian.order.config.kafka.dto.OrderNotificationEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.repository.OrderRepository;
import com.tech.ian.order.utils.OrderFactory;
import com.tech.ian.order.utils.StripeGateway;
import com.tech.ian.order.utils.UserContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;
    @Mock
    private StockApiClient stockApiClient;
    @Mock
    private KafkaTemplate<String, OrderNotificationEventDto> kafkaTemplateNotification;
    @Mock
    private KafkaTemplate<String, OrderBuyProductEventDto> kafkaTemplateBuyProduct;
    @Mock
    private UserContext userContext;
    @Mock
    private OrderFactory orderFactory;
    @Mock
    private StripeGateway stripeGateway;


    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Manually put the correct mock in the correct position
        orderService = new OrderService(
                repository,
                stockApiClient,
                kafkaTemplateNotification, // First argument
                kafkaTemplateBuyProduct,   // Second argument (No confusion here!)
                orderFactory,
                userContext,
                stripeGateway
        );
    }

    @Nested
    class create {
        @Test
        @DisplayName("Should create an order when successful")
        @SneakyThrows
        void shouldCreateAnOrderWhenSuccessful() {
            var price = BigDecimal.valueOf(1000.0);
            var orderEntity = BuildFactory.orderEntityBuild();
            var orderEvent = BuildFactory.orderSendEventBuild();
            when(stockApiClient.getProductInfo(anyString(), anyInt()))
                    .thenReturn(Optional.of(BuildFactory.productResponseBuild()));

            when(userContext.getCurrentUserId()).thenReturn("test@mail.com");

            when(orderFactory.buildOrder(any(), anyString(), any(), any()))
                    .thenReturn(orderEntity);
            when(orderFactory.buildOrderEvent(any(OrderEntity.class)))
                    .thenReturn(orderEvent);
            PaymentIntent mockResponse = new PaymentIntent();
            mockResponse.setId("pi_123");
            mockResponse.setStatus("succeeded");
            when(stripeGateway.createPayment(any(BigDecimal.class), any(CardDetailsDto.class)))
                    .thenReturn(mockResponse);

            SendResult<String, OrderBuyProductEventDto> stockResult = mock(SendResult.class);
            when(kafkaTemplateBuyProduct.send(eq("topic-stock-order"), any(OrderBuyProductEventDto.class)))
                    .thenReturn(CompletableFuture.completedFuture(stockResult));

            SendResult<String, OrderNotificationEventDto> notifResult = mock(SendResult.class);
            when(kafkaTemplateNotification.send(eq("topic-notification-order"), any(OrderNotificationEventDto.class)))
                    .thenReturn(CompletableFuture.completedFuture(notifResult));

            when(repository.save(any(OrderEntity.class)))
                    .thenReturn(orderEntity);

            var order = orderService.createOrder(BuildFactory.orderReqBuild());

            assertNotNull(order);
        }

        @Test
        @DisplayName("Should throw Product Out Of Stock Ex")
        void shouldThrowProductOutOfStockException() {
            when(stockApiClient.getProductInfo("Phone", 1)).thenReturn(Optional.empty());
            var request = BuildFactory.orderReqBuild();
            assertThrows(ProductOutOfStockException.class, () -> orderService.createOrder(request));
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
            var request = BuildFactory.orderReqBuild();
            assertThrows(ProductOutOfStockException.class, () -> orderService.createOrder(request));
        }
    }
}