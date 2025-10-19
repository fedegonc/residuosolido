package com.residuosolido.app.security;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests críticos para el flujo de onboarding de organizaciones
 * 
 * Cubre el problema identificado en las memorias sobre persistencia
 * de profileCompleted y redirecciones correctas
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OrganizationOnboardingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User incompleteOrganization;
    private User completeOrganization;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Organización con perfil incompleto
        incompleteOrganization = createOrganization("org_incomplete", "incomplete@test.com", null);
        
        // Organización con perfil completo
        completeOrganization = createOrganization("org_complete", "complete@test.com", true);
    }

    // ========== TESTS DE REDIRECCIÓN TRAS LOGIN ==========

    @Test
    @DisplayName("Organización con perfil incompleto es redirigida a completar perfil")
    void testIncompleteOrganizationRedirectedToCompleteProfile() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
                .user("org_incomplete")
                .password("password123"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/acopio/completar-perfil"));
    }

    @Test
    @DisplayName("Organización con perfil completo va al dashboard")
    void testCompleteOrganizationRedirectedToDashboard() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
                .user("org_complete")
                .password("password123"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/acopio/inicio"));
    }

    // ========== TESTS DE ACCESO A RUTAS DE ONBOARDING ==========

    @Test
    @DisplayName("Organización incompleta puede acceder a completar perfil")
    void testIncompleteOrgCanAccessCompleteProfile() throws Exception {
        // Simular organización con perfil incompleto
        mockMvc.perform(get("/acopio/completar-perfil")
                .with(request -> {
                    request.setUserPrincipal(() -> "org_incomplete");
                    return request;
                }))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Organización completa NO puede acceder a completar perfil")
    void testCompleteOrgCannotAccessCompleteProfile() throws Exception {
        // Una organización que ya completó su perfil no debería poder volver a la página
        // Esto debería redirigir al dashboard
        mockMvc.perform(get("/acopio/completar-perfil")
                .with(request -> {
                    request.setUserPrincipal(() -> "org_complete");
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acopio/inicio"));
    }

    // ========== TESTS DE PERSISTENCIA DE PROFILE COMPLETED ==========

    @Test
    @DisplayName("Completar perfil persiste profileCompleted = true")
    void testCompleteProfilePersistsProfileCompleted() throws Exception {
        // Verificar estado inicial
        User org = userRepository.findByUsername("org_incomplete").orElseThrow();
        assertNull(org.getProfileCompleted());

        // Completar perfil
        mockMvc.perform(post("/acopio/completar-perfil").with(csrf())
                .param("organizationName", "Test Organization")
                .param("address", "Test Address 123")
                .param("phone", "123456789")
                .param("description", "Test description")
                .param("latitude", "-30.93801")
                .param("longitude", "-55.53412")
                .with(request -> {
                    request.setUserPrincipal(() -> "org_incomplete");
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acopio/inicio"));

        // Verificar que profileCompleted se persistió
        User updatedOrg = userRepository.findByUsername("org_incomplete").orElseThrow();
        assertTrue(updatedOrg.getProfileCompleted());
    }

    // ========== TESTS DE AUTORIZACIÓN EN RUTAS DE ORGANIZACIÓN ==========

    @Test
    @WithMockUser(username = "org_complete", roles = {"ORGANIZATION"})
    @DisplayName("Organización completa puede acceder a todas sus rutas")
    void testCompleteOrgCanAccessAllRoutes() throws Exception {
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/acopio/perfil"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/acopio/solicitudes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Usuario regular NO puede acceder a rutas de organización")
    void testUserCannotAccessOrgRoutes() throws Exception {
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/acopio/perfil"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/acopio/completar-perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin NO puede acceder a rutas específicas de organización")
    void testAdminCannotAccessOrgSpecificRoutes() throws Exception {
        // Admin puede gestionar organizaciones desde /admin/organizations
        // pero no puede acceder a las rutas específicas de organizaciones
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/acopio/completar-perfil"))
                .andExpect(status().isForbidden());
    }

    // ========== TESTS DE VALIDACIÓN DE DATOS ==========

    @Test
    @DisplayName("Completar perfil requiere todos los campos obligatorios")
    void testCompleteProfileRequiresAllFields() throws Exception {
        // Intentar completar perfil sin todos los campos
        mockMvc.perform(post("/acopio/completar-perfil").with(csrf())
                .param("organizationName", "") // Campo vacío
                .param("address", "Test Address")
                .with(request -> {
                    request.setUserPrincipal(() -> "org_incomplete");
                    return request;
                }))
                .andExpect(status().isOk()) // Vuelve al formulario con errores
                .andExpect(model().hasErrors());
    }

    // ========== MÉTODOS AUXILIARES ==========

    private User createOrganization(String username, String email, Boolean profileCompleted) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.ORGANIZATION);
        user.setActive(true);
        user.setFirstName("Test");
        user.setLastName("Organization");
        user.setProfileCompleted(profileCompleted);
        
        return userRepository.save(user);
    }
}
