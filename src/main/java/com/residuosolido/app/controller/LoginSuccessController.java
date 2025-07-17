package com.residuosolido.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para manejar redirecciones después del inicio de sesión
 */
@Controller
public class LoginSuccessController {

    /**
     * Redirecciona al usuario al dashboard correspondiente según su rol
     * @param authentication Información de autenticación del usuario
     * @return URL de redirección
     */
    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/dashboard-admin";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ORGANIZACION"))) {
            return "redirect:/dashboard-organizacion";
        } else {
            return "redirect:/dashboard-usuario";
        }
    }
}
