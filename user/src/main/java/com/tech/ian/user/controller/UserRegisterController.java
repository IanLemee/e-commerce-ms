package com.tech.ian.user.controller;

import com.tech.ian.user.model.dto.UserRegisterRequestDto;
import com.tech.ian.user.model.dto.UserRegisterResponseDto;
import com.tech.ian.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("register")
public class UserRegisterController {
    private final UserService userService;

    public UserRegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("create")
    public ResponseEntity<UserRegisterResponseDto> create(@RequestPart("name") @Valid String name,
                                                          @RequestPart("email") @Valid String email,
                                                          @RequestPart("password") @Valid String password,
                                                          @RequestPart("profilePicture") MultipartFile photo
    ) {
        UserRegisterRequestDto request = new UserRegisterRequestDto(name, email, password, photo);
        UserRegisterResponseDto resp = userService.create(request);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    @GetMapping("verify")
    public ResponseEntity<Void> activeAccount(@RequestParam String email, @RequestParam int code) {
        userService.activeAccount(email, code);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
