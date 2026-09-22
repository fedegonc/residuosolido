package com.residuosolido.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import java.util.List;
import java.util.Map;

public class LandingCardLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Map<String, Object>> loadCards(String lang) {
        try {
            String filename = "static/i18n/landing-cards-" + ("pt".equals(lang) ? "pt" : "es") + ".json";
            ClassPathResource resource = new ClassPathResource(filename);
            Map<String, Object> data = mapper.readValue(resource.getInputStream(), Map.class);
            return (List<Map<String, Object>>) data.get("cards");
        } catch (Exception e) {
            return List.of();
        }
    }
}
