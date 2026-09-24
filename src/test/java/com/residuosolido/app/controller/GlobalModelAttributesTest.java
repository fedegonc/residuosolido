package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.NotificationService;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
class GlobalModelAttributesTest {

    private UserService userService;
    private NotificationService notificationService;
    private GlobalModelAttributes attributes;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        notificationService = mock(NotificationService.class);
        attributes = new GlobalModelAttributes(userService, notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(username, null));
    }

    @Test
    void unreadNotifications_anonymous_returnsNullWithoutQueries() {
        when(userService.isAnonymous(any())).thenReturn(true);

        assertNull(attributes.unreadNotifications());
        verifyNoInteractions(notificationService);
    }

    @Test
    void unreadNotifications_userMissing_returnsNull() {
        authenticate("fantasma");
        when(userService.isAnonymous(any())).thenReturn(false);
        when(userService.findAuthenticatedUserByUsername("fantasma"))
                .thenThrow(new RuntimeException("usuario inexistente"));

        assertNull(attributes.unreadNotifications());
        verifyNoInteractions(notificationService);
    }

    @Test
    void unreadNotifications_orgRole_returnsNull() {
        authenticate("coop");
        User org = TestFixtures.organization("o1", null);
        when(userService.isAnonymous(any())).thenReturn(false);
        when(userService.findAuthenticatedUserByUsername("coop")).thenReturn(org);

        assertNull(attributes.unreadNotifications());
        verifyNoInteractions(notificationService);
    }

    @Test
    void unreadNotifications_citizen_returnsBadgeCount() {
        authenticate("vecino");
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        assertEquals(Role.USER, citizen.getRole());
        when(userService.isAnonymous(any())).thenReturn(false);
        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(citizen);
        when(notificationService.unreadCount(citizen)).thenReturn(2L);

        assertEquals(2L, attributes.unreadNotifications());
    }
}
