package com.trippzo.config;

import com.trippzo.model.User;
import com.trippzo.model.enums.Role;
import com.trippzo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "viktorkrustev03@abv.bg";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String ADMIN_NAME = "Виктор Кръстев";

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            User admin = new User();
            admin.setEmail(ADMIN_EMAIL);
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setFullName(ADMIN_NAME);
            admin.setRole(Role.ROLE_ADMIN);

            userRepository.save(admin);
            System.out.println("Default admin account created successfully!");
            System.out.println("Email: " + ADMIN_EMAIL);
            System.out.println("Username: admin");
        } else {
            System.out.println("Admin account already exists.");
        }
    }
}
