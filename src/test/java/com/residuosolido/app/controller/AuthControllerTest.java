package com.residuosolido.app.controller;

import com.residuosolido.app.EmbeddedMongoTest;
import com.residuosolido.app.config.RateLimiter;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserRegistrationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests de autenticación: registro (rate limit, duplicados, validación)
 * y página de login con params ?error / ?blocked.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class AuthControllerTest extends EmbeddedMongoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService userRegistrationService;

    @MockBean
    private RateLimiter rateLimiter;

    @MockBean
    private AuthenticationManager authenticationManager;

    // ===== Registro =====

    @Test
    void registerGet_rendersFormWithEmptyForm() throws Exception {
        mockMvc.perform(get(Routes.REGISTER))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerPost_success_authenticatesAndRedirectsToRequests() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);
        User created = new User();
        created.setUsername("nuevo");
        when(userRegistrationService.registerCitizen(any(User.class))).thenReturn(created);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new TestingAuthenticationToken("nuevo", null, "ROLE_USER"));

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "nuevo")
                        .param("password", "1234")
                        .param("countryCode", "+598")
                        .param("phoneNational", "99123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS));

        verify(userRegistrationService).registerCitizen(any(User.class));
    }

    @Test
    void registerOrgPost_withTipo_passesToService() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);
        User created = new User();
        created.setUsername("coop");
        when(userRegistrationService.registerOrganization(any(User.class), eq(OrgType.COOPERATIVA))).thenReturn(created);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new TestingAuthenticationToken("coop", null, "ROLE_ORGANIZATION"));

        mockMvc.perform(post(Routes.REGISTER_ORG).with(csrf())
                        .param("username", "coop")
                        .param("password", "1234")
                        .param("countryCode", "+598")
                        .param("phoneNational", "99123456")
                        .param("tipo", "COOPERATIVA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.ORG_REQUESTS));

        verify(userRegistrationService).registerOrganization(any(User.class), eq(OrgType.COOPERATIVA));
    }

    @Test
    void registerOrgPost_missingTipo_rendersErrorAndSkipsService() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);

        mockMvc.perform(post(Routes.REGISTER_ORG).with(csrf())
                        .param("username", "coop")
                        .param("password", "1234")
                        .param("countryCode", "+598")
                        .param("phoneNational", "99123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-org"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(userRegistrationService, never()).registerOrganization(any(), any());
    }

    @Test
    void registerOrgGet_rendersOrgForm() throws Exception {
        mockMvc.perform(get(Routes.REGISTER_ORG))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-org"))
                .andExpect(model().attributeExists("user", "orgTypes"));
    }

    @Test
    void registerPost_rateLimited_rendersFormWithError() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(false);

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "nuevo")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(userRegistrationService, never()).registerCitizen(any());
        verify(userRegistrationService, never()).registerOrganization(any(), any());
    }

    @Test
    void registerPost_duplicateKey_rendersFormWithError() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);
        doThrow(new DuplicateKeyException("username"))
                .when(userRegistrationService).registerCitizen(any(User.class));

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "existente")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void registerPost_blankUsername_beanValidationBlocksBeforeService() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                // Texto real resuelto por JsonMessageSource, no solo "existe el atributo" —
                // si la resolución del código ServerMessage estuviera rota, esto mostraría
                // literalmente "error.register.username_required" en vez del texto.
                .andExpect(model().attribute("errorMessage", "Necesitamos tu nombre."));

        verify(userRegistrationService, never()).registerCitizen(any());
        verify(userRegistrationService, never()).registerOrganization(any(), any());
    }

    @Test
    void registerPost_invalidPin_beanValidationBlocksBeforeService() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "nuevo")
                        .param("password", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("errorMessage", "El PIN debe tener 4 dígitos."));

        verify(userRegistrationService, never()).registerCitizen(any());
        verify(userRegistrationService, never()).registerOrganization(any(), any());
    }

    @Test
    void registerPost_invalidPhone_marksFieldAndPreservesSafeValues() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);

        mockMvc.perform(post(Routes.REGISTER_ORG).with(csrf())
                        .param("username", "Federico Test")
                        .param("password", "1234")
                        .param("countryCode", "+598")
                        .param("phoneNational", "9922249555")
                        .param("tipo", "CENTRO_ACOPIO"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-org"))
                .andExpect(model().attributeHasFieldErrors("user", "phoneNational"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"Federico Test\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"9922249555\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("La cantidad de dígitos no coincide")));

        verify(userRegistrationService, never()).registerCitizen(any());
        verify(userRegistrationService, never()).registerOrganization(any(), any());
    }

    @Test
    void registerPost_validationError_rendersFormWithError() throws Exception {
        // username/password válidos de FORMA (pasan Bean Validation) — el error es del
        // service (ej. teléfono, que es compuesto y Bean Validation no cubre, ver
        // RegistrationForm). Antes usaba password="12" para forzar esto, pero eso ahora
        // lo intercepta el @Pattern del DTO antes de llegar al service (cubierto por
        // registerPost_invalidPin_beanValidationBlocksBeforeService).
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);
        doThrow(new IllegalArgumentException("error.register.phone_required"))
                .when(userRegistrationService).registerCitizen(any(User.class));

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "nuevo")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ===== Login =====

    @Test
    void login_plain_rendersWithoutError() throws Exception {
        mockMvc.perform(get(Routes.LOGIN))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void login_withErrorParam_showsError() throws Exception {
        mockMvc.perform(get(Routes.LOGIN).param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void login_withBlockedParam_showsBlockedMessage() throws Exception {
        mockMvc.perform(get(Routes.LOGIN).param("blocked", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
