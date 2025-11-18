package com.tech.ian.user.utils;

import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.dto.UserRegisterResponseDto;

@org.mapstruct.Mapper(componentModel = "spring")
public interface Mapper {
    UserRegisterResponseDto mapEntityToRegistry(UserEntity user);
}
