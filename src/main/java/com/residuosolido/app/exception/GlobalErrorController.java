package com.residuosolido.app.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GlobalErrorController implements ErrorController {

    @Autowired
    private MessageSource messageSource;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Mensaje simple para pruebas
        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String target = resolvePanel(auth);
            redirectAttributes.addFlashAttribute("warningMessage", messageSource.getMessage("flash.error.not_found_auth", null, LocaleContextHolder.getLocale()));
            return "redirect:" + target;
        }

        redirectAttributes.addFlashAttribute("warningMessage", messageSource.getMessage("flash.error.not_found_guest", null, LocaleContextHolder.getLocale()));
        return "redirect:/auth/login";
    }

    private String resolvePanel(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(r -> "ROLE_ORGANIZATION".equals(r)) ? "/acopio/inicio" :
               "/usuarios/inicio";
    }
}
