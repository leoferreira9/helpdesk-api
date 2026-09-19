package com.leonardo.helpdesk.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        Integer status,
        String message,
        LocalDateTime timestamp,
        String path
) {}
