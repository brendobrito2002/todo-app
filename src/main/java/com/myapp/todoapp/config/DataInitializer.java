package com.myapp.todoapp.config;

import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Role;
import com.myapp.todoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@todoapp.com").isEmpty()) {
            User admin = User.builder()
                    .name("Administrador")
                    .email("admin@todoapp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            
            userRepository.save(admin);
            log.info("Admin criado automaticamente.");
        }
    }
}
