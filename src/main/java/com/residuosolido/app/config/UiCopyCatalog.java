package com.residuosolido.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class UiCopyCatalog {
    private final Map<String, Map<String, String>> catalogs = new HashMap<>();

    public UiCopyCatalog(ObjectMapper mapper) throws IOException {
        for (String language : List.of("es", "pt")) {
            for (String page : List.of("common", "home", "auth", "requests", "track", "users", "org", "onboarding")) {
                try (var input = new ClassPathResource("static/i18n/" + page + "/" + language + ".json").getInputStream()) {
                    catalogs.put(page + "/" + language, Map.copyOf(mapper.readValue(input, new TypeReference<Map<String, String>>() {})));
                }
            }
        }
    }

    @ModelAttribute("uiCopies")
    public Map<String, String> copies(HttpServletRequest request, Locale locale) {
        return forPage(request.getRequestURI().substring(request.getContextPath().length()), locale);
    }

    public Map<String, String> forPage(String path, Locale locale) {
        String language = "pt".equals(locale.getLanguage()) ? "pt" : "es";
        Map<String, String> result = new LinkedHashMap<>(catalogs.get("common/" + language));
        String page = "";
        if (path.equals("/") || path.equals("/index")) page = "home";
        else if (path.equals("/rastrear")) page = "track";
        else if (path.startsWith("/auth/") || path.equals("/error")) page = "auth";
        else if (path.startsWith("/solicitud")) page = "requests";
        else if (path.startsWith("/usuarios/")) page = "users";
        else if (path.startsWith("/acopio/")) page = "org";
        if (!page.isEmpty()) result.putAll(catalogs.get(page + "/" + language));
        if (path.startsWith("/usuarios/") || path.startsWith("/acopio/")) {
            catalogs.get("requests/" + language).forEach(result::putIfAbsent);
        }
        if (path.equals("/acopio/completar-perfil")) result.putAll(catalogs.get("onboarding/" + language));
        return result;
    }
}
