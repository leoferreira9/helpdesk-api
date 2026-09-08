package com.leonardo.helpdesk.dto.response;

import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponseDto(
        UUID id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String requesterName,
        String requesterEmail,
        UserRole requesterRole,
        String technicianName,
        String technicianEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt
) {}
