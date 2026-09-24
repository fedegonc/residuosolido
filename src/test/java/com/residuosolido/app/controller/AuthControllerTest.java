package com.residuosolido.app.controller;

import com.residuosolido.app.config.RateLimiter;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserRegistrationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService userRegistrationService;

    @MockBean
    private RateLimiter rateLimiter;

    // ===== Registro =====

    @Test
    void registerGet_rendersFormWithEmptyForm() throws Exception {
        mockMvc.perform(get(Routes.REGISTER))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerPost_success_redirectsToLogin() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "nuevo")
                        .param("password", "1234")
                        .param("countryCode", "+598")
                        .param("phoneNational", "99123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/entrar"));

        verify(userRegistrationService).registerUser(any(User.class), eq(false));
    }

    @Test
    void registerPost_asOrganization_passesFlagToService() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "coop")
                        .param("password", "1234")
                        .param("isOrganization", "true"))
                .andExpect(status().is3xxRedirection());

        verify(userRegistrationService).registerUser(any(User.class), eq(true));
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

        verify(userRegistrationService, never()).registerUser(any(), anyBoolean());
    }

    @Test
    void registerPost_duplicateKey_rendersFormWithError() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);
        doThrow(new DuplicateKeyException("username"))
                .when(userRegistrationService).registerUser(any(User.class), anyBoolean());

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "existente")
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void registerPost_validationError_rendersFormWithError() throws Exception {
        when(rateLimiter.isAllowed(any(), eq("registration"))).thenReturn(true);
        doThrow(new IllegalArgumentException("error.register.pin_invalid"))
                .when(userRegistrationService).registerUser(any(User.class), anyBoolean());

        mockMvc.perform(post(Routes.REGISTER).with(csrf())
                        .param("username", "nuevo")
                        .param("password", "12"))
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
