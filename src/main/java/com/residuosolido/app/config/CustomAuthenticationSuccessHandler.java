package com.residuosolido.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * Manejador personalizado para redireccionar usuarios según su rol después del login
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Obtener roles del usuario autenticado
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        // Redireccionar según el rol
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_ORG")) {
            response.sendRedirect("/org/dashboard");
        } else if (roles.contains("ROLE_USER")) {
            response.sendRedirect("/dashboard");
        } else {
            // Si no tiene un rol específico, redirigir a la página principal
            response.sendRedirect("/");
        }
    }
}
