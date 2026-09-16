package com.residuosolido.app.config;

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

    // Auth
    public static final String LOGIN = "/auth/login";
    public static final String LOGOUT = "/logout";
    public static final String REGISTER = "/auth/register";

    // Solicitudes
    public static final String REQUESTS_NEW = "/solicitudes/nueva";
    public static final String REQUESTS = "/solicitudes";
    public static final String REQUEST = "/solicitud/{id}";
    public static final String REQUEST_EDIT = "/solicitud/{id}/editar";
    public static final String REQUEST_DELETE = "/solicitud/{id}/eliminar";
    public static final String TRACK = "/rastrear";

    // Usuarios
    public static final String USER_HOME = "/usuarios/inicio";

    // Organización
    public static final String ORG_HOME = "/acopio/inicio";
    public static final String ORG_PROFILE = "/acopio/perfil";
    public static final String ORG_COMPLETE_PROFILE = "/acopio/completar-perfil";
    public static final String ORG_REQUESTS = "/acopio/requests";
    public static final String ORG_REQUEST = "/acopio/requests/{id}";
    public static final String ORG_REQUEST_TRANSITION = "/acopio/requests/{id}/transition";

    // API
    public static final String API_ORGANIZATIONS_BY_CITY = "/api/organizations/by-city";
    public static final String API_ANY = "/api/**";

    // Documentación y estáticos
    public static final String DOCS_ANY = "/docs/**";
    public static final String DOCS_FILE = "/docs/{file}.md";
    public static final String DOCS_DIAGRAM = "/docs/diagrams/{file}.drawio";
    public static final String WELL_KNOWN = "/.well-known/**";
    public static final String ERROR = "/error";

    // Actuator y OpenAPI
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String SWAGGER_V3 = "/v3/api-docs/**";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_HTML = "/swagger-ui.html";
}
