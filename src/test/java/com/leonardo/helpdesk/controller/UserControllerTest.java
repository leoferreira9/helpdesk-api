package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.config.SecurityConfig;
import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.dto.update.ChangePasswordUpdateDto;
import com.leonardo.helpdesk.dto.update.UserUpdateDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.service.JwtService;
import com.leonardo.helpdesk.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void shouldAllowAdminToCreateUser() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        LocalDateTime now = LocalDateTime.now();

        UserRequestDto requestDto = new UserRequestDto(
                "User",
                "user@email.com",
                "test123",
                UserRole.USER
        );

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "User",
                "user@email.com",
                UserRole.USER,
                true,
                now
        );

        when(service.create(any(UserRequestDto.class))).thenReturn(responseDto);

        mvc.perform(
                post("/api/v1/users")
                        .with(user("Admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andExpect(status().isCreated());

        verify(service).create(requestDto);
    }

    @Test
    void shouldDenyNonAdminFromCreatingUser() throws Exception {
        UserRequestDto requestDto = new UserRequestDto(
                "User",
                "user@email.com",
                "test123",
                UserRole.USER
        );

        mvc.perform(
                post("/api/v1/users")
                        .with(user("User").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void shouldAllowAdminToUpdateUserById() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        UserUpdateDto updateDto = new UserUpdateDto("Name", "email@email.com");
        LocalDateTime now = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name",
                "email@email.com",
                UserRole.USER,
                true,
                now
        );

        when(service.update(userId, updateDto)).thenReturn(responseDto);

        mvc.perform(
                patch("/api/v1/users/{id}", userId)
                        .with(user("Admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("email@email.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).update(userId, updateDto);
    }

    @Test
    void shouldDenyNonAdminFromUpdatingUserById() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        UserUpdateDto updateDto = new UserUpdateDto("Name", "email@email.com");

        mvc.perform(
                patch("/api/v1/users/{id}", userId)
                        .with(user("User").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                ).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void shouldUpdateCurrentUser() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        UserUpdateDto updateDto = new UserUpdateDto("User", "user@email.com");
        LocalDateTime now = LocalDateTime.now();

        User authenticatedUser = new User();
        authenticatedUser.setId(userId);
        authenticatedUser.setName("Name");
        authenticatedUser.setEmail("email@email.com");
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

        when(service.update(userId, updateDto)).thenReturn(responseDto);

        mvc.perform(
                patch("/api/v1/users/me")
                        .with(user(authenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).update(userId, updateDto);
    }

    @Test
    void shouldAllowAdminToUpdatePasswordById() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        ChangePasswordUpdateDto updateDto = new ChangePasswordUpdateDto("newPassword");
        LocalDateTime now = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "User",
                "user@email.com",
                UserRole.USER,
                true,
                now
        );

        when(service.updatePassword(userId, updateDto)).thenReturn(responseDto);

        mvc.perform(
                put("/api/v1/users/{id}/password", userId)
                        .with(user("Admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).updatePassword(userId, updateDto);
    }

    @Test
    void shouldDenyNonAdminFromUpdatingPasswordById() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        ChangePasswordUpdateDto updateDto = new ChangePasswordUpdateDto("newPassword");

        mvc.perform(
                put("/api/v1/users/{id}/password", userId)
                        .with(user("User").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void shouldUpdateCurrentUserPassword() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");
        ChangePasswordUpdateDto updateDto = new ChangePasswordUpdateDto("newPassword");
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

        when(service.updatePassword(userId, updateDto)).thenReturn(responseDto);

        mvc.perform(
                put("/api/v1/users/me/password")
                        .with(user(authenticatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).updatePassword(userId, updateDto);
    }

    @Test
    void shouldAllowAdminToDeactivateUser() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");

        mvc.perform(
                patch("/api/v1/users/{id}/deactivate", userId)
                        .with(user("Admin").roles("ADMIN"))
        ).andExpect(status().isNoContent());

        verify(service).deactivate(userId);
    }

    @Test
    void shouldDenyNonAdminFromDeactivatingUser() throws Exception {
        UUID userId = UUID.fromString("3e5cc17f-8eac-42fe-89b3-bd4ad17cafbd");

        mvc.perform(
                patch("/api/v1/users/{id}/deactivate", userId)
                        .with(user("User").roles("USER"))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void shouldDenyUnauthenticatedAccess() throws Exception {
        mvc.perform(
                get("/api/v1/users/me")
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }
}
