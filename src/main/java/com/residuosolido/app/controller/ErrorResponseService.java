package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Centraliza redirecciones de error con validación de seguridad.
 *
 * Responsabilidades:
 * - Validar que Referer sea URL interna (prevenir OPEN REDIRECT)
 * - Fallback a destino seguro por rol si Referer es inválido/malicioso
 * - Añadir flash attributes de error de forma consistente
 */
@Service
public class ErrorResponseService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseService.class);

    /**
     * Valida que una URL de Referer sea segura (misma aplicación).
     *
     * @param referer header Referer (puede ser null o blank)
     * @param request HttpServletRequest para obtener host/scheme
     * @return true si Referer es válido e interno, false si null/blank/malicioso
     */
    protected boolean isValidInternalReferer(String referer, HttpServletRequest request) {
        if (referer == null || referer.isBlank()) {
            return false;
        }

        try {
            URI refererUri = new URI(referer);
            URI requestUri = new URI(request.getScheme(), null, request.getServerName(),
                    request.getServerPort(), null, null, null);

            // Validar: mismo scheme + host + port
            boolean sameScheme = requestUri.getScheme().equals(refererUri.getScheme());
            boolean sameHost = requestUri.getHost().equals(refererUri.getHost());
            boolean samePort = requestUri.getPort() == refererUri.getPort();

            return sameScheme && sameHost && samePort;
        } catch (URISyntaxException e) {
            logger.warn("Referer malformado, ignorado: {}", referer);
            return false;
        }
    }

    /**
     * Resuelve el destino seguro para un error:
     * - Si Referer es válido e interno → usa Referer
     * - Si Referer es inválido/malicioso/null → fallback por rol
     *
     * @param referer header Referer (puede ser null)
     * @param request HttpServletRequest
     * @return URL segura para redirect (con prefijo "redirect:")
     */
    public String resolveErrorDestination(String referer, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isValidInternalReferer(referer, request)) {
            logger.debug("Referer válido e interno, redirigiendo: {}", referer);
            return "redirect:" + referer;
        }

        if (referer != null && !referer.isBlank()) {
            logger.warn("Referer inválido/externo, ignorado: {}", referer);
        }

        // Fallback seguro: por rol
        Object originalUri = request.getAttribute("jakarta.servlet.error.request_uri");
        String currentUri = originalUri instanceof String s ? s : request.getRequestURI();
        return Routes.resolveErrorNavigation(auth, currentUri);
    }

    /**
     * Redirige a destino de error con mensaje flash.
     * Maneja validación de Referer automáticamente.
     *
     * @param referer header Referer (puede ser null)
     * @param request HttpServletRequest
     * @param redirectAttributes para flash attributes
     * @param messageKey ServerMessage enum key
     * @return destino de redirect seguro
     */
    public String redirectWithError(String referer, HttpServletRequest request,
                                   RedirectAttributes redirectAttributes,
                                   com.residuosolido.app.exception.ServerMessage messageKey,
                                   Messages messages) {
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(messageKey));
        return resolveErrorDestination(referer, request);
    }

    /**
     * Redirige a destino de error con mensaje warning.
     */
    public String redirectWithWarning(String referer, HttpServletRequest request,
                                     RedirectAttributes redirectAttributes,
                                     com.residuosolido.app.exception.ServerMessage messageKey,
                                     Messages messages) {
        redirectAttributes.addFlashAttribute("warningMessage", messages.msg(messageKey));
        return resolveErrorDestination(referer, request);
    }
}
