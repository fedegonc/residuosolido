package com.residuosolido.app.security;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.service.RequestMetricsService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de seguridad para flujos de organización.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
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
        mockMvc.perform(get(Routes.ORG_PROFILE))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgProfile() throws Exception {
        mockMvc.perform(get(Routes.ORG_PROFILE))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgRequests_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgRequests() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isForbidden());
    }
}
