package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.RequestViewType;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests del panel de la organización: lista con filtro, detalle y
 * transiciones (aceptar/rechazar/completar). Solo ROLE_ORGANIZATION.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class OrgRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RequestService requestService;

    @MockBean
    private RequestMetricsService requestMetricsService;

    private User org;

    @BeforeEach
    void setUp() {
        org = TestFixtures.organization("org-1", City.RIVERA);
        org.setUsername("coop");
        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
    }

    // ===== Lista =====

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_completeProfile_returnsListView() throws Exception {
        when(requestMetricsService.getOrgRequestStats(any(User.class)))
                .thenReturn(Map.of("pending", 2L, "inProgress", 1L, "completed", 0L));
        when(requestService.getOrgRequestsByStatusFilter(any(User.class), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attribute("viewType", RequestViewType.LIST))
                .andExpect(model().attribute("pendingCount", 2L))
                .andExpect(model().attributeExists("requests", "cards", "breadcrumbs"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_withStatusFilter_passesEstadoToService() throws Exception {
        when(requestMetricsService.getOrgRequestStats(any(User.class)))
                .thenReturn(Map.of("pending", 0L, "inProgress", 0L, "completed", 0L));
        when(requestService.getOrgRequestsByStatusFilter(any(User.class), eq("PENDING"), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get(Routes.ORG_REQUESTS).param("estado", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentStatus", "PENDING"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_incompleteProfile_redirectsToOrgProfile() throws Exception {
        org.setProfileCompleted(false);

        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_PROFILE));
    }

    // ===== Detalle =====

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequestDetail_returnsDetailView() throws Exception {
        Request request = Request.forGuest("Ana", "+59899123456", "cod");
        request.setId("req-1");
        when(requestService.getOwnedOrgRequest("req-1", org)).thenReturn(request);

        mockMvc.perform(get(Routes.ORG_REQUEST, "req-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attribute("viewType", RequestViewType.DETAIL))
                .andExpect(model().attributeExists("request", "timeSlots"));
    }

    // ===== Transiciones =====

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void acceptRequest_success_redirectsToList() throws Exception {
        mockMvc.perform(post(Routes.ORG_REQUEST_ACCEPT, "req-1").with(csrf())
                        .param("confirmedSlot", "MANANA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_REQUESTS));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void acceptRequest_illegalState_redirectsWithError() throws Exception {
        doThrow(new IllegalStateException("error.request.not_pending"))
                .when(requestService).acceptRequest(anyString(), any(User.class), any());

        mockMvc.perform(post(Routes.ORG_REQUEST_ACCEPT, "req-1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_REQUESTS));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void rejectRequest_success_redirectsToList() throws Exception {
        mockMvc.perform(post(Routes.ORG_REQUEST_REJECT, "req-1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_REQUESTS));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void completeRequest_success_redirectsToList() throws Exception {
        mockMvc.perform(post(Routes.ORG_REQUEST_COMPLETE, "req-1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_REQUESTS));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void transition_notOwned_redirectsWithFlash() throws Exception {
        doThrow(new SecurityException("not owned"))
                .when(requestService).rejectRequest(anyString(), any(User.class));

        mockMvc.perform(post(Routes.ORG_REQUEST_REJECT, "req-ajena").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_REQUESTS));
    }

    // ===== Seguridad =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void orgRequests_citizenRole_forbidden() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgRequests_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().is3xxRedirection());
    }
}
