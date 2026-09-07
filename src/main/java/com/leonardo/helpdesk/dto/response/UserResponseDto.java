package com.leonardo.helpdesk.dto.response;

import com.leonardo.helpdesk.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String name,
    String email,
    UserRole role,
    boolean active,
    LocalDateTime createdAt
) {}
