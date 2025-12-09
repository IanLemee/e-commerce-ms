package com.tech.ian.user.model.user.dto;

import com.tech.ian.user.model.user.Role;

import java.util.UUID;

public record UserRegisterResponseDto(
        UUID uuid,
        String name,
        String email,
        String profilePicture,
        Role role
) {
}
