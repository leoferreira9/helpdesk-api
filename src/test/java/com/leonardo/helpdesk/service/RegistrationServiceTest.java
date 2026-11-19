package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.register.RegisterRequestDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.EmailAlreadyRegisteredException;
import com.leonardo.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationTokenService emailVerificationTokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void shouldRegisterInactiveUserAndSendVerificationEmail() {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "User",
                "user@email.com",
                "password"
        );

        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailVerificationTokenService.createToken(any(User.class))).thenReturn("verification-token");

        registrationService.register(requestDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(requestDto.name(), savedUser.getName());
        assertEquals(requestDto.email(), savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(UserRole.USER, savedUser.getRole());
        assertFalse(savedUser.isActive());

        verify(emailVerificationTokenService).createToken(savedUser);
        verify(emailService).sendVerificationEmail(requestDto.email(), "verification-token");
    }

    @Test
    void shouldRejectAlreadyRegisteredEmail() {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "User",
                "user@email.com",
                "password"
        );
        when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> registrationService.register(requestDto));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, emailVerificationTokenService, emailService);
    }
}
