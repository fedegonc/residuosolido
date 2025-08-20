package com.residuosolido.app.controller;

import com.residuosolido.app.service.LanguageTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageController.class);
    
    @Autowired
    private LanguageTrackingService trackingService;

    @Autowired
    private LocaleResolver localeResolver;

    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam("lang") String language,
                                 @RequestParam(value = "referer", required = false) String refererParam,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        
        long startTime = System.currentTimeMillis();
        String userAgent = request.getHeader("User-Agent");
        String clientIP = getClientIP(request);
        String sessionId = request.getSession().getId();
        
        // Obtener idioma actual de la sesión
        Locale currentLocale = (Locale) request.getSession()
            .getAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE");
        String currentLang = currentLocale != null ? currentLocale.getLanguage() : "es";
        
        logger.info("🌍 LANGUAGE_CHANGE_START: {} -> {} | Session: {} | IP: {} | UA: {}", 
                   currentLang, language, sessionId, clientIP, userAgent);
        
        // Validar idioma
        String originalLang = language;
        if (!"es".equals(language) && !"pt".equals(language)) {
            language = "es"; // Default a español
            logger.warn("⚠️ INVALID_LANGUAGE: '{}' -> defaulting to 'es'", originalLang);
        }
        
        // Crear locale
        Locale locale = new Locale(language);
        
        // Aplicar locale usando el LocaleResolver (más robusto que manipular la sesión directamente)
        localeResolver.setLocale(request, response, locale);
        Locale applied = localeResolver.resolveLocale(request);
        logger.info("🌐 LOCALE_APPLIED: {}", applied);
        
        // Obtener URL de referencia
        String referer = request.getHeader("Referer");
        String redirectUrl = "/";
        
        if (refererParam != null && !refererParam.isEmpty()) {
            // Permitir solo rutas relativas por seguridad
            if (refererParam.startsWith("/")) {
                redirectUrl = refererParam;
                logger.info("🔙 REDIRECT_TO_REFERER_PARAM: {}", refererParam);
            } else {
                logger.warn("⚠️ INVALID_REFERER_PARAM: {}", refererParam);
            }
        } else if (referer != null && referer.contains(request.getServerName())) {
            redirectUrl = referer;
            logger.info("🔙 REDIRECT_TO_REFERER_HEADER: {}", referer);
        } else {
            logger.info("🏠 REDIRECT_TO_INDEX: No valid referer");
        }
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("✅ LANGUAGE_CHANGE_COMPLETE: {} -> {} in {}ms | Redirect: {}", 
                   currentLang, language, duration, redirectUrl);
        
        // Trackear el cambio en el servicio de estadísticas
        trackingService.trackLanguageChange(currentLang, language, sessionId, clientIP);
        
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
