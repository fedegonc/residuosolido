package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de regresión para OrganizationController:
 * - Safe parse de status filter (no 500 con valor inválido)
 * - Rutas protegidas por rol ORGANIZATION
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RequestService requestService;

    @MockBean
    private RequestMetricsService requestMetricsService;

    @BeforeEach
    void setUp() {
        User mockOrg = TestFixtures.organization("org1", City.RIVERA);
        mockOrg.setUsername("coop");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(mockOrg);
        when(requestService.getOrgRequestsByStatusFilter(any(User.class), any(), anyInt(), anyInt())).thenReturn(List.of());
        when(requestMetricsService.getOrgRequestStats(any(User.class)))
                .thenReturn(java.util.Map.of("pending", 0L, "inProgress", 0L, "completed", 0L));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_invalidStatusFilter_doesNotReturn500() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS).param("estado", "INVALID_STATUS"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_validStatusFilter_returnsOk() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS).param("estado", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_noFilter_returnsOk() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_garbageString_doesNotReturn500() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS).param("estado", "'; DROP TABLE--"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgRequests() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isForbidden());
    }
}
