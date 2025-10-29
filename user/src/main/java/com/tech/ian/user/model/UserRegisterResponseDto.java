package com.tech.ian.user.model;

import java.util.UUID;

public record UserRegisterResponseDto(
        UUID uuid,
        String name,
        String email,
        String profilePicture,
        Role role
) {
}
