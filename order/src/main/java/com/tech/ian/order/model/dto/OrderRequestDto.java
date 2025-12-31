package com.tech.ian.order.model.dto;

public record OrderRequestDto(String product, int quantity, CardDetailsDto cardDetails) {
}
