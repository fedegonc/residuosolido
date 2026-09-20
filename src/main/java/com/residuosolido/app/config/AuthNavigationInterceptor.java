package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthNavigationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return true;
        }

        String path = request.getRequestURI();
        if (!Routes.GUEST_ONLY_PATHS.contains(path)) {
            return true;
        }

        String targetUrl = Routes.resolveHomeForRole(auth);
        response.sendRedirect(targetUrl);
        return false;
    }
}
