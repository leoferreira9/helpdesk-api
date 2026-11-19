package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.entity.EmailVerificationToken;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.exception.ExpiredEmailVerificationTokenException;
import com.leonardo.helpdesk.exception.InvalidEmailVerificationTokenException;
import com.leonardo.helpdesk.repository.EmailVerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenServiceTest {

    @Mock
    private EmailVerificationTokenRepository repository;

    @InjectMocks
    private EmailVerificationTokenService service;

    @Test
    void shouldCreateVerificationToken() {
        User user = new User();
        user.setEmail("user@email.com");

        String token = service.createToken(user);

        assertDoesNotThrow(() -> UUID.fromString(token));

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(repository).save(tokenCaptor.capture());
        EmailVerificationToken savedToken = tokenCaptor.getValue();

        assertSame(user, savedToken.getUser());
        assertNotEquals(token, savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(savedToken.getExpiresAt().isBefore(LocalDateTime.now().plusHours(25)));
    }

    @Test
    void shouldConfirmValidTokenAndActivateUser() {
        User user = new User();
        user.setActive(false);
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(verificationToken));

        service.confirmToken("valid-token");

        assertTrue(user.isActive());
        assertNotNull(verificationToken.getConfirmedAt());
        verify(repository).findByTokenHash(anyString());
    }

    @Test
    void shouldRejectInvalidToken() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(
                InvalidEmailVerificationTokenException.class,
                () -> service.confirmToken("invalid-token")
        );
    }

    @Test
    void shouldRejectExpiredToken() {
        User user = new User();
        user.setActive(false);
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(verificationToken));

        assertThrows(
                ExpiredEmailVerificationTokenException.class,
                () -> service.confirmToken("expired-token")
        );
        assertFalse(user.isActive());
        assertNull(verificationToken.getConfirmedAt());
    }

    @Test
    void shouldIgnoreAlreadyConfirmedToken() {
        LocalDateTime confirmedAt = LocalDateTime.now().minusMinutes(5);
        User user = new User();
        user.setActive(true);
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setConfirmedAt(confirmedAt);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(verificationToken));

        service.confirmToken("confirmed-token");

        assertTrue(user.isActive());
        assertEquals(confirmedAt, verificationToken.getConfirmedAt());
    }
}
