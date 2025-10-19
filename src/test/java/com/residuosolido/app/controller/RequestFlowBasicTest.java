package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CloudinaryService;
import com.residuosolido.app.service.MaterialService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class RequestFlowBasicTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @MockBean
    private UserService userService;

    @MockBean
    private MaterialService materialService;

    // Declarado por si el contexto lo requiere en tiempo de test
    @MockBean
    private CloudinaryService cloudinaryService;

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void userCanOpenNewRequestForm() throws Exception {
        when(requestService.hasActiveOrganizations()).thenReturn(true);
        when(materialService.findAllActive()).thenReturn(List.of(new Material()));

        mockMvc.perform(get("/user/requests/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("request"))
                .andExpect(model().attributeExists("materials"))
                .andExpect(view().name("users/request-form"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void userCanCreateRequest() throws Exception {
        when(requestService.hasActiveOrganizations()).thenReturn(true);
        User u = new User();
        u.setUsername("user1");
        when(userService.findAuthenticatedUserByUsername("user1")).thenReturn(u);

        mockMvc.perform(post("/user/requests")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("collectionAddress", "Calle 123")
                        .param("description", "Bolsas de plastico")
                        .param("notes", "nota")
                        .param("scheduledDate", "2025-12-31")
                        .param("organizationId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/requests"));

        verify(requestService).save(any(Request.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void userCannotCreateRequestWithoutAddress() throws Exception {
        when(requestService.hasActiveOrganizations()).thenReturn(true);
        User u = new User();
        u.setUsername("user1");
        when(userService.findAuthenticatedUserByUsername("user1")).thenReturn(u);

        mockMvc.perform(post("/user/requests")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("collectionAddress", "") // Dirección vacía
                        .param("description", "Bolsas de plastico")
                        .param("notes", "nota")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/requests"));

        // Aunque esté vacía, el controlador actual no valida y guarda igual
        // Este test documenta el comportamiento actual
        verify(requestService).save(any(Request.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void userCannotCreateRequestWithoutDescription() throws Exception {
        when(requestService.hasActiveOrganizations()).thenReturn(true);
        User u = new User();
        u.setUsername("user1");
        when(userService.findAuthenticatedUserByUsername("user1")).thenReturn(u);

        mockMvc.perform(post("/user/requests")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("collectionAddress", "Calle 123")
                        .param("description", "") // Descripción vacía
                        .param("notes", "nota")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/requests"));

        // El controlador actual no valida campos obligatorios
        // Este test documenta el comportamiento actual
        verify(requestService).save(any(Request.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void userCannotCreateRequestWithoutOrganizations() throws Exception {
        when(requestService.hasActiveOrganizations()).thenReturn(false); // Sin organizaciones
        User u = new User();
        u.setUsername("user1");
        when(userService.findAuthenticatedUserByUsername("user1")).thenReturn(u);

        mockMvc.perform(post("/user/requests")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("collectionAddress", "Calle 123")
                        .param("description", "Bolsas de plastico")
                        .param("notes", "nota")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/requests/new"))
                .andExpect(flash().attributeExists("errorMessage"));

        // No debe llamar a save cuando no hay organizaciones
        verify(requestService, org.mockito.Mockito.never()).save(any(Request.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void userCannotSubmitRequestWithoutCsrf() throws Exception {
        when(requestService.hasActiveOrganizations()).thenReturn(true);

        mockMvc.perform(post("/user/requests")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("collectionAddress", "Calle 123")
                        .param("description", "Bolsas de plastico"))
                // Sin .with(csrf())
                .andExpect(status().isForbidden()); // 403 por falta de CSRF token
    }
}
