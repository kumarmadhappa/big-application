package com.bigapplication.bankingsystem.bootstrap;

import com.bigapplication.bankingsystem.config.AdminProperties;
import com.bigapplication.bankingsystem.entity.BankUser;
import com.bigapplication.bankingsystem.entity.UserRole;
import com.bigapplication.bankingsystem.repository.BankUserRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"dev", "local", "test"})
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    @Bean
    ApplicationRunner ensureDefaultAdmin(BankUserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         AdminProperties adminProperties) {
        return args -> {
            if (userRepository.findByUsername(adminProperties.getUsername()).isPresent()) {
                log.info("Default Bank Admin data already present; skipping seed");
                return;
            }

            BankUser admin = BankUser.builder()
                    .username(adminProperties.getUsername())
                    .email(adminProperties.getEmail())
                    .password(passwordEncoder.encode(adminProperties.getPassword()))
                    .enabled(true)
                    .roles(Set.of(UserRole.ADMIN))
                    .build();
            userRepository.save(admin);
            log.info("Default bank admin created username={}", adminProperties.getUsername());
        };
    }
}
