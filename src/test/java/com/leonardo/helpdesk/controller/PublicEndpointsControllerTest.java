package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.config.SecurityConfig;
import com.leonardo.helpdesk.dto.login.LoginRequestDto;
import com.leonardo.helpdesk.dto.login.LoginResponseDto;
import com.leonardo.helpdesk.dto.register.RegisterRequestDto;
import com.leonardo.helpdesk.service.AuthenticationService;
import com.leonardo.helpdesk.service.EmailVerificationTokenService;
import com.leonardo.helpdesk.service.JwtService;
import com.leonardo.helpdesk.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuthenticationController.class,
        RegistrationController.class,
        EmailVerificationController.class
})
@Import(SecurityConfig.class)
class PublicEndpointsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private EmailVerificationTokenService emailVerificationTokenService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("user@email.com", "password");
        when(authenticationService.login(requestDto)).thenReturn(new LoginResponseDto("jwt-token"));

        mvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authenticationService).login(requestDto);
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "User",
                "user@email.com",
                "password"
        );

        mvc.perform(
                post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        ).andExpect(status().isAccepted());

        verify(registrationService).register(requestDto);
    }

    @Test
    void shouldVerifyEmailSuccessfully() throws Exception {
        String token = "verification-token";

        mvc.perform(
                get("/verify-email")
                        .param("token", token)
        ).andExpect(status().isNoContent());

        verify(emailVerificationTokenService).confirmToken(token);
    }
}
