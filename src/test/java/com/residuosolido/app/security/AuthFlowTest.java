package com.residuosolido.app.security;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("✅ Registro de usuario válido crea usuario ROLE_USER y redirige a login")
    void testSuccessfulUserRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "newuser@test.com")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login*"));

        User created = userRepository.findByUsername("newuser").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getRole()).isEqualTo(Role.USER);
        assertThat(passwordEncoder.matches("password123", created.getPassword())).isTrue();
    }

    @Test
    @DisplayName("✅ Registro de organización marca ROLE_ORGANIZATION")
    void testSuccessfulOrganizationRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "orguser")
                        .param("email", "orguser@test.com")
                        .param("firstName", "Org")
                        .param("lastName", "User")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("isOrganization", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login*"));

        User created = userRepository.findByUsername("orguser").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getRole()).isEqualTo(Role.ORGANIZATION);
    }

    @Test
    @DisplayName("❌ Registro inválido vuelve al formulario con error")
    void testInvalidRegistrationShowsError() throws Exception {
        // password vacía → debería fallar por validación de AuthService
        mockMvc.perform(post("/auth/register")
                        .param("username", "baduser")
                        .param("email", "baduser@test.com")
                        .param("firstName", "Bad")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("error"));

        assertThat(userRepository.findByUsername("baduser")).isEmpty();
    }

    @Test
    @DisplayName("✅ Login con usuario registrado funciona")
    void testLoginWithRegisteredUser() throws Exception {
        // Preparar usuario registrado manualmente
        User user = new User();
        user.setUsername("loginuser");
        user.setEmail("loginuser@test.com");
        user.setFirstName("Login");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        mockMvc.perform(formLogin("/auth/login")
                        .user("loginuser")
                        .password("password123"))
                .andExpect(authenticated().withUsername("loginuser"));
    }

    @Test
    @DisplayName("❌ Login con contraseña incorrecta falla")
    void testLoginWithWrongPassword() throws Exception {
        User user = new User();
        user.setUsername("wrongpass");
        user.setEmail("wrongpass@test.com");
        user.setFirstName("Wrong");
        user.setLastName("Pass");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        mockMvc.perform(formLogin("/auth/login")
                        .user("wrongpass")
                        .password("otherpass"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/auth/login?error"));
    }

    @Test
    @DisplayName("✅ Rutas de formulario de registro/login se sirven correctamente")
    void testLoginAndRegisterPagesAccessible() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }
}
