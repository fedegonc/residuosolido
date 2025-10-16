package com.residuosolido.app.config;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Migración única para actualizar el campo profile_completed en usuarios existentes.
 * Este componente se ejecuta al inicio de la aplicación.
 */
@Component
@Order(1) // Ejecutar temprano
public class ProfileCompletedMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProfileCompletedMigration.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Iniciando migración de campo profile_completed...");
        
        try {
            // Obtener todos los usuarios con profileCompleted = null
            List<User> users = userRepository.findAll();
            int updated = 0;
            
            for (User user : users) {
                if (user.getProfileCompleted() == null) {
                    // Si es organización, marcar como NO completado
                    if (user.getRole() == Role.ORGANIZATION) {
                        user.setProfileCompleted(false);
                        logger.info("Usuario '{}' (ORGANIZATION) marcado como profile_completed=false", user.getUsername());
                    } else {
                        // Otros roles, marcar como completado
                        user.setProfileCompleted(true);
                        logger.info("Usuario '{}' ({}) marcado como profile_completed=true", user.getUsername(), user.getRole());
                    }
                    userRepository.save(user);
                    updated++;
                }
            }
            
            if (updated > 0) {
                logger.info("Migración completada: {} usuarios actualizados", updated);
            } else {
                logger.info("No se encontraron usuarios para migrar");
            }
            
        } catch (Exception e) {
            logger.error("Error durante la migración de profile_completed: {}", e.getMessage(), e);
        }
    }
}
