package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato de boundary HTTP: las entidades de dominio ({@code model/}) nunca
 * cruzan la frontera HTTP — ni como body de entrada (@RequestBody/@ModelAttribute:
 * abriría mass-assignment sobre campos que el formulario no declara) ni como
 * serialización de salida (@ResponseBody/ResponseEntity: expondría campos
 * internos como password hashes a la respuesta).
 *
 * Estado actual verificado (fase 2): los forms bindean @RequestParam escalares
 * o RegistrationForm (el único DTO necesario), los templates leen la entidad
 * pero nunca la bindean (name= manual, sin th:object), y los ResponseEntity
 * existentes devuelven String/files.
 *
 * Invariante adicional: {@code User} NO implementa {@link UserDetails}. El
 * principal de seguridad es {@code org.springframework.security.core.userdetails.User},
 * un objeto lean construido en SecurityBeansConfig — eso ES el "DTO del boundary
 * de auth". Hacer que la entidad implemente UserDetails metería la entidad
 * completa (hash de password + campos de negocio) en el SecurityContext de cada
 * sesión y reintroduciría el acoplamiento framework→dominio que la fase 3.3
 * eliminó de los setters. Si esta regla falla, es una decisión deliberada:
 * borrarla exige actualizar este javadoc con el motivo.
 */
class HttpBoundaryContractTest {

    private static final Path CONTROLLER_DIR = Path.of("src/main/java/com/residuosolido/app/controller");

    private static final Set<String> ENTITIES = Set.of(
            "User", "Request", "Organization", "Notification", "MonthlyReport");

    private static final String ENTITY_CLASS = "(?:User|Request|Organization|Notification|MonthlyReport)";

    private static final Pattern BOUND_INPUT = Pattern.compile(
            "@(?:RequestBody|ModelAttribute)[^;{]*\\b" + ENTITY_CLASS + "\\b");

    private static final Pattern SERIALIZED_OUTPUT = Pattern.compile(
            "(?:ResponseEntity\\s*<[^>]*\\b" + ENTITY_CLASS + "\\b|@ResponseBody[^;{]*\\b" + ENTITY_CLASS + "\\b)");

    @Test
    void entitiesNeverCrossHttpBoundary() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(CONTROLLER_DIR)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String src = Files.readString(file);
                String name = file.getFileName().toString();
                if (BOUND_INPUT.matcher(src).find()) {
                    violations.add(name + ": entidad bindeada por @RequestBody/@ModelAttribute (mass-assignment)");
                }
                if (SERIALIZED_OUTPUT.matcher(src).find()) {
                    violations.add(name + ": entidad serializada en la respuesta HTTP");
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Las entidades de dominio no deben cruzar el boundary HTTP (usar DTO/scalars):\n"
                        + String.join("\n", violations));
    }

    @Test
    void userDoesNotImplementUserDetails() {
        assertFalse(UserDetails.class.isAssignableFrom(User.class),
                "User no debe implementar UserDetails: el principal lean de SecurityBeansConfig "
                        + "es el boundary-DTO de auth; la entidad completa (con hash) no pertenece "
                        + "al SecurityContext de cada sesión.");
    }
}
