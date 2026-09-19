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

    // Auth
    public static final String LOGIN = "/entrar";
    public static final String LOGOUT = "/salir";
    public static final String REGISTER = "/registrarse";

    // Solicitudes
    public static final String REQUESTS_NEW = "/solicitar";
    public static final String REQUESTS = "/mis-solicitudes";
    public static final String REQUEST_EDIT = "/solicitudes/{id}/editar";
    public static final String REQUEST = "/solicitudes/{id}";
    public static final String TRACK = "/rastrear";

    // Organización
    public static final String ORG_PROFILE = "/mi-organizacion";
    public static final String ORG_REQUESTS = "/acopio/solicitudes";
    public static final String ORG_REQUEST = "/acopio/solicitudes/{id}";
    public static final String ORG_REQUEST_ACCEPT = "/acopio/solicitudes/{id}/aceptar";
    public static final String ORG_REQUEST_REJECT = "/acopio/solicitudes/{id}/rechazar";
    public static final String ORG_REQUEST_COMPLETE = "/acopio/solicitudes/{id}/completar";

    // API
    public static final String API_ORGANIZATIONS_BY_CITY = "/organizaciones";
    public static final String HTMX_ORG_OPTIONS = "/solicitudes/org-options";
    public static final String API_ANY = "/api/**";

    // Documentación y estáticos
    public static final String DOCS_ANY = "/docs/**";
    public static final String DOCS_FILE = "/docs/{file}.md";
    public static final String DOCS_DIAGRAM = "/docs/diagrams/{file}.drawio";
    public static final String DOCS_DIAGRAMS_VIEW = "/docs/diagramas";
    public static final String WELL_KNOWN = "/.well-known/**";
    public static final String ERROR = "/error";

    // Actuator y OpenAPI
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String SWAGGER_V3 = "/v3/api-docs/**";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_HTML = "/swagger-ui.html";

    /**
     * A qué pantalla "vuelve" cada rol tras login o tras un error — función pura,
     * sin estado, por eso vive acá como static en vez de ser un @Component inyectado
     * en 4 archivos distintos (LoginSuccessHandler, AuthNavigationInterceptor,
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
}
