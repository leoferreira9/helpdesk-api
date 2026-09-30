package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketAction;
import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.exception.UserNotActiveException;
import com.leonardo.helpdesk.mapper.TicketMapper;
import com.leonardo.helpdesk.repository.TicketRepository;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketHistoryService ticketHistoryService;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldCreateTicket() {
        UUID requesterId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID ticketId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        Ticket ticket = new Ticket();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        TicketRequestDto ticketRequestDto = new TicketRequestDto(
                "Title",
                "Description",
                TicketPriority.HIGH,
                requesterId
        );

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Title",
                "Description",
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                "Requester name",
                "requester@email.com",
                UserRole.USER,
                "Technician name",
                "tech@email.com",
                createdAt,
                updatedAt,
                null
        );

        User user = new User();
        user.setActive(true);
        user.setName("Requester name");

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(user));

        when(ticketMapper.convertToEntity(ticketRequestDto))
                .thenReturn(ticket);

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        TicketResponseDto result = ticketService.create(ticketRequestDto);

        Assertions.assertEquals(ticketId, result.id());
        Assertions.assertEquals("Title", result.title());
        Assertions.assertEquals("Description", result.description());
        Assertions.assertEquals(TicketStatus.OPEN, result.status());
        Assertions.assertEquals(TicketPriority.HIGH, result.priority());
        Assertions.assertEquals("Requester name", result.requesterName());
        Assertions.assertEquals("requester@email.com", result.requesterEmail());
        Assertions.assertEquals(UserRole.USER, result.requesterRole());
        Assertions.assertEquals("Technician name", result.technicianName());
        Assertions.assertEquals("tech@email.com", result.technicianEmail());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertEquals(updatedAt, result.updatedAt());
        Assertions.assertNull(result.resolvedAt());
        Assertions.assertSame(user, ticket.getRequester());

        verify(userRepository).findById(requesterId);
        verify(ticketMapper).convertToEntity(ticketRequestDto);
        verify(ticketRepository).save(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_CREATED, "Ticket created by requester Requester name");
        verify(ticketMapper).convertToResponseDto(ticket);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenRequesterDoesNotExist() {
        UUID requesterId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");

        TicketRequestDto ticketRequestDto = new TicketRequestDto(
                "Title",
                "Description",
                TicketPriority.HIGH,
                requesterId
        );

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.create(ticketRequestDto));

        Assertions.assertEquals("Requester not found with ID: " + ticketRequestDto.requesterId(), exception.getMessage());

        verify(userRepository).findById(requesterId);
        verifyNoInteractions(ticketRepository);
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowUserNotActiveExceptionWhenRequesterIsInactive() {
        UUID requesterId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");

        TicketRequestDto ticketRequestDto = new TicketRequestDto(
                "Title",
                "Description",
                TicketPriority.HIGH,
                requesterId
        );

        User user = new User();
        user.setActive(false);

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(user));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.create(ticketRequestDto));

        Assertions.assertEquals("Requester is not active", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketRepository);
        verifyNoInteractions(ticketHistoryService);
        verify(userRepository).findById(requesterId);
    }
}