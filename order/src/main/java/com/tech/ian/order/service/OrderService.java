package com.tech.ian.order.service;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.order.client.StockApiClient;
import com.tech.ian.order.config.kafka.dto.OrderEventDto;
import com.tech.ian.order.config.kafka.dto.OrderSendEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.OrderResponseDto;
import com.tech.ian.order.model.dto.ProductResponseDto;
import com.tech.ian.order.repository.OrderRepository;
import com.tech.ian.order.utils.OrderFactory;
import com.tech.ian.order.utils.UserContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    private final StockApiClient stockApiClient;

    private final KafkaTemplate<String, OrderSendEventDto> kafkaTemplate;
    private final OrderFactory orderFactory;
    private final UserContext userContext;

    @Retry(name = "order-service", fallbackMethod = "createOrderFallback")
    @CircuitBreaker(name = "order-service", fallbackMethod = "createOrderFallback")
    public OrderResponseDto createOrder(OrderRequestDto req) {
        ProductResponseDto productResponseDto = getProduct(req.product(), req.quantity());

        String userId = userContext.getCurrentUserId();
        BigDecimal totalPrice = productResponseDto.price().multiply(BigDecimal.valueOf(req.quantity()));

        var buildOrder = orderFactory.buildOrder(req, userId, productResponseDto.price(), totalPrice);
        repository.save(buildOrder);

        CardDetailsDto cardDetails = req.cardDetails();
        sendEvent(orderFactory.buildOrderEvent(cardDetails, buildOrder));

        return new OrderResponseDto(buildOrder.getItem().getProduct(), buildOrder.getItem().getQuantity(), buildOrder.getItem().getPrice(), buildOrder.getTotalPrice(), buildOrder.getPaymentStatus());
    }

    public OrderResponseDto createOrderFallback(OrderRequestDto req, Throwable e) {
        return new OrderResponseDto(
                req.product(),
                req.quantity(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                PaymentStatus.DENIED
        );
    }

    @Retry(name = "order-service", fallbackMethod = "getProductFallback")
    @CircuitBreaker(name = "order-service", fallbackMethod = "getProductFallback")
    public ProductResponseDto getProduct(String product, int quantity) {
        Optional<ProductResponseDto> response = stockApiClient.getProductInfo(product, quantity);

        return response.orElseThrow(ProductOutOfStockException::new);
    }

    public ProductResponseDto getProduct(String product) {
        return getProduct(product, 1);
    }

    public ProductResponseDto getProductFallback(String product, Throwable e) {
        return new ProductResponseDto("Produto Indisponível (Offline)", BigDecimal.ZERO);
    }

    private void sendEvent(OrderSendEventDto orderSendEventDto) {
        kafkaTemplate.send("topic-payment-processor", orderSendEventDto);
    }

    @KafkaListener(topics = "order-payment-topic", groupId = "order-service-group")
    private void refreshPaymentStatus(OrderEventDto eventDto) {
        Optional<OrderEntity> byIdAndCustomerId = repository.findByIdAndCustomerId(eventDto.id(), eventDto.customerId());

        byIdAndCustomerId.ifPresent(order -> {
            order.setPaymentStatus(eventDto.paymentStatus());
            repository.save(order);
        });

    }
}
