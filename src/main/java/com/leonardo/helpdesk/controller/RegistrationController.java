package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.register.RegisterRequestDto;
import com.leonardo.helpdesk.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
@Tag(name = "Registration controller", description = "Registration endpoint class")
@SecurityRequirements
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Operation(summary = "Register User", description = "Registers a new User")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        registrationService.register(requestDto);
        return ResponseEntity.accepted().build();
    }
}
