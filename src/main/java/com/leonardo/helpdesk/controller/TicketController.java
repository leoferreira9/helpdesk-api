package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.request.TicketCommentRequestDto;
import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketCommentResponseDto;
import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.service.TicketCommentService;
import com.leonardo.helpdesk.service.TicketHistoryService;
import com.leonardo.helpdesk.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket controller", description = "Ticket endpoints class")
public class TicketController {

    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final TicketCommentService ticketCommentService;

    public TicketController(TicketService ticketService, TicketHistoryService ticketHistoryService, TicketCommentService ticketCommentService) {
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.ticketCommentService = ticketCommentService;
    }

    @Operation(summary = "Create Ticket", description = "Creates a new Ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    @PostMapping
    public ResponseEntity<TicketResponseDto> create(@AuthenticationPrincipal User authenticatedUser, @Valid @RequestBody TicketRequestDto requestDto) {
        TicketResponseDto savedTicket = ticketService.create(authenticatedUser, requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedTicket.id())
                .toUri();

        return ResponseEntity.created(location).body(savedTicket);
    }

    @Operation(summary = "Create Ticket comment", description = "Creates a new comment for ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data, ID format, or ticket status"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentResponseDto> createComment(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID id, @Valid @RequestBody TicketCommentRequestDto requestDto) {
        TicketCommentResponseDto ticketComment = ticketCommentService.create(authenticatedUser, id, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketComment);
    }

    @Operation(summary = "Find Ticket comments", description = "Find ticket comments by its ID ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket comments found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TicketCommentResponseDto>> findCommentsByTicketId(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID id) {
        List<TicketCommentResponseDto> comments = ticketCommentService.findByTicketId(authenticatedUser, id);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Find Ticket", description = "Finds a ticket by its ID ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> findById(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID id) {
        TicketResponseDto ticket = ticketService.findById(authenticatedUser, id);
        return ResponseEntity.ok(ticket);
    }

    @Operation(summary = "Find Ticket history", description = "Finds a ticket history by its ID ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket history found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketHistoryResponseDto>> findTicketHistoryById(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID id) {
        List<TicketHistoryResponseDto> ticketHistory = ticketHistoryService.findByTicketId(authenticatedUser, id);
        return ResponseEntity.ok(ticketHistory);
    }

    @Operation(summary = "Find tickets", description = "Return all created tickets")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets successfully returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter, pagination, or date range"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token")
    })
    @GetMapping
    public ResponseEntity<Page<TicketResponseDto>> findAll(
            Pageable pageable,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false, name = "requesterId") UUID requester,
            @RequestParam(required = false, name = "technicianId") UUID technician,
            @RequestParam(required = false, name = "start") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam(required = false, name = "end") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate,
            @AuthenticationPrincipal User authenticatedUser
            ) {
        Page<TicketResponseDto> tickets = ticketService.findAll(authenticatedUser, pageable, status, priority, requester, technician, startDate, endDate);
        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Update ticket", description = "Updates a ticket by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data, ID format, or ticket status"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDto> update(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId, @Valid @RequestBody TicketDetailsUpdateDto updateDto) {
        TicketResponseDto updatedTicket = ticketService.updateDetails(authenticatedUser, ticketId, updateDto);
        return ResponseEntity.ok(updatedTicket);
    }

    @Operation(summary = "Assign technician", description = "Assign a technician to a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Technician successfully assigned"),
            @ApiResponse(responseCode = "400", description = "Invalid request data, ID format, or ticket status"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket or Technician not found"),
            @ApiResponse(responseCode = "409", description = "Ticket already assigned")
    })
    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDto> assignTechnician(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId, @RequestBody UUID technicianId) {
        TicketResponseDto assignedTicket = ticketService.assignTechnician(authenticatedUser, ticketId, technicianId);
        return ResponseEntity.ok(assignedTicket);
    }

    @Operation(summary = "Resolve ticket", description = "Resolves a ticket by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket successfully resolved"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format or ticket status"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PatchMapping("/{ticketId}/resolve")
    public ResponseEntity<TicketResponseDto> resolve(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId) {
        TicketResponseDto resolvedTicket = ticketService.resolve(authenticatedUser, ticketId);
        return ResponseEntity.ok(resolvedTicket);
    }

    @Operation(summary = "Close ticket", description = "Close a ticket by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket successfully closed"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format or ticket status"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PatchMapping("/{ticketId}/close")
    public ResponseEntity<TicketResponseDto> close(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId) {
        TicketResponseDto closedTicket = ticketService.close(authenticatedUser, ticketId);
        return ResponseEntity.ok(closedTicket);
    }
}
