package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.TicketHistory;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketAction;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.mapper.TicketHistoryMapper;
import com.leonardo.helpdesk.repository.TicketHistoryRepository;
import com.leonardo.helpdesk.repository.TicketRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketHistoryMapper ticketHistoryMapper;
    private final TicketRepository ticketRepository;

    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository, TicketHistoryMapper ticketHistoryMapper, TicketRepository ticketRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketHistoryMapper = ticketHistoryMapper;
        this.ticketRepository = ticketRepository;
    }

    public void record(Ticket ticket, User user, TicketAction action, String description) {
        TicketHistory ticketHistory = new TicketHistory();
        ticketHistory.setUser(user);
        ticketHistory.setAction(action);
        ticketHistory.setDescription(description);
        ticketHistory.setTicket(ticket);
        ticketHistoryRepository.save(ticketHistory);
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryResponseDto> findByTicketId(User authenticatedUser, UUID ticketId) {
        Ticket ticketExists = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        boolean isAdmin = authenticatedUser.getRole().equals(UserRole.ADMIN);
        boolean sameRequester = ticketExists.getRequester().getId().equals(authenticatedUser.getId());
        boolean sameTechnician = ticketExists.getTechnician() != null && ticketExists.getTechnician().getId().equals(authenticatedUser.getId());

        if(!isAdmin && !sameRequester && !sameTechnician) {
            throw new AccessDeniedException("Can't read ticket history, ticket does not belong to authenticated user");
        }

        return ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(ticketHistoryMapper::convertToResponseDto).toList();
    }
}
