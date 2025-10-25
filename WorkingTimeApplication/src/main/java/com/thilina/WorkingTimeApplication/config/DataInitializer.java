package com.thilina.WorkingTimeApplication.config;

import com.thilina.WorkingTimeApplication.enums.Role;
import com.thilina.WorkingTimeApplication.model.User;
import com.thilina.WorkingTimeApplication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if project manager already exists
            if (!userRepository.existsByUsername("pm")) {
                User pm = new User();
                pm.setUsername("pm");
                pm.setFirstName("Nimal");
                pm.setLastName("Perera");
                pm.setEmail("nimal@gmail.com");
                pm.setPhoneNo("+94712345678");
                pm.setPassword(passwordEncoder.encode("admin123"));
                pm.setRole(Role.PROJECT_MANAGER);
                userRepository.save(pm);
                System.out.println("✅ Project Manager user created successfully!");
            }
        };
    }
}
