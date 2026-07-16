package com.tech.ian.user.controller;

import com.tech.ian.user.model.user.Role;
import com.tech.ian.user.model.user.UserEntity;
import com.tech.ian.user.model.user.dto.UserRegisterRequestDto;
import com.tech.ian.user.repo.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserRegisterControllerTestIT {

    private static final String URL = "http://localhost";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private Integer port;

    @Autowired
    UserRepository userRepository;

    @BeforeEach()
    void setup() {
        RestAssured.baseURI = URL;
        RestAssured.port = port;
    }

    // Todo
    // should return json response

    @Test
    void shouldCreateUserWhenSuccessful() {
        var testUser = new UserRegisterRequestDto("Test", "Test@mail.com", "12345678901");
        byte[] imageBytes = new byte[0];

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("data", testUser, "application/json")
                .multiPart("file", "photo.png", imageBytes,"image/png")
                .when()
                .post("register/create")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void shouldVerifyUserWhenSuccessful() {
        var build = UserEntity.builder().uuid(UUID.randomUUID()).name("Test").email("Test2@mail.com").password("12345678901").profilePicture("url").role(Role.USER).isEnabled(false).verificationCode(123456).build();
        userRepository.save(build);

        given()
                .formParam("email", build.getEmail())
                .formParam("code", build.getVerificationCode())
                .when()
                .put("register/verify")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}