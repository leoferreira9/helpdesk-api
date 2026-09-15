package com.leonardo.helpdesk.dto.response;

import com.leonardo.helpdesk.enums.TicketAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketHistoryResponseDto(
        UUID id,
        UUID ticketId,
        TicketAction action,
        String description,
        UUID userId,
        String userName,
        LocalDateTime createdAt
) {}
