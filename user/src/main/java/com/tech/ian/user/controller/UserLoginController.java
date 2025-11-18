package com.tech.ian.user.controller;

import com.tech.ian.user.model.UserEntity;
import com.tech.ian.user.model.dto.UserLoginRequestDto;
import com.tech.ian.user.service.UserService;
import com.tech.ian.user.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("login")
public class UserLoginController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public UserLoginController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequestDto req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password())).isAuthenticated();
        UserEntity user = userService.getUserEntity(req.email()).get();
        String token = jwtUtils.generateToken(user);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }
}
