package com.tech.ian.user.service;

import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.model.Role;
import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.UserRegisterRequestDto;
import com.tech.ian.user.model.UserRegisterResponseDto;
import com.tech.ian.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tech.ian.user.utils.Mapper;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, Mapper mapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserRegisterResponseDto create(UserRegisterRequestDto req) throws Exception {

        if (getUserEntity(req).isPresent()) {
            throw new UserAlreadyExistException();
        }
        UserEntity user = mapper.mapRegistryToEntity(req);
        user.setUuid(UUID.randomUUID());
        user.setEmail(req.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        mapper.mapEntityToRegistry(user);
        return null;
    }

    private Optional<UserEntity> getUserEntity(UserRegisterRequestDto req) {
        return userRepository.loadUserByUsername(req.email().toLowerCase());
    }
}
