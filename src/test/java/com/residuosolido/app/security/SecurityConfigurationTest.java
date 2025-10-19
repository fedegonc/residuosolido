package com.residuosolido.app.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para verificar la configuración específica de SecurityConfig
 * 
 * Verifica que todas las rutas definidas en SecurityConfig funcionen
 * correctamente según la configuración de autorización
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== TESTS DE RUTAS PÚBLICAS ==========

    @Test
    @DisplayName("Rutas públicas principales accesibles sin autenticación")
    void testMainPublicRoutes() throws Exception {
        // Rutas principales
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/invitados"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rutas de autenticación públicas")
    void testAuthRoutes() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rutas de contenido público (posts, categorías)")
    void testContentRoutes() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rutas especiales públicas")
    void testSpecialPublicRoutes() throws Exception {
        mockMvc.perform(get("/sistema-visual"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/change-language"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/faq"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Recursos estáticos accesibles")
    void testStaticResources() throws Exception {
        // Estos pueden devolver 404 si no existen, pero no deben requerir autenticación
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isNotFound()); // 404 es OK, 401/403 no
                
        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isNotFound());
                
        mockMvc.perform(get("/images/logo.png"))
                .andExpect(status().isNotFound());
                
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound());
    }

    // ========== TESTS DE RUTAS PROTEGIDAS ==========

    @Test
    @DisplayName("API endpoints requieren autenticación")
    void testAPIEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
                
        mockMvc.perform(get("/api/requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("API endpoints accesibles para usuarios autenticados")
    void testAPIEndpointsAccessibleForAuthenticatedUsers() throws Exception {
        // Estos pueden devolver 404 si no existen, pero no deben requerir más autenticación
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isNotFound()); // 404 es OK para endpoints que no existen
    }

    // ========== TESTS DE AUTORIZACIÓN POR ROLES ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Rutas de admin accesibles solo para ADMIN")
    void testAdminRoutesForAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/admin/requests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Rutas de usuario accesibles solo para USER")
    void testUserRoutesForUser() throws Exception {
        mockMvc.perform(get("/users/dashboard"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/requests/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "org", roles = {"ORGANIZATION"})
    @DisplayName("Rutas de organización accesibles solo para ORGANIZATION")
    void testOrgRoutesForOrganization() throws Exception {
        mockMvc.perform(get("/org/dashboard"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/organization/profile"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isOk());
    }

    // ========== TESTS DE ACCESO CRUZADO NEGADO ==========

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER no puede acceder a rutas de ADMIN")
    void testUserCannotAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER no puede acceder a rutas de ORGANIZATION")
    void testUserCannotAccessOrgRoutes() throws Exception {
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/organization/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "org", roles = {"ORGANIZATION"})
    @DisplayName("ORGANIZATION no puede acceder a rutas de USER")
    void testOrgCannotAccessUserRoutes() throws Exception {
        mockMvc.perform(get("/users/dashboard"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/requests/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "org", roles = {"ORGANIZATION"})
    @DisplayName("ORGANIZATION no puede acceder a rutas de ADMIN")
    void testOrgCannotAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    // ========== TESTS DE RUTAS ESPECIALES ==========

    @Test
    @DisplayName("Feedback accesible públicamente (controlado en @PreAuthorize)")
    void testFeedbackPublicAccess() throws Exception {
        mockMvc.perform(get("/feedback"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/feedback/new"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Páginas de error accesibles públicamente")
    void testErrorPagesPublic() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk());
    }

    // ========== TESTS DE CABECERAS DE SEGURIDAD ==========

    @Test
    @DisplayName("Cabeceras de seguridad están configuradas")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("CSP permite recursos necesarios")
    void testCSPConfiguration() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", 
                    org.hamcrest.Matchers.containsString("script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com")))
                .andExpect(header().string("Content-Security-Policy", 
                    org.hamcrest.Matchers.containsString("style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net")));
    }
}
