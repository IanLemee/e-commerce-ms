package com.tech.ian.stock.service;

import com.tech.ian.exception.ProductOutOfStockException;
import com.tech.ian.stock.commons.Factory;
import com.tech.ian.stock.model.StockEntity;
import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.config.kafka.dto.StockBuyProductDto;
import com.tech.ian.stock.repository.StockRepository;
import com.tech.ian.stock.utils.StockFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockFactory stockFactory;

    @InjectMocks
    private StockService service;

    @Nested
    class create {
        @Test
        @DisplayName("should create a new product when successful")
        void shouldCreateANewProductWhenSuccessful() {
            when(stockRepository.findByProduct("Phone")).thenReturn(Optional.empty());
            var entity = Factory.stockEntity();
            when(stockRepository.save(entity)).thenReturn(entity);
            when(stockFactory.buildStockEntity(entity.getProduct(), entity.getQuantity(), entity.getPrice())).thenReturn(entity);
            when(stockFactory.buildStockCreate("Phone", 100, BigDecimal.valueOf(1000.0))).thenReturn(Factory.stockCreate());

            var stockCreateDto = service.create(Factory.stockCreate());
            assertNotNull(stockCreateDto);
        }

        @Test
        @DisplayName("should update a product when product already exist")
        void shouldUpdateAProductWhenProductAlreadyExists() {
            var entity = Factory.stockEntity();
            var request = Factory.stockCreate();
            var expectedFinalQuantity = entity.getQuantity() + request.quantity();

            when(stockRepository.findByProduct("Phone")).thenReturn(Optional.of(entity));
            when(stockRepository.save(entity)).thenReturn(entity);
            when(stockFactory.buildStockCreate(entity.getProduct(), expectedFinalQuantity, entity.getPrice()))
                    .thenReturn(new StockCreateDto(entity.getProduct(), expectedFinalQuantity,entity.getPrice()));

            var result = service.create(request);
            assertNotNull(result);
            assertEquals(expectedFinalQuantity, result.quantity());
        }
    }

    @Nested
    class buyProduct{
        @Test
        void shouldDecreaseStockQuantity_WhenPurchaseIsSuccessful() {
            ArgumentCaptor<StockEntity> captor = ArgumentCaptor.forClass(StockEntity.class);
            var entity = Factory.stockEntity();
            var request = Factory.stockRequest();
            var expectedQuantity = entity.getQuantity() - request.quantity();
            when(stockRepository.findByProductAndUpdate("Phone", 4)).thenReturn(Optional.of(entity));
            when(stockRepository.save(entity)).thenReturn(entity);
            service.buyProduct(request);
            verify(stockRepository).save(captor.capture());
            var value = captor.getValue();

            assertEquals(expectedQuantity, value.getQuantity());
        }

        @Test
        void shouldThrowException_WhenProductDoesNotExist() {
            when(stockRepository.findByProductAndUpdate("Phone", 4)).thenReturn(Optional.empty());
            var request = Factory.stockRequest();
            var productOutOfStockException = assertThrows(ProductOutOfStockException.class, () -> service.buyProduct(request));
            var expectedMessage = "Product out of stock";
            var actualMessage = productOutOfStockException.getMessage();
            assertEquals(expectedMessage, actualMessage);
        }

        @Test
        void shouldThrowException_WhenRequestedQuantityExceedsAvailableStock(){
            when(stockRepository.findByProductAndUpdate("Phone", 103)).thenReturn(Optional.empty());
            var request = new StockBuyProductDto("Phone", 103);
            var productOutOfStockException = assertThrows(ProductOutOfStockException.class, () -> service.buyProduct(request));
            var expectedMessage = "Product out of stock";
            var actualMessage = productOutOfStockException.getMessage();
            assertEquals(expectedMessage, actualMessage);
        }
    }

    @Nested
    class getStockInfo {
        @Test
        void shouldThrowException_WhenProductDoesNotExist() {
            when(stockRepository.findByProduct(anyString())).thenReturn(Optional.empty());
            var request = Factory.stockRequest();
            var productOutOfStockException = assertThrows(ProductOutOfStockException.class, () -> service.getStockInfo(request));
            var expectedMessage = "Product out of stock";
            var actualMessage = productOutOfStockException.getMessage();
            assertEquals(expectedMessage, actualMessage);
        }

        @Test
        void shouldReturnProduct_WhenSuccessful() {
            var entity = Factory.stockEntity();
            var request = Factory.stockRequest();
            when(stockRepository.findByProduct(anyString())).thenReturn(Optional.of(entity));
            var stockInfo = service.getStockInfo(request);
            assertNotNull(stockInfo);
            var expected = Factory.stockResponse();
            assertEquals(expected, stockInfo);
        }
    }

}