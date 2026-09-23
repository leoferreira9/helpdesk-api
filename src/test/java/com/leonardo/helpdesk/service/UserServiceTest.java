package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.EmailAlreadyRegisteredException;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.mapper.UserMapper;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    public UserService service;

    @Mock
    public UserRepository repository;

    @Mock
    public UserMapper mapper;

    @Mock
    public PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateUser() {

        LocalDateTime createdAt = LocalDateTime.now();

        User user = new User();

        UserRequestDto requestDto = new UserRequestDto(
                "Name",
                "email@email.com",
                "Password",
                UserRole.USER
        );

        UserResponseDto responseDto = new UserResponseDto(
                UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c"),
                "Name",
                "email@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        String exampleHash = "$2a$10$dXJ3w46eJZ9vCBq7f8b9e.aX6.D9g7W/SgqR2d8yHj9Y0Z4E7b7q6";

        when(passwordEncoder.encode(requestDto.password()))
                .thenReturn(exampleHash);

        when(mapper.convertToEntity(requestDto))
                .thenReturn(user);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail("email@email.com"))
                .thenReturn(false);

        UserResponseDto resultado = service.create(requestDto);

        Assertions.assertEquals(UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c"), resultado.id());
        Assertions.assertEquals("Name", resultado.name());
        Assertions.assertEquals("email@email.com", resultado.email());
        Assertions.assertEquals(UserRole.USER, resultado.role());
        Assertions.assertEquals(createdAt, resultado.createdAt());
        Assertions.assertTrue(resultado.active());
        Assertions.assertEquals(UserRole.USER, user.getRole());
        Assertions.assertEquals(exampleHash, user.getPassword());

        verify(repository).existsByEmail(requestDto.email());
        verify(repository).save(user);
        verify(mapper).convertToResponseDto(user);
        verify(mapper).convertToEntity(requestDto);
        verify(passwordEncoder).encode(requestDto.password());
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithEmailAlreadyRegistered() {
        UserRequestDto requestDto = new UserRequestDto(
                "Name",
                "email@email.com",
                "Password",
                UserRole.USER
        );

        when(repository.existsByEmail(requestDto.email()))
                .thenReturn(true);

        EmailAlreadyRegisteredException exception = Assertions.assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.create(requestDto));

        Assertions.assertEquals("Email email@email.com already registered", exception.getMessage());

        verify(repository).existsByEmail("email@email.com");
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper, passwordEncoder);
    }

    @Test
    void shouldFindUserById() {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = new User();
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name",
                "email@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto response = service.findById(userId);

        Assertions.assertEquals(userId, response.id());
        Assertions.assertEquals("Name", response.name());
        Assertions.assertEquals("email@email.com", response.email());
        Assertions.assertEquals(UserRole.USER, response.role());
        Assertions.assertTrue(response.active());
        Assertions.assertEquals(createdAt, response.createdAt());

        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
    }

    @Test
    void shouldThrowExceptionWhenFindingUserById() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");

        when(repository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows( ResourceNotFoundException.class,
                () -> service.findById(userId));

        Assertions.assertEquals("User not found with ID: " + userId, exception.getMessage());

        verify(repository).findById(userId);
        verifyNoInteractions(mapper);
    }
}