package com.tech.ian.user.service;

import com.tech.ian.user.commons.Factory;
import com.tech.ian.user.config.dto.S3UploadSuccessEvent;
import com.tech.ian.user.config.dto.UserEmailVerificationDto;
import com.tech.ian.user.exception.exceptions.UserAlreadyEnabledException;
import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.exception.exceptions.WrongVerificationCodeException;
import com.tech.ian.user.model.user.UserEntity;
import com.tech.ian.user.repo.UserRepository;
import com.tech.ian.user.utils.Mapper;
import com.tech.ian.user.utils.VerificationCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;
    @Mock
    private Mapper mapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private VerificationCodeGenerator codeGenerator;
    @Mock
    private S3UploadService s3UploadService;
    @Mock
    private KafkaTemplate<String, UserEmailVerificationDto> template;
    @Mock
    private VerificationFailureService verificationFailureService;


    @InjectMocks
    private UserService service;

    private Factory factory;

    @BeforeEach
    void setup() {
        factory = new Factory();
    }

    @Nested
    class create {
        @Test
        void shouldCreateAUserWhenSuccessful() {
            var expected = factory.userResponseFactory();
            when(repository.save(any(UserEntity.class))).thenReturn(factory.userEntityFactory());
            when(mapper.mapEntityToRegistry(any(UserEntity.class))).thenReturn(expected);
            when(passwordEncoder.encode(anyString())).thenReturn("12345678901");
            when(codeGenerator.generate()).thenReturn(123456);
            doNothing().when(s3UploadService).uploadFileAsync(anyString(), any(byte[].class), anyString(), anyString());
            when(template.send(anyString(), any(UserEmailVerificationDto.class))).thenReturn(new CompletableFuture<>());

            var response = service.create(factory.userRequestFactory(), factory.file());
            assertNotNull(response);
            assertEquals(expected, response);
        }

        @Test
        void shouldThrowAnErrorWhenEmailAlreadyExists() {
            when(repository.findUserByEmail(factory.userRequestFactory().email())).thenReturn(Optional.of(factory.userEntityFactory()));

            assertThrows(UserAlreadyExistException.class, () -> service.create(factory.userRequestFactory(), factory.file()));
            verify(repository, never()).save(any(UserEntity.class));
            verify(s3UploadService, never()).uploadFileAsync(anyString(), any(), anyString(), anyString());
            verify(template, never()).send(anyString(), any(UserEmailVerificationDto.class));
            verify(mapper, never()).mapEntityToRegistry(any(UserEntity.class));
        }
    }

    @Nested
    class enableAccount {
        @Test
        void shouldActiveAnAccountWhenSuccessful() {
            var user = factory.userEntityFactory();
            when(repository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
            service.enableAccount(user.getEmail(), user.getVerificationCode());
            assertEquals(0, user.getVerificationCode());
            verify(repository, times(1)).save(user);
        }

        @Test
        void shouldThrowUserAlreadyEnabledExceptionWhenAccountIsAlreadyActivated() {
            var user = factory.userEntityFactory();
            var verificationCode = user.getVerificationCode();
            user.setVerificationCode(0);
            when(repository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
            assertThrows(UserAlreadyEnabledException.class, () -> service.enableAccount(user.getEmail(), verificationCode));
            verify(repository, never()).save(any(UserEntity.class));
        }

        @Test
        void shouldThrowWrongVerificationCodeExceptionWhenVerificationCodeIsDifferentFromCodeAccount() {
            UserEntity user = factory.userEntityFactory();
            int code = 332813;
            when(repository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
            assertThrows(WrongVerificationCodeException.class, () -> service.enableAccount(user.getEmail(), code));
            verify(repository, never()).save(any(UserEntity.class));
        }
    }

    @Nested
    class getUserEntity {
        @Test
        void shouldReturnAnEmptyObjectWhenEmailDoesntExist() {
            when(repository.findUserByEmail(factory.userEntityFactory().getEmail())).thenReturn(Optional.empty());
            var optionalUser = service.getUserEntity(factory.userEntityFactory().getEmail());
            assertTrue(optionalUser.isEmpty());
        }

        @Test
        void shouldReturnAnUserObjectWhenSuccessful() {
            var expected = factory.userEntityFactory();
            when(repository.findUserByEmail(expected.getEmail())).thenReturn(Optional.of(expected));
            var optionalUser = service.getUserEntity(expected.getEmail());
            assertEquals(expected, optionalUser.get());
        }
    }

    @Nested
    class sendEvent {
        @Test
        void shouldSendAnEventWhenSuccessful() {
            var user = factory.userEntityFactory();
            var dto = new UserEmailVerificationDto(user.getEmail(), user.getVerificationCode());
            when(template.send("email-verification-topic", dto)).thenReturn(new CompletableFuture<>());

            assertDoesNotThrow(() -> service.sendEvent(user));
            verify(template, times(1)).send("email-verification-topic", dto);
        }

        @Test
        void shouldThrowAnExceptionWhenEventWentWrong() {
            var user = factory.userEntityFactory();
            var dto = new UserEmailVerificationDto(user.getEmail(), user.getVerificationCode());
            RuntimeException kafkaException = new RuntimeException("Kafka is down!");
            CompletableFuture<SendResult<String, UserEmailVerificationDto>> failedFuture = CompletableFuture.failedFuture(kafkaException);

            when(template.send(eq("email-verification-topic"), any(UserEmailVerificationDto.class)))
                    .thenReturn(failedFuture);
            doNothing().when(verificationFailureService).create(dto.email(), dto.code());
            service.sendEvent(user);
            verify(verificationFailureService, times(1)).create(dto.email(), dto.code());
        }
    }

    @Nested
    class handleUploadSuccess {
        @Test
        void shouldHandleUploadWhenSuccessful() {
            var user = factory.userEntityFactory();
            var event = new S3UploadSuccessEvent(user.getEmail(), "Url");
            when(repository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
            
            assertDoesNotThrow(() -> service.handleUploadSuccess(event));
            verify(repository, times(1)).save(user);
        }
        @Test
        void shouldThrowAnException() {
            var user = factory.userEntityFactory();
            var event = new S3UploadSuccessEvent(user.getEmail(), "Url");
            when(repository.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,() -> service.handleUploadSuccess(event));
        }
    }
}