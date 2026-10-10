package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.login.LoginRequestDto;
import com.leonardo.helpdesk.dto.login.LoginResponseDto;
import com.leonardo.helpdesk.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    private String authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = (User) authentication.getPrincipal();
        return jwtService.generateToken(user);
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        String token = authenticate(requestDto.email(), requestDto.password());
        return new LoginResponseDto(token);
    }
}
