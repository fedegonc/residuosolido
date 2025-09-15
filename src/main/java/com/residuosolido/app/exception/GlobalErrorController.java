package com.residuosolido.app.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Mensaje simple para pruebas
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String target = resolvePanel(auth);
            redirectAttributes.addFlashAttribute("warningMessage", "Página no encontrada. Te hemos redirigido a tu panel principal.");
            return "redirect:" + target;
        }

        redirectAttributes.addFlashAttribute("warningMessage", "Página no encontrada. Por favor, inicia sesión.");
        return "redirect:/auth/login";
    }

    private String resolvePanel(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> r.equals("ROLE_ADMIN")) ? "/admin/dashboard" :
               auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> r.equals("ROLE_ORGANIZATION")) ? "/org/dashboard" :
               "/user/dashboard";
    }
}
