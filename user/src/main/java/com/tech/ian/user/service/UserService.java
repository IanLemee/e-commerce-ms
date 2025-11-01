package com.tech.ian.user.service;

import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.model.Role;
import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.UserRegisterRequestDto;
import com.tech.ian.user.model.UserRegisterResponseDto;
import com.tech.ian.user.repo.UserRepository;
import com.tech.ian.user.utils.Mapper;
import com.tech.ian.user.utils.VerificationCodeGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeGenerator codeGenerator;

    public UserService(UserRepository userRepository, Mapper mapper, PasswordEncoder passwordEncoder, VerificationCodeGenerator codeGenerator) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
    }

    public UserRegisterResponseDto create(UserRegisterRequestDto req) {
        if (getUserEntity(req.email()).isPresent()) {
            throw new UserAlreadyExistException();
        }
        UserEntity user = mapper.mapRegistryToEntity(req);
        user.setUuid(UUID.randomUUID());
        user.setEmail(req.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.USER);
        user.setEnabled(false);
        user.setVerificationCode(codeGenerator.generate());
        userRepository.save(user);
        return mapper.mapEntityToRegistry(user);
    }

    public Optional<UserEntity> getUserEntity(String email) {
        return userRepository.findUserByEmail(email.toLowerCase());
    }
}
