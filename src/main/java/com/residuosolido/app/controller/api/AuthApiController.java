package com.residuosolido.app.controller.api;

import com.residuosolido.app.dto.LoginRequest;
import com.residuosolido.app.dto.LoginResponse;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(
    origins = {"http://localhost:3000", "https://residuosolido.onrender.com"},
    allowCredentials = "true",
    methods = { RequestMethod.POST, RequestMethod.OPTIONS },
    allowedHeaders = { "*" },
    maxAge = 3600
)
public class AuthApiController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthApiController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if ((request.getUsername() == null || request.getUsername().isBlank()) &&
            (request.getEmail() == null || request.getEmail().isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoginResponse.error("Debe enviar username o email"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoginResponse.error("La contraseña es obligatoria"));
        }

        Optional<User> userOpt = (request.getUsername() != null && !request.getUsername().isBlank())
                ? userService.findByUsername(request.getUsername())
                : userService.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.error("Usuario no encontrado"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.error("Credenciales inválidas"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(LoginResponse.error("Usuario inactivo"));
        }

        return ResponseEntity.ok(LoginResponse.fromUser(user));
    }
}
