package com.leonardo.helpdesk.dto.response;

public record DashboardResponseDto(
        Long total,
        Long open,
        Long inProgress,
        Long resolved,
        Long closed
) {}
