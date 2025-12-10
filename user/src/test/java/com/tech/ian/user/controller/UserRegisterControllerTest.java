package com.tech.ian.user.controller;

import com.tech.ian.user.commons.Factory;
import com.tech.ian.user.commons.Loader;
import com.tech.ian.user.exception.exceptions.UserAlreadyExistException;
import com.tech.ian.user.exception.exceptions.WrongVerificationCodeException;
import com.tech.ian.user.model.user.Role;
import com.tech.ian.user.model.user.dto.UserRegisterResponseDto;
import com.tech.ian.user.security.AuthFilter;
import com.tech.ian.user.service.UserDetailsServiceImpl;
import com.tech.ian.user.service.UserService;
import com.tech.ian.user.utils.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UserRegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({Loader.class, Factory.class})
class UserRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService service;
    @MockitoBean
    private AuthFilter authFilter;
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private Factory factory;
    @Autowired
    private Loader loader;

    @Nested
    class create {
        @Test
        @DisplayName("POST '/register/create' Should return status 200 and correct json when user create be successful")
        void shouldReturnStatus200AndCorrectJsonWhenUserCreateBeSuccessful() throws Exception {
            var fixedUuid = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

            var response = new UserRegisterResponseDto(
                    fixedUuid,
                    "teste",
                    "teste@gmail.com",
                    "url_da_foto_s3",
                    Role.USER
            );

            when(service.create(any(), any())).thenReturn(response);
            var reqJson = loader.load("userRegister/post-user-created-req-200.json");
            var respJson = loader.load("userRegister/post-user-created-resp-200.json");
            mockMvc.perform(MockMvcRequestBuilders.multipart("/register/create")
                            .file(factory.fileJsonFactory(reqJson.getBytes(StandardCharsets.UTF_8)))
                            .file(factory.filePhotoFactory())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.content().json(respJson));
        }

        @Test
        @DisplayName("POST '/register/create' Should return status 409 when email already exists")
        void shouldReturnStatus409WhenEmailAlreadyExists() throws Exception {
            when(service.create(any(), any())).thenThrow(UserAlreadyExistException.class);
            var reqJson = loader.load("userRegister/post-user-created-req-200.json");
            mockMvc.perform(MockMvcRequestBuilders.multipart("/register/create")
                            .file(factory.fileJsonFactory(reqJson.getBytes(StandardCharsets.UTF_8)))
                            .file(factory.filePhotoFactory())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isConflict());
        }

        @ParameterizedTest
        @MethodSource(value = "postUserBadRequestSource")
        @DisplayName("POST '/register/create' Should return status 400 when email, name or password be invalid")
        void shouldReturnStatus400WhenArgumentsBeInvalid(String fileName) throws Exception {
            var reqJson = loader.load("userRegister/%s".formatted(fileName));
            var mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/register/create")
                            .file(factory.fileJsonFactory(reqJson.getBytes(StandardCharsets.UTF_8)))
                            .file(factory.filePhotoFactory())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();

            var resolvedException = mvcResult.getResolvedException();
            Assertions.assertNotNull(resolvedException);
        }

        private static Stream<Arguments> postUserBadRequestSource() {
            return Stream.of(
                    Arguments.of("post-user-created-req-empty-fields-400.json"),
                    Arguments.of("post-user-created-req-invalid-fields-400.json"),
                    Arguments.of("post-user-created-req-blank-fields-400.json")
            );
        }
    }


    @Nested
    class verify {
        @Test
        @DisplayName("PUT '/register/verify' Should return status 204 when user enabled successfully")
        void shouldReturn204WhenUserEnabledSuccessfully() throws Exception {
            var userEntity = factory.userEntityFactory();
            when(service.getUserEntity(any())).thenReturn(Optional.of(userEntity));
            mockMvc.perform(MockMvcRequestBuilders.put("/register/verify")
                            .param("email", userEntity.getEmail())
                            .param("code", String.valueOf(userEntity.getVerificationCode())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isNoContent());
        }

        @ParameterizedTest
        @MethodSource(value = "putUserConflictSource")
        @DisplayName("PUT '/register/verify' Should return status 409 and UserAlreadyExistException OR WrongVerificationCodeException when user be already enabled OR the code be wrong")
        void shouldReturn409AndUserAlreadyExistExceptionOrWrongVerificationCodeExceptionWhenUserBeAlreadyEnabledOrTheCodeBeWrong(Exception ex) throws Exception {
            String email = "teste@mail.com";
            String code = "123456";

            doThrow(ex).when(service).enableAccount(email, Integer.parseInt(code));

            mockMvc.perform(MockMvcRequestBuilders.put("/register/verify")
                            .param("email", email)
                            .param("code", code))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isConflict());
        }

        private static Stream<Arguments> putUserConflictSource() {
            return Stream.of(
                    Arguments.of(new UserAlreadyExistException()),
                    Arguments.of(new WrongVerificationCodeException())
            );
        }
    }

}