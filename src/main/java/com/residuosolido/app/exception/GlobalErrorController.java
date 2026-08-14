package com.residuosolido.app.exception;

import com.residuosolido.app.config.RoleBasedLoginTargetUrlResolver;
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

    private final MessageSource messageSource;
    private final RoleBasedLoginTargetUrlResolver targetUrlResolver;

    @Autowired
    public GlobalErrorController(MessageSource messageSource, RoleBasedLoginTargetUrlResolver targetUrlResolver) {
        this.messageSource = messageSource;
        this.targetUrlResolver = targetUrlResolver;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        if (status != null && Integer.valueOf(404).equals(status)) {
            return "error/404";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            String target = targetUrlResolver.resolveTargetUrl(auth);
            redirectAttributes.addFlashAttribute("warningMessage", messageSource.getMessage("flash.error.not_found_auth", null, LocaleContextHolder.getLocale()));
            return "redirect:" + target;
        }

        redirectAttributes.addFlashAttribute("warningMessage", messageSource.getMessage("flash.error.not_found_guest", null, LocaleContextHolder.getLocale()));
        return "redirect:/auth/login";
    }
}
