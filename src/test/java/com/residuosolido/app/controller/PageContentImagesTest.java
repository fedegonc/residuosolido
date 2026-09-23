package com.residuosolido.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PageContentImagesTest {

    private ObjectMapper mapper;
    private Map<String, Object> pagesEs;
    private Map<String, Object> pagesPt;

    @BeforeEach
    void setup() throws IOException {
        mapper = new ObjectMapper();
        ClassPathResource resourceEs = new ClassPathResource("static/i18n/pages-es.json");
        ClassPathResource resourcePt = new ClassPathResource("static/i18n/pages-pt.json");

        pagesEs = mapper.readValue(resourceEs.getInputStream(), Map.class);
        pagesPt = mapper.readValue(resourcePt.getInputStream(), Map.class);
    }

    @Test
    void allPagesHaveImages() {
        List<Map<String, Object>> pagesListEs = (List<Map<String, Object>>) pagesEs.get("pages");
        List<Map<String, Object>> pagesListPt = (List<Map<String, Object>>) pagesPt.get("pages");

        assertNotNull(pagesListEs, "Spanish pages list should not be null");
        assertNotNull(pagesListPt, "Portuguese pages list should not be null");

        assertEquals(pagesListEs.size(), pagesListPt.size(),
            "Spanish and Portuguese should have same number of pages");

        for (Map<String, Object> page : pagesListEs) {
            String slug = (String) page.get("slug");
            assertNotNull(page.get("image"), "Page '" + slug + "' must have image");
            assertNotNull(page.get("title"), "Page '" + slug + "' must have title");
            assertNotNull(page.get("intro"), "Page '" + slug + "' must have intro");
        }
    }

    @Test
    void imagesAreValidDataUris() {
        List<Map<String, Object>> pagesList = (List<Map<String, Object>>) pagesEs.get("pages");

        for (Map<String, Object> page : pagesList) {
            String slug = (String) page.get("slug");
            String image = (String) page.get("image");

            assertTrue(image.startsWith("data:image/svg+xml,"),
                "Image for '" + slug + "' must be data:image/svg+xml");

            assertTrue(image.contains("%3Csvg"),
                "Image for '" + slug + "' must contain URL-encoded SVG tag");

            assertFalse(image.contains("&#39;") || image.contains("&quot;"),
                "Image for '" + slug + "' must NOT have HTML entity escapes");
        }
    }

    @Test
    void dataUrisCanBeDecoded() {
        List<Map<String, Object>> pagesList = (List<Map<String, Object>>) pagesEs.get("pages");

        Pattern svgPattern = Pattern.compile("data:image/svg\\+xml,(.+)");

        for (Map<String, Object> page : pagesList) {
            String slug = (String) page.get("slug");
            String image = (String) page.get("image");

            var matcher = svgPattern.matcher(image);
            assertTrue(matcher.find(), "Image for '" + slug + "' must match SVG data URI pattern");

            String encodedSvg = matcher.group(1);
            try {
                String decodedSvg = URLDecoder.decode(encodedSvg, StandardCharsets.UTF_8);
                assertTrue(decodedSvg.contains("<svg"),
                    "Decoded SVG for '" + slug + "' must contain <svg tag");
                assertTrue(decodedSvg.contains("</svg>"),
                    "Decoded SVG for '" + slug + "' must contain closing </svg>");
            } catch (Exception e) {
                fail("Cannot decode image for '" + slug + "': " + e.getMessage());
            }
        }
    }

    @Test
    void noDoubleEncodingInImages() {
        List<Map<String, Object>> pagesList = (List<Map<String, Object>>) pagesEs.get("pages");

        for (Map<String, Object> page : pagesList) {
            String slug = (String) page.get("slug");
            String image = (String) page.get("image");

            // %27 is correct (URL-encoded single quote)
            // &#39; or %2527 would be wrong (double-encoded)
            assertFalse(image.contains("%2527"),
                "Image for '" + slug + "' has double-encoded quotes (%2527)");

            assertFalse(image.contains("&#39;"),
                "Image for '" + slug + "' has HTML entity escapes (&#39;)");

            // Should have %27 for single quotes
            assertTrue(image.contains("%27"),
                "Image for '" + slug + "' must have URL-encoded quotes (%27)");
        }
    }

    @Test
    void imagesSpanishAndPortugueseMatch() {
        List<Map<String, Object>> pagesListEs = (List<Map<String, Object>>) pagesEs.get("pages");
        List<Map<String, Object>> pagesListPt = (List<Map<String, Object>>) pagesPt.get("pages");

        for (int i = 0; i < pagesListEs.size(); i++) {
            Map<String, Object> pageEs = pagesListEs.get(i);
            Map<String, Object> pagePt = pagesListPt.get(i);

            String imageEs = (String) pageEs.get("image");
            String imagePt = (String) pagePt.get("image");

            assertEquals(imageEs, imagePt,
                "Spanish and Portuguese versions of page should have same image");
        }
    }
}
