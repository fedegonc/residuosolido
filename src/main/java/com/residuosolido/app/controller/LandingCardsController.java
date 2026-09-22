package com.residuosolido.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

/** Sirve el contenido de cards educativas de la landing en JSON.
    Endpoint ligero para llenar cards dinámicamente sin duplicar copys en templates. */
@RestController
@RequestMapping("/api/landing-cards")
public class LandingCardsController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public JsonNode getLandingCards(@RequestParam(defaultValue = "es") String lang) throws Exception {
        String filename = "landing-cards-" + ("pt".equals(lang) ? "pt" : "es") + ".json";
        ClassPathResource resource = new ClassPathResource("static/i18n/" + filename);
        return objectMapper.readTree(resource.getInputStream());
    }
}
