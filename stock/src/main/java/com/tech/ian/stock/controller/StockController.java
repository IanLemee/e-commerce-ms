package com.tech.ian.stock.controller;

import com.tech.ian.stock.model.dto.StockCreateDto;
import com.tech.ian.stock.model.dto.StockRequestDto;
import com.tech.ian.stock.model.dto.StockResponseDto;
import com.tech.ian.stock.service.StockService;
import jakarta.websocket.server.PathParam;
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

    @PutMapping
    public ResponseEntity<StockResponseDto> buyProduct(@RequestBody StockRequestDto request) {
        StockResponseDto response = stockService.buyProduct(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getProduct")
    public ResponseEntity<StockResponseDto> getProductInfo(@RequestParam("product") String product, @RequestParam("quantity") int quantity) {
        StockRequestDto request = new StockRequestDto(product, quantity);

        StockResponseDto stockInfo = stockService.getStockInfo(request);
        return new ResponseEntity<>(stockInfo, HttpStatus.OK);
    }
}
