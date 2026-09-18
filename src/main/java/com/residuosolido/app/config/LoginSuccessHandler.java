package com.residuosolido.app.config;

import com.residuosolido.app.enums.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler post-login: resetea intentos fallidos, invalida el locale de sesión
 * (para que CityAwareLocaleResolver recalcule el idioma según la ciudad del usuario)
 * y delega la redirección a Routes.resolveHomeForRole.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final LoginAttemptService loginAttemptService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public LoginSuccessHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        loginAttemptService.loginSucceeded(authentication.getName());
        request.getSession().removeAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE");
        String targetUrl = Routes.resolveHomeForRole(authentication);
        logger.info("Usuario '{}' autenticado. Redirigiendo a '{}'", authentication.getName(), targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
}
