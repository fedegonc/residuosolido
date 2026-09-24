package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.config.RateLimiter;
import com.residuosolido.app.config.Routes;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests del flujo de creación de solicitudes (GET/POST /solicitar):
 * invitado con rate limit + redirect a rastreo, usuario registrado a
 * /mis-solicitudes, y las tres ramas de error.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class RequestCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @MockBean
    private CityOrgService cityOrgService;

    @MockBean
    private RateLimiter rateLimiter;

    @MockBean
    private UserService userService;

    private User citizen;

    @BeforeEach
    void setUp() {
        citizen = TestFixtures.citizen("u1", "+59899123456");
        citizen.setUsername("vecino");
        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(citizen);
        when(cityOrgService.getAvailableCities()).thenReturn(List.of(City.RIVERA, City.LIVRAMENTO));
    }

    // ===== GET /solicitar =====

    @Test
    void newRequestForm_anonymous_rendersAsGuest() throws Exception {
        when(userService.resolveUser(any())).thenReturn(null);

        mockMvc.perform(get(Routes.REQUESTS_NEW))
                .andExpect(status().isOk())
                .andExpect(view().name("users/request-form"))
                .andExpect(model().attribute("isGuest", true))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("request", "cities", "materials", "timeSlots"));
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void newRequestForm_loggedUser_rendersAsRegistered() throws Exception {
        when(userService.resolveUser(any())).thenReturn(citizen);

        mockMvc.perform(get(Routes.REQUESTS_NEW))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isGuest", false))
                .andExpect(model().attribute("needsPhone", false));
    }

    @Test
    void newRequestForm_withCity_loadsOrganizations() throws Exception {
        when(userService.resolveUser(any())).thenReturn(null);
        User org = TestFixtures.organization("o1", City.RIVERA);
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of(org));

        mockMvc.perform(get(Routes.REQUESTS_NEW).param("ciudad", "RIVERA"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedCity", City.RIVERA))
                .andExpect(model().attributeExists("organizations"));
    }

    @Test
    void orgOptionsForCity_returnsOptionsFragment() throws Exception {
        when(cityOrgService.getOrganizationsByCity(City.LIVRAMENTO)).thenReturn(List.of());

        mockMvc.perform(get(Routes.ORG_OPTIONS).param("ciudad", "LIVRAMENTO"))
                .andExpect(status().isOk());
    }

    // ===== POST /solicitar — invitado =====

    @Test
    void createRequest_guestRateLimited_redirectsWithError() throws Exception {
        when(userService.resolveUser(any())).thenReturn(null);
        when(rateLimiter.isAllowed(any())).thenReturn(false);

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS_NEW + "?error"));

        verify(requestService, never()).createRequestWithImage(any(), any(), anyString(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void createRequest_guestSuccess_redirectsToTrackingWithEncodedPhone() throws Exception {
        when(userService.resolveUser(any())).thenReturn(null);
        when(rateLimiter.isAllowed(any())).thenReturn(true);
        Request created = Request.forGuest("Ana", "+59899123456", "cod123");
        when(requestService.createRequestWithImage(isNull(), eq(City.RIVERA), eq("Calle 1"), any(),
                any(), eq("Ana"), eq("+59899123456"), any(), any())).thenReturn(created);

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1")
                        .param("guestName", "Ana")
                        .param("guestPhone", "+59899123456"))
                .andExpect(status().is3xxRedirection())
                // el "+" va URL-encoded: sin codificar se lee como espacio y rompe el rastreo
                .andExpect(redirectedUrlPattern("/rastrear?telefono=%2B59899123456&codigo=*"));
    }

    // ===== POST /solicitar — usuario =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void createRequest_userSuccess_redirectsToMyRequests() throws Exception {
        when(userService.resolveUser(any())).thenReturn(citizen);
        Request created = Request.forCitizen(citizen);
        when(requestService.createRequestWithImage(any(User.class), eq(City.RIVERA), eq("Calle 1"), any(),
                any(), any(), any(), any(), any())).thenReturn(created);

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS));
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void createRequest_userWithoutPhone_updatesProfileFirst() throws Exception {
        citizen.setPhone(null);
        when(userService.resolveUser(any())).thenReturn(citizen);
        Request created = Request.forCitizen(citizen);
        when(requestService.createRequestWithImage(any(), any(), anyString(), any(),
                any(), any(), any(), any(), any())).thenReturn(created);

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1")
                        .param("userCountryCode", "+598")
                        .param("userPhoneNational", "99123456"))
                .andExpect(status().is3xxRedirection());

        verify(userService).updateProfile(eq(citizen), isNull(), isNull(), eq("+59899123456"), isNull());
    }

    // ===== POST /solicitar — errores =====

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void createRequest_validationError_redirectsToForm() throws Exception {
        when(userService.resolveUser(any())).thenReturn(citizen);
        when(requestService.createRequestWithImage(any(), any(), anyString(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("error.request.address_required"));

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS_NEW));
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void createRequest_illegalState_loggedUser_redirectsToMyRequests() throws Exception {
        when(userService.resolveUser(any())).thenReturn(citizen);
        when(requestService.createRequestWithImage(any(), any(), anyString(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("error.request.not_pending"));

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS));
    }

    @Test
    void createRequest_illegalState_guest_redirectsToFormNotLogin() throws Exception {
        // Un invitado en /mis-solicitudes rebotaría a login por Security: va al form.
        when(userService.resolveUser(any())).thenReturn(null);
        when(rateLimiter.isAllowed(any())).thenReturn(true);
        when(requestService.createRequestWithImage(any(), any(), anyString(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("error.request.invalid"));

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS_NEW));
    }

    @Test
    void createRequest_unexpectedError_redirectsToFormWithGenericError() throws Exception {
        when(userService.resolveUser(any())).thenReturn(null);
        when(rateLimiter.isAllowed(any())).thenReturn(true);
        when(requestService.createRequestWithImage(any(), any(), anyString(), any(),
                any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post(Routes.REQUESTS_NEW).with(csrf())
                        .param("ciudad", "RIVERA")
                        .param("address", "Calle 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS_NEW));
    }
}
