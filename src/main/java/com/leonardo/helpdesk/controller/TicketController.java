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
public class TicketController {

    private final TicketService ticketService;
    private final TicketHistoryService ticketHistoryService;
    private final TicketCommentService ticketCommentService;

    public TicketController(TicketService ticketService, TicketHistoryService ticketHistoryService, TicketCommentService ticketCommentService) {
        this.ticketService = ticketService;
        this.ticketHistoryService = ticketHistoryService;
        this.ticketCommentService = ticketCommentService;
    }

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

    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentResponseDto> createComment(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID id, @Valid @RequestBody TicketCommentRequestDto requestDto) {
        TicketCommentResponseDto ticketComment = ticketCommentService.create(authenticatedUser, id, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketComment);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TicketCommentResponseDto>> findCommentsByTicketId(@PathVariable UUID id) {
        List<TicketCommentResponseDto> comments = ticketCommentService.findByTicketId(id);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> findById(@PathVariable UUID id) {
        TicketResponseDto ticket = ticketService.findById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketHistoryResponseDto>> findTicketHistoryById(@PathVariable UUID id) {
        List<TicketHistoryResponseDto> ticketHistory = ticketHistoryService.findByTicketId(id);
        return ResponseEntity.ok(ticketHistory);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponseDto>> findAll(
            Pageable pageable,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false, name = "requesterId") UUID requester,
            @RequestParam(required = false, name = "technicianId") UUID technician,
            @RequestParam(required = false, name = "start") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam(required = false, name = "end") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate
            ) {
        Page<TicketResponseDto> tickets = ticketService.findAll(pageable, status, priority, requester, technician, startDate, endDate);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDto> update(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId, @Valid @RequestBody TicketDetailsUpdateDto updateDto) {
        TicketResponseDto updatedTicket = ticketService.updateDetails(authenticatedUser, ticketId, updateDto);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDto> assignTechnician(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId, @RequestBody UUID technicianId) {
        TicketResponseDto assignedTicket = ticketService.assignTechnician(authenticatedUser, ticketId, technicianId);
        return ResponseEntity.ok(assignedTicket);
    }

    @PatchMapping("/{ticketId}/resolve")
    public ResponseEntity<TicketResponseDto> resolve(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId) {
        TicketResponseDto resolvedTicket = ticketService.resolve(authenticatedUser, ticketId);
        return ResponseEntity.ok(resolvedTicket);
    }

    @PatchMapping("/{ticketId}/close")
    public ResponseEntity<TicketResponseDto> close(@AuthenticationPrincipal User authenticatedUser, @PathVariable UUID ticketId) {
        TicketResponseDto closedTicket = ticketService.close(authenticatedUser, ticketId);
        return ResponseEntity.ok(closedTicket);
    }
}
