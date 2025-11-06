package com.tech.ian.user.controller;

import com.tech.ian.user.model.UserRegisterRequestDto;
import com.tech.ian.user.model.UserRegisterResponseDto;
import com.tech.ian.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class UserRegisterController {
    private final UserService userService;

    public UserRegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserRegisterResponseDto> create(@RequestBody UserRegisterRequestDto request) {
        UserRegisterResponseDto resp = userService.create(request);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> activeAccount(@RequestParam String email, @RequestParam int code) {
        userService.activeAccount(email, code);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
