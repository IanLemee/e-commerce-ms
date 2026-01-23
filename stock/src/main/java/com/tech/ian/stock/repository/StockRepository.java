package com.tech.ian.stock.repository;

import com.tech.ian.stock.model.StockEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByProduct(String product);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockEntity s where s.product = :product and s.quantity >= :quantity")
    Optional<StockEntity> findByProductAndUpdate(String product, int quantity);
}
