package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthNavigationInterceptor implements HandlerInterceptor {

    private static final java.util.Set<String> GUEST_ONLY_PATHS = java.util.Set.of(
            "/auth/login", "/auth/register", "/", "/index"
    );

    private final RoleBasedLoginTargetUrlResolver targetUrlResolver;

    public AuthNavigationInterceptor(RoleBasedLoginTargetUrlResolver targetUrlResolver) {
        this.targetUrlResolver = targetUrlResolver;
    }

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

        String targetUrl = targetUrlResolver.resolveTargetUrl(auth.getAuthorities());
        response.sendRedirect(targetUrl);
        return false;
    }
}
