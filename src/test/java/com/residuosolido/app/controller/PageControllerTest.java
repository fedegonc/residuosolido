package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests de las páginas públicas: landing y páginas de contenido por slug
 * (un slug sin entrada en pages-{lang}.json → 404 real).
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void root_rendersLandingWithCards() throws Exception {
        mockMvc.perform(get(Routes.HOME))
                .andExpect(status().isOk())
                .andExpect(view().name("public/index"))
                .andExpect(model().attributeExists("cards"));
    }

    @Test
    void index_rendersSameLanding() throws Exception {
        mockMvc.perform(get(Routes.INDEX))
                .andExpect(status().isOk())
                .andExpect(view().name("public/index"));
    }

    @Test
    void showPage_knownSlug_rendersContentPage() throws Exception {
        mockMvc.perform(get("/pagina/catadores"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/page-content"))
                .andExpect(model().attributeExists("page"));
    }

    @Test
    void showPage_unknownSlug_returns404() throws Exception {
        mockMvc.perform(get("/pagina/no-existe-esta-pagina"))
                .andExpect(status().isNotFound());
    }
}
