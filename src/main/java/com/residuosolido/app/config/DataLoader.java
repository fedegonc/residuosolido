package com.residuosolido.app.config;

import com.residuosolido.app.repository.InformalCollectorRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               RequestRepository requestRepository,
                               InformalCollectorRepository informalCollectorRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.seed:false}") boolean shouldSeed) {
        return args -> {
            if (!shouldSeed) return;
            SeedDataFactory.seedAll(userRepository, requestRepository, informalCollectorRepository, passwordEncoder);
        };
    }
}
