package com.residuosolido.app.controller;

import com.residuosolido.app.config.DataSeeder;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/** Endpoint dev-only para cargar datos de demostración sin reiniciar la app. */
@RestController
@Profile("dev")
public class SeedController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RequestRepository requestRepository;
    private final DataSeeder dataSeeder;

    public SeedController(UserRepository userRepository,
                          OrganizationRepository organizationRepository,
                          RequestRepository requestRepository,
                          DataSeeder dataSeeder,
                          Environment environment) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.requestRepository = requestRepository;
        this.dataSeeder = dataSeeder;

        Set<String> profiles = Set.of(environment.getActiveProfiles());
        if (!profiles.contains("dev") && !profiles.contains("test")) {
            throw new IllegalStateException("SeedController solo puede activarse en profiles dev o test; perfiles activos: " + profiles);
        }
    }

    @GetMapping(Routes.SEED)
    public ResponseEntity<String> seed(@org.springframework.web.bind.annotation.RequestParam(required = false) boolean force) {
        long beforeUsers = userRepository.count();
        long beforeRequests = requestRepository.count();
        if (force) {
            requestRepository.deleteAll();
            organizationRepository.deleteAll();
            userRepository.deleteAll();
        }
        dataSeeder.seedAllIfNeeded();
        long afterUsers = userRepository.count();
        long afterRequests = requestRepository.count();
        String body = "Seed ejecutado" + (force ? " (force)" : "") + ". Usuarios: " + beforeUsers + " -> " + afterUsers
                + " | Solicitudes: " + beforeRequests + " -> " + afterRequests
                + (afterUsers == beforeUsers ? " — la base ya tenía datos, usá /seed?force=true para limpiar y cargar" : "")
                + "\n<a href=\"/\">Volver al inicio</a>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(body);
    }
}
