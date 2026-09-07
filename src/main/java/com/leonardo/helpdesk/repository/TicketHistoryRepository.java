package com.leonardo.helpdesk.repository;

import com.leonardo.helpdesk.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, UUID> {}
