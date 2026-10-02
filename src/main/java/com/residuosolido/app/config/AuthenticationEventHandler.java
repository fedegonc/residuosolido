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
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.io.IOException;

/**
 * Maneja éxito y fallo de login. Éxito: resetea intentos, redirige a URL original
 * si existe (guardada en sesión), sino home por rol. Fallo: registra intento y
 * redirige con ?blocked o ?error.
 */
@Component
public class AuthenticationEventHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventHandler.class);
    private static final String SAVED_REQUEST_URL_SESSION_KEY = "savedRequestUrl";

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
        request.getSession().removeAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);

        String targetUrl = resolvePrincipalDestination(request, authentication);
        logger.info("Usuario '{}' autenticado. Redirigiendo a '{}'", authentication.getName(), targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    private String resolvePrincipalDestination(HttpServletRequest request, Authentication authentication) {
        // 1. Si hay URL guardada en sesión (intent to access protected resource), úsala
        Object savedUrl = request.getSession().getAttribute(SAVED_REQUEST_URL_SESSION_KEY);
        if (savedUrl instanceof String url && !url.isBlank() && isInternalPath(url)) {
            request.getSession().removeAttribute(SAVED_REQUEST_URL_SESSION_KEY);
            logger.debug("Redirigiendo a destino original: {}", url);
            return url;
        }

        // 2. Fallback: home por rol
        return Routes.resolveHomeForRole(authentication);
    }

    private boolean isInternalPath(String url) {
        return url != null && (url.startsWith("/") || url.isEmpty());
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
        redirectStrategy.sendRedirect(request, response, Routes.LOGIN + "?" + param);
    }
}
