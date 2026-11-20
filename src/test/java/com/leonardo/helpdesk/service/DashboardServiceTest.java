package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.response.DashboardResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private TicketRepository ticketRepository;

    @Test
    void shouldReturnSummaryForAdmin() {

        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setName("admin");
        admin.setEmail("admin@email.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        when(ticketRepository.count())
                .thenReturn(12L);

        when(ticketRepository.countByStatus(TicketStatus.OPEN))
                .thenReturn(5L);

        when(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS))
                .thenReturn(1L);

        when(ticketRepository.countByStatus(TicketStatus.RESOLVED))
                .thenReturn(5L);

        when(ticketRepository.countByStatus(TicketStatus.CLOSED))
                .thenReturn(1L);

        DashboardResponseDto result = dashboardService.summary(admin);

        Assertions.assertEquals(12L, result.total());
        Assertions.assertEquals(5L, result.open());
        Assertions.assertEquals(1L, result.inProgress());
        Assertions.assertEquals(5L, result.resolved());
        Assertions.assertEquals(1L, result.closed());
    }

    @Test
    void shouldReturnSummaryForUser() {
        UUID userId = UUID.fromString("4476a347-bc24-41ba-b6a2-ab0dd3cf660b");

        User user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        when(ticketRepository.countByRequesterId(userId))
                .thenReturn(12L);

        when(ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.OPEN))
                .thenReturn(5L);

        when(ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.IN_PROGRESS))
                .thenReturn(1L);

        when(ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.RESOLVED))
                .thenReturn(5L);

        when(ticketRepository.countByRequesterIdAndStatus(userId, TicketStatus.CLOSED))
                .thenReturn(1L);

        DashboardResponseDto result = dashboardService.summary(user);

        Assertions.assertEquals(12L, result.total());
        Assertions.assertEquals(5L, result.open());
        Assertions.assertEquals(1L, result.inProgress());
        Assertions.assertEquals(5L, result.resolved());
        Assertions.assertEquals(1L, result.closed());
    }

    @Test
    void shouldReturnSummaryForTechnician() {
        UUID technicianId = UUID.fromString("0722d277-d2e3-48f2-bdd7-c9f1fc6c0d07");

        User technician = new User();
        technician.setId(technicianId);
        technician.setName("Technician");
        technician.setEmail("tech@email.com");
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);

        when(ticketRepository.countByTechnicianId(technicianId))
                .thenReturn(12L);

        when(ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.OPEN))
                .thenReturn(5L);

        when(ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.IN_PROGRESS))
                .thenReturn(1L);

        when(ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.RESOLVED))
                .thenReturn(5L);

        when(ticketRepository.countByTechnicianIdAndStatus(technicianId, TicketStatus.CLOSED))
                .thenReturn(1L);

        DashboardResponseDto result = dashboardService.summary(technician);

        Assertions.assertEquals(12L, result.total());
        Assertions.assertEquals(5L, result.open());
        Assertions.assertEquals(1L, result.inProgress());
        Assertions.assertEquals(5L, result.resolved());
        Assertions.assertEquals(1L, result.closed());
    }
}