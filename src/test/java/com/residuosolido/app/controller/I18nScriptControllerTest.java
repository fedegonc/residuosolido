package com.residuosolido.app.controller;

import com.residuosolido.app.config.UiCopyCatalog;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class I18nScriptControllerTest {

    private UiCopyCatalog uiCopyCatalog;
    private I18nScriptController controller;

    @BeforeEach
    void setUp() {
        uiCopyCatalog = mock(UiCopyCatalog.class);
        controller = new I18nScriptController(uiCopyCatalog);
    }

    @Test
    void i18nScript_emitsCopiesAsJsObject() {
        when(uiCopyCatalog.copies(any(), any())).thenReturn(Map.of("saludo", "hola"));

        ResponseEntity<String> response = controller.i18nScript(mock(HttpServletRequest.class));

        assertEquals("application/javascript", response.getHeaders().getContentType().toString());
        assertTrue(response.getBody().startsWith("window.uiCopies = {"));
        assertTrue(response.getBody().contains("\"saludo\":\"hola\""));
    }

    @Test
    void i18nScript_escapesQuotesBackslashesAndNewlines() {
        Map<String, String> copies = new LinkedHashMap<>();
        copies.put("frase", "dijo \"hola\"\ny se fue\\");
        when(uiCopyCatalog.copies(any(), any())).thenReturn(copies);

        String body = controller.i18nScript(mock(HttpServletRequest.class)).getBody();

        assertTrue(body.contains("\\\"hola\\\""));
        assertTrue(body.contains("\\n"));
        assertFalse(body.contains("\n")); // el newline literal rompería el JS
    }

    @Test
    void i18nScript_emptyCopies_emitsEmptyObject() {
        when(uiCopyCatalog.copies(any(), any())).thenReturn(Map.of());

        String body = controller.i18nScript(mock(HttpServletRequest.class)).getBody();

        assertEquals("window.uiCopies = {};", body);
    }
}
