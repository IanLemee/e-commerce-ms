package com.tech.ian.user.controller;

import com.tech.ian.user.commons.Loader;
import com.tech.ian.user.model.user.Role;
import com.tech.ian.user.model.user.UserEntity;
import com.tech.ian.user.repo.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserLoginControllerTestIT {

    private static final String URL = "http://localhost:";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private Integer port;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Loader loader;
    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach()
    void setup() {
        RestAssured.baseURI = URL + port;
        var encode = encoder.encode("12345678901");
        var user = UserEntity
                .builder()
                .uuid(UUID.randomUUID())
                .name("Test")
                .email("Test@mail.com".toLowerCase())
                .password(encode)
                .profilePicture("image")
                .role(Role.USER)
                .isEnabled(true)
                .verificationCode(0)
                .build();
        userRepository.save(user);

    }

    @SneakyThrows
    @Test
    void shouldReturn202WhenLoginSuccessful() {
        var loginJson = loader.load("userLogin/post-user-login-req-IT-200.json");
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/login")
                .then()
                .log().all()
                .statusCode(HttpStatus.ACCEPTED.value());
    }
}