package com.residuosolido.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.util.LandingCardLoader;
import com.residuosolido.app.util.PageContentLoader;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba determinística de las capas del sistema de landing cards + páginas por slug,
 * de abajo hacia arriba:
 * 1) JsonDataLayer: landing-cards-{es,pt}.json crudo en disco, válido y completo.
 * 1b) PageContentJsonLayer: pages-{es,pt}.json crudo + integridad referencial contra
 *     landing-cards (todo "id" de card debe tener su "slug" acá).
 * 2) LoaderLayer: LandingCardLoader.loadCards() y PageContentLoader.loadPage() traducen
 *    esos JSON a Map sin perder datos.
 * 3) RenderingLayer: el HTML final (MockMvc, stack Spring real) — 9 cards en el home,
 *    9 páginas reales en /pagina/{slug}, todas con status 200.
 *
 * Motivo de existir: el bug real de esta feature (coma final en el JSON, ver MEJORAS.md #178)
 * rompía la capa 1, pero LandingCardLoader.loadCards() atrapa cualquier Exception y devuelve
 * List.of() en silencio (ver util/LandingCardLoader.java) — la capa 3 seguía devolviendo 200 OK
 * con el contenedor vacío, sin ningún log ni stacktrace. Sin este test, ese fallo solo se
 * detecta mirando el navegador. Correr con: mvn test -Dtest=LandingCardsTest
 */
@Tag("integration")
class LandingCardsTest {

    private static final String[] LANGS = {"es", "pt"};
    private static final int EXPECTED_CARD_COUNT = 9;
    private static final Set<String> REQUIRED_FIELDS =
            Set.of("id", "icon", "title", "description", "button", "href");

    // ══════════════════════════════════════════════════════════════════
    // Capa 1: el JSON crudo en el classpath. Sin Spring, sin mocks.
    // Si esto falla, es SIEMPRE un problema de sintaxis/contenido del archivo,
    // nunca de código Java — el JSON es la fuente de verdad de esta feature.
    // ══════════════════════════════════════════════════════════════════
    @Nested
    class JsonDataLayer {

        private final ObjectMapper mapper = new ObjectMapper();

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void jsonFile_parseableAndWellFormed(String lang) throws Exception {
            JsonNode root = readRawJson(lang);
            assertTrue(root.has("cards"), "[" + lang + "] falta la clave raíz 'cards'");
            assertTrue(root.get("cards").isArray(), "[" + lang + "] 'cards' debe ser un array");
        }

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void jsonFile_hasExactlyExpectedCardCount(String lang) throws Exception {
            JsonNode cards = readRawJson(lang).get("cards");
            assertEquals(EXPECTED_CARD_COUNT, cards.size(),
                    "[" + lang + "] cantidad de cards cambió — actualizar EXPECTED_CARD_COUNT si es intencional");
        }

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void everyCard_hasAllRequiredFieldsNonBlank(String lang) throws Exception {
            JsonNode cards = readRawJson(lang).get("cards");
            for (JsonNode card : cards) {
                String id = card.path("id").asText("<sin id>");
                for (String field : REQUIRED_FIELDS) {
                    assertTrue(card.has(field), "[" + lang + "] card '" + id + "' no tiene campo '" + field + "'");
                    assertFalse(card.get(field).asText().isBlank(),
                            "[" + lang + "] card '" + id + "' tiene '" + field + "' vacío");
                }
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void everyCard_hasUniqueId(String lang) throws Exception {
            JsonNode cards = readRawJson(lang).get("cards");
            java.util.Set<String> ids = new java.util.HashSet<>();
            for (JsonNode card : cards) {
                String id = card.path("id").asText();
                assertTrue(ids.add(id), "[" + lang + "] id duplicado: '" + id + "'");
            }
        }

        @Test
        void firstCard_alwaysLinksToCatadoresSlugPage() throws Exception {
            String expected = Routes.pageUrl("catadores");
            for (String lang : LANGS) {
                JsonNode firstCard = readRawJson(lang).get("cards").get(0);
                assertEquals(expected, firstCard.get("href").asText(),
                        "[" + lang + "] la primera card debe apuntar a " + expected
                                + " (contrato con PageController.SLUG_TEMPLATES/SecurityConfig)");
            }
        }

        @Test
        void bothLanguages_haveSameCardIdsInSameOrder() throws Exception {
            List<String> idsEs = idsOf(readRawJson("es"));
            List<String> idsPt = idsOf(readRawJson("pt"));
            assertEquals(idsEs, idsPt, "El orden/conjunto de ids debe ser idéntico en ES y PT");
        }

        private List<String> idsOf(JsonNode root) {
            List<String> ids = new java.util.ArrayList<>();
            root.get("cards").forEach(c -> ids.add(c.get("id").asText()));
            return ids;
        }

        private JsonNode readRawJson(String lang) throws Exception {
            String filename = "static/i18n/landing-cards-" + lang + ".json";
            try (InputStream in = new ClassPathResource(filename).getInputStream()) {
                return mapper.readTree(in);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Capa 1b: pages-{es,pt}.json — el contenido real detrás de cada card
    // (ver PageController + PageContentLoader). Incluye integridad referencial
    // con landing-cards-{es,pt}.json: todo "id" de card debe tener su "slug"
    // acá, si no el href de la card apunta a un 404.
    // ══════════════════════════════════════════════════════════════════
    @Nested
    class PageContentJsonLayer {

        private final ObjectMapper mapper = new ObjectMapper();

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void pagesJson_parseableWithExpectedCount(String lang) throws Exception {
            JsonNode pages = readPagesJson(lang).get("pages");
            assertNotNull(pages, "[" + lang + "] falta la clave raíz 'pages'");
            assertEquals(EXPECTED_CARD_COUNT, pages.size(),
                    "[" + lang + "] cantidad de páginas no coincide con la cantidad de cards");
        }

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void everyPage_hasRequiredStructure(String lang) throws Exception {
            for (JsonNode page : readPagesJson(lang).get("pages")) {
                String slug = page.path("slug").asText("<sin slug>");
                for (String field : Set.of("slug", "title", "intro", "sections", "closingTitle", "closingBody")) {
                    assertTrue(page.has(field), "[" + lang + "] página '" + slug + "' no tiene campo '" + field + "'");
                }
                JsonNode sections = page.get("sections");
                assertTrue(sections.isArray() && sections.size() >= 1,
                        "[" + lang + "] página '" + slug + "' debe tener al menos 1 sección");
                for (JsonNode section : sections) {
                    assertTrue(section.has("title") && section.has("body"),
                            "[" + lang + "] sección de '" + slug + "' sin title/body");
                }
            }
        }

        @Test
        void everyLandingCardId_hasMatchingPageSlug() throws Exception {
            for (String lang : LANGS) {
                Set<String> pageSlugs = new java.util.HashSet<>();
                readPagesJson(lang).get("pages").forEach(p -> pageSlugs.add(p.get("slug").asText()));

                for (Map<String, Object> card : LandingCardLoader.loadCards(lang)) {
                    String id = (String) card.get("id");
                    assertTrue(pageSlugs.contains(id),
                            "[" + lang + "] card '" + id + "' no tiene página en pages-" + lang + ".json — "
                                    + "su href apunta a un slug que devuelve 404");
                }
            }
        }

        private JsonNode readPagesJson(String lang) throws Exception {
            String filename = "static/i18n/pages-" + lang + ".json";
            try (InputStream in = new ClassPathResource(filename).getInputStream()) {
                return mapper.readTree(in);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Capa 2: LandingCardLoader tal como lo usa AuthController.
    // Sin @SpringBootTest — es un método estático puro, no necesita contexto.
    // ══════════════════════════════════════════════════════════════════
    @Nested
    class LoaderLayer {

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void loadCards_returnsExpectedCountForValidLang(String lang) {
            List<Map<String, Object>> cards = LandingCardLoader.loadCards(lang);
            assertEquals(EXPECTED_CARD_COUNT, cards.size(),
                    "loadCards(\"" + lang + "\") no devolvió " + EXPECTED_CARD_COUNT + " cards — "
                            + "si el JSON es válido (ver JsonDataLayer) y esto falla igual, "
                            + "el bug está en LandingCardLoader, no en el archivo");
        }

        @Test
        void loadCards_unknownLang_fallsBackToSpanish() {
            List<Map<String, Object>> fallback = LandingCardLoader.loadCards("fr");
            List<Map<String, Object>> es = LandingCardLoader.loadCards("es");
            assertEquals(es.size(), fallback.size(), "lang desconocido debe caer a ES, no a lista vacía");
        }

        @Test
        void loadCards_nullLang_fallsBackToSpanish() {
            List<Map<String, Object>> fallback = LandingCardLoader.loadCards(null);
            assertEquals(EXPECTED_CARD_COUNT, fallback.size(), "lang null debe caer a ES, no romper ni vaciar");
        }

        @Test
        void loadCards_neverThrows() {
            assertDoesNotThrow(() -> LandingCardLoader.loadCards(""));
            assertDoesNotThrow(() -> LandingCardLoader.loadCards("xx-YY-inválido"));
        }

        @Test
        void loadCards_preservesFieldValues() {
            Map<String, Object> first = LandingCardLoader.loadCards("es").get(0);
            assertEquals("catadores", first.get("id"));
            assertEquals(Routes.pageUrl("catadores"), first.get("href"));
            assertTrue(((String) first.get("title")).contains("catadores"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"es", "pt"})
        void pageContentLoader_loadsKnownSlug(String lang) {
            Map<String, Object> page = PageContentLoader.loadPage("catadores", lang);
            assertNotNull(page, "[" + lang + "] 'catadores' debe existir en pages-" + lang + ".json");
            assertEquals("catadores", page.get("slug"));
        }

        @Test
        void pageContentLoader_unknownSlug_returnsNull() {
            assertNull(PageContentLoader.loadPage("slug-que-no-existe", "es"));
        }

        @Test
        void pageContentLoader_everyCardIdLoadsSuccessfully() {
            for (String lang : LANGS) {
                for (Map<String, Object> card : LandingCardLoader.loadCards(lang)) {
                    String id = (String) card.get("id");
                    assertNotNull(PageContentLoader.loadPage(id, lang),
                            "[" + lang + "] loadPage(\"" + id + "\") no debería ser null");
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Capa 3: HTML final servido por Spring, stack completo (Thymeleaf + Security + Controller).
    // Esta es la única capa que un `curl localhost:8080/` o el navegador realmente ve.
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @SpringBootTest(properties = {
            "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
            "spring.data.mongodb.auto-index-creation=false"
    })
    @AutoConfigureMockMvc
    class RenderingLayer {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void homePage_rendersExpectedCardCount() throws Exception {
            String html = mockMvc.perform(get(Routes.HOME))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            int count = countOccurrences(html, "class=\"landing-card\"");
            assertEquals(EXPECTED_CARD_COUNT, count,
                    "index.html debe renderizar " + EXPECTED_CARD_COUNT + " <a class=\"landing-card\"> — "
                            + "encontrado: " + count + ". Si esto da 0, el fragment no está recibiendo "
                            + "${cards} del modelo (ver AuthController.rootOrIndex) o el JSON quedó vacío "
                            + "en silencio (ver LoaderLayer)");
        }

        @Test
        void homePage_everyCardHasAnImage() throws Exception {
            String html = mockMvc.perform(get(Routes.HOME))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            int cardCount = countOccurrences(html, "class=\"landing-card\"");
            int imgCount = countOccurrences(html, "landing-card__img");
            assertEquals(cardCount, imgCount, "toda landing-card debe traer su <img class=\"landing-card__img\">");
        }

        @Test
        void homePage_doesNotReferenceDeletedJsFile() throws Exception {
            String html = mockMvc.perform(get(Routes.HOME))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertFalse(html.contains("landing-cards.js"),
                    "index.html no debe referenciar landing-cards.js — la implementación es 100% "
                            + "server-side (Thymeleaf), ese script fue eliminado. Si aparece, es HTML viejo "
                            + "cacheado en el template o un <script> residual sin borrar");
        }

        @Test
        void homePage_firstCardTitle_matchesLocale() throws Exception {
            String htmlEs = mockMvc.perform(get(Routes.HOME))
                    .andReturn().getResponse().getContentAsString();
            assertTrue(htmlEs.contains("Sobre los catadores"), "default (ES) debe mostrar el título en español");

            String htmlPt = mockMvc.perform(get(Routes.HOME).param("lang", "pt"))
                    .andReturn().getResponse().getContentAsString();
            assertTrue(htmlPt.contains("Sobre os catadores"), "?lang=pt debe mostrar el título en portugués");
        }

        @Test
        void catadoresSlugPage_isPubliclyAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get(Routes.pageUrl("catadores")))
                    .andExpect(status().isOk());
        }

        @Test
        void unknownSlug_returns404() throws Exception {
            mockMvc.perform(get(Routes.pageUrl("slug-que-no-existe")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void everyCardHref_resolvesTo200InBothLanguages() throws Exception {
            // Las 9 cards tienen contenido real en pages-{es,pt}.json (ver
            // MEJORAS.md #178/#179) — si se agrega una card nueva sin escribir
            // su página, este test la atrapa (su href apuntaría a un slug 404).
            for (String lang : LANGS) {
                for (Map<String, Object> card : LandingCardLoader.loadCards(lang)) {
                    String href = (String) card.get("href");
                    mockMvc.perform(get(href))
                            .andExpect(status().isOk());
                }
            }
        }

        private int countOccurrences(String haystack, String needle) {
            int count = 0, idx = 0;
            while ((idx = haystack.indexOf(needle, idx)) != -1) {
                count++;
                idx += needle.length();
            }
            return count;
        }
    }
}
