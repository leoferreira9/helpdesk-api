package com.leonardo.helpdesk.dto.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto (
    @Size(max = 150) @NotBlank String name,
    @Size(max = 100) @Email @NotBlank String email,
    @Size(max = 255) @NotBlank String password
) {}