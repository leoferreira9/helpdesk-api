package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.login.LoginRequestDto;
import com.leonardo.helpdesk.dto.login.LoginResponseDto;
import com.leonardo.helpdesk.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto login = authenticationService.login(requestDto);
        return ResponseEntity.ok(login);
    }
}
