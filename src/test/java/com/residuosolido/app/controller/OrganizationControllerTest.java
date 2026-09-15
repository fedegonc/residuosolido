package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.RequestQueryService;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RequestQueryService requestQueryService;

    @BeforeEach
    void setUp() {
        User mockOrg = new User();
        mockOrg.setId("org1");
        mockOrg.setUsername("coop");
        mockOrg.setRole(Role.ORGANIZATION);
        mockOrg.setProfileCompleted(true);
        mockOrg.setCity(City.RIVERA);
        mockOrg.setPhone("+59899123456");

        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(mockOrg);
        when(requestQueryService.getOrgRequestsByStatusFilter(any(User.class), any(), anyInt(), anyInt())).thenReturn(List.of());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_invalidStatusFilter_doesNotReturn500() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS).param("status", "INVALID_STATUS"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRequests_validStatusFilter_returnsOk() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS).param("status", "PENDING"))
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
        mockMvc.perform(get(Routes.ORG_REQUESTS).param("status", "'; DROP TABLE--"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgRequests() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isForbidden());
    }
}
