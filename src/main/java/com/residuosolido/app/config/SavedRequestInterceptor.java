package com.residuosolido.app.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor que guarda la URL original en sesión cuando un usuario no autenticado
 * intenta acceder a un recurso protegido. Post-login, AuthenticationEventHandler
 * redirige al usuario a esa URL guardada.
 *
 * Esto permite flujos como: usuario intenta /solicitudes → no autenticado →
 * redirige a login → usuario hace login → redirige a /solicitudes (no a home).
 */
@Component
public class SavedRequestInterceptor implements HandlerInterceptor {

    private static final String SAVED_REQUEST_URL_SESSION_KEY = "savedRequestUrl";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);

        // Solo guarda si NO está autenticado y es una URL "interesante" (no estática)
        if (!isAuthenticated && isInterestingPath(request.getRequestURI())) {
            request.getSession().setAttribute(SAVED_REQUEST_URL_SESSION_KEY, request.getRequestURI());
        }

        return true;
    }

    private boolean isInterestingPath(String uri) {
        if (uri == null || uri.isEmpty()) return false;
        // Excluir: login, register, recursos estáticos, etc.
        return !uri.contains("/static/")
                && !uri.contains("/login")
                && !uri.contains("/register")
                && !uri.contains("/error")
                && !uri.contains("/.well-known")
                && !uri.contains("/actuator")
                && !uri.contains("/admin");
    }
}
