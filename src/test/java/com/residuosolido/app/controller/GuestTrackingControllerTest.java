package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.RequestService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests del rastreo de invitados (GET /rastrear, público):
 * la búsqueda solo se ejecuta con teléfono Y código — el teléfono
 * solo no alcanza porque cualquiera podría conocerlo.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class GuestTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @Test
    void track_noParams_rendersFormWithoutSearching() throws Exception {
        mockMvc.perform(get(Routes.TRACK))
                .andExpect(status().isOk())
                .andExpect(view().name("users/track"))
                .andExpect(model().attribute("searched", false));

        verify(requestService, never()).getGuestRequests(anyString(), anyString());
    }

    @Test
    void track_onlyPhone_doesNotSearch() throws Exception {
        mockMvc.perform(get(Routes.TRACK).param("telefono", "+59899123456"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searched", false))
                .andExpect(model().attribute("telefono", "+59899123456"));

        verify(requestService, never()).getGuestRequests(anyString(), anyString());
    }

    @Test
    void track_blankCode_doesNotSearch() throws Exception {
        mockMvc.perform(get(Routes.TRACK)
                        .param("telefono", "+59899123456")
                        .param("codigo", "   "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searched", false));

        verify(requestService, never()).getGuestRequests(anyString(), anyString());
    }

    @Test
    void track_phoneAndCode_searchesAndReturnsResults() throws Exception {
        Request request = Request.forGuest("Ana", "+59899123456", "cod123");
        when(requestService.getGuestRequests("+59899123456", "cod123"))
                .thenReturn(List.of(request));

        mockMvc.perform(get(Routes.TRACK)
                        .param("telefono", "+59899123456")
                        .param("codigo", "cod123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searched", true))
                .andExpect(model().attribute("requests", List.of(request)));
    }
}
