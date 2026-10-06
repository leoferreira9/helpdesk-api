package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.request.TicketCommentRequestDto;
import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketCommentResponseDto;
import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.service.TicketCommentService;
import com.leonardo.helpdesk.service.TicketHistoryService;
import com.leonardo.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<TicketResponseDto> create(@Valid @RequestBody TicketRequestDto requestDto) {
        TicketResponseDto savedTicket = ticketService.create(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedTicket.id())
                .toUri();

        return ResponseEntity.created(location).body(savedTicket);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentResponseDto> createComment(@PathVariable UUID id, @Valid @RequestBody TicketCommentRequestDto requestDto) {
        TicketCommentResponseDto ticketComment = ticketCommentService.create(id, requestDto);
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
    public ResponseEntity<Page<TicketResponseDto>> findAll(Pageable pageable) {
        Page<TicketResponseDto> tickets = ticketService.findAll(pageable);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDto> update(@PathVariable UUID ticketId, @Valid @RequestBody TicketDetailsUpdateDto updateDto, @RequestParam UUID updaterId) {
        TicketResponseDto updatedTicket = ticketService.updateDetails(ticketId, updateDto, updaterId);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDto> assignTechnician(@PathVariable UUID ticketId, @RequestParam UUID technicianId) {
        TicketResponseDto assignedTicket = ticketService.assignTechnician(ticketId, technicianId);
        return ResponseEntity.ok(assignedTicket);
    }

    @PatchMapping("/{ticketId}/resolve")
    public ResponseEntity<TicketResponseDto> resolve(@PathVariable UUID ticketId, @RequestParam UUID technicianId) {
        TicketResponseDto resolvedTicket = ticketService.resolve(ticketId, technicianId);
        return ResponseEntity.ok(resolvedTicket);
    }

    @PatchMapping("/{ticketId}/close")
    public ResponseEntity<TicketResponseDto> close(@PathVariable UUID ticketId, @RequestParam UUID userId) {
        TicketResponseDto closedTicket = ticketService.close(ticketId, userId);
        return ResponseEntity.ok(closedTicket);
    }
}
