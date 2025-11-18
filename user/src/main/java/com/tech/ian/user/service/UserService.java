package com.tech.ian.user.service;

import com.tech.ian.user.config.dto.S3UploadSuccessEvent;
import com.tech.ian.user.config.dto.UserEmailVerificationDto;
import com.tech.ian.user.exception.exceptions.UserAlreadyEnabledException;
import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.exception.exceptions.WrongVerificationCodeException;
import com.tech.ian.user.model.Role;
import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.dto.UserRegisterRequestDto;
import com.tech.ian.user.model.dto.UserRegisterResponseDto;
import com.tech.ian.user.repo.UserRepository;
import com.tech.ian.user.utils.Mapper;
import com.tech.ian.user.utils.VerificationCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeGenerator codeGenerator;
    private final S3UploadService s3UploadService;
    @Qualifier("kafkaTemplateVerification")
    private final KafkaTemplate<String, UserEmailVerificationDto> templateVerification;

    public UserService(UserRepository userRepository, Mapper mapper, PasswordEncoder passwordEncoder, VerificationCodeGenerator codeGenerator, S3UploadService s3UploadService, KafkaTemplate<String, UserEmailVerificationDto> templateVerification) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
        this.s3UploadService = s3UploadService;
        this.templateVerification = templateVerification;
    }

    public UserRegisterResponseDto create(UserRegisterRequestDto req) {
        log.info("Starting user registration for email={}", req.email());
        if (getUserEntity(req.email()).isPresent()) {
            log.warn("Registration aborted - user with email={} already exists", req.email());
            throw new UserAlreadyExistException();
        }

        UserEntity user = buildUserEntity(req);
        userRepository.save(user);
        log.info("User saved successfuly - id={}, email={}", user.getId(), user.getEmail());

        String key = req.profilePicture().getOriginalFilename();
        byte[] data;
        try {
            data = req.profilePicture().getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String contentType = req.profilePicture().getContentType();
        log.info("Uploading profile picture for email={}", user.getEmail());
        s3UploadService.uploadFileAsync(key, data, contentType, user.getEmail());

        sendEvent(user);

        log.info("Registration flow completed for email=${}", user.getEmail());
        return mapper.mapEntityToRegistry(user);
    }

    private UserEntity buildUserEntity(UserRegisterRequestDto req) {
        log.debug("Encoding password for email={}", req.email());
        String encode = passwordEncoder.encode(req.password());
        log.debug("Building user entity for email={}", req.email());
        return UserEntity.builder()
                .uuid(UUID.randomUUID())
                .name(req.name())
                .email(req.email().toLowerCase())
                .password(encode)
                .profilePicture(null)
                .role(Role.USER)
                .isEnabled(false)
                .verificationCode(codeGenerator.generate())
                .build();
    }

    public void activeAccount(String email, int code) {
        getUserEntity(email).ifPresent(user ->
        {
            if (user.getVerificationCode() == code) {
                log.info("Activating account for email={}", email);
                user.setEnabled(true);
                user.setVerificationCode(0);
                userRepository.save(user);
            } else if (user.getVerificationCode() == 0) {
                log.warn("Account already activated for email={}", email);
                throw new UserAlreadyEnabledException();
            } else {
                log.warn("Wrong verification code for code={}", code);
                throw new WrongVerificationCodeException();
            }
        });

    }

    public Optional<UserEntity> getUserEntity(String email) {
        log.info("Finding user by email={}", email);
        return userRepository.findUserByEmail(email.toLowerCase());
    }

    public void sendEvent(UserEntity user) {
        UserEmailVerificationDto emailDto = new UserEmailVerificationDto(user.getEmail(), user.getVerificationCode());
        log.info("Sending verification email event for email={}", user.getEmail());

        templateVerification.send("email-verification-topic", emailDto)
                .whenComplete(((result, throwable) -> {
                    if (throwable == null) {
                        log.info("Kafka event sent successfully — topic={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send Kafka event for email={}: {}", user.getEmail(), throwable.getMessage());
                    }
                }));
    }

    @KafkaListener(topics = "s3-success-topic", groupId = "user-service-group")
    public void handleUploadSuccess(S3UploadSuccessEvent event) {
        log.info("Saving photo url for email={}", event.email());
        getUserEntity(event.email()).ifPresent(user -> {
            user.setProfilePicture(event.url());
            userRepository.save(user);
        });
    }
}
