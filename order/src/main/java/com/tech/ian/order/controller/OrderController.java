package com.tech.ian.order.controller;

import com.tech.ian.order.model.OrderEntity;
import com.tech.ian.order.model.dto.OrderRequestDto;
import com.tech.ian.order.model.dto.OrderResponseDto;
import com.tech.ian.order.model.dto.ProductResponseDto;
import com.tech.ian.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/order")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("create")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto request) {
        OrderResponseDto order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/info")
    public ResponseEntity<ProductResponseDto> getProduct(@RequestParam String product) {
        ProductResponseDto response = orderService.getProduct(product);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderEntity>> findAll(Pageable pageable) {
        Page<OrderEntity> allOrders = orderService.findAllOrders(pageable);
        return new ResponseEntity<>(allOrders, HttpStatus.OK);
    }
}
