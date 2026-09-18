package com.residuosolido.app.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
class RoutesTest {

    @Test
    void organization_redirectsToAcopioInicio() {
        Authentication auth = new UsernamePasswordAuthenticationToken("org", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION")));
        assertEquals("/acopio/inicio", Routes.resolveHomeForRole(auth));
    }

    @Test
    void user_redirectsToUsuariosInicio() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals("/usuarios/inicio", Routes.resolveHomeForRole(auth));
    }

    @Test
    void unknownRole_redirectsToDefault() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_UNKNOWN")));
        assertEquals("/", Routes.resolveHomeForRole(auth));
    }

    @Test
    void emptyAuthorities_redirectsToDefault() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass", List.of());
        assertEquals("/", Routes.resolveHomeForRole(auth));
    }

    @Test
    void anonymous_redirectsToDefault() {
        Authentication auth = new AnonymousAuthenticationToken("key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        assertEquals("/", Routes.resolveHomeForRole(auth));
    }

    @Test
    void nullAuthentication_redirectsToDefault() {
        assertEquals("/", Routes.resolveHomeForRole(null));
    }
}
