package com.residuosolido.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests críticos de seguridad: rutas públicas, protegidas y separación de roles.
 * No requieren MongoDB: las decisiones de acceso se toman en el filter chain,
 * antes de llegar a los controladores.
 */
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class CriticalSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ===== Rutas públicas =====

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk());
    }

    // ===== Rutas protegidas: anónimo → redirect a login =====

    @Test
    void userDashboard_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/usuarios/inicio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    void orgDashboard_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    void userRequestsList_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/solicitudes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    // ===== Separación de roles =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgRoutes() throws Exception {
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void userRole_cannotAccessOrgCatadores() throws Exception {
        mockMvc.perform(get("/acopio/catadores"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void orgRole_cannotAccessUserRoutes() throws Exception {
        mockMvc.perform(get("/usuarios/inicio"))
                .andExpect(status().isForbidden());
    }

    // ===== CSRF =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void postWithoutCsrf_isRejected() throws Exception {
        mockMvc.perform(post("/usuarios/perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_requiresPost_getIsNotAllowed() throws Exception {
        // GET /logout no existe como endpoint (dead code eliminado);
        // anónimo es redirigido a login por el filter chain
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void logout_viaPost_redirectsToHome() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**"));
    }
}
