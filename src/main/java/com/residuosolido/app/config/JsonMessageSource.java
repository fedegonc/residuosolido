package com.residuosolido.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MessageSource que lee traducciones desde JSON (static/i18n/{lang}.json).
 * Unifica i18n server-side y client-side en un solo archivo por idioma.
 * Las claves server-side se prefijan con "_server_" y usan "_" en vez de ".".
 */
public class JsonMessageSource implements MessageSource {

    private final Map<String, Map<String, String>> catalogs;

    public JsonMessageSource(ObjectMapper mapper) throws IOException {
        Map<String, Map<String, String>> tmp = new HashMap<>();
        for (String lang : List.of("es", "pt")) {
            try (var input = new ClassPathResource("static/i18n/" + lang + ".json").getInputStream()) {
                tmp.put(lang, mapper.readValue(input, new TypeReference<Map<String, String>>() {}));
            }
        }
        this.catalogs = Map.copyOf(tmp);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) {
        return resolve(code, args, null, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return resolve(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        for (String code : resolvable.getCodes()) {
            String result = resolve(code, resolvable.getArguments(), null, locale);
            if (!result.equals(code)) return result;
        }
        return resolvable.getDefaultMessage() != null ? resolvable.getDefaultMessage() : "";
    }

    private String resolve(String code, Object[] args, String fallback, Locale locale) {
        String lang = "pt".equals(locale.getLanguage()) ? "pt" : "es";
        Map<String, String> catalog = catalogs.get(lang);
        if (catalog == null) catalog = catalogs.get("es");

        // Convertir clave con puntos (login.success) a formato JSON (_server_auth_login_success)
        String jsonKey = toServerKey(code);
        String value = catalog.get(jsonKey);
        if (value == null) value = catalog.get(code);
        if (value == null) value = fallback;
        if (value == null) value = code;

        if (args != null && args.length > 0) {
            try {
                return MessageFormat.format(value, args);
            } catch (Exception e) {
                return value;
            }
        }
        return value;
    }

    /**
     * Convierte una clave con puntos al formato JSON server-side.
     * Ej: "login.success" → "_server_auth_login_success"
     * Ej: "flash.request.created" → "_server_flash_request_created"
     * Ej: "error.register.username_required" → "_server_error_register_username_required"
     */
    private static String toServerKey(String code) {
        return "_server_" + code.replace(".", "_");
    }
}
