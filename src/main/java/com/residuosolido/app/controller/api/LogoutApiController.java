package com.residuosolido.app.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(
    origins = {"http://localhost:3000", "http://localhost:5173", "https://residuosolido.onrender.com"},
    allowCredentials = "true",
    methods = { RequestMethod.POST, RequestMethod.OPTIONS },
    allowedHeaders = { "*" },
    maxAge = 3600
)
public class LogoutApiController {

    @PostMapping(value = "/logout", produces = "application/json")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        var context = SecurityContextHolder.getContext();
        var auth = context != null ? context.getAuthentication() : null;
        try {
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            } else {
                var session = request.getSession(false);
                if (session != null) session.invalidate();
                SecurityContextHolder.clearContext();
            }
            // Expirar cookie JSESSIONID en el navegador
            Cookie cookie = new Cookie("JSESSIONID", "");
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logout exitoso"
        ));
    }
}
