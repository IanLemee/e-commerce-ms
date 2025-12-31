package com.tech.ian.order.controller;

import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.OrderResponseDto;
import com.tech.ian.order.model.dto.ProductResponseDto;
import com.tech.ian.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/order")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto request) {
        OrderResponseDto order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ProductResponseDto> getProduct(@RequestParam String product) {
        ProductResponseDto response = orderService.getProduct(product);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
