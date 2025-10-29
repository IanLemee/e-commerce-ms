package com.tech.ian.user.utils;

import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.UserRegisterRequestDto;
import com.tech.ian.user.model.UserRegisterResponseDto;

@org.mapstruct.Mapper
public interface Mapper {

    UserEntity mapRegistryToEntity(UserRegisterRequestDto userRegisterRequestDto);

    UserRegisterResponseDto mapEntityToRegistry(UserEntity user);
}
