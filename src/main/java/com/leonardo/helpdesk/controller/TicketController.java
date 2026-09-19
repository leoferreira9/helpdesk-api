package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
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

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> findById(@PathVariable UUID id) {
        TicketResponseDto ticket = ticketService.findById(id);
        return ResponseEntity.ok(ticket);
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
