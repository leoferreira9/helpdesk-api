package com.leonardo.helpdesk.service;

import com.leonardo.helpdesk.dto.register.RegisterRequestDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.exception.EmailAlreadyRegisteredException;
import com.leonardo.helpdesk.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailVerificationTokenService emailVerificationTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @Transactional
    public void register(RegisterRequestDto registerRequestDto) {
        if(userRepository.existsByEmail(registerRequestDto.email())) {
            throw new EmailAlreadyRegisteredException("Email already registered!");
        }

        User user = new User();
        user.setActive(false);
        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setEmail(registerRequestDto.email());
        user.setName(registerRequestDto.name());

        User savedUser = userRepository.save(user);
        String token = emailVerificationTokenService.createToken(savedUser);
    }
}
