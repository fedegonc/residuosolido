package com.residuosolido.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Manejador personalizado para redireccionar usuarios según su rol después del login.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Map<String, String> ROLE_TARGET_URL_MAP = Map.of(
            "ROLE_ADMIN", "/admin/dashboard",
            "ROLE_ORG", "/org/dashboard",
            "ROLE_USER", "/users/dashboard"
    );

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        for (String role : roles) {
            String targetUrl = ROLE_TARGET_URL_MAP.get(role);
            if (targetUrl != null) {
                response.sendRedirect(targetUrl);
                return;
            }
        }

        // Si no encuentra ningún rol, lo mando a home.
        response.sendRedirect("/");
    }
}
