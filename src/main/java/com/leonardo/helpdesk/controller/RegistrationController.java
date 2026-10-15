package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.register.RegisterRequestDto;
import com.leonardo.helpdesk.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        registrationService.register(requestDto);
        return ResponseEntity.accepted().build();
    }
}
