package com.tech.ian.user.controller;

import com.tech.ian.user.commons.Factory;
import com.tech.ian.user.commons.Loader;
import com.tech.ian.user.service.UserDetailsServiceImpl;
import com.tech.ian.user.service.UserService;
import com.tech.ian.user.utils.JwtUtils;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UserLoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({Loader.class, Factory.class})
class UserLoginControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService service;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private Loader loader;
    @Autowired
    private Factory factory;

    @Nested
    class login {

        @Test
        @DisplayName("POST '/login' Should return status 202 when user login successful")
        void shouldReturnStatus202WhenUserLoginSuccessful() throws Exception {
            var authMock = mock(Authentication.class);
            when(authMock.isAuthenticated()).thenReturn(true);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);

            when(jwtUtils.generateToken(any())).thenReturn("token-jwt");
            when(service.getUserEntity(anyString())).thenReturn(Optional.ofNullable(factory.userEntityFactory()));

            var reqJson = loader.load("userLogin/post-user-login-req-200.json");
            mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .content(reqJson)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isAccepted());
        }

        @Test
        void test() throws Exception {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));
            var reqJson = loader.load("userLogin/post-user-login-req-403.json");

            mockMvc.perform(MockMvcRequestBuilders.post("/login").content(reqJson)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @ParameterizedTest
        @MethodSource(value = "postUserBadRequest")
        @DisplayName("POST '/login' Should return status 400 when user data is blank Or empty")
        void shouldReturnStatus400WhenUserDataIsBlankOrEmpty(String file) throws Exception {

            var reqJson = loader.load(file);
            mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .content(reqJson)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        private static Stream<Arguments> postUserBadRequest() {
            return Stream.of(
                    Arguments.of("userLogin/post-user-login-req-empty-400.json"),
                    Arguments.of("userLogin/post-user-login-req-blank-400.json")
            );
        }
    }


}