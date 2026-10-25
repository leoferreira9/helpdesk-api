package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.TicketCommentRequestDto;
import com.leonardo.helpdesk.dto.response.TicketCommentResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.TicketComment;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketStatus;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setId(ticketId);

        User user = new User();
        user.setActive(true);
        user.setId(userId);

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
}