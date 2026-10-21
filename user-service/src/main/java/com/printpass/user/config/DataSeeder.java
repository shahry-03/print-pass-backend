package com.printpass.user.config;

import com.printpass.user.entity.User;
import com.printpass.user.enums.Role; // Yahan dhyan rakhein, Role import theek ho
import com.printpass.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔒 THE SECURE WAY: Reading from application.properties / Env Variables
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        // 1. Check karo ke kya Admin pehle se mojood hai? (Ab email hardcode nahi hai)
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            log.info("Admin account not found. Seeding default Super Admin...");

            // 2. Naya Admin User banao
            User superAdmin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword)) // Password hashed aur variable se!
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();

            // 3. Database mein save kardo
            userRepository.save(superAdmin);

            log.info("✅ Super Admin seeded successfully! [Email: {}]", adminEmail);
        } else {
            log.info("⚡ Super Admin already exists. Skipping data seeding.");
        }
    }
}