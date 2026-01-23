package com.tech.ian.stock.service;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.stock.model.StockEntity;
import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.config.kafka.dto.StockBuyProductDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import com.tech.ian.stock.repository.StockRepository;
import com.tech.ian.stock.utils.StockFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockFactory stockFactory;

    @CachePut(value = "StockCache", key = "#result.product()")
    public StockCreateDto create(StockCreateDto request) {
        StockEntity stockToSave = getProduct(request.product())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.quantity());
                    return existing;
                })
                .orElseGet(() -> stockFactory.buildStockEntity(request.product(), request.quantity(), request.price()));

        stockRepository.save(stockToSave);

        return stockFactory.buildStockCreate(stockToSave.getProduct(), stockToSave.getQuantity(), stockToSave.getPrice());
    }

    @Transactional
    @CacheEvict(value = "StockCache", key = "#result.product()")
    @KafkaListener(topics = "topic-stock-order", groupId = "stock-group")
    public void buyProduct(StockBuyProductDto request) {
        StockEntity stock = stockRepository.findByProductAndUpdate(request.product(), request.quantity()).orElseThrow(ProductOutOfStockException::new);

        stock.setQuantity(stock.getQuantity() - request.quantity());
        stockRepository.save(stock);
    }

    @Cacheable(value = "StockCache", key = "#request")
    public StockResponseDto getStockInfo(StockBuyProductDto request) {
        StockEntity stockEntity = getProduct(request.product()).orElseThrow(ProductOutOfStockException::new);
        return new StockResponseDto(stockEntity.getProduct(), stockEntity.getPrice());
    }

    public Optional<StockEntity> getProduct(String request) {
        return stockRepository.findByProduct(request);
    }
}
