package com.tech.ian.stock.controller;

import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.config.kafka.dto.StockBuyProductDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import com.tech.ian.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/stock")
@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<StockCreateDto> create(@RequestBody StockCreateDto stockCreateDto) {
        StockCreateDto response = stockService.create(stockCreateDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getProduct")
    public ResponseEntity<StockResponseDto> getProductInfo(@RequestParam("product") String product, @RequestParam("quantity") int quantity) {
        StockBuyProductDto request = new StockBuyProductDto(product, quantity);

        StockResponseDto stockInfo = stockService.getStockInfo(request);
        return new ResponseEntity<>(stockInfo, HttpStatus.OK);
    }
}
