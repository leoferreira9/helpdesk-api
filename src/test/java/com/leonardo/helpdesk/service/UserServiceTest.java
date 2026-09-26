package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.dto.update.ChangePasswordUpdateDto;
import com.leonardo.helpdesk.dto.update.UserUpdateDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.EmailAlreadyRegisteredException;
import com.leonardo.helpdesk.exception.ResourceNotFoundException;
import com.leonardo.helpdesk.mapper.UserMapper;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    private static final UUID USER_ID = UUID.fromString("4a8b2c1e-9f3d-4e2a-8b1c-7d6e5f4a3b2c");
    private static final String USER_NAME = "Name";
    private static final String USER_EMAIL = "email@email.com";
    private static final String UPDATED_NAME = "Name 2";
    private static final String UPDATED_EMAIL = "email2@email.com";
    private static final String USER_PASSWORD = "Password";
    private static final String ORIGINAL_PASSWORD = "Original password";
    private static final String NEW_PASSWORD = "teste";
    private static final String ENCODED_PASSWORD = "$2a$10$dXJ3w46eJZ9vCBq7f8b9e.aX6.D9g7W/SgqR2d8yHj9Y0Z4E7b7q6";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, 10, Sort.by("name").ascending());
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName(USER_NAME);
        user.setEmail(USER_EMAIL);
        user.setPassword(ORIGINAL_PASSWORD);
        user.setRole(UserRole.USER);
        user.setActive(true);
    }

    @Test
    void shouldCreateUser() {
        UserRequestDto requestDto = new UserRequestDto(
                USER_NAME,
                USER_EMAIL,
                USER_PASSWORD,
                UserRole.USER
        );

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(passwordEncoder.encode(requestDto.password()))
                .thenReturn(ENCODED_PASSWORD);

        when(mapper.convertToEntity(requestDto))
                .thenReturn(user);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(USER_EMAIL))
                .thenReturn(false);

        UserResponseDto resultado = service.create(requestDto);

        Assertions.assertEquals(USER_ID, resultado.id());
        Assertions.assertEquals(USER_NAME, resultado.name());
        Assertions.assertEquals(USER_EMAIL, resultado.email());
        Assertions.assertEquals(UserRole.USER, resultado.role());
        Assertions.assertEquals(CREATED_AT, resultado.createdAt());
        Assertions.assertTrue(resultado.active());
        Assertions.assertEquals(UserRole.USER, user.getRole());
        Assertions.assertEquals(ENCODED_PASSWORD, user.getPassword());

        verify(repository).existsByEmail(requestDto.email());
        verify(repository).save(user);
        verify(mapper).convertToResponseDto(user);
        verify(mapper).convertToEntity(requestDto);
        verify(passwordEncoder).encode(requestDto.password());
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithEmailAlreadyRegistered() {
        UserRequestDto requestDto = new UserRequestDto(
                USER_NAME,
                USER_EMAIL,
                USER_PASSWORD,
                UserRole.USER
        );

        when(repository.existsByEmail(requestDto.email()))
                .thenReturn(true);

        EmailAlreadyRegisteredException exception = Assertions.assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.create(requestDto));

        Assertions.assertEquals("Email " + USER_EMAIL + " already registered", exception.getMessage());

        verify(repository).existsByEmail(USER_EMAIL);
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper, passwordEncoder);
    }

    @Test
    void shouldFindUserById() {


        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto response = service.findById(USER_ID);

        Assertions.assertEquals(USER_ID, response.id());
        Assertions.assertEquals(USER_NAME, response.name());
        Assertions.assertEquals(USER_EMAIL, response.email());
        Assertions.assertEquals(UserRole.USER, response.role());
        Assertions.assertTrue(response.active());
        Assertions.assertEquals(CREATED_AT, response.createdAt());

        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
    }

    @Test
    void shouldThrowExceptionWhenFindingUserById() {

        when(repository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows( ResourceNotFoundException.class,
                () -> service.findById(USER_ID));

        Assertions.assertEquals("User not found with ID: " + USER_ID, exception.getMessage());

        verify(repository).findById(USER_ID);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldFindAllUsers() {

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );


        Page<User> pageable = new PageImpl<>(List.of(user), PAGE_REQUEST, 1);

        when(repository.findAll(PAGE_REQUEST))
                .thenReturn(pageable);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        Page<UserResponseDto> result = service.findAll(PAGE_REQUEST);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(USER_ID, result.getContent().getFirst().id());
        Assertions.assertEquals(USER_NAME, result.getContent().getFirst().name());
        Assertions.assertEquals(USER_EMAIL, result.getContent().getFirst().email());
        Assertions.assertEquals(UserRole.USER, result.getContent().getFirst().role());
        Assertions.assertEquals(CREATED_AT, result.getContent().getFirst().createdAt());
        Assertions.assertTrue(result.getContent().getFirst().active());

        verify(repository).findAll(PAGE_REQUEST);
        verify(mapper).convertToResponseDto(user);
    }

    @Test
    void shouldReturnEmptyPageWhenFindingAllUsers() {

        when(repository.findAll(PAGE_REQUEST))
                .thenReturn(Page.empty(PAGE_REQUEST));

        Page<UserResponseDto> result = service.findAll(PAGE_REQUEST);

        Assertions.assertTrue(result.isEmpty());

        verify(repository).findAll(PAGE_REQUEST);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldUpdateUser() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(UPDATED_NAME, UPDATED_EMAIL);

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                UPDATED_NAME,
                UPDATED_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(false);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(UPDATED_NAME, user.getName());
        Assertions.assertEquals(UPDATED_EMAIL, user.getEmail());
        Assertions.assertEquals(UPDATED_NAME, result.name());
        Assertions.assertEquals(UPDATED_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldThrowUserNotFoundWhenUpdatingUser() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(UPDATED_NAME, UPDATED_EMAIL);


        when(repository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.update(USER_ID, userUpdateDto));

        Assertions.assertEquals("User not found with ID: " + USER_ID, exception.getMessage());


        verify(repository).findById(USER_ID);
        verifyNoInteractions(mapper);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowEmailAlreadyRegisteredWhenUpdatingUser() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(UPDATED_NAME, UPDATED_EMAIL);
        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(true);

        EmailAlreadyRegisteredException exception = Assertions.assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.update(USER_ID, userUpdateDto));

        Assertions.assertEquals("Email " + userUpdateDto.email() + " already registered", exception.getMessage());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(USER_ID);
        verifyNoInteractions(mapper);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldAllowUpdatingUserWithSameEmail() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(UPDATED_NAME, UPDATED_EMAIL);

        user.setEmail(UPDATED_EMAIL);

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                UPDATED_NAME,
                UPDATED_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(true);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(UPDATED_NAME, user.getName());
        Assertions.assertEquals(UPDATED_EMAIL, user.getEmail());
        Assertions.assertEquals(UPDATED_NAME, result.name());
        Assertions.assertEquals(UPDATED_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyEmailWhenNameIsNull() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(null, UPDATED_EMAIL);

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                UPDATED_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(false);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(USER_NAME, user.getName());
        Assertions.assertEquals(UPDATED_EMAIL, user.getEmail());
        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(UPDATED_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyEmailWhenNameIsBlank() {
        UserUpdateDto userUpdateDto = new UserUpdateDto("", UPDATED_EMAIL);

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                UPDATED_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        when(repository.existsByEmail(userUpdateDto.email()))
                .thenReturn(false);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(USER_NAME, user.getName());
        Assertions.assertEquals(UPDATED_EMAIL, user.getEmail());
        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(UPDATED_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository).existsByEmail(userUpdateDto.email());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailIsNull() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(UPDATED_NAME, null);

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                UPDATED_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(UPDATED_NAME, user.getName());
        Assertions.assertEquals(USER_EMAIL, user.getEmail());
        Assertions.assertEquals(UPDATED_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailIsBlank() {
        UserUpdateDto userUpdateDto = new UserUpdateDto(UPDATED_NAME, "");

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                UPDATED_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(UPDATED_NAME, user.getName());
        Assertions.assertEquals(USER_EMAIL, user.getEmail());
        Assertions.assertEquals(UPDATED_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldKeepUserUnchangedWhenUpdateDtoIsNull() {
        UserUpdateDto userUpdateDto = null;

        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.update(USER_ID, userUpdateDto);

        Assertions.assertEquals(USER_NAME, user.getName());
        Assertions.assertEquals(USER_EMAIL, user.getEmail());
        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertTrue(result.active());

        verify(repository, never()).existsByEmail(any());
        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
    }

    @Test
    void shouldUpdatePassword() {
        ChangePasswordUpdateDto changePasswordUpdateDto = new ChangePasswordUpdateDto(NEW_PASSWORD);


        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(changePasswordUpdateDto.password()))
                .thenReturn(ENCODED_PASSWORD);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        when(repository.save(user))
                .thenReturn(user);

        UserResponseDto result = service.updatePassword(USER_ID, changePasswordUpdateDto);

        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertEquals(ENCODED_PASSWORD, user.getPassword());
        Assertions.assertTrue(result.active());

        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
        verify(passwordEncoder).encode(changePasswordUpdateDto.password());
    }

    @Test
    void shouldThrowUserNotFoundWhenUpdatingPassword() {
        ChangePasswordUpdateDto changePasswordUpdateDto = new ChangePasswordUpdateDto(NEW_PASSWORD);
        when(repository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.updatePassword(USER_ID, changePasswordUpdateDto));

        Assertions.assertEquals("User not found with ID: " + USER_ID, exception.getMessage());

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(mapper);
        verify(repository).findById(USER_ID);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldKeepPasswordUnchangedWhenPasswordUpdateDtoIsNull() {


        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(repository.save(user))
                .thenReturn(user);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result = service.updatePassword(USER_ID, null);

        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertEquals(ORIGINAL_PASSWORD, user.getPassword());
        Assertions.assertTrue(result.active());

        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldKeepPasswordUnchangedWhenPasswordIsNull() {
        ChangePasswordUpdateDto changePasswordUpdateDto = new ChangePasswordUpdateDto(null);


        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(repository.save(user))
                .thenReturn(user);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result = service.updatePassword(USER_ID, changePasswordUpdateDto);

        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertEquals(ORIGINAL_PASSWORD, user.getPassword());
        Assertions.assertTrue(result.active());

        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldKeepPasswordUnchangedWhenPasswordIsBlank() {
        ChangePasswordUpdateDto changePasswordUpdateDto = new ChangePasswordUpdateDto("");


        UserResponseDto responseDto = new UserResponseDto(
                USER_ID,
                USER_NAME,
                USER_EMAIL,
                UserRole.USER,
                true,
                CREATED_AT
        );

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(repository.save(user))
                .thenReturn(user);

        when(mapper.convertToResponseDto(user))
                .thenReturn(responseDto);

        UserResponseDto result = service.updatePassword(USER_ID, changePasswordUpdateDto);

        Assertions.assertEquals(USER_NAME, result.name());
        Assertions.assertEquals(USER_EMAIL, result.email());
        Assertions.assertEquals(UserRole.USER, result.role());
        Assertions.assertEquals(CREATED_AT, result.createdAt());
        Assertions.assertEquals(ORIGINAL_PASSWORD, user.getPassword());
        Assertions.assertTrue(result.active());

        verify(repository).findById(USER_ID);
        verify(mapper).convertToResponseDto(user);
        verify(repository).save(user);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldDeactivateUser() {

        when(repository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        service.deactivate(USER_ID);

        Assertions.assertFalse(user.isActive());

        verify(repository).findById(USER_ID);
        verify(repository).save(user);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldThrowUserNotFoundWhenDeactivatingUser() {

        when(repository.findById(USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.deactivate(USER_ID));

        Assertions.assertEquals("User not found with ID: " + USER_ID, exception.getMessage());

        verify(repository).findById(USER_ID);
        verify(repository, never()).save(any());
    }
}
