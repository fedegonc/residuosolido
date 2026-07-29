package com.residuosolido.app.config;

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

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final RoleBasedLoginTargetUrlResolver targetUrlResolver;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public LoginSuccessHandler(RoleBasedLoginTargetUrlResolver targetUrlResolver) {
        this.targetUrlResolver = targetUrlResolver;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = targetUrlResolver.resolveTargetUrl(authentication.getAuthorities());
        logger.info("Usuario '{}' autenticado. Redirigiendo a '{}'", authentication.getName(), targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
}