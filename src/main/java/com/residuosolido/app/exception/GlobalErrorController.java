package com.residuosolido.app.exception;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.controller.BaseController;
import com.residuosolido.app.enums.ServerMessage;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * msg() heredado de BaseController — antes era una copia propia que usaba la
 * variante de getMessage() SIN fallback (tiraba NoSuchMessageException si
 * faltaba la clave, justo en la página de error). El redirect por rol ahora
 * reusa Routes.resolveErrorNavigation en vez de reimplementarlo a mano —
 * gana la guarda anti-loop que esa función ya tiene (ver #144: el bug real
 * de loop infinito era exactamente esta clase de problema).
 */
@Controller
public class GlobalErrorController extends BaseController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        if (status != null && Integer.valueOf(404).equals(status)) {
            return "error/404";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
        redirectAttributes.addFlashAttribute("warningMessage",
                msg(authenticated ? ServerMessage.FLASH_ERROR_NOT_FOUND_AUTH : ServerMessage.FLASH_ERROR_NOT_FOUND_GUEST));
        return Routes.resolveErrorNavigation(auth, request.getRequestURI());
    }
}
