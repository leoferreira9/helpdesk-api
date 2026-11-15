package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.config.SecurityConfig;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.service.JwtService;
import com.leonardo.helpdesk.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void shouldAllowAdminToFindUserById() throws Exception {
        UUID id = UUID.fromString("427ff5e6-1b2f-4be8-8de5-65bd27f0f450");
        LocalDateTime now = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                id,
                "User",
                "email@email.com",
                UserRole.USER,
                true,
                now
        );

        when(service.findById(id)).thenReturn(responseDto);

        mvc.perform(
            get("/api/v1/users/{id}", id)
                    .with(user("admin").roles("ADMIN"))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("email@email.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.toString()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldDenyNonAdminFromFindingUserById() throws Exception {
        UUID id = UUID.fromString("427ff5e6-1b2f-4be8-8de5-65bd27f0f450");

        mvc.perform(
                get("/api/v1/users/{id}", id)
                        .with(user("User").roles("USER"))
        ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(service);
    }

    @Test
    void shouldAllowAdminToFindAllUsers() throws Exception {
        UUID userId = UUID.fromString("9c2d7f41-6a83-4e95-b127-5f8d3a60c214");
        LocalDateTime now = LocalDateTime.now();

        UserResponseDto user = new UserResponseDto(
                userId,
                "User",
                "user@email.com",
                UserRole.USER,
                true,
                now
        );

        List<UserResponseDto> users = List.of(user);
        Pageable pageable = PageRequest.of(0, 20);
        Page<UserResponseDto> page = new PageImpl<>(users, pageable, users.size());

        when(service.findAll(pageable)).thenReturn(page);

        mvc.perform(
                get("/api/v1/users")
                        .with(user("Admin").roles("ADMIN"))
        ).andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].name").value("User"));

        verify(service).findAll(pageable);
    }

    @Test
    void shouldDenyNonAdminFromFindingAllUsers() throws Exception {
        mvc.perform(
                get("/api/v1/users")
                        .with(user("User").roles("USER"))
        ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(service);
    }

    @Test
    void shouldFindCurrentUserThroughMeEndpoint() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        LocalDateTime now = LocalDateTime.now();

        User authenticatedUser = new User();
        authenticatedUser.setId(userId);
        authenticatedUser.setName("User");
        authenticatedUser.setEmail("user@email.com");
        authenticatedUser.setPassword("password");
        authenticatedUser.setRole(UserRole.USER);
        authenticatedUser.setActive(true);

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "User",
                "user@email.com",
                UserRole.USER,
                true,
                now
        );

        when(service.findCurrentUser(authenticatedUser)).thenReturn(responseDto);

        mvc.perform(
                get("/api/v1/users/me")
                        .with(user(authenticatedUser))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).findCurrentUser(authenticatedUser);
    }
}
