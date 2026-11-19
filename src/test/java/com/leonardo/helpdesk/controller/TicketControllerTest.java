package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.config.SecurityConfig;
import com.leonardo.helpdesk.dto.request.TicketCommentRequestDto;
import com.leonardo.helpdesk.dto.request.TicketRequestDto;
import com.leonardo.helpdesk.dto.response.TicketCommentResponseDto;
import com.leonardo.helpdesk.dto.response.TicketHistoryResponseDto;
import com.leonardo.helpdesk.dto.response.TicketResponseDto;
import com.leonardo.helpdesk.dto.update.TicketDetailsUpdateDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.TicketAction;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    void shouldCreateTicketAsAuthenticatedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketRequestDto requestDto = new TicketRequestDto(
                "Title",
                "Description",
                TicketPriority.LOW
        );

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Title",
                "Description",
                TicketStatus.OPEN,
                TicketPriority.LOW,
                "User",
                "user@email.com",
                UserRole.USER,
                null,
                null,
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15),
                null
        );

        when(ticketService.create(user, requestDto))
                .thenReturn(responseDto);

        mvc.perform(
                post("/api/v1/tickets")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value(TicketStatus.OPEN.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.LOW.name()))
                .andExpect(jsonPath("$.requesterName").value("User"))
                .andExpect(jsonPath("$.requesterEmail").value("user@email.com"))
                .andExpect(jsonPath("$.requesterRole").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.technicianName").doesNotExist())
                .andExpect(jsonPath("$.technicianEmail").doesNotExist())
                .andExpect(jsonPath("$.createdAt").value("2026-11-17T09:30:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-17T10:15:00"))
                .andExpect(jsonPath("$.resolvedAt").doesNotExist());

        verify(ticketService).create(user, requestDto);
    }

    @Test
    void shouldFindTicketByIdAsAuthenticatedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Title",
                "Description",
                TicketStatus.OPEN,
                TicketPriority.LOW,
                "User",
                "user@email.com",
                UserRole.USER,
                null,
                null,
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15),
                null
        );

        when(ticketService.findById(user, ticketId)).thenReturn(responseDto);

        mvc.perform(
                get("/api/v1/tickets/{id}", ticketId)
                        .with(user(user))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value(TicketStatus.OPEN.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.LOW.name()))
                .andExpect(jsonPath("$.requesterName").value("User"))
                .andExpect(jsonPath("$.requesterEmail").value("user@email.com"))
                .andExpect(jsonPath("$.requesterRole").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.technicianName").doesNotExist())
                .andExpect(jsonPath("$.technicianEmail").doesNotExist())
                .andExpect(jsonPath("$.createdAt").value("2026-11-17T09:30:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-17T10:15:00"))
                .andExpect(jsonPath("$.resolvedAt").doesNotExist());

        verify(ticketService).findById(user, ticketId);
    }

    @Test
    void shouldFindAllTicketsWithFilters() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");
        UUID requesterId = UUID.fromString("f6758cc2-f346-41a5-bb93-b29bd0baaa4d");
        UUID technicianId = UUID.fromString("e821e8b9-a139-46f7-a51f-7fe27bcb26ef");
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);
        Pageable pageable = PageRequest.of(0, 20);

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@email.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Title",
                "Description",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                "User",
                "user@email.com",
                UserRole.USER,
                "Technician",
                "technician@email.com",
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15),
                null
        );
        Page<TicketResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);

        when(ticketService.findAll(
                admin,
                pageable,
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                requesterId,
                technicianId,
                startDate,
                endDate
        )).thenReturn(page);

        mvc.perform(
                get("/api/v1/tickets")
                        .with(user(admin))
                        .param("status", TicketStatus.IN_PROGRESS.name())
                        .param("priority", TicketPriority.HIGH.name())
                        .param("requesterId", requesterId.toString())
                        .param("technicianId", technicianId.toString())
                        .param("start", "01/11/2026")
                        .param("end", "30/11/2026")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ticketId.toString()))
                .andExpect(jsonPath("$.content[0].status").value(TicketStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.content[0].priority").value(TicketPriority.HIGH.name()));

        verify(ticketService).findAll(
                admin,
                pageable,
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                requesterId,
                technicianId,
                startDate,
                endDate
        );
    }

    @Test
    void shouldUpdateTicketDetailsAsAuthenticatedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketDetailsUpdateDto updateDto = new TicketDetailsUpdateDto(
                "Updated title",
                "Updated description",
                TicketPriority.HIGH
        );

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Updated title",
                "Updated description",
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                "User",
                "user@email.com",
                UserRole.USER,
                null,
                null,
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15),
                null
        );

        when(ticketService.updateDetails(user, ticketId, updateDto)).thenReturn(responseDto);

        mvc.perform(
                patch("/api/v1/tickets/{ticketId}", ticketId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.priority").value(TicketPriority.HIGH.name()));

        verify(ticketService).updateDetails(user, ticketId, updateDto);
    }

    @Test
    void shouldCloseTicketAsAuthenticatedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");

        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketResponseDto responseDto = new TicketResponseDto(
                ticketId,
                "Title",
                "Description",
                TicketStatus.CLOSED,
                TicketPriority.LOW,
                "User",
                "user@email.com",
                UserRole.USER,
                "Technician",
                "technician@email.com",
                LocalDateTime.of(2026, 11, 17, 9, 30),
                LocalDateTime.of(2026, 11, 17, 11, 30),
                LocalDateTime.of(2026, 11, 17, 10, 15)
        );

        when(ticketService.close(user, ticketId)).thenReturn(responseDto);

        mvc.perform(
                patch("/api/v1/tickets/{ticketId}/close", ticketId)
                        .with(user(user))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.status").value(TicketStatus.CLOSED.name()))
                .andExpect(jsonPath("$.resolvedAt").value("2026-11-17T10:15:00"));

        verify(ticketService).close(user, ticketId);
    }

    @Test
    void shouldCreateCommentAsAuthorizedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");
        UUID userId = UUID.fromString("f6758cc2-f346-41a5-bb93-b29bd0baaa4d");
        UUID commentId = UUID.fromString("bc4dc3e2-40bd-47c3-badf-2941c0c51dc9");

        User user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketCommentRequestDto requestDto = new TicketCommentRequestDto("Additional information");
        TicketCommentResponseDto responseDto = new TicketCommentResponseDto(
                commentId,
                userId,
                ticketId,
                "User",
                "Additional information",
                LocalDateTime.of(2026, 11, 17, 10, 30)
        );

        when(ticketCommentService.create(user, ticketId, requestDto)).thenReturn(responseDto);

        mvc.perform(
                post("/api/v1/tickets/{id}/comments", ticketId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.userName").value("User"))
                .andExpect(jsonPath("$.comment").value("Additional information"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-17T10:30:00"));

        verify(ticketCommentService).create(user, ticketId, requestDto);
    }

    @Test
    void shouldFindCommentsByTicketIdAsAuthorizedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");
        UUID userId = UUID.fromString("f6758cc2-f346-41a5-bb93-b29bd0baaa4d");
        UUID commentId = UUID.fromString("bc4dc3e2-40bd-47c3-badf-2941c0c51dc9");

        User user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketCommentResponseDto responseDto = new TicketCommentResponseDto(
                commentId,
                userId,
                ticketId,
                "User",
                "Additional information",
                LocalDateTime.of(2026, 11, 17, 10, 30)
        );

        when(ticketCommentService.findByTicketId(user, ticketId)).thenReturn(List.of(responseDto));

        mvc.perform(
                get("/api/v1/tickets/{id}/comments", ticketId)
                        .with(user(user))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$[0].comment").value("Additional information"));

        verify(ticketCommentService).findByTicketId(user, ticketId);
    }

    @Test
    void shouldFindTicketHistoryByIdAsAuthorizedUser() throws Exception {
        UUID ticketId = UUID.fromString("7a8f4bb2-5b9d-4c63-91f5-66e41064bd82");
        UUID userId = UUID.fromString("f6758cc2-f346-41a5-bb93-b29bd0baaa4d");
        UUID historyId = UUID.fromString("31dd29ac-6315-4eec-b0c1-53f5d7e57854");

        User user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@email.com");
        user.setRole(UserRole.USER);
        user.setActive(true);

        TicketHistoryResponseDto responseDto = new TicketHistoryResponseDto(
                historyId,
                ticketId,
                TicketAction.TICKET_CREATED,
                "Ticket created by requester User",
                userId,
                "User",
                LocalDateTime.of(2026, 11, 17, 9, 30)
        );

        when(ticketHistoryService.findByTicketId(user, ticketId)).thenReturn(List.of(responseDto));

        mvc.perform(
                get("/api/v1/tickets/{id}/history", ticketId)
                        .with(user(user))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(historyId.toString()))
                .andExpect(jsonPath("$[0].ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$[0].action").value(TicketAction.TICKET_CREATED.name()))
                .andExpect(jsonPath("$[0].description").value("Ticket created by requester User"))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].userName").value("User"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-11-17T09:30:00"));

        verify(ticketHistoryService).findByTicketId(user, ticketId);
    }
}
