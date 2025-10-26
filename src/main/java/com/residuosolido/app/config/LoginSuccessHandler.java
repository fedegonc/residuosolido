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
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    private static final String ROLE_ADMIN = ROLE_PREFIX + "ADMIN";
    private static final String ROLE_ORGANIZATION = ROLE_PREFIX + "ORGANIZATION";
    private static final String ROLE_USER = ROLE_PREFIX + "USER";
    
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
            // IMPORTANTE: Siempre traer desde BD, nunca desde cache
            if (userRoles.contains(ROLE_ORGANIZATION)) {
                try {
                    logger.info("========================================");
                    logger.info("=== LOGIN ORGANIZACIÓN ===");
                    logger.info("========================================");
                    
                    User user = userService.findAuthenticatedUserByUsername(username);
                    Boolean profileCompleted = user.getProfileCompleted();
                    
                    logger.info("Username: {}", username);
                    logger.info("ID en BD: {}", user.getId());
                    logger.info("Email: {}", user.getEmail());
                    logger.info("Rol: {}", user.getRole());
                    logger.info("----------------------------------------");
                    logger.info("profileCompleted en BD: {}", profileCompleted);
                    logger.info("Tipo de dato: {}", (profileCompleted != null ? profileCompleted.getClass().getSimpleName() : "NULL"));
                    logger.info("----------------------------------------");
                    
                    if (profileCompleted == null || Boolean.FALSE.equals(profileCompleted)) {
                        logger.warn("⚠️ PERFIL INCOMPLETO DETECTADO ⚠️");
                        logger.warn("Valor actual: {}", profileCompleted);
                        logger.warn("Acción: Redirigiendo a /acopio/completar-perfil");
                        logger.info("========================================");
                        response.sendRedirect("/acopio/completar-perfil");
                        return;
                    } else {
                        logger.info("✅ PERFIL COMPLETO DETECTADO ✅");
                        logger.info("Valor: {}", profileCompleted);
                        logger.info("Acción: Permitiendo acceso a /acopio/inicio");
                        logger.info("========================================");
                    }
                } catch (Exception e) {
                    logger.error("========================================");
                    logger.error("❌ ERROR al verificar perfil de organización '{}'", username);
                    logger.error("Mensaje: {}", e.getMessage());
                    logger.error("Stack trace:", e);
                    logger.error("========================================");
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
        if (userRoles.contains(ROLE_ADMIN)) {
            return "/admin/dashboard";
        } else if (userRoles.contains(ROLE_ORGANIZATION)) {
            return "/acopio/inicio";
        } else if (userRoles.contains(ROLE_USER)) {
            return "/usuarios/inicio";
        } else {
            return DEFAULT_TARGET_URL;
        }
    }
    
}