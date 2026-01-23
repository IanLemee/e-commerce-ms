package com.tech.ian.order.client;

import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "Stock-service", url = "${stock-service.url}")
@Component
public interface StockApiClient {

    @GetMapping(value = "/getProduct")
    Optional<ProductResponseDto> getProductInfo(@RequestParam String product, @RequestParam(defaultValue = "1") int quantity);
}
