package com.residuosolido.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador de prueba para verificar la resolución de mensajes internacionalizados
 * Este controlador es solo para fines de diagnóstico y puede ser eliminado en producción
 */
@Controller
@RequestMapping("/i18n-test")
public class I18nTestController {

    private static final Logger log = LoggerFactory.getLogger(I18nTestController.class);
    private final MessageSource messageSource;

    public I18nTestController(MessageSource messageSource) {
        this.messageSource = messageSource;
        log.info("[I18N-TEST] Controlador de prueba I18n inicializado");
    }

    /**
     * Endpoint para probar la resolución de mensajes en español
     */
    @GetMapping("/es")
    @ResponseBody
    public Map<String, String> testSpanish() {
        log.info("[I18N-TEST] Probando resolución de mensajes en español");
        return getMessages(new Locale("es"));
    }

    /**
     * Endpoint para probar la resolución de mensajes en portugués
     */
    @GetMapping("/pt")
    @ResponseBody
    public Map<String, String> testPortuguese() {
        log.info("[I18N-TEST] Probando resolución de mensajes en portugués");
        return getMessages(new Locale("pt"));
    }

    /**
     * Endpoint para mostrar una página HTML con los mensajes resueltos
     */
    @GetMapping
    public String testPage(Model model, Locale locale) {
        log.info("[I18N-TEST] Mostrando página de prueba con locale: {}", locale);
        model.addAttribute("currentLocale", locale.toString());
        model.addAttribute("messages", getMessages(locale));
        return "i18n-test";
    }

    /**
     * Método auxiliar para obtener mensajes en un locale específico
     */
    private Map<String, String> getMessages(Locale locale) {
        Map<String, String> messages = new HashMap<>();
        
        // Mensajes del hero
        messages.put("index.hero.title", getMessage("index.hero.title", locale));
        messages.put("index.hero.subtitle", getMessage("index.hero.subtitle", locale));
        messages.put("index.hero.description", getMessage("index.hero.description", locale));
        messages.put("index.hero.recyclables.title", getMessage("index.hero.recyclables.title", locale));
        messages.put("index.hero.recyclables.description", getMessage("index.hero.recyclables.description", locale));
        
        // Idiomas
        messages.put("language.spanish", getMessage("language.spanish", locale));
        messages.put("language.portuguese", getMessage("language.portuguese", locale));
        
        // Navegación
        messages.put("nav.categories", getMessage("nav.categories", locale));
        messages.put("nav.posts", getMessage("nav.posts", locale));
        messages.put("nav.organizations", getMessage("nav.organizations", locale));
        
        log.info("[I18N-TEST] Mensajes resueltos para locale {}: {}", locale, messages);
        return messages;
    }

    /**
     * Método para resolver un mensaje con manejo de errores
     */
    private String getMessage(String code, Locale locale) {
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (Exception e) {
            log.error("[I18N-TEST] Error al resolver mensaje '{}' para locale {}: {}", code, locale, e.getMessage());
            return "ERROR: " + code;
        }
    }
}
