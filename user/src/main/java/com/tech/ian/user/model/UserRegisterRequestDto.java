package com.tech.ian.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
        @NotNull @NotBlank
        @Size(min = 2, max = 64)
        String name,
        @NotNull @NotBlank
        @Email
        String email,
        @NotNull @NotBlank
        @Size(min = 11, max = 30)
        String password,
        String profilePicture,
        Role role
) {
}
