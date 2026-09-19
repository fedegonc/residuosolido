package com.residuosolido.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maneja éxito y fallo de login en una sola clase (comparten logger,
 * RedirectStrategy y RateLimiter). Éxito: resetea intentos fallidos, invalida
 * el locale de sesión (para que CityAwareLocaleResolver recalcule el idioma
 * según la ciudad del usuario) y delega la redirección a
 * Routes.resolveHomeForRole. Fallo: registra el intento y redirige con
 * ?blocked o ?error según corresponda.
 * Antes eran LoginSuccessHandler + LoginFailureHandler por separado.
 */
@Component
public class AuthenticationEventHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventHandler.class);

    private final RateLimiter rateLimiter;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public AuthenticationEventHandler(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        rateLimiter.loginSucceeded(authentication.getName());
        request.getSession().removeAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE");
        String targetUrl = Routes.resolveHomeForRole(authentication);
        logger.info("Usuario '{}' autenticado. Redirigiendo a '{}'", authentication.getName(), targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        boolean isLocked = exception instanceof LockedException;
        if (username != null && !username.isBlank() && !isLocked) {
            rateLimiter.loginFailed(username);
        }
        logger.warn("Intento de login fallido para usuario '{}' ({})", username, exception.getMessage());
        String param = isLocked || rateLimiter.isBlocked(username) ? "blocked" : "error";
        redirectStrategy.sendRedirect(request, response, "/entrar?" + param);
    }
}
