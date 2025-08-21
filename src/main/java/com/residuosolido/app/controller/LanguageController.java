package com.residuosolido.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
public class LanguageController {
    

    @Autowired
    private LocaleResolver localeResolver;

    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam("lang") String language,
                                 @RequestParam(value = "referer", required = false) String refererParam,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        String clientIP = getClientIP(request);
        String sessionId = request.getSession().getId();
        
        // Obtener idioma actual de la sesión
        Locale currentLocale = (Locale) request.getSession()
            .getAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE");
        String currentLang = currentLocale != null ? currentLocale.getLanguage() : "es";
        
        // Validar idioma
        String originalLang = language;
        if (!"es".equals(language) && !"pt".equals(language)) {
            language = "es"; // Default a español
        }
        
        // Crear locale
        Locale locale = new Locale(language);
        
        // Aplicar locale usando el LocaleResolver (más robusto que manipular la sesión directamente)
        localeResolver.setLocale(request, response, locale);
        
        // Obtener URL de referencia
        String referer = request.getHeader("Referer");
        String redirectUrl = "/";
        
        if (refererParam != null && !refererParam.isEmpty()) {
            // Permitir solo rutas relativas por seguridad
            if (refererParam.startsWith("/")) {
                redirectUrl = refererParam;
            } else {
                // invalid referer param; fallback
            }
        } else if (referer != null && referer.contains(request.getServerName())) {
            redirectUrl = referer;
        } else {
            // no valid referer; fallback to index
        }
        
        
        return "redirect:" + redirectUrl;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
