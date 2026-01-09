package com.tech.ian.stock.commons;

import com.tech.ian.stock.model.StockEntity;
import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.model.dto.StockRequestDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Factory {

    public static StockEntity stockEntity() {
        return new StockEntity(1L, "Phone", 100, BigDecimal.valueOf(1000.0));
    }

    public static StockCreateDto stockCreate() {
        return new StockCreateDto("Phone", 100, BigDecimal.valueOf(1000.0));
    }

    public static StockRequestDto stockRequest() {
        return new StockRequestDto("Phone", 4);
    }

    public static StockResponseDto stockResponse() {
        return new StockResponseDto("Phone", BigDecimal.valueOf(1000.0));
    }
}
