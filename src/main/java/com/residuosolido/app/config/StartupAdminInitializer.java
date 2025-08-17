package com.residuosolido.app.config;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Bootstrap mínimo: si la base está vacía (0 usuarios), crea un ADMIN por defecto.
 */
@Component
@Order(1)
public class StartupAdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupAdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StartupAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Si la base está vacía, crear usuario admin por defecto
        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole(Role.ADMIN);
            admin.setPreferredLanguage("es");
            admin.setActive(true);
            userRepository.save(admin);
            log.info("[Bootstrap] Creado usuario ADMIN por defecto: username='admin'");
        }
    }
}
