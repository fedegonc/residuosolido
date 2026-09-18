package com.residuosolido.app.controller;

import com.residuosolido.app.config.DataLoader;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint dev-only para cargar datos de demostración sin reiniciar la app. */
@RestController
@Profile("dev")
public class SeedController {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedController(UserRepository userRepository,
                          RequestRepository requestRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(Routes.SEED)
    public ResponseEntity<String> seed(@org.springframework.web.bind.annotation.RequestParam(required = false) boolean force) {
        long beforeUsers = userRepository.count();
        long beforeRequests = requestRepository.count();
        if (force) {
            requestRepository.deleteAll();
            userRepository.deleteAll();
        }
        DataLoader.seedAll(userRepository, requestRepository, passwordEncoder);
        long afterUsers = userRepository.count();
        long afterRequests = requestRepository.count();
        String body = "Seed ejecutado" + (force ? " (force)" : "") + ". Usuarios: " + beforeUsers + " -> " + afterUsers
                + " | Solicitudes: " + beforeRequests + " -> " + afterRequests
                + (afterUsers == beforeUsers ? " — la base ya tenía datos, usá /seed?force=true para limpiar y cargar" : "")
                + "\n<a href=\"/\">Volver al inicio</a>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(body);
    }
}
