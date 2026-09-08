package com.leonardo.helpdesk.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordUpdateDto(@Size(max = 255) @NotBlank String password) {}
