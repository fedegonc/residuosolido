package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;
import java.util.Map;

/**
 * Expone el catálogo i18n completo como modelo {@code uiCopies} para templates
 * y para {@code I18nScriptController} (window.uiCopies). No parsea los JSON:
 * reusa la única carga que hace {@link JsonMessageSource} al arrancar.
 */
@ControllerAdvice
public class UiCopyCatalog {

    private final JsonMessageSource messageSource;

    public UiCopyCatalog(JsonMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ModelAttribute("uiCopies")
    public Map<String, String> copies(HttpServletRequest request, Locale locale) {
        return messageSource.catalogFor(locale);
    }
}
