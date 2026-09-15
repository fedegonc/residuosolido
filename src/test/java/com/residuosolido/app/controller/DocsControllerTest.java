package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de regresión para DocsController y rutas públicas de documentación:
 * - /documentos responde 200 y muestra el índice
 * - /diagramas responde 200 y muestra el índice
 * - /docs/*.md se sirve con content-type text/markdown (no octet-stream)
 * - /docs/diagrams/*.drawio se sirve con content-type application/xml
 * - Archivos inexistentes devuelven 404
 * - Path traversal bloqueado
 */
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class DocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== Páginas de índice ==========

    @Test
    void documentosPage_returns200() throws Exception {
        mockMvc.perform(get(Routes.DOCUMENTOS))
                .andExpect(status().isOk())
                .andExpect(view().name("public/docs"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Documentación técnica")));
    }

    @Test
    void diagramasPage_returns200() throws Exception {
        mockMvc.perform(get(Routes.DIAGRAMAS))
                .andExpect(status().isOk())
                .andExpect(view().name("public/diagrams"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Figura 1")));
    }

    @Test
    void documentosPage_listsAllDocs() throws Exception {
        String html = mockMvc.perform(get(Routes.DOCUMENTOS))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // Verifica que los 5 documentos aparecen como links
        for (String doc : new String[]{"DEFENSA", "METODOLOGIA", "DIAGRAMAS", "ENDPOINTS", "MEJORAS"}) {
            org.junit.jupiter.api.Assertions.assertTrue(
                    html.contains("/docs/" + doc + ".md"),
                    "Falta link a " + doc + ".md en /documentos");
        }
    }

    @Test
    void diagramasPage_listsAllFigures() throws Exception {
        String html = mockMvc.perform(get(Routes.DIAGRAMAS))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (String fig : new String[]{"figura1-casos-uso", "figura2-modelo-logico",
                "figura3-clases", "figura4-estados"}) {
            org.junit.jupiter.api.Assertions.assertTrue(
                    html.contains(fig + ".drawio"),
                    "Falta link a " + fig + ".drawio en /diagramas");
        }
    }

    // ========== Content-type de archivos .md ==========

    @Test
    void markdownFile_servedWithMarkdownContentType() throws Exception {
        mockMvc.perform(get(Routes.DOCS_FILE, "DEFENSA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.TEXT_MARKDOWN));
    }

    @Test
    void markdownFile_hasInlineDisposition() throws Exception {
        mockMvc.perform(get(Routes.DOCS_FILE, "DEFENSA"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")));
    }

    @Test
    void markdownFile_containsContent() throws Exception {
        String body = mockMvc.perform(get(Routes.DOCS_FILE, "DEFENSA"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertFalse(body.isBlank(), "El archivo .md no debe estar vacío");
    }

    @Test
    void allMarkdownFiles_servedCorrectly() throws Exception {
        for (String doc : new String[]{"DEFENSA", "METODOLOGIA", "DIAGRAMAS", "ENDPOINTS", "MEJORAS"}) {
            mockMvc.perform(get("/docs/" + doc + ".md"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.TEXT_MARKDOWN));
        }
    }

    // ========== Content-type de archivos .drawio ==========

    @Test
    void drawioFile_servedWithXmlContentType() throws Exception {
        mockMvc.perform(get(Routes.DOCS_DIAGRAM, "figura4-estados"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_XML));
    }

    @Test
    void drawioFile_containsMxfileRoot() throws Exception {
        String body = mockMvc.perform(get(Routes.DOCS_DIAGRAM, "figura4-estados"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(
                body.contains("<mxfile") || body.contains("mxGraphModel"),
                "El archivo .drawio debe contener XML de mxGraph");
    }

    @Test
    void allDrawioFiles_servedCorrectly() throws Exception {
        for (String fig : new String[]{"figura1-casos-uso", "figura2-modelo-logico",
                "figura3-clases", "figura4-estados"}) {
            mockMvc.perform(get("/docs/diagrams/" + fig + ".drawio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_XML));
        }
    }

    // ========== Casos de error ==========

    @Test
    void nonExistentMarkdown_returns404() throws Exception {
        mockMvc.perform(get(Routes.DOCS_FILE, "NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonExistentDrawio_returns404() throws Exception {
        mockMvc.perform(get(Routes.DOCS_DIAGRAM, "NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void pathTraversal_blocked() throws Exception {
        // El path traversal no debe escapar de docs/ — Spring rechaza con 400 o 404
        int status = mockMvc.perform(get("/docs/..%2F..%2Fetc%2Fpasswd"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(
                status == 400 || status == 404,
                "Path traversal debe ser bloqueado (400 o 404), fue: " + status);
    }

    // ========== Accesibilidad pública ==========

    @Test
    void documentosPage_accessibleWithoutAuth() throws Exception {
        mockMvc.perform(get(Routes.DOCUMENTOS))
                .andExpect(status().isOk());
    }

    @Test
    void diagramasPage_accessibleWithoutAuth() throws Exception {
        mockMvc.perform(get(Routes.DIAGRAMAS))
                .andExpect(status().isOk());
    }

    @Test
    void figurasHtml_accessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/docs/diagrams/figuras.html"))
                .andExpect(status().isOk());
    }
}
