package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.exception.OwnershipException;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.RequestMetricsService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests del detalle de solicitud del ciudadano (GET /solicitudes/{id}):
 * lectura en cualquier estado, misma regla de ownership que editar/borrar.
 * Casos borde validados primero en scratch/sim (detail.*).
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RequestService requestService;

    @MockBean
    private RequestMetricsService requestMetricsService;

    private User citizen;

    @BeforeEach
    void setUp() {
        citizen = TestFixtures.citizen("user-1", "+59899123456");
        citizen.setUsername("vecino");
        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(citizen);
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void requestDetail_owner_returnsView() throws Exception {
        Request request = Request.forCitizen(citizen);
        request.setId("req-1");
        when(requestService.getOwnedRequest(eq("req-1"), any(User.class))).thenReturn(request);

        mockMvc.perform(get("/solicitudes/req-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void requestDetail_notFound_redirectsWithFlash() throws Exception {
        when(requestService.getOwnedRequest(eq("no-existe"), any(User.class)))
                .thenThrow(new ValidationException(ServerMessage.FLASH_REQUEST_NOT_FOUND));

        mockMvc.perform(get("/solicitudes/no-existe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS));
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void requestDetail_notOwned_redirectsWithFlash() throws Exception {
        when(requestService.getOwnedRequest(eq("req-ajena"), any(User.class)))
                .thenThrow(new OwnershipException(ServerMessage.FLASH_REQUEST_NOT_OWNED));

        mockMvc.perform(get("/solicitudes/req-ajena"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Routes.REQUESTS));
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void requestDetail_orgRole_forbidden() throws Exception {
        mockMvc.perform(get("/solicitudes/req-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestDetail_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/solicitudes/req-1"))
                .andExpect(status().is3xxRedirection());
    }
}
