package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.model.Notification;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.NotificationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de la bandeja in-app (GET /notificaciones): solo ROLE_USER,
 * abrir la página marca todo como leído.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false"
})
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    private User citizen;

    @BeforeEach
    void setUp() {
        citizen = TestFixtures.citizen("user-1", "+59899123456");
        citizen.setUsername("vecino");
        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(citizen);
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void notifications_user_returnsViewAndMarksRead() throws Exception {
        Notification unread = new Notification(citizen, "req-1", NotificationType.ACCEPTED, null);
        unread.setId("n-1");
        when(notificationService.listFor(any(User.class))).thenReturn(List.of(unread));

        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().isOk());

        verify(notificationService).markRead(anyList());
    }

    @Test
    @WithMockUser(username = "vecino", roles = "USER")
    void notifications_emptyInbox_returnsView() throws Exception {
        when(notificationService.listFor(any(User.class))).thenReturn(List.of());

        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coop", roles = "ORGANIZATION")
    void notifications_orgRole_forbidden() throws Exception {
        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().isForbidden());
    }

    @Test
    void notifications_anonymous_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/notificaciones"))
                .andExpect(status().is3xxRedirection());
    }
}
