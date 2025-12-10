package com.tech.ian.user.commons;

import com.tech.ian.user.model.user.Role;
import com.tech.ian.user.model.user.UserEntity;
import com.tech.ian.user.model.user.dto.UserRegisterRequestDto;
import com.tech.ian.user.model.user.dto.UserRegisterResponseDto;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
@Component
public class Factory {

    public MockMultipartFile filePhotoFactory() {
        return new MockMultipartFile("file", "photo.png", "image/png", new byte[0]);
    }

    public MockMultipartFile fileJsonFactory(byte[] bytes) {
        return new MockMultipartFile("data", "data.json", "application/json", bytes);
    }

    public UserRegisterResponseDto respFactory() {
        return new UserRegisterResponseDto(UUID.randomUUID(), "Ian", "teste@mail.com", "photo", Role.USER);
    }

    public UserEntity userEntityFactory() {
        return UserEntity.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .name("Ian")
                .email("teste@mail.com")
                .password("012345678910")
                .profilePicture("photo")
                .role(Role.USER)
                .isEnabled(false)
                .verificationCode(123456)
                .build();
    }

    public UserRegisterResponseDto userResponseFactory() {
        return new UserRegisterResponseDto(UUID.randomUUID(), "Ian", "teste@mail.com", "photo", Role.USER);
    }

    public UserRegisterRequestDto userRequestFactory() {
        return new UserRegisterRequestDto("Ian", "teste@mail.com", "012345678910");
    }

    public MultipartFile file() {
        return new MockMultipartFile("profilePicture",
                "teste.jpg",
                "image/jpeg",
                new byte[0]);
    }
}
