package com.residuosolido.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class UiCopyCatalog {
    private final Map<String, Map<String, String>> catalogs;

    public UiCopyCatalog(ObjectMapper mapper) throws IOException {
        java.util.Map<String, Map<String, String>> tmp = new java.util.HashMap<>();
        for (String language : List.of("es", "pt")) {
            try (var input = new ClassPathResource("static/i18n/" + language + ".json").getInputStream()) {
                tmp.put(language, Map.copyOf(mapper.readValue(input, new TypeReference<Map<String, String>>() {})));
            }
        }
        this.catalogs = Map.copyOf(tmp);
    }

    @ModelAttribute("uiCopies")
    public Map<String, String> copies(HttpServletRequest request, Locale locale) {
        return catalogs.get("pt".equals(locale.getLanguage()) ? "pt" : "es");
    }
}
