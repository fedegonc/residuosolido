package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de regresión para DocsController:
 * - /docs/*.md se sirve con content-type text/markdown
 * - /docs/diagrams/*.drawio se sirve con content-type application/xml
 * - Archivos inexistentes devuelven 404
 * - Path traversal bloqueado
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class DocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void allMarkdownFiles_servedCorrectly() throws Exception {
        for (String doc : new String[]{"DEFENSA", "METODOLOGIA", "DIAGRAMAS", "ENDPOINTS", "MEJORAS"}) {
            mockMvc.perform(get("/docs/" + doc + ".md"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.TEXT_MARKDOWN));
        }
    }

    @Test
    void markdownView_rendersHtmlInsideLayout() throws Exception {
        mockMvc.perform(get(Routes.DOCS_VIEW, "REQUISITOS"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("doc-content")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<h")));
    }

    @Test
    void markdownView_nonExistent_returns404() throws Exception {
        mockMvc.perform(get(Routes.DOCS_VIEW, "NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void markdownView_doesNotBreakOnDiagramsRoute() throws Exception {
        // /docs/diagramas es una ruta literal, no un {file}: no debe caer en viewMarkdown.
        mockMvc.perform(get(Routes.DOCS_DIAGRAMS_VIEW))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("mxgraph")));
    }

    @Test
    void drawioFile_servedWithXmlContentType() throws Exception {
        mockMvc.perform(get(Routes.DOCS_DIAGRAM, "figura4-secuencia"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_XML));
    }

    @Test
    void allDrawioFiles_servedCorrectly() throws Exception {
        for (String fig : new String[]{"figura1-casos-uso", "figura2-modelo-logico",
                "figura3-clases", "figura4-secuencia", "figura4-estados",
                "figura5-gitflow", "figura6-notificaciones"}) {
            mockMvc.perform(get("/docs/diagrams/" + fig + ".drawio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_XML));
        }
    }

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
        int status = mockMvc.perform(get("/docs/..%2F..%2Fetc%2Fpasswd"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(
                status == 400 || status == 404,
                "Path traversal debe ser bloqueado (400 o 404), fue: " + status);
    }

    /**
     * Regresión del drift de #204: el hub mostraba conteos hardcodeados que
     * quedaban desactualizados. Este test no fija un número exacto (cambiaría
     * con cada mejora nueva y rompería el test sin motivo) — solo verifica
     * que el conteo viene de parsear MEJORAS.md de verdad: si hoy hay
     * "N implementadas", tiene que ser un número positivo real, no "0" (que
     * es lo que devolvería si el parseo se rompiera o el archivo no se
     * encontrara).
     */
    @Test
    void hub_mejorasStatsComeFromRealFile() throws Exception {
        mockMvc.perform(get(Routes.DOCS_HUB))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("mejorasStats"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("0 implementadas"))));
    }

    @Test
    void designSystemPage_rendersOk() throws Exception {
        mockMvc.perform(get(Routes.DOCS_DESIGN_SYSTEM))
                .andExpect(status().isOk());
    }

    @Test
    void uxUiPage_rendersOk() throws Exception {
        mockMvc.perform(get(Routes.DOCS_UX_UI))
                .andExpect(status().isOk());
    }
}
