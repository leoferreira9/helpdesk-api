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
import com.leonardo.helpdesk.exception.InvalidTicketStatusException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        when(ticketRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        Page<TicketResponseDto> result = ticketService.findAll(pageable);


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

        verify(ticketRepository).findAll(pageable);
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

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.of(user));

        when(ticketRepository.save(ticket))
                .thenReturn(ticket);

        when(ticketMapper.convertToResponseDto(ticket))
                .thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.of(user));

        InvalidTicketStatusException exception = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId));

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

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.of(user));

        UserNotActiveException exception = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId));

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

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId));

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

        when(ticketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(userRepository.findById(updaterId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId));

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description 2", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description 2", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description 2", TicketStatus.OPEN, TicketPriority.HIGH,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title", "Description", TicketStatus.OPEN, TicketPriority.HIGH,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(user));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, null, updaterId);

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
        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId, "Title 2", "Description 2", TicketStatus.OPEN, TicketPriority.MEDIUM,
                "Requester name", "requester@email.com", UserRole.USER,
                "Technician name", "tech@email.com", LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(updaterId)).thenReturn(Optional.of(technician));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.convertToResponseDto(ticket)).thenReturn(responseDto);

        TicketResponseDto result = ticketService.updateDetails(ticketId, detailsUpdateDto, updaterId);

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
}
