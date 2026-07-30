package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;
import java.util.Set;

@Component
public class AuthNavigationInterceptor implements HandlerInterceptor {

    private static final Set<String> GUEST_ONLY_PATHS = Set.of(
            "/auth/login", "/auth/register", "/", "/index"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return true;
        }

        String path = request.getRequestURI();
        if (!GUEST_ONLY_PATHS.contains(path)) {
            return true;
        }

        String targetUrl = resolveTargetUrl(auth.getAuthorities());
        response.sendRedirect(targetUrl);
        return false;
    }

    private String resolveTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        boolean isOrg = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZATION"));
        if (isOrg) {
            return "/acopio/inicio";
        }
        return "/usuarios/inicio";
    }
}
