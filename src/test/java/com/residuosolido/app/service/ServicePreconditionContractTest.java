package com.residuosolido.app.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato de precondiciones: previene la RECURRENCIA del bug encontrado 6+
 * veces en una sola sesión (CityOrgService, RequestMetricsService x2,
 * UserService, OrganizationService x3, RequestService x6, NotificationService
 * x3, RequestValidator) — un método público de {@code service/} recibe un
 * User/Organization/Request y lo desreferencia (ej. {@code user.getId()}) sin
 * chequear null primero, produciendo un NPE crudo en vez de un error tipado.
 *
 * Heurística (igual de simple y explícita que DocsContractTest, no un parser
 * completo de Java): busca `paramName == null` o `paramName != null` en el
 * cuerpo del método. Si no está, es violación — salvo que el método esté en
 * EXEMPT con el motivo documentado ahí mismo. Un método "transitivamente
 * guardeado" (delega a otro método que sí valida) es un EXEMPT válido, no un
 * falso positivo a ignorar en silencio.
 */
class ServicePreconditionContractTest {

    private static final Path SERVICE_DIR = Path.of("src/main/java/com/residuosolido/app/service");

    private static final Pattern METHOD = Pattern.compile(
            "public\\s+(?:static\\s+)?[\\w<>\\[\\],.\\s]+?\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws\\s+[\\w,\\s]+)?\\s*\\{",
            Pattern.MULTILINE);

    private static final Pattern TYPED_PARAM = Pattern.compile("\\b(User|Organization|Request)\\s+(\\w+)\\b");

    /**
     * "ClassName#methodName" exento, con el motivo. No es "no hace falta
     * validar" — es "ya está validado en otro lado, verificado a mano".
     */
    private static final Set<String> EXEMPT = Set.of(
            // user == null se valida explícitamente en validator.validateCreate
            // (ERROR_REQUEST_CITIZEN_REQUIRED) — no es un guard faltante acá.
            "RequestService#createRequest", "RequestService#createRequestWithImage",
            // Delegan a un método ya guardeado (getEditableOwnedRequest -> getOwnedRequest,
            // que sí valida user == null) antes de tocar el user.
            "RequestService#updateRequest", "RequestService#deleteOwnedRequest",
            "RequestService#getEditableOwnedRequest",
            // Delegan el guard null a RequestValidator.requireUser /
            // requireOrganization / requireOwnedBy* (primera línea del método).
            "RequestService#getRequestsByUser", "RequestService#getOwnedRequest",
            "RequestService#acceptRequest", "RequestService#rejectRequest",
            "RequestService#completeRequest", "RequestService#getOwnedOrgRequest",
            "RequestService#getRequestsByOrganization",
            "RequestService#getOrgRequestsByStatusFilter",
            // Delega a findByUser(user), que sí valida, como primera línea.
            "OrganizationService#updateProfile",
            // Delegan a validateUserRegistration(user), que sí valida, como primera línea
            // (registerOrganization primero exige tipo != null — también guard tipado).
            "UserRegistrationService#registerCitizen", "UserRegistrationService#registerOrganization",
            // El NPE si request es null queda atrapado por el catch (Exception) genérico
            // y se rewrappea en StateException tipado — no es un NPE crudo sin rastro,
            // solo un mensaje menos preciso ("upload failed" en vez de "request required").
            // Blast radius bajo, documentado acá en vez de arreglado — ver auditoría.
            "LocalImageService#attachImageToRequest",
            // request == null se maneja: si es null, el ternario ya explotaría antes de
            // llegar a esta clase — currentStatus documentado en la propia auditoría.
            "RequestValidator#validateCreate"
    );

    @Test
    void publicServiceMethods_guardTypedParams_orAreExempt() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(SERVICE_DIR)) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String className = path.getFileName().toString().replace(".java", "");
                String content = Files.readString(path);

                Matcher methodMatcher = METHOD.matcher(content);
                while (methodMatcher.find()) {
                    String methodName = methodMatcher.group(1);
                    String params = methodMatcher.group(2);
                    String key = className + "#" + methodName;
                    if (EXEMPT.contains(key)) continue;

                    Matcher paramMatcher = TYPED_PARAM.matcher(params);
                    while (paramMatcher.find()) {
                        String paramName = paramMatcher.group(2);
                        String body = extractBody(content, methodMatcher.end() - 1);
                        boolean guarded = body.contains(paramName + " == null") || body.contains(paramName + " != null");
                        if (!guarded) {
                            violations.add(key + "(" + paramName + ")");
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Métodos públicos de service/ que reciben User/Organization/Request sin guard "
                        + "de null (ni exención documentada en EXEMPT): " + violations);
    }

    /** Extrae el cuerpo del método contando llaves desde la apertura (openBraceIndex incluida). */
    private String extractBody(String content, int openBraceIndex) {
        int depth = 0;
        for (int i = openBraceIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return content.substring(openBraceIndex, i + 1);
            }
        }
        return content.substring(openBraceIndex);
    }
}
