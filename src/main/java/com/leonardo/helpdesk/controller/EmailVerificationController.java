package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.service.EmailVerificationTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify-email")
public class EmailVerificationController {

    private final EmailVerificationTokenService emailVerificationTokenService;

    public EmailVerificationController(EmailVerificationTokenService emailVerificationTokenService) {
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @GetMapping
    public ResponseEntity<Void> verificationToken(@RequestParam String token) {
        emailVerificationTokenService.confirmToken(token);
        return ResponseEntity.noContent().build();
    }
}
