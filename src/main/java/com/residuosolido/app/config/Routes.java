package com.residuosolido.app.config;

import com.residuosolido.app.enums.Role;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Fuente única de verdad para las rutas de la aplicación.
 *
 * Centraliza los strings que antes estaban dispersos en controllers,
 * tests, seguridad y configuración. Un cambio de URL pasa por aquí.
 *
 * Nota: las rutas reflejan el sistema actual. La normalización a REST
 * puro (sin verbos en URL, verbos HTTP correctos) es una mejora futura.
 */
public final class Routes {

    private Routes() {}

    // Páginas públicas
    public static final String HOME = "/";
    public static final String INDEX = "/index";
    public static final String LANGUAGE = "/change-language";
    public static final String SEED = "/seed";
    /** Página de contenido genérica por slug (ver PageController). Agregar una página nueva = 1 entrada en su registro, sin tocar rutas. */
    public static final String PAGE_BY_SLUG = "/pagina/{slug}";

    public static String pageUrl(String slug) {
        return "/pagina/" + slug;
    }

    // Auth
    public static final String LOGIN = "/entrar";
    public static final String LOGOUT = "/salir";
    public static final String REGISTER = "/registrarse";

    /** Rutas donde un usuario ya autenticado no debería estar (login/registro/home) — ver AuthNavigationInterceptor. */
    public static final java.util.Set<String> GUEST_ONLY_PATHS = java.util.Set.of(HOME, INDEX, LOGIN, REGISTER);

    // Solicitudes
    public static final String REQUESTS_NEW = "/solicitar";
    public static final String REQUESTS = "/mis-solicitudes";
    public static final String REQUEST_EDIT = "/solicitudes/{id}/editar";
    public static final String REQUEST = "/solicitudes/{id}";
    public static final String TRACK = "/rastrear";
    /** Bandeja in-app del ciudadano (notificaciones de aceptada/rechazada). */
    public static final String NOTIFICATIONS = "/notificaciones";

    // Organización
    public static final String ORG_PROFILE = "/mi-organizacion";
    public static final String ORG_REQUESTS = "/acopio/solicitudes";
    public static final String ORG_REQUEST = "/acopio/solicitudes/{id}";
    public static final String ORG_REQUEST_ACCEPT = "/acopio/solicitudes/{id}/aceptar";
    public static final String ORG_REQUEST_REJECT = "/acopio/solicitudes/{id}/rechazar";
    public static final String ORG_REQUEST_COMPLETE = "/acopio/solicitudes/{id}/completar";

    // API
    public static final String API_ORGANIZATIONS_BY_CITY = "/organizaciones";
    public static final String ORG_OPTIONS = "/solicitudes/org-options";
    public static final String API_ANY = "/api/**";

    // Documentación y estáticos
    public static final String DOCS_ANY = "/docs/**";
    public static final String DOCS_FILE = "/docs/{file}.md";
    public static final String DOCS_VIEW = "/docs/{file}";
    public static final String DOCS_DIAGRAM = "/docs/diagrams/{file}.drawio";
    public static final String DOCS_DIAGRAMS_VIEW = "/docs/diagramas";
    public static final String DOCS_HUB = "/docs/hub";
    /** Sandbox de dominio (gitignored): existe en local/dev, 404 en prod porque nunca se sube al repo. */
    public static final String SCRATCH_ANY = "/scratch/**";
    public static final String SCRATCH_FILE = "/scratch/{file}.java";
    public static final String WELL_KNOWN = "/.well-known/**";
    public static final String ERROR = "/error";

    // Actuator y OpenAPI
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_INFO = "/actuator/info";
    public static final String SWAGGER_V3 = "/v3/api-docs/**";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_HTML = "/swagger-ui.html";

    /**
     * A qué pantalla "vuelve" cada rol tras login o tras un error — función pura,
     * sin estado, por eso vive acá como static en vez de ser un @Component inyectado
     * en 4 archivos distintos (AuthenticationEventHandler, AuthNavigationInterceptor,
     * GlobalErrorController, GlobalExceptionHandler).
     */
    public static String resolveHomeForRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return HOME;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String name = authority.getAuthority();
            if (name.equals("ROLE_" + Role.ORGANIZATION.name())) return ORG_REQUESTS;
            if (name.equals("ROLE_" + Role.USER.name())) return REQUESTS;
        }
        return HOME;
    }

    /**
     * A dónde navegar tras una excepción: el home del rol (o /entrar para
     * anónimos). Si el destino es la misma URI que lanzó la excepción,
     * redirigir crearía un loop infinito (el flash sobrevive y relanza el
     * error en cada render) — en ese caso devuelve la vista de error.
     * Función pura: (auth, uri) → view name.
     */
    public static String resolveErrorNavigation(Authentication auth, String currentUri) {
        String home = resolveHomeForRole(auth);
        String target = home.equals(HOME) ? LOGIN : home;
        return target.equals(currentUri) ? "error/404" : "redirect:" + target;
    }
}
