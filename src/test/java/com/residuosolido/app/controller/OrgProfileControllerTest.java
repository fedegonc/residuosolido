package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests del perfil de organización (GET/PUT /mi-organizacion):
 * ciudad y teléfono son obligatorios siempre — no solo en onboarding.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class OrgProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User org;

    @BeforeEach
    void setUp() {
        org = TestFixtures.organization("org-1", City.RIVERA, MaterialCategory.PLASTICO);
        org.setUsername("coop");
        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgProfile_returnsViewWithFormData() throws Exception {
        mockMvc.perform(get(Routes.ORG_PROFILE))
                .andExpect(status().isOk())
                .andExpect(view().name("org/profile"))
                .andExpect(model().attribute("organization", org))
                .andExpect(model().attributeExists("cities", "materials", "breadcrumbs"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void updateProfile_success_updatesAndRedirects() throws Exception {
        mockMvc.perform(put(Routes.ORG_PROFILE).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("phone", "+59899123456")
                        .param("materiales", "PLASTICO", "VIDRIO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_PROFILE));

        verify(userService).updateProfile(eq(org), isNull(), isNull(), eq("+59899123456"),
                eq(City.RIVERA), eq(java.util.List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO)));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void updateProfile_missingPhone_doesNotSave() throws Exception {
        mockMvc.perform(put(Routes.ORG_PROFILE).with(csrf())
                        .param("ciudad", "RIVERA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_PROFILE));

        verify(userService, never()).updateProfile(any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void updateProfile_missingCity_doesNotSave() throws Exception {
        mockMvc.perform(put(Routes.ORG_PROFILE).with(csrf())
                        .param("phone", "+59899123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_PROFILE));

        verify(userService, never()).updateProfile(any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void updateProfile_serviceKeyedError_redirectsWithFlash() throws Exception {
        doThrow(new ValidationException(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED))
                .when(userService).updateProfile(any(), any(), any(), anyString(), any(), anyList());

        mockMvc.perform(put(Routes.ORG_PROFILE).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("phone", "+59899123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_PROFILE));
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void orgProfile_citizenRole_forbidden() throws Exception {
        mockMvc.perform(get(Routes.ORG_PROFILE))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgProfile_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get(Routes.ORG_PROFILE))
                .andExpect(status().is3xxRedirection());
    }
}
