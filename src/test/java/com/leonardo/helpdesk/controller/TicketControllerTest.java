package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.config.SecurityConfig;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketPriority;
import com.leonardo.helpdesk.enums.TicketStatus;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.service.JwtService;
import com.leonardo.helpdesk.service.TicketCommentService;
import com.leonardo.helpdesk.service.TicketHistoryService;
import com.leonardo.helpdesk.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(SecurityConfig.class)
class TicketControllerTest {

    @MockitoBean
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TicketHistoryService ticketHistoryService;

    @MockitoBean
    private TicketCommentService ticketCommentService;

    @Test
    void shouldAllowAdminToAssignTechnician() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");
        UUID technicianId = UUID.fromString("e821e8b9-a139-46f7-a51f-7fe27bcb26ef");

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@email.com");
        admin.setRole(UserRole.ADMIN);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Erro ao acessar o sistema",
                "O usuário não consegue acessar o painel administrativo.",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                "Maria Silva",
                "maria@email.com",
                UserRole.USER,
                "João Santos",
                "joao@email.com",
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15),
                null
        );

        when(ticketService.assignTechnician(admin, ticketId, technicianId))
                .thenReturn(responseDto);

        mvc.perform (
                patch("/api/v1/tickets/{ticketId}/assign", ticketId)
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(technicianId))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Erro ao acessar o sistema"))
                .andExpect(jsonPath("$.description").value("O usuário não consegue acessar o painel administrativo."))
                .andExpect(jsonPath("$.status").value(TicketStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.HIGH.name()))
                .andExpect(jsonPath("$.requesterName").value("Maria Silva"))
                .andExpect(jsonPath("$.requesterEmail").value("maria@email.com"))
                .andExpect(jsonPath("$.requesterRole").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.technicianName").value("João Santos"))
                .andExpect(jsonPath("$.technicianEmail").value("joao@email.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-17T09:30:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-17T10:15:00"))
                .andExpect(jsonPath("$.resolvedAt").doesNotExist());

        verify(ticketService).assignTechnician(admin, ticketId, technicianId);
    }

    @Test
    void shouldDenyNonAdminFromAssigningTechnician() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");
        UUID technicianId = UUID.fromString("e821e8b9-a139-46f7-a51f-7fe27bcb26ef");

        User user = new User();
        user.setName("Use");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);

        mvc.perform (
                patch("/api/v1/tickets/{ticketId}/assign", ticketId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(technicianId))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(ticketService);
    }

    @Test
    void shouldAllowTechnicianToResolveTicket() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        User technician = new User();
        technician.setName("João Santos");
        technician.setEmail("joao@email.com");
        technician.setRole(UserRole.TECHNICIAN);
        technician.setActive(true);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Erro ao acessar o sistema",
                "O usuário não consegue acessar o painel administrativo.",
                TicketStatus.RESOLVED,
                TicketPriority.HIGH,
                "Maria Silva",
                "maria@email.com",
                UserRole.USER,
                "João Santos",
                "joao@email.com",
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15),
                LocalDateTime.of(2026, 11, 17, 10, 15)
        );

        when(ticketService.resolve(technician, ticketId))
                .thenReturn(responseDto);

        mvc.perform (
                patch("/api/v1/tickets/{ticketId}/resolve", ticketId)
                        .with(user(technician))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Erro ao acessar o sistema"))
                .andExpect(jsonPath("$.description").value("O usuário não consegue acessar o painel administrativo."))
                .andExpect(jsonPath("$.status").value(TicketStatus.RESOLVED.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.HIGH.name()))
                .andExpect(jsonPath("$.requesterName").value("Maria Silva"))
                .andExpect(jsonPath("$.requesterEmail").value("maria@email.com"))
                .andExpect(jsonPath("$.requesterRole").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.technicianName").value("João Santos"))
                .andExpect(jsonPath("$.technicianEmail").value("joao@email.com"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-17T09:30:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-17T10:15:00"))
                .andExpect(jsonPath("$.resolvedAt").value("2026-11-17T10:15:00"));

        verify(ticketService).resolve(technician, ticketId);
    }

    @Test
    void shouldDenyNonTechnicianFromResolvingTicket() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        mvc.perform (
                patch("/api/v1/tickets/{ticketId}/resolve", ticketId)
                        .with(user(user))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(ticketService);
    }

    @Test
    void shouldDenyUnauthenticatedAccessToTicketById() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        mvc.perform (
                get("/api/v1/tickets/{id}", ticketId)
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(ticketService);
    }
}