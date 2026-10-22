package com.leonardo.helpdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCommentRequestDto (
        @NotBlank @Size(max = 200) String comment
) {}
