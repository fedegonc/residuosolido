package com.residuosolido.app.security;

import com.residuosolido.app.service.RequestMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de seguridad para flujos nuevos: org, catadores y métricas.
 */
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class NewFlowsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestMetricsService requestMetricsService;

    @Test
    void orgProfile_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/acopio/perfil"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgProfile() throws Exception {
        mockMvc.perform(get("/acopio/perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgRequests_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/acopio/requests"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgRequests() throws Exception {
        mockMvc.perform(get("/acopio/requests"))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgCatadores_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/acopio/catadores"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgCatadores() throws Exception {
        mockMvc.perform(get("/acopio/catadores"))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricsPage_isPublic() throws Exception {
        when(requestMetricsService.getPublicMetricsByCity()).thenReturn(Collections.emptyMap());
        when(requestMetricsService.getPublicTotalCompleted()).thenReturn(0L);

        mockMvc.perform(get("/metricas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void metricsPage_isPublic_forAuthenticatedUser() throws Exception {
        when(requestMetricsService.getPublicMetricsByCity()).thenReturn(Collections.emptyMap());
        when(requestMetricsService.getPublicTotalCompleted()).thenReturn(0L);

        mockMvc.perform(get("/metricas"))
                .andExpect(status().isOk());
    }
}
