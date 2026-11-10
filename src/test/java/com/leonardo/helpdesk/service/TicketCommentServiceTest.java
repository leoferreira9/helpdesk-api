package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.TicketCommentRequestDto;
import com.leonardo.helpdesk.dto.response.TicketCommentResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.TicketComment;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.InvalidTicketStatusException;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.exception.UserNotActiveException;
import com.leonardo.helpdesk.mapper.TicketCommentMapper;
import com.leonardo.helpdesk.repository.TicketCommentRepository;
import com.leonardo.helpdesk.repository.TicketRepository;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    @InjectMocks
    private TicketCommentService ticketCommentService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private TicketCommentMapper ticketCommentMapper;

    @Test
    void shouldCreateTicketComment() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID ticketCommentId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);

        TicketComment ticketComment = new TicketComment();

        TicketCommentResponseDto ticketCommentResponseDto = new TicketCommentResponseDto(
                ticketCommentId,
                userId,
                ticketId,
                "Username",
                "Comment",
                now
        );

        TicketCommentRequestDto ticketCommentRequestDto = new TicketCommentRequestDto("Comment");

        ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ticketCommentMapper.convertToResponseDto(ticketComment)).thenReturn(ticketCommentResponseDto);
        when(ticketCommentRepository.save(any(TicketComment.class))).thenReturn(ticketComment);

        TicketCommentResponseDto result = ticketCommentService.create(user, ticketId, ticketCommentRequestDto);

        verify(ticketCommentRepository).save(captor.capture());

        TicketComment captured = captor.getValue();

        Assertions.assertEquals("Comment", captured.getComment());
        Assertions.assertEquals("Username", result.userName());
        Assertions.assertEquals(now, result.createdAt());
        Assertions.assertSame(user, captured.getUser());
        Assertions.assertSame(ticket, captured.getTicket());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verify(ticketCommentMapper).convertToResponseDto(ticketComment);
    }

    @Test
    void shouldCreateTicketCommentWhenUserIsAssignedTechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID ticketCommentId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        UUID technicianId = UUID.fromString("b74e6c2a-1f93-4d85-a7b0-9c42e6f3185d");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);
        technician.setActive(true);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setName("Technician");
        technician.setEmail("tech@email.com");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);
        ticket.setTechnician(technician);

        TicketComment ticketComment = new TicketComment();

        TicketCommentResponseDto ticketCommentResponseDto = new TicketCommentResponseDto(
                ticketCommentId,
                technicianId,
                ticketId,
                "Technician",
                "Comment",
                now
        );

        TicketCommentRequestDto ticketCommentRequestDto = new TicketCommentRequestDto("Comment");

        ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));
        when(ticketCommentMapper.convertToResponseDto(ticketComment)).thenReturn(ticketCommentResponseDto);
        when(ticketCommentRepository.save(any(TicketComment.class))).thenReturn(ticketComment);

        TicketCommentResponseDto result = ticketCommentService.create(technician, ticketId, ticketCommentRequestDto);

        verify(ticketCommentRepository).save(captor.capture());

        TicketComment captured = captor.getValue();

        Assertions.assertEquals("Comment", captured.getComment());
        Assertions.assertEquals("Technician", result.userName());
        Assertions.assertEquals(now, result.createdAt());
        Assertions.assertSame(technician, captured.getUser());
        Assertions.assertSame(ticket, captured.getTicket());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(technicianId);
        verify(ticketCommentMapper).convertToResponseDto(ticketComment);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTechnicianIsNotAssignedToTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID technicianId = UUID.fromString("b74e6c2a-1f93-4d85-a7b0-9c42e6f3185d");
        UUID technicianId2 = UUID.fromString("3e91a7d4-62b8-4f0c-9a35-d8c71e2465fb");

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);
        technician.setActive(true);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setName("Technician");
        technician.setEmail("tech@email.com");

        User technician2 = new User();
        technician2.setId(technicianId2);
        technician2.setActive(true);
        technician2.setRole(UserRole.TECHNICIAN);
        technician2.setName("Technician2");
        technician2.setEmail("tech2@email.com");

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);
        ticket.setTechnician(technician);

        TicketCommentRequestDto ticketCommentRequestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(technicianId2)).thenReturn(Optional.of(technician2));

        AccessDeniedException result = Assertions.assertThrows(AccessDeniedException.class,
                () -> ticketCommentService.create(technician2, ticketId, ticketCommentRequestDto));

        Assertions.assertEquals("Can't comment, ticket does not belong to authenticated user", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(technicianId2);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenRequesterDoesNotOwnTicket() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID technicianId = UUID.fromString("b74e6c2a-1f93-4d85-a7b0-9c42e6f3185d");
        UUID userId2 = UUID.fromString("3e91a7d4-62b8-4f0c-9a35-d8c71e2465fb");

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        User technician = new User();
        technician.setId(technicianId);
        technician.setActive(true);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setName("Technician");
        technician.setEmail("tech@email.com");

        User user2 = new User();
        user2.setId(userId2);
        user2.setActive(true);
        user2.setRole(UserRole.USER);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);
        ticket.setTechnician(technician);

        TicketCommentRequestDto ticketCommentRequestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        AccessDeniedException result = Assertions.assertThrows(AccessDeniedException.class,
                () -> ticketCommentService.create(user2, ticketId, ticketCommentRequestDto));

        Assertions.assertEquals("Can't comment, ticket does not belong to authenticated user", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId2);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }

    @Test
    void shouldCreateTicketCommentWhenUserIsAdmin() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID adminId = UUID.fromString("b74e6c2a-1f93-4d85-a7b0-9c42e6f3185d");
        UUID ticketCommentId = UUID.fromString("7b3e9a42-6f81-4c25-a9d0-13e8b57c24f6");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setId(userId);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);
        ticket.setRequester(user);

        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        TicketComment ticketComment = new TicketComment();

        TicketCommentResponseDto ticketCommentResponseDto = new TicketCommentResponseDto(
                ticketCommentId,
                adminId,
                ticketId,
                "Admin",
                "Comment",
                now
        );

        TicketCommentRequestDto ticketCommentRequestDto = new TicketCommentRequestDto("Comment");

        ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(ticketCommentMapper.convertToResponseDto(ticketComment)).thenReturn(ticketCommentResponseDto);
        when(ticketCommentRepository.save(any(TicketComment.class))).thenReturn(ticketComment);

        TicketCommentResponseDto result = ticketCommentService.create(admin, ticketId, ticketCommentRequestDto);

        verify(ticketCommentRepository).save(captor.capture());

        TicketComment captured = captor.getValue();

        Assertions.assertEquals("Comment", captured.getComment());
        Assertions.assertEquals("Admin", result.userName());
        Assertions.assertEquals(now, result.createdAt());
        Assertions.assertSame(admin, captured.getUser());
        Assertions.assertSame(ticket, captured.getTicket());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(adminId);
        verify(ticketCommentMapper).convertToResponseDto(ticketComment);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTicketHasNoAssignedTechnician() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID requesterId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");
        UUID technicianId = UUID.fromString("b74e6c2a-1f93-4d85-a7b0-9c42e6f3185d");

        User requester = new User();
        requester.setId(requesterId);
        requester.setRole(UserRole.USER);
        requester.setActive(true);

        User technician = new User();
        technician.setId(technicianId);
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(requester);

        TicketCommentRequestDto requestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(technicianId)).thenReturn(Optional.of(technician));

        AccessDeniedException result = Assertions.assertThrows(AccessDeniedException.class,
                () -> ticketCommentService.create(technician, ticketId, requestDto));

        Assertions.assertEquals("Can't comment, ticket does not belong to authenticated user", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(technicianId);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTicketDoesNotExist() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketCommentRequestDto requestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        ResourceNotFoundException result = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketCommentService.create(user, ticketId, requestDto));

        Assertions.assertEquals("Ticket not found with ID: " + ticketId, result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.USER);
        user.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(user);

        TicketCommentRequestDto requestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException result = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> ticketCommentService.create(user, ticketId, requestDto));

        Assertions.assertEquals("User not found with ID: " + userId, result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }

    @Test
    void shouldThrowUserNotActiveExceptionWhenUserIsInactive() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.USER);
        user.setActive(false);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(user);

        TicketCommentRequestDto requestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserNotActiveException result = Assertions.assertThrows(UserNotActiveException.class,
                () -> ticketCommentService.create(user, ticketId, requestDto));

        Assertions.assertEquals("User is not active", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }

    @Test
    void shouldThrowInvalidTicketStatusExceptionWhenTicketIsClosed() {
        UUID ticketId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UUID userId = UUID.fromString("8c4f0b53-7a92-4d36-b0e1-24f9c68d35a7");

        User user = new User();
        user.setId(userId);
        user.setRole(UserRole.USER);
        user.setActive(true);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setRequester(user);

        TicketCommentRequestDto requestDto = new TicketCommentRequestDto("Comment");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidTicketStatusException result = Assertions.assertThrows(InvalidTicketStatusException.class,
                () -> ticketCommentService.create(user, ticketId, requestDto));

        Assertions.assertEquals("Can't comment, this ticket is closed", result.getMessage());

        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(ticketCommentRepository);
        verifyNoInteractions(ticketCommentMapper);
    }
}
