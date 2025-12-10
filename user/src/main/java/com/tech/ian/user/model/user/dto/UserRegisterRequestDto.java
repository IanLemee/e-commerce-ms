package com.tech.ian.user.model.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UserRegisterRequestDto(
        @NotBlank @Size(min = 2, max = 64)
        String name,
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 11, max = 30)
        String password
) {
}
