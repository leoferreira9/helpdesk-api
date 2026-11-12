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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketCommentMapper ticketCommentMapper;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketCommentService(TicketCommentRepository ticketCommentRepository, TicketCommentMapper ticketCommentMapper, TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketCommentMapper = ticketCommentMapper;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    private Ticket findTicketByIdOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
    }

    private User findUserByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public TicketCommentResponseDto create(User authenticatedUser, UUID id, TicketCommentRequestDto requestDto) {
        Ticket ticketExists = findTicketByIdOrThrow(id);
        User userExists = findUserByIdOrThrow(authenticatedUser.getId());

        if(!userExists.isActive()) {
            throw new UserNotActiveException("User is not active");
        }

        if(ticketExists.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidTicketStatusException("Can't comment, this ticket is closed");
        }

        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        boolean sameTechnician = ticketExists.getTechnician() != null && ticketExists.getTechnician().getId().equals(authenticatedUser.getId());
        boolean sameRequester = ticketExists.getRequester().getId().equals(authenticatedUser.getId());

        if(!isAdmin && !sameTechnician && !sameRequester) {
            throw new AccessDeniedException("Can't comment, ticket does not belong to authenticated user");
        }

        TicketComment ticketComment = new TicketComment();
        ticketComment.setComment(requestDto.comment());
        ticketComment.setTicket(ticketExists);
        ticketComment.setUser(userExists);

        TicketComment savedTicketComment = ticketCommentRepository.save(ticketComment);

        return ticketCommentMapper.convertToResponseDto(savedTicketComment);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponseDto> findByTicketId(User authenticatedUser, UUID id) {
        Ticket ticketExists = findTicketByIdOrThrow(id);

        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        boolean sameTechnician = ticketExists.getTechnician() != null && ticketExists.getTechnician().getId().equals(authenticatedUser.getId());
        boolean sameRequester = ticketExists.getRequester().getId().equals(authenticatedUser.getId());

        if(!isAdmin && !sameTechnician && !sameRequester) {
            throw new AccessDeniedException("Can't read comments, ticket does not belong to authenticated user");
        }

        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketExists.getId())
                .stream().map(ticketCommentMapper::convertToResponseDto).toList();
    }
}
