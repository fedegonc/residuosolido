package com.residuosolido.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private static final String ROLE_ORGANIZATION = "ROLE_ORGANIZATION";
    private static final String ROLE_USER = "ROLE_USER";

    private static final String DEFAULT_TARGET_URL = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> userRoles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String username = authentication.getName();
        logger.info("Usuario '{}' autenticado. Roles: {}", username, userRoles);

        String targetUrl = getTargetUrlByRoles(userRoles);
        logger.info("Redirigiendo a '{}' tras login de '{}'", targetUrl, username);
        response.sendRedirect(targetUrl);
    }

    public void redirectToDashboard(HttpServletRequest request,
                                    HttpServletResponse response,
                                    UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        Set<String> userRoles = AuthorityUtils.authorityListToSet(userDetails.getAuthorities());
        String targetUrl = getTargetUrlByRoles(userRoles);

        logger.info("Redirigiendo usuario '{}' a '{}' según rol: {}", username, targetUrl, userRoles);
        response.sendRedirect(targetUrl);
    }

    private String getTargetUrlByRoles(Set<String> userRoles) {
        if (userRoles.contains(ROLE_ORGANIZATION)) {
            return "/acopio/inicio";
        } else if (userRoles.contains(ROLE_USER)) {
            return "/usuarios/inicio";
        } else {
            return DEFAULT_TARGET_URL;
        }
    }
}