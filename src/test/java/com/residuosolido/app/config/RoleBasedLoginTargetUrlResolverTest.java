package com.residuosolido.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleBasedLoginTargetUrlResolverTest {

    private final RoleBasedLoginTargetUrlResolver resolver = new RoleBasedLoginTargetUrlResolver();

    @Test
    void organization_redirectsToAcopioInicio() {
        String url = resolver.resolveTargetUrl(List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION")));
        assertEquals("/acopio/inicio", url);
    }

    @Test
    void user_redirectsToUsuariosInicio() {
        String url = resolver.resolveTargetUrl(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals("/usuarios/inicio", url);
    }

    @Test
    void unknownRole_redirectsToDefault() {
        String url = resolver.resolveTargetUrl(List.of(new SimpleGrantedAuthority("ROLE_UNKNOWN")));
        assertEquals("/", url);
    }

    @Test
    void emptyAuthorities_redirectsToDefault() {
        String url = resolver.resolveTargetUrl(List.of());
        assertEquals("/", url);
    }
}
