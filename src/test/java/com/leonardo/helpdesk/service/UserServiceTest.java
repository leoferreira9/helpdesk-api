package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.dto.update.UserUpdateDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    void shouldFindAllUsers() {
        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c"),
                "Name",
                "email@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        User user = new User();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<User> pageable = new PageImpl<>(List.of(user), pageRequest, 1);

        when(repository.findAll(pageRequest))
                .thenReturn(pageable);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result = service.findAll(pageRequest);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c"), result.getContent().getFirst().id());
        Assertions.assertEquals("Name", result.getContent().getFirst().name());
        Assertions.assertEquals("email@email.com", result.getContent().getFirst().email());
        Assertions.assertEquals(UserRole.USER, result.getContent().getFirst().role());
        Assertions.assertEquals(createdAt, result.getContent().getFirst().createdAt());
        Assertions.assertTrue(result.getContent().getFirst().active());

        verify(repository).findAll(pageRequest);
        verify(mapper).convertToResponseDto(user);
    }

    @Test
    void shouldReturnEmptyPageWhenFindingAllUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());

        when(repository.findAll(pageRequest))
                .thenReturn(Page.empty(pageRequest));

        Page<UserResponseDto> result = service.findAll(pageRequest);

        Assertions.assertTrue(result.isEmpty());

        verify(repository).findAll(pageRequest);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldUpdateUser() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("Name 2", "email2@email.com");

        User user = new User();
        user.setName("Name");
        user.setEmail("email@email.com");

        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name 2",
                "email2@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(false);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name 2", user.getName());
        Assertions.assertEquals("email2@email.com", user.getEmail());
        Assertions.assertEquals("Name 2", result.name());
        Assertions.assertEquals("email2@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldThrowUserNotFoundWhenUpdatingUser() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("Name 2", "email2@email.com");


        when(repository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.update(userId, userUpdateDto));

        Assertions.assertEquals("User not found with ID: " + userId, exception.getMessage());


        verify(repository).findById(userId);
        verifyNoInteractions(mapper);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowEmailAlreadyRegisteredWhenUpdatingUser() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("Name 2", "email2@email.com");
        User user = new User();
        user.setEmail("email@email.com");

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(true);

        EmailAlreadyRegisteredException exception = Assertions.assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.update(userId, userUpdateDto));

        Assertions.assertEquals("Email " + userUpdateDto.email() + " already registered", exception.getMessage());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(userId);
        verifyNoInteractions(mapper);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldAllowUpdatingUserWithSameEmail() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("Name 2", "email2@email.com");

        User user = new User();
        user.setName("Name");
        user.setEmail("email2@email.com");

        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name 2",
                "email2@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(true);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name 2", user.getName());
        Assertions.assertEquals("email2@email.com", user.getEmail());
        Assertions.assertEquals("Name 2", result.name());
        Assertions.assertEquals("email2@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyEmailWhenNameIsNull() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto(null, "email2@email.com");

        User user = new User();
        user.setName("Name");
        user.setEmail("email@email.com");

        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name",
                "email2@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(false);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name", user.getName());
        Assertions.assertEquals("email2@email.com", user.getEmail());
        Assertions.assertEquals("Name", result.name());
        Assertions.assertEquals("email2@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyEmailWhenNameIsBlank() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("", "email2@email.com");

        User user = new User();
        user.setName("Name");
        user.setEmail("email@email.com");

        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name",
                "email2@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(false);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name", user.getName());
        Assertions.assertEquals("email2@email.com", user.getEmail());
        Assertions.assertEquals("Name", result.name());
        Assertions.assertEquals("email2@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailIsNull() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("Name 2", null);

        User user = new User();
        user.setName("Name");
        user.setEmail("email@email.com");

        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name 2",
                "email@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name 2", user.getName());
        Assertions.assertEquals("email@email.com", user.getEmail());
        Assertions.assertEquals("Name 2", result.name());
        Assertions.assertEquals("email@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailIsBlank() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        UserUpdateDto userUpdateDto = new UserUpdateDto("Name 2", "");

        User user = new User();
        user.setName("Name");
        user.setEmail("email@email.com");

        LocalDateTime createdAt = LocalDateTime.now();

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name 2",
                "email@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name 2", user.getName());
        Assertions.assertEquals("email@email.com", user.getEmail());
        Assertions.assertEquals("Name 2", result.name());
        Assertions.assertEquals("email@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldKeepUserUnchangedWhenUpdateDtoIsNull() {
        UUID userId = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
        LocalDateTime createdAt = LocalDateTime.now();
        UserUpdateDto userUpdateDto = null;

        UserResponseDto responseDto = new UserResponseDto(
                userId,
                "Name",
                "email@email.com",
                UserRole.USER,
                true,
                createdAt
        );

        User user = new User();
        user.setName("Name");
        user.setEmail("email@email.com");

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.update(userId, userUpdateDto);

        Assertions.assertEquals("Name", user.getName());
        Assertions.assertEquals("email@email.com", user.getEmail());
        Assertions.assertEquals("Name", result.name());
        Assertions.assertEquals("email@email.com", result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(createdAt, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(userId);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }
}