package com.residuosolido.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manejador personalizado para redireccionar usuarios según su rol después del login.
 * Implementa jerarquía de roles donde ADMIN tiene mayor precedencia que ORG y USER.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    // Roles ordenados por precedencia (mayor a menor)
    private static final List<String> ROLE_HIERARCHY = List.of(
            "ROLE_ADMIN",
            "ROLE_ORGANIZATION", 
            "ROLE_USER"
    );
    
    private static final Map<String, String> ROLE_TARGET_URL_MAP = Map.of(
            "ROLE_ADMIN", "/admin/dashboard",
            "ROLE_ORGANIZATION", "/org/dashboard",
            "ROLE_USER", "/users/dashboard"
    );
    
    private static final String DEFAULT_TARGET_URL = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Set<String> userRoles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String username = authentication.getName();
        
        logger.info("Usuario '{}' autenticado exitosamente con roles: {}", username, userRoles);
        
        try {
            String targetUrl = determineTargetUrl(userRoles);
            logger.info("Redirigiendo usuario '{}' a: {}", username, targetUrl);
            response.sendRedirect(targetUrl);
            
        } catch (Exception e) {
            logger.error("Error al redireccionar usuario '{}': {}", username, e.getMessage(), e);
            response.sendRedirect(DEFAULT_TARGET_URL);
        }
    }

    /**
     * Método público para redireccionar usuarios según su rol desde controladores
     * @param request HttpServletRequest
     * @param response HttpServletResponse  
     * @param userDetails UserDetails del usuario autenticado
     * @throws IOException si hay error en la redirección
     */
    public void redirectByRole(HttpServletRequest request,
                              HttpServletResponse response,
                              UserDetails userDetails) throws IOException {
        
        Set<String> userRoles = AuthorityUtils.authorityListToSet(userDetails.getAuthorities());
        String username = userDetails.getUsername();
        
        logger.info("Redirigiendo usuario '{}' desde controlador con roles: {}", username, userRoles);
        
        try {
            String targetUrl = determineTargetUrl(userRoles);
            logger.info("Redirigiendo usuario '{}' a: {}", username, targetUrl);
            response.sendRedirect(targetUrl);
            
        } catch (Exception e) {
            logger.error("Error al redireccionar usuario '{}' desde controlador: {}", username, e.getMessage(), e);
            response.sendRedirect(DEFAULT_TARGET_URL);
        }
    }

    /**
     * Determina la URL de destino basada en la jerarquía de roles.
     * El rol con mayor precedencia determina el destino.
     * 
     * @param userRoles Set de roles del usuario
     * @return URL de destino
     */
    private String determineTargetUrl(Set<String> userRoles) {
        return ROLE_HIERARCHY.stream()
                .filter(userRoles::contains)
                .map(ROLE_TARGET_URL_MAP::get)
                .findFirst()
                .orElseGet(() -> {
                    logger.warn("Usuario sin roles reconocidos: {}. Redirigiendo a página por defecto.", userRoles);
                    return DEFAULT_TARGET_URL;
                });
    }
    

}