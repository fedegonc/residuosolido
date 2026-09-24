package com.residuosolido.app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PageContentRenderingTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void imageHeroRenderWithoutHtmlEscapes() throws Exception {
        String html = mvc.perform(get("/pagina/catadores"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("page-content__hero"), "Should have hero container");
        assertTrue(html.contains("page-content__image"), "Should have image class");

        String src = extractSrcFromHtml(html);
        assertTrue(src.startsWith("https://"), "Should have https image URL");
        assertFalse(src.contains("&#39;") || src.contains("&quot;"),
            "Image src should not have HTML entity escapes");
    }

    @Test
    void imageHasValidSrcAttribute() throws Exception {
        String html = mvc.perform(get("/pagina/catadores"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("class=\"page-content__image\""), "Should have image class attribute");
        assertTrue(html.contains("src=\"https://"), "Should have valid src attribute");
    }

    @Test
    void allPageImagesRender() throws Exception {
        String[] pages = {"catadores", "impacto", "sostenibilidad", "comunidad",
                         "proceso", "compromiso", "eventos", "recursos", "faq"};

        for (String page : pages) {
            String html = mvc.perform(get("/pagina/" + page))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

            assertTrue(html.contains("page-content__image"),
                "Page '" + page + "' should have image");
            assertTrue(html.contains("src=\"https://"),
                "Page '" + page + "' should have https image URL");
        }
    }

    @Test
    void bilingualImagesMatch() throws Exception {
        String imageEs = mvc.perform(get("/pagina/catadores"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String imagePt = mvc.perform(get("/pagina/catadores")
            .header("Accept-Language", "pt"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String srcEs = extractSrcFromHtml(imageEs);
        String srcPt = extractSrcFromHtml(imagePt);

        assertEquals(srcEs, srcPt, "Image src should be same in both languages");
    }

    @Test
    void dataUriEncodingIsCorrect() throws Exception {
        String html = mvc.perform(get("/pagina/catadores"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String src = extractSrcFromHtml(html);
        assertTrue(src.startsWith("https://"), "Image src should be an https URL");

        assertFalse(src.contains("&#39;") || src.contains("&quot;"),
            "Image src should not have HTML entity escapes");
        assertFalse(src.contains("%25"), "Image src should not have double-encoded characters");
    }

    private String extractSrcFromHtml(String html) {
        int start = html.indexOf("page-content__image");
        if (start == -1) return "";

        int srcStart = html.indexOf("src=\"", start);
        if (srcStart == -1) return "";

        int srcEnd = html.indexOf("\"", srcStart + 5);
        if (srcEnd == -1) return "";

        return html.substring(srcStart + 5, srcEnd);
    }
}
