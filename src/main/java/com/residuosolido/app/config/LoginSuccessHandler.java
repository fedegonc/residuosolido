package com.residuosolido.app.config;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
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
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
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
    
    @Autowired
    @org.springframework.context.annotation.Lazy
    private UserService userService;
    
    // Prefijo estándar para roles en Spring Security
    private static final String ROLE_PREFIX = "ROLE_";
    
    private static final String DEFAULT_TARGET_URL = "/dashboard";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Set<String> userRoles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String username = authentication.getName();
        logger.info("Usuario '{}' autenticado. Redirigiendo según rol. Roles: {}", username, userRoles);

        try {
            // Verificar si es una organización con perfil incompleto
            if (userRoles.contains("ROLE_ORGANIZATION")) {
                try {
                    User user = userService.findAuthenticatedUserByUsername(username);
                    if (user.getProfileCompleted() == null || !user.getProfileCompleted()) {
                        logger.info("Organización '{}' con perfil incompleto, redirigiendo a completar perfil", username);
                        response.sendRedirect("/organization/complete-profile");
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Error al verificar perfil de organización '{}': {}", username, e.getMessage());
                }
            }
            
            String targetUrl = getTargetUrlByRoles(userRoles);

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
    public void redirectToDashboard(HttpServletRequest request,
                                    HttpServletResponse response,
                                    UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        Set<String> userRoles = AuthorityUtils.authorityListToSet(userDetails.getAuthorities());
        String targetUrl = getTargetUrlByRoles(userRoles);
        
        logger.info("Redirigiendo usuario '{}' a '{}' según rol: {}", username, targetUrl, userRoles);
        response.sendRedirect(targetUrl);
    }

    /**
     * Determina la URL de destino basada en los roles del usuario.
     * 
     * @param userRoles Set de roles del usuario
     * @return URL de destino
     */
    private String getTargetUrlByRoles(Set<String> userRoles) {
        if (userRoles.contains("ROLE_ADMIN")) {
            return "/admin/dashboard";
        } else if (userRoles.contains("ROLE_ORGANIZATION")) {
            return "/acopio/inicio";
        } else if (userRoles.contains("ROLE_USER")) {
            return "/usuarios/inicio";
        } else {
            return DEFAULT_TARGET_URL;
        }
    }
    
}