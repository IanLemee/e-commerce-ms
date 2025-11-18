package com.tech.ian.user.model.dto;

import com.tech.ian.user.model.Role;

import java.util.UUID;

public record UserRegisterResponseDto(
        UUID uuid,
        String name,
        String email,
        String profilePicture,
        Role role
) {
}
