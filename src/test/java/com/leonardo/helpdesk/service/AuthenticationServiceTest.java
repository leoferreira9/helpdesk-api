package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.login.LoginRequestDto;
import com.leonardo.helpdesk.dto.login.LoginResponseDto;
import com.leonardo.helpdesk.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldAuthenticateUserAndReturnToken() {
        LoginRequestDto requestDto = new LoginRequestDto("user@email.com", "password");
        User user = new User();
        user.setEmail(requestDto.email());
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponseDto result = authenticationService.login(requestDto);

        assertEquals("jwt-token", result.token());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());
        assertEquals(requestDto.email(), authenticationCaptor.getValue().getPrincipal());
        assertEquals(requestDto.password(), authenticationCaptor.getValue().getCredentials());
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectInvalidCredentials() {
        LoginRequestDto requestDto = new LoginRequestDto("user@email.com", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(requestDto));

        verifyNoInteractions(jwtService);
    }
}
