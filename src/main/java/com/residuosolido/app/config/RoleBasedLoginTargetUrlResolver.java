package com.residuosolido.app.config;

import com.residuosolido.app.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Strategy para resolver la URL de destino post-login según el rol.
 * Para agregar un rol nuevo: agregar una entrada al mapa, sin tocar el handler.
 */
@Component
public class RoleBasedLoginTargetUrlResolver {

    private static final String DEFAULT_TARGET = "/";
    private static final String ROLE_PREFIX = "ROLE_";

    private final Map<Role, String> roleToUrl = new EnumMap<>(Role.class);

    public RoleBasedLoginTargetUrlResolver() {
        roleToUrl.put(Role.ORGANIZATION, "/acopio/inicio");
        roleToUrl.put(Role.USER, "/usuarios/inicio");
    }

    public String resolveTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .map(this::toRole)
                .filter(roleToUrl::containsKey)
                .findFirst()
                .map(roleToUrl::get)
                .orElse(DEFAULT_TARGET);
    }

    private Role toRole(String name) {
        try {
            return Role.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
