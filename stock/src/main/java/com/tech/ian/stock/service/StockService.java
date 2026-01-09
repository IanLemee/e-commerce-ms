package com.tech.ian.stock.service;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.stock.model.StockEntity;
import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.model.dto.StockRequestDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import com.tech.ian.stock.repository.StockRepository;
import com.tech.ian.stock.utils.StockFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockFactory stockFactory;

    @Transactional
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
    public StockResponseDto buyProduct(StockRequestDto request) {
        StockEntity stock = getProduct(request.product()).orElseThrow(ProductOutOfStockException::new);

        if (request.quantity() > stock.getQuantity()) {
            throw new ProductOutOfStockException("Not enough items");
        }

        stock.setQuantity(stock.getQuantity() - request.quantity());
        stockRepository.save(stock);

        return stockFactory.buildStockResponse(stock.getProduct(), stock.getPrice());
    }

    @Transactional
    @Cacheable(value = "StockCache", key = "#request")
    public StockResponseDto getStockInfo(StockRequestDto request) {
        StockEntity stockEntity = getProduct(request.product()).orElseThrow(ProductOutOfStockException::new);
        return new StockResponseDto(stockEntity.getProduct(), stockEntity.getPrice());
    }

    public Optional<StockEntity> getProduct(String request) {
        return stockRepository.findByProduct(request);
    }
}
