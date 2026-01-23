package com.tech.ian.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "order")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {

    @MongoId
    private String id;
    @Indexed(name = "customer_id")
    private String customerId;
    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;
    private LocalDateTime timestamp;
    private OrderItem item;
}
