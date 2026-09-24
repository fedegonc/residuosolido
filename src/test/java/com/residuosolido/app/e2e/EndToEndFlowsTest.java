package com.residuosolido.app.e2e;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.RequestViewType;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests E2E (End-to-End) para validar los flujos implementados.
 * Usa MockMvc + MockBean para aislar la capa de presentación sin requerir MongoDB.
 * Cubre los 8 flujos principales del sistema.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class EndToEndFlowsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;
    @MockBean
    private UserService userService;
    @MockBean
    private RequestMetricsService requestMetricsService;
    @MockBean
    private CityOrgService cityOrgService;

    // ═══════════════════════════════════════════════════════
    // Flujo 6: Tracking de invitado (track.html)
    // ═══════════════════════════════════════════════════════

    @Test
    void flujo6_guestTracking_pageLoadsAndShowsForm() throws Exception {
        when(requestService.getGuestRequests(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(Routes.TRACK))
                .andExpect(status().isOk())
                .andExpect(view().name("users/track"))
                .andExpect(model().attributeExists("telefono", "codigo", "requests", "searched"));
    }

    @Test
    void flujo6_guestTracking_searchByPhoneAndCode_returnsResults() throws Exception {
        Request req = new Request();
        req.setId("abc123");
        req.setGuestContact("Juan", "+59899123456", "AB12CD34");
        req.restoreStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.updateDraft(City.RIVERA, "Calle 1", null, List.of(MaterialCategory.PLASTICO));

        when(requestService.getGuestRequests("+59899123456", "AB12CD34")).thenReturn(List.of(req));

        mockMvc.perform(get(Routes.TRACK)
                        .param("telefono", "+59899123456")
                        .param("codigo", "AB12CD34"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/track"))
                .andExpect(model().attribute("searched", true))
                .andExpect(model().attribute("telefono", "+59899123456"))
                .andExpect(model().attribute("codigo", "AB12CD34"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 3: Creación de solicitud (request-form.html)
    // ═══════════════════════════════════════════════════════

    @Test
    void flujo3_requestForm_guestCanAccess() throws Exception {
        when(userService.isAnonymous(any())).thenReturn(true);
        when(cityOrgService.getAvailableCities()).thenReturn(List.of(City.RIVERA));
        when(cityOrgService.getOrganizationsByCity(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(Routes.REQUESTS_NEW))
                .andExpect(status().isOk())
                .andExpect(view().name("users/request-form"))
                .andExpect(model().attribute("isGuest", true))
                .andExpect(model().attributeExists("cities", "materials", "timeSlots"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 7: Dashboard unificado (requests.html con stats)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void flujo7_userRequestsList_loadsWithStats() throws Exception {
        User user = TestFixtures.citizen("u1", "+59899123456");
        user.setUsername("vecino");
        user.setFirstName("Vecino");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(requestService.getRequestsByUser(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(requestMetricsService.getUserRequestStats(user))
                .thenReturn(Map.of("total", 0L, "pending", 0L, "inProgress", 0L, "completed", 0L));

        mockMvc.perform(get(Routes.REQUESTS))
                .andExpect(status().isOk())
                .andExpect(view().name("users/requests"))
                .andExpect(model().attributeExists("user", "requests", "requestStats"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 4: Gestión de solicitudes USER (requests.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void flujo4_userRequestsList_loadsSuccessfully() throws Exception {
        User user = TestFixtures.citizen("u1", "+59899123456");
        user.setUsername("vecino");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(requestService.getRequestsByUser(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(Routes.REQUESTS))
                .andExpect(status().isOk())
                .andExpect(view().name("users/requests"))
                .andExpect(model().attributeExists("requests", "currentPage", "pageSize"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 8: Panel y perfil de organización
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo8_orgPanel_loadsWithStats() throws Exception {
        User org = TestFixtures.organization("o1", City.RIVERA);
        org.setUsername("coop");
        org.setFirstName("Cooperativa");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(requestMetricsService.getOrgRequestStats(org))
                .thenReturn(Map.of("pending", 3L, "inProgress", 1L, "completed", 10L));
        when(requestService.getOrgRequestsByStatusFilter(any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attributeExists("pendingCount", "inProgressCount", "completedCount", "requests", "breadcrumbs"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo8_orgProfile_loadsAndCanUpdate() throws Exception {
        User org = TestFixtures.organization("o1", City.RIVERA);
        org.setUsername("coop");
        org.setFirstName("Cooperativa");
        org.setEmail("coop@test.com");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(userService.updateProfile(any(), any(), any(), any(), any(), any())).thenReturn(org);

        mockMvc.perform(get(Routes.ORG_PROFILE))
                .andExpect(status().isOk())
                .andExpect(view().name("org/profile"))
                .andExpect(model().attributeExists("organization", "cities"));

        // POST update
        mockMvc.perform(put(Routes.ORG_PROFILE).with(csrf())
                        .param("email", "coop@test.com")
                        .param("firstName", "Cooperativa")
                        .param("phone", "+59899123456")
                        .param("ciudad", "RIVERA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mi-organizacion"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 5: Gestión de solicitudes ORG (org/requests.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo5_orgRequestsList_loadsSuccessfully() throws Exception {
        User org = TestFixtures.organization("o1", City.RIVERA);
        org.setUsername("coop");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(requestService.getOrgRequestsByStatusFilter(any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attributeExists("requests", "viewType", "currentPage", "pageSize"))
                .andExpect(model().attribute("viewType", RequestViewType.LIST));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo5_orgRequestDetail_loadsSuccessfully() throws Exception {
        User org = TestFixtures.organization("o1", City.RIVERA);
        org.setUsername("coop");

        Request req = new Request();
        req.setId("req1");
        req.restoreStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.updateDraft(City.RIVERA, "Calle 1", null, List.of(MaterialCategory.PLASTICO));
        req.setGuestContact("Juan", "+59899123456", null);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(requestService.getOwnedOrgRequest("req1", org)).thenReturn(req);

        mockMvc.perform(get(Routes.ORG_REQUEST, "req1"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attribute("viewType", RequestViewType.DETAIL))
                .andExpect(model().attributeExists("request", "timeSlots"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo5_orgAcceptRequest_redirectsOnSuccess() throws Exception {
        User org = TestFixtures.organization("o1", City.RIVERA);
        org.setUsername("coop");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);

        mockMvc.perform(post(Routes.ORG_REQUEST_ACCEPT, "req1").with(csrf())
                        .param("confirmedSlot", "MANANA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acopio/solicitudes"));
    }

    // ═══════════════════════════════════════════════════════
    // RN-10: Validación server-side (error redirect)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void rn10_createRequest_emptyAddress_redirectsWithError() throws Exception {
        User user = TestFixtures.citizen("u1", "+59899123456");
        user.setUsername("vecino");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(userService.resolveUser(any())).thenReturn(user);
        when(requestService.createRequestWithImage(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("error.request.address_required"));

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "")
                        .param("organizationId", "org1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/solicitar"));
    }
}
