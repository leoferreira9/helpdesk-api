package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.config.SecurityConfig;
import com.leonardo.helpdesk.dto.response.DashboardResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.service.DashboardService;
import com.leonardo.helpdesk.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private DashboardService dashboardService;

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldReturnSummaryWhenUserIsAuthenticated() throws Exception {
        UUID userId = UUID.fromString("0722d277-d2e3-48f2-bdd7-c9f1fc6c0d07");

        User authenticatedUser = new User();
        authenticatedUser.setId(userId);
        authenticatedUser.setName("User");
        authenticatedUser.setEmail("user@email.com");
        authenticatedUser.setPassword("password");
        authenticatedUser.setRole(UserRole.USER);
        authenticatedUser.setActive(true);

        DashboardResponseDto responseDto = new DashboardResponseDto(
                12L,
                5L,
                1L,
                5L,
                1L
        );

        when(dashboardService.summary(authenticatedUser)).thenReturn(responseDto);

        mvc.perform(
                get("/api/v1/dashboard/summary")
                        .with(user(authenticatedUser))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12L))
                .andExpect(jsonPath("$.open").value(5L))
                .andExpect(jsonPath("$.inProgress").value(1L))
                .andExpect(jsonPath("$.resolved").value(5L))
                .andExpect(jsonPath("$.closed").value(1L));
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(
                get("/api/v1/dashboard/summary")
        ).andExpect(status().isUnauthorized());
    }
}