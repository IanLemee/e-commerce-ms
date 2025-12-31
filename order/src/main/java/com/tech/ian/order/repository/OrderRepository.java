package com.tech.ian.order.repository;

import com.tech.ian.order.model.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {

    Optional<OrderEntity> findByIdAndCustomerId(Long id, String customerId);
}
