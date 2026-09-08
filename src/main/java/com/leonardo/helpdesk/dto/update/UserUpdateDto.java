package com.leonardo.helpdesk.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @Size(max = 150) @Pattern(regexp = ".*\\S.*") String name,
        @Email @Size(max = 100) @Pattern(regexp = ".*\\S.*") String email
) {}
