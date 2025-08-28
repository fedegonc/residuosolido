package com.residuosolido.app.config;

import com.residuosolido.app.model.Role;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/**
 * Manejador personalizado para redireccionar usuarios según su rol después del login.
 * Sistema flexible que utiliza el enum Role con prioridades para determinar la redirección.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);
    
    // Prefijo estándar para roles en Spring Security
    private static final String ROLE_PREFIX = "ROLE_";
    
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
            logger.info("Destino de autenticación para '{}': {}", username, targetUrl);

            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                    || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");
                String body = "{\"success\":true,\"redirectUrl\":\"" + targetUrl + "\"}";
                response.getWriter().write(body);
                response.getWriter().flush();
            } else {
                response.sendRedirect(targetUrl);
            }
            
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
     * Determina la URL de destino basada en la prioridad de roles.
     * El rol con mayor prioridad determina el destino.
     * 
     * @param userRoles Set de roles del usuario
     * @return URL de destino
     */
    private String determineTargetUrl(Set<String> userRoles) {
        // Obtener el rol con mayor prioridad que tenga el usuario
        Optional<Role> highestPriorityRole = Arrays.stream(Role.values())
                .filter(role -> userRoles.contains(ROLE_PREFIX + role.name()))
                .max(Comparator.comparing(Role::getPriority));
                
        if (highestPriorityRole.isPresent()) {
            String targetUrl = highestPriorityRole.get().getDashboardUrl();
            logger.info("Rol con mayor prioridad: {}, redirigiendo a: {}", 
                    highestPriorityRole.get(), targetUrl);
            return targetUrl;
        } else {
            logger.warn("Usuario sin roles reconocidos: {}. Redirigiendo a página por defecto.", userRoles);
            return DEFAULT_TARGET_URL;
        }
    }
    

}