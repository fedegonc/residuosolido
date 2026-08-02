package com.residuosolido.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginFailureHandler.class);

    private final LoginAttemptService loginAttemptService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public LoginFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        boolean isLocked = exception instanceof LockedException;
        if (username != null && !username.isBlank() && !isLocked) {
            loginAttemptService.loginFailed(username);
        }
        logger.warn("Intento de login fallido para usuario '{}' ({})", username, exception.getMessage());
        String param = isLocked || loginAttemptService.isBlocked(username) ? "blocked" : "error";
        redirectStrategy.sendRedirect(request, response, "/auth/login?" + param);
    }
}
