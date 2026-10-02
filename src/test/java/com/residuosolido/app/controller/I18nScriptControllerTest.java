package com.residuosolido.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.config.JsonMessageSource;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class I18nScriptControllerTest {

    private JsonMessageSource messageSource;
    private I18nScriptController controller;

    @BeforeEach
    void setUp() {
        messageSource = mock(JsonMessageSource.class);
        controller = new I18nScriptController(messageSource, new ObjectMapper());
    }

    @Test
    void i18nScript_emitsCopiesAsJsObject() throws Exception {
        when(messageSource.catalogFor(any())).thenReturn(Map.of("saludo", "hola"));

        ResponseEntity<String> response = controller.i18nScript(mock(HttpServletRequest.class));

        assertEquals("application/javascript", response.getHeaders().getContentType().toString());
        assertTrue(response.getBody().startsWith("window.uiCopies = {"));
        assertTrue(response.getBody().contains("\"saludo\":\"hola\""));
    }

    @Test
    void i18nScript_escapesQuotesBackslashesAndNewlines() throws Exception {
        Map<String, String> copies = new LinkedHashMap<>();
        copies.put("frase", "dijo \"hola\"\ny se fue\\");
        when(messageSource.catalogFor(any())).thenReturn(copies);

        String body = controller.i18nScript(mock(HttpServletRequest.class)).getBody();

        assertTrue(body.contains("\\\"hola\\\""));
        assertTrue(body.contains("\\n"));
        assertFalse(body.contains("\n")); // el newline literal rompería el JS
    }

    @Test
    void i18nScript_controlCharactersRoundTripAsJson() throws Exception {
        Map<String, String> copies = Map.of("clave\t", "texto\t\b\f\u0001");
        when(messageSource.catalogFor(any())).thenReturn(copies);

        String body = controller.i18nScript(mock(HttpServletRequest.class)).getBody();
        String json = body.substring("window.uiCopies = ".length(), body.length() - 1);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.valueToTree(copies), mapper.readTree(json));
    }

    @Test
    void i18nScript_usesResolvedLocaleInsteadOfBrowserLocale() throws Exception {
        Locale original = LocaleContextHolder.getLocale();
        Locale portuguese = Locale.forLanguageTag("pt");
        when(messageSource.catalogFor(portuguese)).thenReturn(Map.of("saludo", "olá"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.forLanguageTag("es"));
        try {
            LocaleContextHolder.setLocale(portuguese);
            assertTrue(controller.i18nScript(request).getBody().contains("\"saludo\":\"olá\""));
        } finally {
            LocaleContextHolder.setLocale(original);
        }
    }

    @Test
    void i18nScript_emptyCopies_emitsEmptyObject() throws Exception {
        when(messageSource.catalogFor(any())).thenReturn(Map.of());

        String body = controller.i18nScript(mock(HttpServletRequest.class)).getBody();

        assertEquals("window.uiCopies = {};", body);
    }
}
