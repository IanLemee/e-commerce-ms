package com.tech.ian.user.model.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @NotBlank
        String email,
        @NotBlank
        String password) {
}
