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

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockFactory stockFactory;

    @Transactional
    @CachePut(value = "StockCache", key = "#result.product()")
    public StockCreateDto create(StockCreateDto request) {
        StockEntity stockToSave = stockRepository.findByProduct(request.product())
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
        StockEntity stock = stockRepository.findByProduct(request.product()).orElseThrow();

        if (request.quantity() > stock.getQuantity()) {
                        throw new ProductOutOfStockException("There's no sufficent items");
        }

        stock.setQuantity(stock.getQuantity() - request.quantity());
        stockRepository.save(stock);

        return stockFactory.buildStockResponse(stock.getProduct(), stock.getPrice());
    }

    @Transactional
    @Cacheable(value = "StockCache", key = "#request")
    public StockResponseDto getStockInfo(StockRequestDto request) {
        StockEntity stockEntity = stockRepository.findByProduct(request.product()).orElseThrow(RuntimeException::new);
        return new StockResponseDto(stockEntity.getProduct(), stockEntity.getPrice());
    }
}
