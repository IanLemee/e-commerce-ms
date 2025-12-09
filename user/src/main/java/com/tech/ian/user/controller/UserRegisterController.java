package com.tech.ian.user.controller;

import com.tech.ian.user.model.user.dto.UserRegisterRequestDto;
import com.tech.ian.user.model.user.dto.UserRegisterResponseDto;
import com.tech.ian.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/register")
public class UserRegisterController {
    private final UserService userService;

    public UserRegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserRegisterResponseDto> create(@RequestPart("data") @Valid UserRegisterRequestDto data,
                                                          @RequestPart("file") MultipartFile file
    ) {
        UserRegisterResponseDto resp = userService.create(data, file);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    @PutMapping("/verify")
    public ResponseEntity<Void> activeAccount(@RequestParam String email, @RequestParam int code) {
        userService.enableAccount(email, code);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
