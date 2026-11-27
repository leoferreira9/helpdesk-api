package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.service.EmailVerificationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify-email")
@Tag(name = "Email verification controller", description = "Email verification endpoint")
@SecurityRequirements
public class EmailVerificationController {

    private final EmailVerificationTokenService emailVerificationTokenService;

    public EmailVerificationController(EmailVerificationTokenService emailVerificationTokenService) {
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @Operation(summary = "Verify email", description = "Verifies email by token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email successfully verified"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token"),
            @ApiResponse(responseCode = "410", description = "Token expired")
    })
    @GetMapping
    public ResponseEntity<Void> verificationToken(@RequestParam String token) {
        emailVerificationTokenService.confirmToken(token);
        return ResponseEntity.noContent().build();
    }
}
