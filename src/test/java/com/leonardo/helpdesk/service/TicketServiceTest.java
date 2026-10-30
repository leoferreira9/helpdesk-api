package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketAction;
import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.*;
import com.leonardo.helpdesk.mapper.TicketMapper;
import com.leonardo.helpdesk.repository.TicketRepository;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
                TicketPriority.HIGH
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
        user.setId(requesterId);

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(user));

        when(ticketMapper.convertToEntity(ticketRequestDto))
                .thenReturn(ticket);

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        TicketResponseDto result = ticketService.create(user, ticketRequestDto);

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

        User authenticatedUser = new User();
        authenticatedUser.setId(requesterId);

        TicketRequestDto ticketRequestDto = new TicketRequestDto(
                "Title",
                "Description",
                TicketPriority.HIGH
        );

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.create(authenticatedUser, ticketRequestDto));

        Assertions.assertEquals("Requester not found with ID: " + authenticatedUser.getId(), exception.getMessage());

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
                TicketPriority.HIGH
        );

        User user = new User();
        user.setActive(false);
        user.setId(requesterId);

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(user));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.create(user, ticketRequestDto));

        Assertions.assertEquals("Requester is not active", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketRepository);
        verifyNoInteractions(ticketHistoryService);
        verify(userRepository).findById(requesterId);
    }

    @Test
    void shouldFindTicketById() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        Ticket ticket = new Ticket();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

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

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        TicketResponseDto result = ticketService.findById(ticketId);

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

        verify(ticketRepository).findById(ticketId);
        verify(ticketMapper).convertToResponseDto(ticket);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingTicketById() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.findById(ticketId));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void shouldFindAllTickets() {
        Pageable pageable = PageRequest.of(0, 10);
        Ticket ticket = new Ticket();

        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID requesterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 19);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

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

        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        Page<TicketResponseDto> result = ticketService.findAll(
                pageable,
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                requesterId,
                technicianId,
                startDate,
                endDate
        );

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(ticketId, result.getContent().getFirst().id());
        Assertions.assertEquals("Title", result.getContent().getFirst().title());
        Assertions.assertEquals("Description", result.getContent().getFirst().description());
        Assertions.assertEquals(TicketStatus.OPEN, result.getContent().getFirst().status());
        Assertions.assertEquals(TicketPriority.HIGH, result.getContent().getFirst().priority());
        Assertions.assertEquals("Requester name", result.getContent().getFirst().requesterName());
        Assertions.assertEquals("requester@email.com", result.getContent().getFirst().requesterEmail());
        Assertions.assertEquals(UserRole.USER, result.getContent().getFirst().requesterRole());
        Assertions.assertEquals("Technician name", result.getContent().getFirst().technicianName());
        Assertions.assertEquals("tech@email.com", result.getContent().getFirst().technicianEmail());
        Assertions.assertEquals(createdAt, result.getContent().getFirst().createdAt());
        Assertions.assertEquals(updatedAt, result.getContent().getFirst().updatedAt());
        Assertions.assertNull(result.getContent().getFirst().resolvedAt());

        verify(ticketRepository).findAll(ArgumentMatchers.<Specification<Ticket>>any(), eq(pageable));
        verify(ticketMapper).convertToResponseDto(ticket);
    }

    @Test
    void shouldUpdateTicketDetails() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                TicketPriority.MEDIUM
        );

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Title 2",
                "Description 2",
                TicketStatus.OPEN,
                TicketPriority.MEDIUM,
                "Requester name",
                "requester@email.com",
                UserRole.USER,
                "Technician name",
                "tech@email.com",
                createdAt,
                updatedAt,
                null
        );

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.of(user));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title 2", ticket.getTitle());
        Assertions.assertEquals("Description 2", ticket.getDescription());
        Assertions.assertEquals(TicketStatus.OPEN, ticket.getStatus());
        Assertions.assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketRepository).save(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldThrowInvalidTicketStatusException() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");

        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);

        User user = new User();
        user.setId(updaterId);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.of(user));

        InvalidTicketStatusException exception = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketService.updateDetails(user, ticketId, detailsUpdateDto));

        Assertions.assertEquals("Ticket can't be updated because it's Closed", exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowUserNotActiveException() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");

        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User user = new User();
        user.setActive(false);
        user.setId(updaterId);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.of(user));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.updateDetails(user, ticketId, detailsUpdateDto));

        Assertions.assertEquals("User must be active to update this ticket", exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingTicketDoesNotExist() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                TicketPriority.MEDIUM
        );

        User user = new User();
        user.setId(updaterId);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.updateDetails(user, ticketId, detailsUpdateDto));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(userRepository);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdaterDoesNotExist() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                TicketPriority.MEDIUM
        );
        Ticket ticket = new Ticket();
        User user = new User();
        user.setId(updaterId);

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.updateDetails(user, ticketId, detailsUpdateDto));

        Assertions.assertEquals("User not found with ID: " + updaterId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldKeepTitleWhenUpdatingTicketWithNullTitle() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                null,
                "Description 2",
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description 2", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title", ticket.getTitle());
        Assertions.assertEquals("Description 2", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldKeepTitleWhenUpdatingTicketWithBlankTitle() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                " ",
                "Description 2",
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description 2", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title", ticket.getTitle());
        Assertions.assertEquals("Description 2", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldKeepDescriptionWhenUpdatingTicketWithNullDescription() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                null,
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title 2", ticket.getTitle());
        Assertions.assertEquals("Description", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldKeepDescriptionWhenUpdatingTicketWithBlankDescription() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                " ",
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title 2", ticket.getTitle());
        Assertions.assertEquals("Description", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldKeepPriorityWhenUpdatingTicketWithNullPriority() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                null
        );

        Ticket ticket = new Ticket();
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description 2", TicketStatus.OPEN, TicketPriority.HIGH,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title 2", ticket.getTitle());
        Assertions.assertEquals("Description 2", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.HIGH, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldKeepTicketDetailsUnchangedWhenUpdateDtoIsNull() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");

        Ticket ticket = new Ticket();
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);

        User user = new User();
        user.setName("Updater name");
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description", TicketStatus.OPEN, TicketPriority.HIGH,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(user, ticketId, null);

        Assertions.assertEquals("Title", ticket.getTitle());
        Assertions.assertEquals("Description", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.HIGH, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by user Updater name");
    }

    @Test
    void shouldRecordTechnicianWhenUpdatingTicketDetails() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID updaterId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        TicketDetailsUpdateDto detailsUpdateDto = new TicketDetailsUpdateDto(
                "Title 2",
                "Description 2",
                TicketPriority.MEDIUM
        );

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User technician = new User();
        technician.setName("Technician name");
        technician.setActive(true);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setId(updaterId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description 2", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(technician));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(technician, ticketId, detailsUpdateDto);

        Assertions.assertEquals("Title 2", ticket.getTitle());
        Assertions.assertEquals("Description 2", ticket.getDescription());
        Assertions.assertEquals(TicketPriority.MEDIUM, ticket.getPriority());
        Assertions.assertEquals(responseDto, result);

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(updaterId);
        verify(ticketRepository).save(ticket);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketHistoryService).record(ticket, technician, TicketAction.TICKET_DETAILS_UPDATED, "Ticket updated by technician Technician name");
    }

    @Test
    void shouldAssignTechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User technician = new User();
        technician.setName("Technician name");
        technician.setActive(true);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setId(technicianId);

        User user = new User();
        user.setId(userId);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description", TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", now, now, null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketResponseDto result = ticketService.assignTechnician(user, ticketId, technicianId);

        Assertions.assertEquals("Title", result.title());
        Assertions.assertEquals("Description", result.description());
        Assertions.assertEquals("Requester name", result.requesterName());
        Assertions.assertEquals("requester@email.com", result.requesterEmail());
        Assertions.assertEquals("Technician name", result.technicianName());
        Assertions.assertEquals("tech@email.com", result.technicianEmail());
        Assertions.assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        Assertions.assertSame(technician, ticket.getTechnician());
        Assertions.assertEquals(TicketPriority.MEDIUM, result.priority());
        Assertions.assertEquals(UserRole.USER, result.requesterRole());
        Assertions.assertEquals(now, result.createdAt());
        Assertions.assertEquals(now, result.updatedAt());
        Assertions.assertNull(result.resolvedAt());

        verify(userRepository).findById(userId);
        verify(userRepository).findById(technicianId);
        verify(ticketRepository).findById(ticketId);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketRepository).save(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_ASSIGNED, "Ticket assigned to technician Technician name");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAssigningTechnicianToNonexistentTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(userRepository);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAssigningTechnicianByNonexistentUser() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("User not found with ID: " + userId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verify(userRepository, never()).findById(technicianId);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTechnicianDoesNotExist() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(technicianId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("User not found with ID: " + technicianId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verify(userRepository).findById(technicianId);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowInvalidTicketStatusExceptionWhenAssigningTechnicianToNonOpenTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        User user = new User();
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));

        InvalidTicketStatusException exception = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("Ticket must be Open to be assigned", exception.getMessage());

        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowTicketAlreadyAssignedExceptionWhenTicketHasTechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User assignedTechnician = new User();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setTechnician(assignedTechnician);

        User user = new User();
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));

        TicketAlreadyAssignedException exception = Assertions.assertThrows(TicketAlreadyAssignedException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("Ticket already has an assigned technician", exception.getMessage());

        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowRoleNotAllowedExceptionWhenAssignedUserIsNotTechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User user = new User();
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);
        technician.setRole(UserRole.USER);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));

        RoleNotAllowedException exception = Assertions.assertThrows(RoleNotAllowedException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("User must be a Technician", exception.getMessage());

        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowUserNotActiveExceptionWhenAssignedTechnicianIsInactive() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        User user = new User();
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(false);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.assignTechnician(user, ticketId, technicianId));

        Assertions.assertEquals("Technician is not active", exception.getMessage());

        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldResolveTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID technicianId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        LocalDateTime now = LocalDateTime.now();

        User technician = new User();
        technician.setId(technicianId);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);
        technician.setName("Technician name");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setTechnician(technician);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description", TicketStatus.RESOLVED, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", now, now, now
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        LocalDateTime beforeResolve = LocalDateTime.now();
        TicketResponseDto result = ticketService.resolve(technician, ticketId);
        LocalDateTime afterResolve = LocalDateTime.now();

        Assertions.assertEquals("Title", result.title());
        Assertions.assertEquals("Description", result.description());
        Assertions.assertEquals("Requester name", result.requesterName());
        Assertions.assertEquals("requester@email.com", result.requesterEmail());
        Assertions.assertEquals("Technician name", result.technicianName());
        Assertions.assertEquals("tech@email.com", result.technicianEmail());
        Assertions.assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
        Assertions.assertSame(technician, ticket.getTechnician());
        Assertions.assertEquals(TicketPriority.MEDIUM, result.priority());
        Assertions.assertEquals(UserRole.USER, result.requesterRole());
        Assertions.assertEquals(now, result.createdAt());
        Assertions.assertEquals(now, result.updatedAt());
        Assertions.assertNotNull(ticket.getResolvedAt());
        Assertions.assertFalse(ticket.getResolvedAt().isBefore(beforeResolve));
        Assertions.assertFalse(ticket.getResolvedAt().isAfter(afterResolve));

        verify(userRepository).findById(technicianId);
        verify(ticketRepository).findById(ticketId);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketRepository).save(ticket);
        verify(ticketHistoryService).record(ticket, technician, TicketAction.TICKET_RESOLVED, "Ticket resolved by technician Technician name");
    }

    @Test
    void shouldThrowInvalidTicketStatusExceptionWhenTicketIsAlreadyResolved() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESOLVED);

        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidTicketStatusException exception = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketService.resolve(user, ticketId));

        Assertions.assertEquals("Ticket already resolved", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowInvalidTicketStatusExceptionWhenTicketStatusIsNotInProgress() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CLOSED);

        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidTicketStatusException exception = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketService.resolve(user, ticketId));

        Assertions.assertEquals("Ticket must be In Progress to be resolved", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowRoleNotAllowedExceptionWhenAuthenticatedUserIsNotATechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        User user = new User();
        user.setRole(UserRole.USER);
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        RoleNotAllowedException exception = Assertions.assertThrows(RoleNotAllowedException.class,
                () -> ticketService.resolve(user, ticketId));

        Assertions.assertEquals("User must be a technician to resolve this ticket", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowUserNotActiveExceptionWhenTechnicianIsNotActive() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        User technician = new User();
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(false);
        technician.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(technician));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.resolve(technician, ticketId));

        Assertions.assertEquals("Technician is not active", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowTechnicianNotResponsibleExceptionWhenAnotherTechnicianIsAssignedToTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID user2Id = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");

        User technician = new User();
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);
        technician.setId(userId);

        User anotherTechnician = new User();
        anotherTechnician.setRole(UserRole.TECHNICIAN);
        anotherTechnician.setActive(true);
        anotherTechnician.setId(user2Id);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setTechnician(anotherTechnician);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(technician));

        TechnicianNotResponsibleException exception = Assertions.assertThrows(TechnicianNotResponsibleException.class,
                () -> ticketService.resolve(technician, ticketId));

        Assertions.assertEquals("Technician not responsible for this ticket", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowTechnicianNotResponsibleExceptionWhenTicketHasNoAssignedTechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        User technician = new User();
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);
        technician.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(technician));

        TechnicianNotResponsibleException exception = Assertions.assertThrows(TechnicianNotResponsibleException.class,
                () -> ticketService.resolve(technician, ticketId));

        Assertions.assertEquals("Ticket must have a technician to be resolved", exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAuthenticatedTechnicianIsNotFound() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        User technician = new User();
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);
        technician.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.resolve(technician, ticketId));

        Assertions.assertEquals("User not found with ID: " + userId, exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTicketIsNotFound() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User technician = new User();
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);
        technician.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.resolve(technician, ticketId));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(userRepository);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    void shouldCloseTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setId(userId);
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setName("Requester name");
        user.setEmail("requester@email.com");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setRequester(user);
        ticket.setResolvedAt(now);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description", TicketStatus.CLOSED, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", now, now, now
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.close(user, ticketId);

        Assertions.assertEquals("Title", result.title());
        Assertions.assertEquals("Description", result.description());
        Assertions.assertEquals("Requester name", result.requesterName());
        Assertions.assertEquals("requester@email.com", result.requesterEmail());
        Assertions.assertEquals("Technician name", result.technicianName());
        Assertions.assertEquals("tech@email.com", result.technicianEmail());
        Assertions.assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        Assertions.assertSame(user, ticket.getRequester());
        Assertions.assertEquals(TicketPriority.MEDIUM, result.priority());
        Assertions.assertEquals(UserRole.USER, result.requesterRole());
        Assertions.assertEquals(now, result.createdAt());
        Assertions.assertEquals(now, result.updatedAt());
        Assertions.assertEquals(now, ticket.getResolvedAt());

        verify(userRepository).findById(userId);
        verify(ticketRepository).findById(ticketId);
        verify(ticketMapper).convertToResponseDto(ticket);
        verify(ticketRepository).save(ticket);
        verify(ticketHistoryService).record(ticket, user, TicketAction.TICKET_CLOSED, "Ticket closed by Requester name");
    }

    @Test
    void shouldThrowInvalidTicketStatusExceptionWhenTicketIsNotResolved() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidTicketStatusException exception = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketService.close(user, ticketId));

        Assertions.assertEquals("Ticket must be Resolved to be Closed", exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(ticketMapper);
        verify(ticketRepository, never()).save(ticket);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowUserNotActiveExceptionWhenAuthenticatedUserIsInactive() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.RESOLVED);

        User user = new User();
        user.setId(userId);
        user.setActive(false);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.close(user, ticketId));

        Assertions.assertEquals("User must be active to close this ticket", exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(ticketMapper);
        verify(ticketRepository, never()).save(ticket);
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenClosingNonexistentTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User user = new User();
        user.setId(userId);
        user.setActive(true);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.close(user, ticketId));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, exception.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(ticketMapper);
        verify(ticketRepository, never()).save(any());
        verifyNoInteractions(ticketHistoryService);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenClosingTicketByNonexistentUser() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");


        Ticket ticket = new Ticket();
        ticket.setId(ticketId);

        User user = new User();
        user.setId(userId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.close(user, ticketId));

        Assertions.assertEquals("User not found with ID: " + userId, exception.getMessage());

        verifyNoInteractions(ticketMapper);
        verifyNoInteractions(ticketHistoryService);
        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verify(ticketRepository, never()).save(any());
    }
}
