package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * LocaleResolver con soporte de cookie persistente. Prioridad:
 * 1. Sesión (elegida explícitamente en esta sesión)
 * 2. Cookie (persiste tras login/logout) ← 1 año
 * 3. Ciudad del usuario logueado (RIVERA→es, LIVRAMENTO→pt)
 * 4. Accept-Language del navegador
 * 5. Fallback: español
 */
public class CityAwareLocaleResolver implements LocaleResolver {

    private static final Logger logger = LoggerFactory.getLogger(CityAwareLocaleResolver.class);
    private static final Locale DEFAULT_LOCALE = new Locale("es", "ES");
    private static final String SESSION_LOCALE_KEY = SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME;
    private static final String COOKIE_LOCALE_KEY = "app_locale";
    private static final int COOKIE_MAX_AGE = 365 * 24 * 60 * 60; // 1 year in seconds

    private final UserRepository userRepository;

    public CityAwareLocaleResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 1. Sesión: elegida explícitamente en esta sesión
        Locale sessionLocale = (Locale) request.getSession().getAttribute(SESSION_LOCALE_KEY);
        if (sessionLocale != null) {
            return sessionLocale;
        }

        // 2. Cookie: persiste tras login/logout
        Locale cookieLocale = resolveFromCookie(request);
        if (cookieLocale != null) {
            return cookieLocale;
        }

        // 3. Usuario logueado: idioma de su ciudad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Locale cityLocale = resolveByUserCity(auth.getName());
            if (cityLocale != null) {
                request.getSession().setAttribute(SESSION_LOCALE_KEY, cityLocale);
                return cityLocale;
            }
        }

        // 4. No autenticado: Accept-Language
        Locale browserLocale = resolveFromBrowser(request);
        if (browserLocale != null) {
            return browserLocale;
        }

        // 5. Fallback
        return DEFAULT_LOCALE;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale == null) {
            request.getSession().removeAttribute(SESSION_LOCALE_KEY);
            removeCookie(response);
        } else {
            request.getSession().setAttribute(SESSION_LOCALE_KEY, locale);
            writeCookie(response, locale);
        }
    }

    private Locale resolveFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (COOKIE_LOCALE_KEY.equals(cookie.getName())) {
                String value = cookie.getValue();
                if ("es".equals(value)) return new Locale("es", "ES");
                if ("pt".equals(value)) return new Locale("pt", "BR");
            }
        }
        return null;
    }

    private void writeCookie(HttpServletResponse response, Locale locale) {
        String langValue = locale.getLanguage();
        Cookie cookie = new Cookie(COOKIE_LOCALE_KEY, langValue);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true en producción con HTTPS
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_LOCALE_KEY, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private Locale resolveByUserCity(String username) {
        try {
            return userRepository.findByUsername(username)
                    .map(User::getCity)
                    .map(this::cityToLocale)
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("No se pudo resolver la ciudad para el usuario '{}': {}", username, e.getMessage());
            return null;
        }
    }

    private Locale cityToLocale(City city) {
        if (city == null) return null;
        return switch (city) {
            case RIVERA -> new Locale("es", "ES");
            case LIVRAMENTO -> new Locale("pt", "BR");
        };
    }

    private Locale resolveFromBrowser(HttpServletRequest request) {
        String acceptLang = request.getHeader("Accept-Language");
        if (acceptLang == null || acceptLang.isBlank()) {
            return DEFAULT_LOCALE;
        }

        String primary = acceptLang.split("[,;]")[0].trim();
        String lang = primary.split("-")[0].toLowerCase();

        if ("pt".equals(lang)) {
            return new Locale("pt", "BR");
        }
        return DEFAULT_LOCALE;
    }
}
