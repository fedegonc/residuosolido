package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.config.JsonMessageSource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Sirve las traducciones client-side como JS externo (elimina inline script del CSP). */
@RestController
public class I18nScriptController {

    private final JsonMessageSource messageSource;
    private final ObjectMapper mapper;

    public I18nScriptController(JsonMessageSource messageSource, ObjectMapper mapper) {
        this.messageSource = messageSource;
        this.mapper = mapper;
    }

    @GetMapping(Routes.I18N_JS)
    public ResponseEntity<String> i18nScript(HttpServletRequest request) throws JsonProcessingException {
        // LocaleContextHolder refleja el LocaleResolver de Spring (sesión/ciudad),
        // no Accept-Language — request.getLocale() ignoraría el ?lang= elegido.
        Map<String, String> copies = messageSource.catalogFor(LocaleContextHolder.getLocale());
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/javascript"))
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofHours(1)))
                .body("window.uiCopies = " + mapper.writeValueAsString(copies) + ";");
    }
}
