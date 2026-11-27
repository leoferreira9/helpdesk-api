package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.response.DashboardResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard controller", description = "Dashboard endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Dashboard summary", description = "Returns dashboard summary for authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary successfully returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token")
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardResponseDto> summary(@AuthenticationPrincipal User authenticatedUser) {
        DashboardResponseDto responseDto = dashboardService.summary(authenticatedUser);
        return ResponseEntity.ok(responseDto);
    }
}
