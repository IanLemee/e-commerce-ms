package com.tech.ian.order.repository;

import com.tech.ian.order.model.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {

    Optional<OrderEntity> findByIdAndCustomerId(String id, String customerId);

    Page<OrderEntity> findAllByCustomerId(String customerId, Pageable pageable);
}
