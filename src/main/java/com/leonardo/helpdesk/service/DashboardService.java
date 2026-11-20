package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.response.DashboardResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository){
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponseDto summary(User authenticatedUser) {
        return switch (authenticatedUser.getRole()){
            case ADMIN -> summaryForAdmin();
            case USER -> summaryForUser(authenticatedUser.getId());
            case TECHNICIAN -> summaryForTechnician(authenticatedUser.getId());
        };
    }

    private DashboardResponseDto summaryForAdmin() {
        return new DashboardResponseDto(
                ticketRepository.count(),
                ticketRepository.countByStatus(TicketStatus.OPEN),
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS),
                ticketRepository.countByStatus(TicketStatus.RESOLVED),
                ticketRepository.countByStatus(TicketStatus.CLOSED)
        );
    }

    private DashboardResponseDto summaryForUser(UUID userId) {
        return new DashboardResponseDto(
                ticketRepository.countByRequesterId(userId),
                ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.OPEN),
                ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.IN_PROGRESS),
                ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.RESOLVED),
                ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.CLOSED)
        );
    }

    private DashboardResponseDto summaryForTechnician(UUID technicianId) {
        return new DashboardResponseDto(
                ticketRepository.countByTechnicianId(technicianId),
                ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.OPEN),
                ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.IN_PROGRESS),
                ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.RESOLVED),
                ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.CLOSED)
        );
    }
}
