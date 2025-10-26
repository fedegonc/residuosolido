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
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests críticos de autenticación y autorización
 * 
 * Cubre los principales factores de riesgo de seguridad:
 * - Login con credenciales válidas/inválidas
 * - Autorización por roles (ADMIN, USER, ORGANIZATION)
 * - Acceso no autorizado
 * - Gestión de sesiones
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthenticationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        userRepository.deleteAll();
        
        // Crear usuarios de prueba con nombres únicos para evitar conflictos
        createTestUser("testadmin", "testadmin@test.com", Role.ADMIN, true);
        createTestUser("testuser", "testuser@test.com", Role.USER, true);
        createTestUser("testorg", "testorg@test.com", Role.ORGANIZATION, true);
    }

    // ========== TESTS DE AUTENTICACIÓN BÁSICA ==========

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void testLoginWithValidCredentials() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
                .user("testadmin")
                .password("password123"))
                .andExpect(authenticated().withUsername("testadmin"))
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @DisplayName("Login fallido con credenciales inválidas")
    void testLoginWithInvalidCredentials() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
                .user("testadmin")
                .password("wrongpassword"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/auth/login?error"));
    }

    @Test
    @DisplayName("Login fallido con usuario inexistente")
    void testLoginWithNonExistentUser() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
                .user("nonexistent")
                .password("password123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/auth/login?error"));
    }

    @Test
    @DisplayName("Login fallido con usuario inactivo")
    void testLoginWithInactiveUser() throws Exception {
        // Crear usuario inactivo
        createTestUser("testinactive", "testinactive@test.com", Role.USER, false);
        
        mockMvc.perform(formLogin("/auth/login")
                .user("testinactive")
                .password("password123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/auth/login?error"));
    }

    // ========== TESTS DE AUTORIZACIÓN POR ROLES ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin puede acceder a rutas administrativas")
    void testAdminCanAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/admin/requests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Usuario regular puede acceder a sus rutas")
    void testUserCanAccessUserRoutes() throws Exception {
        mockMvc.perform(get("/users/dashboard"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/requests/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "org", roles = {"ORGANIZATION"})
    @DisplayName("Organización puede acceder a sus rutas")
    void testOrganizationCanAccessOrgRoutes() throws Exception {
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/acopio/perfil"))
                .andExpect(status().isOk());
    }

    // ========== TESTS DE ACCESO NO AUTORIZADO ==========

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Usuario regular NO puede acceder a rutas de admin")
    void testUserCannotAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "org", roles = {"ORGANIZATION"})
    @DisplayName("Organización NO puede acceder a rutas de usuario")
    void testOrganizationCannotAccessUserRoutes() throws Exception {
        mockMvc.perform(get("/users/dashboard"))
                .andExpect(status().isForbidden());
                
        mockMvc.perform(get("/requests/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario no autenticado es redirigido al login")
    void testUnauthenticatedUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
                
        mockMvc.perform(get("/users/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
                
        mockMvc.perform(get("/acopio/inicio"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    // ========== TESTS DE RUTAS PÚBLICAS ==========

    @Test
    @DisplayName("Rutas públicas accesibles sin autenticación")
    void testPublicRoutesAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk());
    }

    // ========== TESTS DE GESTIÓN DE SESIONES ==========

    @Test
    @DisplayName("Logout invalida la sesión correctamente")
    void testLogoutInvalidatesSession() throws Exception {
        // Primero hacer login
        mockMvc.perform(formLogin("/auth/login")
                .user("admin")
                .password("password123"))
                .andExpect(authenticated());
                
        // Luego hacer logout
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("CSRF protection está activo")
    void testCSRFProtectionActive() throws Exception {
        // Intentar POST sin token CSRF debe fallar
        mockMvc.perform(post("/auth/login")
                .param("username", "admin")
                .param("password", "password123"))
                .andExpect(status().isForbidden());
    }

    // ========== MÉTODOS AUXILIARES ==========

    private User createTestUser(String username, String email, Role role, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setActive(active);
        user.setFirstName("Test");
        user.setLastName("User");
        
        if (role == Role.ORGANIZATION) {
            user.setProfileCompleted(true);
        }
        
        return userRepository.save(user);
    }
}
