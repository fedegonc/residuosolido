package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class CurrentUserArgumentResolverTest {

    private UserService userService;
    private CurrentUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        resolver = new CurrentUserArgumentResolver(userService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unused")
    private void annotated(@CurrentUser User user) {}

    @SuppressWarnings("unused")
    private void notAnnotated(User user) {}

    @SuppressWarnings("unused")
    private void wrongType(@CurrentUser String notAUser) {}

    private MethodParameter param(String methodName) {
        try {
            Method m = getClass().getDeclaredMethod(methodName,
                    methodName.equals("wrongType") ? String.class : User.class);
            return new MethodParameter(m, 0);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void supportsParameter_currentUserAnnotatedUser_returnsTrue() throws Exception {
        assertTrue(resolver.supportsParameter(param("annotated")));
    }

    @Test
    void supportsParameter_noAnnotation_returnsFalse() throws Exception {
        assertFalse(resolver.supportsParameter(param("notAnnotated")));
    }

    @Test
    void supportsParameter_wrongType_returnsFalse() throws Exception {
        assertFalse(resolver.supportsParameter(param("wrongType")));
    }

    @Test
    void resolveArgument_noAuthentication_returnsNull() {
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getUserPrincipal()).thenReturn(null);

        assertNull(resolver.resolveArgument(param("annotated"), null, request, null));
        verify(userService, never()).findAuthenticatedUserByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void resolveArgument_anonymousToken_returnsNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getUserPrincipal()).thenReturn(null);

        assertNull(resolver.resolveArgument(param("annotated"), null, request, null));
    }

    @Test
    void resolveArgument_authenticated_resolvesUserFromContext() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("vecino", null));
        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(citizen);
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getUserPrincipal()).thenReturn(null);

        assertSame(citizen, resolver.resolveArgument(param("annotated"), null, request, null));
    }

    @Test
    void resolveArgument_principalFromRequest_takesPrecedenceOverContext() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        TestingAuthenticationToken principal = new TestingAuthenticationToken("vecino", null);
        when(userService.findAuthenticatedUserByUsername("vecino")).thenReturn(citizen);
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);

        assertSame(citizen, resolver.resolveArgument(param("annotated"), null, request, null));
    }

}
