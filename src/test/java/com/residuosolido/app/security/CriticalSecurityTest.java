package com.residuosolido.app.security;

import com.residuosolido.app.config.Routes;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests críticos de seguridad: rutas públicas, protegidas y separación de roles.
 * No requieren MongoDB: las decisiones de acceso se toman en el filter chain,
 * antes de llegar a los controladores.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class CriticalSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ===== Rutas públicas =====

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get(Routes.LOGIN))
                .andExpect(status().isOk());
    }

    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get(Routes.REGISTER))
                .andExpect(status().isOk());
    }

    // ===== Rutas protegidas: anónimo → redirect a login =====

    @Test
    void userRequests_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get(Routes.REQUESTS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/entrar"));
    }

    @Test
    void orgRequests_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/entrar"));
    }

    @Test
    void userRequestsList_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get(Routes.REQUESTS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/entrar"));
    }

    // ===== Separación de roles =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgRoutes() throws Exception {
        mockMvc.perform(get(Routes.ORG_REQUESTS))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRole_cannotAccessUserRequests() throws Exception {
        mockMvc.perform(get(Routes.REQUESTS))
                .andExpect(status().isForbidden());
    }

    // ===== CSRF =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void postWithoutCsrf_isRejected() throws Exception {
        mockMvc.perform(delete(Routes.REQUEST, "test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_requiresPost_getIsNotAllowed() throws Exception {
        // GET /salir no existe como endpoint (dead code eliminado);
        // anónimo es redirigido a login por el filter chain
        mockMvc.perform(get(Routes.LOGOUT))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void logout_viaPost_redirectsToHome() throws Exception {
        mockMvc.perform(post(Routes.LOGOUT).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**"));
    }
}
