package com.leonardo.helpdesk.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketCommentResponseDto (
        UUID id,
        UUID userId,
        UUID ticketId,
        String userName,
        String comment,
        LocalDateTime createdAt
) {}
