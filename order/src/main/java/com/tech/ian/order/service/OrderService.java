package com.tech.ian.order.service;

import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.order.client.StockApiClient;
import com.tech.ian.order.config.kafka.dto.OrderBuyProductEventDto;
import com.tech.ian.order.config.kafka.dto.OrderNotificationEventDto;
import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.PaymentStatus;
import com.tech.ian.order.model.dto.CardDetailsDto;
import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.OrderResponseDto;
import com.tech.ian.order.model.dto.ProductResponseDto;
import com.tech.ian.order.repository.OrderRepository;
import com.tech.ian.order.utils.OrderFactory;
import com.tech.ian.order.utils.StripeGateway;
import com.tech.ian.order.utils.UserContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    private final StockApiClient stockApiClient;

    private final KafkaTemplate<String, OrderNotificationEventDto> kafkaTemplateNotification;
    private final KafkaTemplate<String, OrderBuyProductEventDto> kafkaTemplateBuyProduct;
    private final OrderFactory orderFactory;
    private final UserContext userContext;
    private final StripeGateway stripeGateway;

    @Retry(name = "order-service", fallbackMethod = "createOrderFallback")
    @CircuitBreaker(name = "order-service", fallbackMethod = "createOrderFallback")
    public OrderResponseDto createOrder(OrderRequestDto req) {
        ProductResponseDto productResponseDto = getProduct(req.product(), req.quantity());

        String userId = userContext.getCurrentUserId();
        BigDecimal totalPrice = productResponseDto.price().multiply(BigDecimal.valueOf(req.quantity()));

        CardDetailsDto cardDetails = req.cardDetails();
        OrderEntity buildOrder = orderFactory.buildOrder(req, userId, productResponseDto.price(), totalPrice);

        try {
            PaymentIntent paymentIntent = stripeGateway.createPayment(totalPrice, cardDetails);
            if ("succeeded".equals(paymentIntent.getStatus())) {
                buildOrder.setPaymentStatus(PaymentStatus.APPROVED);
            }
        } catch (CardException e) {
            // TODO
            // CREATE retrys for cardException depending on the reason
            throw new RuntimeException(e.getMessage());
        } catch (StripeException e) {
            throw new RuntimeException(e.getMessage());
        }

        OrderBuyProductEventDto orderBuyProductEventDto = new OrderBuyProductEventDto(buildOrder.getItem().getProduct(), buildOrder.getItem().getQuantity());

        try{
            sendBuyProductEvent(orderBuyProductEventDto).join();
        } catch (CompletionException e) {
            throw new RuntimeException(e.getCause().getMessage());
        }

        OrderEntity saved = repository.save(buildOrder);
        sendNotificationEvent(orderFactory.buildOrderEvent(saved));

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
        return new ProductResponseDto(product, BigDecimal.ZERO);
    }

    public Page<OrderEntity> findAllOrders(Pageable pageable) {
        return repository.findAllByCustomerId(userContext.getCurrentUserId(), pageable);
    }

    private void sendNotificationEvent(OrderNotificationEventDto orderNotificationEventDto) {
        kafkaTemplateNotification.send("topic-notification-order", orderNotificationEventDto);
    }

    private CompletableFuture<?> sendBuyProductEvent(OrderBuyProductEventDto orderBuyProductEventDto) {
        return kafkaTemplateBuyProduct.send("topic-stock-order", orderBuyProductEventDto);
    }
}
