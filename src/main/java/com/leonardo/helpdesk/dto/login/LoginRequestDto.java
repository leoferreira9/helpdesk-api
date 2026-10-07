package com.leonardo.helpdesk.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
    @Size(max = 100) @Email @NotBlank String email,
    @Size(max = 255) @NotBlank String password
) {}
