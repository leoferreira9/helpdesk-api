package com.leonardo.helpdesk.repository;

import com.leonardo.helpdesk.entity.Ticket;
import com.leonardo.helpdesk.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {
    Long countByStatus(TicketStatus status);
    Long countByRequesterId(UUID requesterId);
    Long countByRequesterIdAndStatus(UUID requesterId, TicketStatus status);
    Long countByTechnicianId(UUID technicianId);
    Long countByTechnicianIdAndStatus(UUID technicianId, TicketStatus status);
}
