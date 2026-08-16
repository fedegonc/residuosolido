package com.residuosolido.app.e2e;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.BreadcrumbService;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestOrgService;
import com.residuosolido.app.service.RequestTransitionService;
import com.residuosolido.app.service.InformalCollectorService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.RequestUpdateService;
import com.residuosolido.app.service.RequestQueryService;
import com.residuosolido.app.service.UserService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests E2E (End-to-End) para validar los flujos implementados.
 * Usa MockMvc + MockBean para aislar la capa de presentación sin requerir MongoDB.
 * Cubre los 8 flujos principales del sistema.
 */
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class EndToEndFlowsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;
    @MockBean
    private RequestUpdateService requestUpdateService;
    @MockBean
    private RequestQueryService requestQueryService;
    @MockBean
    private UserService userService;
    @MockBean
    private RequestMetricsService requestMetricsService;
    @MockBean
    private CityOrgService cityOrgService;
    @MockBean
    private RequestOrgService requestOrgService;
    @MockBean
    private RequestTransitionService requestTransitionService;
    @MockBean
    private InformalCollectorService informalCollectorService;

    // ═══════════════════════════════════════════════════════
    // Flujo 6: Tracking de invitado (track.html)
    // ═══════════════════════════════════════════════════════

    @Test
    void flujo6_guestTracking_pageLoadsAndShowsForm() throws Exception {
        when(requestQueryService.getGuestRequestsByPhone(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rastrear"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/track"))
                .andExpect(model().attributeExists("phone", "requests", "searched"));
    }

    @Test
    void flujo6_guestTracking_searchByPhone_returnsResults() throws Exception {
        Request req = new Request();
        req.setId("abc123");
        req.setGuestName("Juan");
        req.setGuestPhone("+59899123456");
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.setCity(City.RIVERA);
        req.setMaterials(List.of(MaterialCategory.PLASTICO));

        when(requestQueryService.getGuestRequestsByPhone("+59899123456")).thenReturn(List.of(req));

        mockMvc.perform(post("/rastrear").with(csrf()).param("phone", "+59899123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/track"))
                .andExpect(model().attribute("searched", true))
                .andExpect(model().attribute("phone", "+59899123456"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 3: Creación de solicitud (request-form.html)
    // ═══════════════════════════════════════════════════════

    @Test
    void flujo3_requestForm_guestCanAccess() throws Exception {
        when(userService.isAnonymous(any())).thenReturn(true);
        when(cityOrgService.getAvailableCities()).thenReturn(List.of(City.RIVERA));
        when(cityOrgService.getOrganizationsByCity(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/solicitudes/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/request-form"))
                .andExpect(model().attribute("isGuest", true))
                .andExpect(model().attributeExists("cities", "materials", "timeSlots"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 7: Perfil de usuario (dashboard.html + profile.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void flujo7_userDashboard_loadsWithRecentRequests() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setUsername("vecino");
        user.setFirstName("Vecino");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(requestQueryService.getRecentRequestsByUser(user, 5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/usuarios/inicio"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/dashboard"))
                .andExpect(model().attributeExists("user", "recentRequests", "breadcrumbs"));
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void flujo7_userProfile_loadsWithStats() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setUsername("vecino");
        user.setFirstName("Vecino");
        user.setEmail("vecino@test.com");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(requestMetricsService.getUserDashboardStats(user))
                .thenReturn(Map.of("total", 5L, "pending", 2L, "inProgress", 1L, "completed", 2L));

        mockMvc.perform(get("/usuarios/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/profile"))
                .andExpect(model().attributeExists("user", "requestStats", "cities", "breadcrumbs"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 4: Gestión de solicitudes USER (requests.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void flujo4_userRequestsList_loadsSuccessfully() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setUsername("vecino");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(requestQueryService.getRequestsByUser(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/requests"))
                .andExpect(model().attributeExists("requests", "currentPage", "pageSize"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 8: Perfil de organización (org/profile.html + org/dashboard.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo8_orgDashboard_loadsWithStats() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setFirstName("Cooperativa");
        org.setProfileCompleted(true);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(requestMetricsService.getOrgDashboardData(org))
                .thenReturn(Map.of("pending", 3L, "inProgress", 1L, "completed", 10L));
        when(requestOrgService.getRecentPendingRequestsByOrganization(org, 5))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/dashboard"))
                .andExpect(model().attributeExists("pendingRequests", "inProgressRequests", "completedRequests", "pendingRequestsList", "breadcrumbs"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo8_orgProfile_loadsAndCanUpdate() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setFirstName("Cooperativa");
        org.setEmail("coop@test.com");
        org.setPhone("+59899123456");
        org.setCity(City.RIVERA);
        org.setProfileCompleted(true);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(userService.updateProfile(any(), any(), any(), any(), any())).thenReturn(org);

        mockMvc.perform(get("/acopio/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/profile"))
                .andExpect(model().attributeExists("organization", "cities"));

        // POST update
        mockMvc.perform(post("/acopio/perfil").with(csrf())
                        .param("email", "coop@test.com")
                        .param("firstName", "Cooperativa")
                        .param("phone", "+59899123456")
                        .param("city", "RIVERA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acopio/perfil"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 5: Gestión de solicitudes ORG (org/requests.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo5_orgRequestsList_loadsSuccessfully() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setProfileCompleted(true);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(requestOrgService.getOrgRequestsByStatusFilter(any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/acopio/requests"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attributeExists("requests", "viewType", "currentPage", "pageSize"))
                .andExpect(model().attribute("viewType", "list"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo5_orgRequestDetail_loadsSuccessfully() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setProfileCompleted(true);

        Request req = new Request();
        req.setId("req1");
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.setCity(City.RIVERA);
        req.setMaterials(List.of(MaterialCategory.PLASTICO));
        req.setGuestName("Juan");
        req.setGuestPhone("+59899123456");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(requestOrgService.getOwnedOrgRequest("req1", org)).thenReturn(req);

        mockMvc.perform(get("/acopio/requests/req1"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/requests"))
                .andExpect(model().attribute("viewType", "detail"))
                .andExpect(model().attributeExists("request", "timeSlots"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo5_orgAcceptRequest_redirectsOnSuccess() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setProfileCompleted(true);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);

        mockMvc.perform(post("/acopio/requests/req1/transition").with(csrf())
                        .param("action", "accept")
                        .param("confirmedSlot", "MANANA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acopio/requests"));
    }

    // ═══════════════════════════════════════════════════════
    // Flujo 9: Catadores (org/catadores.html)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo9_catadoresList_loadsSuccessfully() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setProfileCompleted(true);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);
        when(informalCollectorService.findByOrganization(org)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/acopio/catadores"))
                .andExpect(status().isOk())
                .andExpect(view().name("org/catadores"))
                .andExpect(model().attributeExists("catadores", "cities", "materialCategories"));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void flujo9_catadorSave_redirectsOnSuccess() throws Exception {
        User org = new User();
        org.setId("o1");
        org.setUsername("coop");
        org.setProfileCompleted(true);

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);

        mockMvc.perform(post("/acopio/catadores").with(csrf())
                        .param("name", "João")
                        .param("phone", "+59899123456")
                        .param("city", "RIVERA")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acopio/catadores"));
    }

    // ═══════════════════════════════════════════════════════
    // RN-10: Validación server-side (error redirect)
    // ═══════════════════════════════════════════════════════

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void rn10_createRequest_emptyAddress_redirectsWithError() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setUsername("vecino");

        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(user);
        when(userService.resolveUser(any())).thenReturn(user);
        when(requestService.createRequestWithImage(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("error.request.address_required"));

        mockMvc.perform(post("/solicitudes").with(csrf())
                        .param("city", "RIVERA")
                        .param("address", "")
                        .param("organizationId", "org1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/solicitudes/nueva"));
    }
}
