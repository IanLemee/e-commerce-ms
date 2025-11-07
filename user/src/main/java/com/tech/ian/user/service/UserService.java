package com.tech.ian.user.service;

import com.tech.ian.user.config.dto.UserEmailVerificationDto;
import com.tech.ian.user.exception.exceptions.UserAlreadyEnabledException;
import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.exception.exceptions.WrongVerificationCodeException;
import com.tech.ian.user.model.Role;
import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.UserRegisterRequestDto;
import com.tech.ian.user.model.UserRegisterResponseDto;
import com.tech.ian.user.repo.UserRepository;
import com.tech.ian.user.utils.Mapper;
import com.tech.ian.user.utils.VerificationCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeGenerator codeGenerator;
    private final KafkaTemplate<String, UserEmailVerificationDto> kafkaTemplate;
    private final S3Client client;
    private final S3Properties s3Properties;

    public UserService(UserRepository userRepository, Mapper mapper, PasswordEncoder passwordEncoder, VerificationCodeGenerator codeGenerator, KafkaTemplate<String, UserEmailVerificationDto> kafkaTemplate) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.codeGenerator = codeGenerator;
        this.kafkaTemplate = kafkaTemplate;
        this.client = client;
        this.s3Properties = s3Properties;
    }

    public UserRegisterResponseDto create(UserRegisterRequestDto req) {
        if (getUserEntity(req.email()).isPresent()) {
            throw new UserAlreadyExistException();
        }
        String imageUrl = uploadFile(req.profilePicture());
        UserEntity user = buildUserEntity(req, imageUrl);
        userRepository.save(user);

        sendEvent(user);

        return mapper.mapEntityToRegistry(user);
    }

    private UserEntity buildUserEntity(UserRegisterRequestDto req, String url) {
        String encode = passwordEncoder.encode(req.password());
        return UserEntity.builder()
                .uuid(UUID.randomUUID())
                .name(req.name())
                .email(req.email().toLowerCase())
                .password(encode)
                .profilePicture(url)
                .role(Role.USER)
                .isEnabled(false)
                .verificationCode(codeGenerator.generate())
                .build();
    }

    public void activeAccount(String email, int code) {
        UserEntity userEntity = getUserEntity(email).get();
        if (userEntity.getVerificationCode() == code) {
            userEntity.setEnabled(true);
            userEntity.setVerificationCode(0);
            userRepository.save(userEntity);
        } else if (userEntity.getVerificationCode() == 0) {
            throw new UserAlreadyEnabledException();
        } else {
            throw new WrongVerificationCodeException();
        }
    }

    public Optional<UserEntity> getUserEntity(String email) {
        return userRepository.findUserByEmail(email.toLowerCase());
    }

    private void sendEvent(UserEntity user) {
        UserEmailVerificationDto emailDto = new UserEmailVerificationDto(user.getEmail(), user.getVerificationCode());
        kafkaTemplate.send("email-verification-topic", emailDto);
    }
}
