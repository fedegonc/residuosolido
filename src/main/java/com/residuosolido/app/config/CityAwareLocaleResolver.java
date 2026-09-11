package com.residuosolido.app.config;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
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
 * LocaleResolver que setea el idioma según la ciudad del usuario logueado:
 * - RIVERA → español (es)
 * - LIVRAMENTO → portugués (pt)
 *
 * Prioridad:
 * 1. Si el usuario eligió explícitamente un idioma (sesión), respeta esa elección.
 * 2. Si el usuario está logueado, usa el idioma de su ciudad.
 * 3. Si es invitado, usa Accept-Language del navegador (con fallback a es).
 * 4. Fallback: español.
 *
 * Esto evita que el idioma cambie "de la nada" — solo cambia si el usuario
 * lo elige o si cambia su ciudad en el perfil.
 */
public class CityAwareLocaleResolver implements LocaleResolver {

    private static final Logger logger = LoggerFactory.getLogger(CityAwareLocaleResolver.class);
    private static final Locale DEFAULT_LOCALE = new Locale("es", "ES");
    private static final String SESSION_LOCALE_KEY = SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME;

    private final UserRepository userRepository;

    public CityAwareLocaleResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 1. Si ya hay un locale en sesión (elegido explícitamente), respetarlo
        Locale sessionLocale = (Locale) request.getSession().getAttribute(SESSION_LOCALE_KEY);
        if (sessionLocale != null) {
            return sessionLocale;
        }

        // 2. Si el usuario está logueado, usar el idioma de su ciudad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Locale cityLocale = resolveByUserCity(auth.getName());
            if (cityLocale != null) {
                // Persistir en sesión para no consultar la DB en cada request
                request.getSession().setAttribute(SESSION_LOCALE_KEY, cityLocale);
                return cityLocale;
            }
        }

        // 3. Invitado: usar Accept-Language del navegador
        Locale browserLocale = resolveFromBrowser(request);
        if (browserLocale != null) {
            return browserLocale;
        }

        // 4. Fallback
        return DEFAULT_LOCALE;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale == null) {
            request.getSession().removeAttribute(SESSION_LOCALE_KEY);
        } else {
            request.getSession().setAttribute(SESSION_LOCALE_KEY, locale);
        }
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

        // Parsear el primer idioma del header: "pt-BR,pt;q=0.9,en;q=0.8" → "pt"
        String primary = acceptLang.split("[,;]")[0].trim();
        String lang = primary.split("-")[0].toLowerCase();

        if ("pt".equals(lang)) {
            return new Locale("pt", "BR");
        }
        // Default a español para cualquier otro caso
        return DEFAULT_LOCALE;
    }
}
