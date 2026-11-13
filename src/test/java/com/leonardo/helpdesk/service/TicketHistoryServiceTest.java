package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.TicketHistory;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketAction;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.mapper.TicketHistoryMapper;
import com.leonardo.helpdesk.repository.TicketHistoryRepository;
import com.leonardo.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketHistoryServiceTest {

    @InjectMocks
    private TicketHistoryService ticketHistoryService;

    @Mock
    private TicketHistoryMapper ticketHistoryMapper;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private TicketRepository ticketRepository;


    @Test
    void shouldFindTicketHistoryByTicketIdWhenUserIsAdmin() {
        UUID ticketId = UUID.fromString("f2a91c7e-6b43-4d85-a019-3e7c52b8f164");
        UUID adminId = UUID.fromString("8d47b2a6-1e95-4fc3-b760-29a8e5d14c37");
        UUID userId = UUID.fromString("c63e8f19-7a24-4b50-9d86-f1a372e4b695");
        UUID ticketHistoryId = UUID.fromString("1b75d3c8-9f42-46ea-a531-7c08e2d694bf");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);

        TicketHistory ticketHistory = new TicketHistory();

        TicketHistoryResponseDto responseDto = new TicketHistoryResponseDto(
                ticketHistoryId, ticketId, TicketAction.TICKET_CREATED, "Description", userId, "Username", now
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(List.of(ticketHistory));
        when(ticketHistoryMapper.convertToResponseDto(ticketHistory)).thenReturn(responseDto);

        List<TicketHistoryResponseDto> result = ticketHistoryService.findByTicketId(admin, ticketId);

        Assertions.assertEquals(ticketHistoryId, result.getFirst().id());
        Assertions.assertEquals(ticketId, result.getFirst().ticketId());
        Assertions.assertEquals(TicketAction.TICKET_CREATED, result.getFirst().action());
        Assertions.assertEquals("Description", result.getFirst().description());
        Assertions.assertEquals(userId, result.getFirst().userId());
        Assertions.assertEquals("Username", result.getFirst().userName());
        Assertions.assertEquals(now, result.getFirst().createdAt());

        verify(ticketRepository).findById(ticketId);
        verify(ticketHistoryRepository).findByTicketIdOrderByCreatedAtAsc(ticketId);
        verify(ticketHistoryMapper).convertToResponseDto(ticketHistory);
    }

    @Test
    void shouldFindTicketHistoryByTicketIdWhenUserIsRequester() {
        UUID ticketId = UUID.fromString("f2a91c7e-6b43-4d85-a019-3e7c52b8f164");
        UUID userId = UUID.fromString("c63e8f19-7a24-4b50-9d86-f1a372e4b695");
        UUID ticketHistoryId = UUID.fromString("1b75d3c8-9f42-46ea-a531-7c08e2d694bf");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);

        TicketHistory ticketHistory = new TicketHistory();

        TicketHistoryResponseDto responseDto = new TicketHistoryResponseDto(
                ticketHistoryId, ticketId, TicketAction.TICKET_CREATED, "Description", userId, "Username", now
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(List.of(ticketHistory));
        when(ticketHistoryMapper.convertToResponseDto(ticketHistory)).thenReturn(responseDto);

        List<TicketHistoryResponseDto> result = ticketHistoryService.findByTicketId(user, ticketId);

        Assertions.assertEquals(ticketHistoryId, result.getFirst().id());
        Assertions.assertEquals(ticketId, result.getFirst().ticketId());
        Assertions.assertEquals(TicketAction.TICKET_CREATED, result.getFirst().action());
        Assertions.assertEquals("Description", result.getFirst().description());
        Assertions.assertEquals(userId, result.getFirst().userId());
        Assertions.assertEquals("Username", result.getFirst().userName());
        Assertions.assertEquals(now, result.getFirst().createdAt());

        verify(ticketRepository).findById(ticketId);
        verify(ticketHistoryRepository).findByTicketIdOrderByCreatedAtAsc(ticketId);
        verify(ticketHistoryMapper).convertToResponseDto(ticketHistory);
    }

    @Test
    void shouldFindTicketHistoryByTicketIdWhenUserIsAssignedTechnician() {
        UUID ticketId = UUID.fromString("f2a91c7e-6b43-4d85-a019-3e7c52b8f164");
        UUID technicianId = UUID.fromString("8d47b2a6-1e95-4fc3-b760-29a8e5d14c37");
        UUID userId = UUID.fromString("c63e8f19-7a24-4b50-9d86-f1a372e4b695");
        UUID ticketHistoryId = UUID.fromString("1b75d3c8-9f42-46ea-a531-7c08e2d694bf");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);
        ticket.setTechnician(technician);

        TicketHistory ticketHistory = new TicketHistory();

        TicketHistoryResponseDto responseDto = new TicketHistoryResponseDto(
                ticketHistoryId, ticketId, TicketAction.TICKET_CREATED, "Description", userId, "Username", now
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(List.of(ticketHistory));
        when(ticketHistoryMapper.convertToResponseDto(ticketHistory)).thenReturn(responseDto);

        List<TicketHistoryResponseDto> result = ticketHistoryService.findByTicketId(technician, ticketId);

        Assertions.assertEquals(ticketHistoryId, result.getFirst().id());
        Assertions.assertEquals(ticketId, result.getFirst().ticketId());
        Assertions.assertEquals(TicketAction.TICKET_CREATED, result.getFirst().action());
        Assertions.assertEquals("Description", result.getFirst().description());
        Assertions.assertEquals(userId, result.getFirst().userId());
        Assertions.assertEquals("Username", result.getFirst().userName());
        Assertions.assertEquals(now, result.getFirst().createdAt());

        verify(ticketRepository).findById(ticketId);
        verify(ticketHistoryRepository).findByTicketIdOrderByCreatedAtAsc(ticketId);
        verify(ticketHistoryMapper).convertToResponseDto(ticketHistory);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindingHistoryAsRequesterWhoDoesNotOwnTicket() {
        UUID ticketId = UUID.fromString("f2a91c7e-6b43-4d85-a019-3e7c52b8f164");
        UUID userId2 = UUID.fromString("8d47b2a6-1e95-4fc3-b760-29a8e5d14c37");
        UUID userId = UUID.fromString("c63e8f19-7a24-4b50-9d86-f1a372e4b695");

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        User user2 = new User();
        user2.setId(userId2);
        user2.setRole(UserRole.USER);
        user2.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        AccessDeniedException result = Assertions.assertThrows(AccessDeniedException.class,
                () -> ticketHistoryService.findByTicketId(user2, ticketId));

        Assertions.assertEquals("Can't read ticket history, ticket does not belong to authenticated user", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(ticketHistoryRepository);
        verifyNoInteractions(ticketHistoryMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindingHistoryAsTechnicianNotAssignedToTicket() {
        UUID ticketId = UUID.fromString("f2a91c7e-6b43-4d85-a019-3e7c52b8f164");
        UUID requesterId = UUID.fromString("c63e8f19-7a24-4b50-9d86-f1a372e4b695");
        UUID assignedTechnicianId = UUID.fromString("8d47b2a6-1e95-4fc3-b760-29a8e5d14c37");
        UUID authenticatedTechnicianId = UUID.fromString("1b75d3c8-9f42-46ea-a531-7c08e2d694bf");

        User requester = new User();
        requester.setId(requesterId);
        requester.setRole(UserRole.USER);
        requester.setActive(true);

        User assignedTechnician = new User();
        assignedTechnician.setId(assignedTechnicianId);
        assignedTechnician.setRole(UserRole.TECHNICIAN);
        assignedTechnician.setActive(true);

        User authenticatedTechnician = new User();
        authenticatedTechnician.setId(authenticatedTechnicianId);
        authenticatedTechnician.setRole(UserRole.TECHNICIAN);
        authenticatedTechnician.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setRequester(requester);
        ticket.setTechnician(assignedTechnician);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        AccessDeniedException result = Assertions.assertThrows(AccessDeniedException.class,
                () -> ticketHistoryService.findByTicketId(authenticatedTechnician, ticketId));

        Assertions.assertEquals("Can't read ticket history, ticket does not belong to authenticated user", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(ticketHistoryRepository);
        verifyNoInteractions(ticketHistoryMapper);

    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingHistoryForNonexistentTicket() {
        UUID ticketId = UUID.fromString("f2a91c7e-6b43-4d85-a019-3e7c52b8f164");
        UUID userId = UUID.fromString("c63e8f19-7a24-4b50-9d86-f1a372e4b695");

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.USER);
        user.setActive(true);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException result = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketHistoryService.findByTicketId(user, ticketId));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(ticketHistoryRepository);
        verifyNoInteractions(ticketHistoryMapper);

    }
}
