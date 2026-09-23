package com.residuosolido.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;

/**
 * Carga el contenido de las páginas servidas por PageController desde
 * pages-{es,pt}.json. A diferencia de LandingCardLoader (ver
 * docs/TRADEOFFS.md §30 — el bug real de un JSON malformado devolviendo
 * lista vacía en silencio), acá un JSON inválido se loggea como ERROR en
 * vez de tragarse: un 404 ruidoso en los logs es preferible a una página
 * vacía sin explicación.
 */
public class PageContentLoader {
    private static final Logger logger = LoggerFactory.getLogger(PageContentLoader.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadPage(String slug, String lang) {
        String filename = "static/i18n/pages-" + ("pt".equals(lang) ? "pt" : "es") + ".json";
        try {
            ClassPathResource resource = new ClassPathResource(filename);
            Map<String, Object> data = mapper.readValue(resource.getInputStream(), Map.class);
            List<Map<String, Object>> pages = (List<Map<String, Object>>) data.get("pages");
            return pages.stream()
                    .filter(p -> slug.equals(p.get("slug")))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("No se pudo cargar '{}' desde {}: {}", slug, filename, e.getMessage(), e);
            return null;
        }
    }
}
