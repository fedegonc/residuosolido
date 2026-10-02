package com.residuosolido.app.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * LocaleResolver simplificado con cookie persistente. Prioridad:
 * 1. Cookie (persiste tras login/logout) ← 1 año
 * 2. Accept-Language del navegador
 * 3. Fallback: español
 *
 * Eliminadas sesión (duplica cookie) y ciudad (menos relevante para idioma real).
 */
public class CityAwareLocaleResolver implements LocaleResolver {

    private static final Locale DEFAULT_LOCALE = new Locale("es", "ES");
    private static final String COOKIE_LOCALE_KEY = "app_locale";
    private static final int COOKIE_MAX_AGE = 365 * 24 * 60 * 60; // 1 year in seconds

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 1. Cookie: persiste tras login/logout
        Locale cookieLocale = resolveFromCookie(request);
        if (cookieLocale != null) {
            return cookieLocale;
        }

        // 2. Accept-Language del navegador
        Locale browserLocale = resolveFromBrowser(request);
        if (browserLocale != null) {
            return browserLocale;
        }

        // 3. Fallback
        return DEFAULT_LOCALE;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale == null) {
            removeCookie(response);
        } else {
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
