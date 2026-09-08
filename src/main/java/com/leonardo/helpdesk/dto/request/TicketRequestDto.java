package com.leonardo.helpdesk.dto.request;

import com.leonardo.helpdesk.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TicketRequestDto(
        @Size(max = 150) @NotBlank String title,
        @NotBlank @Size(max = 10000) String description,
        @NotNull TicketPriority priority,
        @NotNull UUID requesterId
) {}
