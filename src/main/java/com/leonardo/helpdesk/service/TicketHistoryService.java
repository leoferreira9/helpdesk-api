package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.entity.TicketHistory;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketAction;
import com.leonardo.helpdesk.mapper.TicketHistoryMapper;
import com.leonardo.helpdesk.repository.TicketHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketHistoryMapper ticketHistoryMapper;

    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository, TicketHistoryMapper ticketHistoryMapper) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketHistoryMapper = ticketHistoryMapper;
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
    public List<TicketHistoryResponseDto> findByTicketId(UUID ticketId) {
        return ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(ticketHistoryMapper::convertToResponseDto).toList();
    }
}
