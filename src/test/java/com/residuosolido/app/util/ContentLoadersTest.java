package com.residuosolido.app.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de los loaders de contenido estático (landing cards y páginas):
 * fallback de idioma (cualquier lang ≠ pt → es), slug inexistente → null.
 */
@Tag("unit")
class ContentLoadersTest {

    // ===== LandingCardLoader =====

    @Test
    void loadCards_spanish_returnsCards() {
        List<Map<String, Object>> cards = LandingCardLoader.loadCards("es");

        assertNotNull(cards);
        assertFalse(cards.isEmpty());
    }

    @Test
    void loadCards_portuguese_returnsPtCards() {
        List<Map<String, Object>> cards = LandingCardLoader.loadCards("pt");

        assertNotNull(cards);
        assertFalse(cards.isEmpty());
    }

    @Test
    void loadCards_unknownLang_fallsBackToSpanish() {
        // Solo "pt" bifurca: cualquier otro lang carga el JSON en español.
        List<Map<String, Object>> cards = LandingCardLoader.loadCards("fr");

        assertNotNull(cards);
        assertFalse(cards.isEmpty());
    }

    // ===== PageContentLoader =====

    @Test
    void loadPage_knownSlugSpanish_returnsPage() {
        Map<String, Object> page = PageContentLoader.loadPage("catadores", "es");

        assertNotNull(page);
        assertEquals("catadores", page.get("slug"));
    }

    @Test
    void loadPage_knownSlugPortuguese_returnsPage() {
        Map<String, Object> page = PageContentLoader.loadPage("catadores", "pt");

        assertNotNull(page);
        assertEquals("catadores", page.get("slug"));
    }

    @Test
    void loadPage_unknownSlug_returnsNull() {
        assertNull(PageContentLoader.loadPage("pagina-inexistente", "es"));
    }

    @Test
    void loadPage_unknownLang_fallsBackToSpanish() {
        Map<String, Object> page = PageContentLoader.loadPage("catadores", "de");

        assertNotNull(page);
        assertEquals("catadores", page.get("slug"));
    }
}
