package com.tech.ian.order.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.YearMonth;

public record CardDetailsDto(String cardNumber, @JsonFormat(pattern = "MM/yy") YearMonth expDate, String securityCode, String nameOnCard) {
}
