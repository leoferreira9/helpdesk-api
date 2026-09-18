package com.leonardo.helpdesk.dto.request;

import com.leonardo.helpdesk.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @Size(max = 150) @NotBlank String name,
        @Email @Size(max = 100) @NotBlank String email,
        @Size(max = 255) @NotBlank String password,
        @NotNull UserRole role
) {}
