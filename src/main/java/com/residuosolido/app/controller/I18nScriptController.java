package com.residuosolido.app.controller;

import com.residuosolido.app.config.UiCopyCatalog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

/** Sirve las traducciones client-side como JS externo (elimina inline script del CSP). */
@RestController
public class I18nScriptController {

    private final UiCopyCatalog uiCopyCatalog;

    public I18nScriptController(@Lazy UiCopyCatalog uiCopyCatalog) {
        this.uiCopyCatalog = uiCopyCatalog;
    }

    @GetMapping("/js/i18n.js")
    public ResponseEntity<String> i18nScript(HttpServletRequest request) {
        // LocaleContextHolder refleja el LocaleResolver de Spring (sesión/ciudad),
        // no Accept-Language — request.getLocale() ignoraría el ?lang= elegido.
        Map<String, String> copies = uiCopyCatalog.copies(request, LocaleContextHolder.getLocale());
        StringBuilder sb = new StringBuilder();
        sb.append("window.uiCopies = {");
        boolean first = true;
        for (Map.Entry<String, String> e : copies.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":\"").append(escape(e.getValue())).append("\"");
        }
        sb.append("};");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/javascript"))
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofHours(1)))
                .body(sb.toString());
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
