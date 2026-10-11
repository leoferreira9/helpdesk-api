package com.leonardo.helpdesk.config;

import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.enums.UserRole;
import com.leonardo.helpdesk.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminName;
    private final String initialAdminEmail;
    private final String initialAdminPassword;

    public InitialAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${INITIAL_ADMIN_NAME}") String initialAdminName,
            @Value("${INITIAL_ADMIN_EMAIL}") String initialAdminEmail,
            @Value("${INITIAL_ADMIN_PASSWORD}") String initialAdminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialAdminName = initialAdminName;
        this.initialAdminEmail = initialAdminEmail;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        if(userRepository.existsByEmail(initialAdminEmail)) {
            return;
        }

        String encodedInitialAdminPassword = passwordEncoder.encode(initialAdminPassword);

        User user = new User();
        user.setName(initialAdminName);
        user.setEmail(initialAdminEmail);
        user.setPassword(encodedInitialAdminPassword);
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);
    }
}
