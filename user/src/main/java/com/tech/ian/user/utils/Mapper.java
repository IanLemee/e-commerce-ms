package com.tech.ian.user.utils;

import com.tech.ian.user.model.user.UserEntity;
import com.tech.ian.user.model.user.dto.UserRegisterResponseDto;

@org.mapstruct.Mapper(componentModel = "spring")
public interface Mapper {
    UserRegisterResponseDto mapEntityToRegistry(UserEntity user);
}
