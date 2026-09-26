package com.residuosolido.app.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato de documentación: el drift docs↔código se convierte en build rojo.
 * Mismo patrón que los contract tests de i18n — los docs envejecen igual que
 * las claves, y la convención "actualizar el doc" falla en silencio (bug real:
 * 3 services inexistentes descritos como vigentes en ARQUITECTURA/CONTEXTO).
 *
 * Chequea:
 *  1. IDs de filas de MEJORAS.md únicos (hubo duplicados #144 y #150 reales).
 *  2. Todo docs/**​/*.md (y CLAUDE.md/README.md) listado en INDICE.md.
 *  3. Nombre de clase backtickeado en un doc → existe en src/scratch, es
 *     framework (whitelist), es término de dominio (whitelist), o el contexto
 *     lo marca como histórico ("eliminado", "fusionado en", "no existe"...) —
 *     mencionar una clase muerta solo vale si decís que está muerta.
 *  4. Paths backtickeados → existen en disco (hubiera atrapado
 *     `htmx-states.css` inexistente). Se resuelven también bajo
 *     src/main/resources/ porque los docs usan `static/`/`templates/` como
 *     shorthand. Paths marcados como eliminados en la misma línea se permiten.
 */
class DocsContractTest {

    private static final Path DOCS = Path.of("docs");
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final List<Path> ROOT_DOCS = List.of(Path.of("CLAUDE.md"), Path.of("README.md"));
    private static final Path INDICE = Path.of("docs/INDICE.md");

    private static final Pattern ROW_ID = Pattern.compile("^\\|\\s*(\\d+)\\s*\\|");
    private static final Pattern BACKTICK_CLASS = Pattern.compile("`([A-Z][A-Za-z0-9]+)`");
    private static final Pattern BACKTICK_PATH = Pattern.compile("`((?:docs|src|scratch|static|templates)/[^`\\s]+)`");
    private static final Pattern TYPE_DECL = Pattern.compile("(?:class|enum|interface|record)\\s+([A-Z][A-Za-z0-9]*)");
    private static final Pattern LINE_REF = Pattern.compile(":\\d+(-\\d+)?$");
    private static final Pattern ALL_CAPS = Pattern.compile("[A-Z0-9_]+");

    /** Menciones de clases/paths muertos solo valen si el contexto dice que están muertos. */
    private static final Pattern HISTORIC_MARKER = Pattern.compile(
            "elimin|remov|descart|fusion|consolid|reemplaz|absorb|deprecat|retir|renombr|migrad"
                    + "|rechaz|no existe|no hay|no se implement|históric|viejo|anterior|stale"
                    + "|en vez de|resuelto|planificad|latente",
            Pattern.CASE_INSENSITIVE);

    /** Tipos de framework/librería citados legítimamente en docs (no son del proyecto). */
    private static final Set<String> FRAMEWORK = Set.of(
            "AuthenticationFailureHandler", "AuthenticationSuccessHandler", "AuthorizationFilter",
            "CommandLineRunner", "ContentSecurityPolicyHeaderWriter", "DuplicateKeyException",
            "ErrorController", "FileTemplateResolver", "HandlerInterceptor", "HiddenHttpMethodFilter",
            "HttpServletRequest", "IllegalArgumentException", "IllegalStateException",
            "JpaRepository", "LocaleResolver", "LockedException", "MessageSource", "MockBean",
            "MockHttpServletRequest", "MockMvc", "MockServletContext", "MongoRepository",
            "MongoTemplate", "MultipartFile", "NoResourceFoundException", "NoSuchMessageException",
            "OptimisticLockingFailureException", "PasswordEncoder", "RequestMapping",
            "SecurityException", "TemplateEngine", "TemplateProcessingException",
            "UserDetailsService", "UserDetails", "WebMvcConfigurer", "AccessDeniedException",
            "AnonymousAuthenticationToken", "BCryptPasswordEncoder", "ControllerAdvice",
            "ExceptionHandler", "GetMapping", "PostMapping", "PutMapping", "SpringBootTest",
            "WithMockUser", "DocumentReference", "ResponseEntity", "ResponseStatusException", "ObjectMapper",
            "JsonNode", "InputStream", "TestRestTemplate", "Page", "Pageable",
            "Scanner", "ConcurrentHashMap", "AtomicLong", "AtomicInteger", "Duration",
            "Instant", "LocalDateTime", "SecureRandom", "UUID", "ServletContext",
            "IndexKeySpecsConflict", "String", "Integer", "Long", "Boolean", "Object",
            "List", "Map", "Set", "Optional", "Stream", "Exception", "RuntimeException",
            "PlatformTransactionManager", "ApplicationEventPublisher", "MongoTransactionManager");

    /** Términos de dominio/nombres propios citados en backticks que no son clases Java. */
    private static final Set<String> NON_CLASS_TERMS = Set.of(
            "Dockerfile", "Cuenta", "Invitado", "Usuario", "Catador", "Organización",
            "FedericoGoncalvez", "CountryCode", "StatTile", "IndexedDB", "CacheStorage");

    @Test
    void mejorasIdsAreUnique() throws IOException {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> dups = new TreeSet<>();
        for (String line : Files.readAllLines(DOCS.resolve("MEJORAS.md"))) {
            Matcher m = ROW_ID.matcher(line);
            if (m.find() && !seen.add(Integer.parseInt(m.group(1))))
                dups.add(Integer.parseInt(m.group(1)));
        }
        assertTrue(dups.isEmpty(), "IDs duplicados en MEJORAS.md: " + dups);
    }

    @Test
    void everyDocIsListedInIndice() throws IOException {
        String indice = Files.readString(INDICE);
        List<String> missing = new ArrayList<>();
        for (Path doc : allDocs()) {
            String name = doc.getFileName().toString();
            if (doc.equals(INDICE)) continue;
            if (!indice.contains(name)) missing.add(doc.toString());
        }
        assertTrue(missing.isEmpty(), "Docs no listados en INDICE.md: " + missing);
    }

    @Test
    void backtickedClassNamesExistOrAreMarkedHistoric() throws IOException {
        Set<String> declared = declaredTypeNames();
        List<String> violations = new ArrayList<>();
        for (Path doc : allDocs()) {
            List<String> lines = Files.readAllLines(doc);
            for (int i = 0; i < lines.size(); i++) {
                String context = contextBlock(lines, i);
                Matcher m = BACKTICK_CLASS.matcher(lines.get(i));
                while (m.find()) {
                    String name = m.group(1);
                    if (ALL_CAPS.matcher(name).matches()) continue; // constante enum, no clase
                    if (declared.contains(name) || FRAMEWORK.contains(name)
                            || NON_CLASS_TERMS.contains(name)) continue;
                    if (HISTORIC_MARKER.matcher(context).find()) continue;
                    violations.add(doc + ":" + (i + 1) + " `" + name + "`");
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Clases citadas en docs que no existen ni están marcadas como históricas:\n  "
                        + String.join("\n  ", violations));
    }

    @Test
    void backtickedPathsExist() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path doc : allDocs()) {
            List<String> lines = Files.readAllLines(doc);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher m = BACKTICK_PATH.matcher(line);
                while (m.find()) {
                    String p = LINE_REF.matcher(m.group(1)).replaceAll("");
                    if (p.contains("<") || p.contains("*") || p.contains("..")) continue;
                    if (HISTORIC_MARKER.matcher(line).find()) continue;
                    if (!resolve(p)) violations.add(doc + ":" + (i + 1) + " `" + p + "`");
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "Paths citados en docs que no existen:\n  " + String.join("\n  ", violations));
    }

    /** Existe como path directo, bajo src/main/resources (shorthand de docs), o +".java". */
    private boolean resolve(String p) {
        if (p.contains("{")) {
            String parent = p.substring(0, p.indexOf('{'));
            parent = parent.endsWith("/") ? parent.substring(0, parent.length() - 1)
                    : parent.substring(0, Math.max(parent.lastIndexOf('/'), 0));
            return parent.isEmpty() || resolve(parent);
        }
        for (String candidate : List.of(p, RESOURCES.resolve(p).toString(), p + ".java",
                RESOURCES.resolve(p + ".java").toString())) {
            if (Files.exists(Path.of(candidate))) return true;
        }
        return false;
    }

    /** Línea + continuaciones indentadas (wrap de markdown) — el marker puede caer abajo. */
    private String contextBlock(List<String> lines, int i) {
        StringBuilder sb = new StringBuilder(lines.get(i));
        for (int k = i + 1; k < lines.size(); k++) {
            String next = lines.get(k);
            if (next.isBlank() || !(next.startsWith(" ") || next.startsWith("\t"))) break;
            sb.append(' ').append(next);
        }
        return sb.toString();
    }

    private List<Path> allDocs() throws IOException {
        List<Path> docs = new ArrayList<>();
        for (Path p : ROOT_DOCS) if (Files.exists(p)) docs.add(p);
        try (Stream<Path> s = Files.walk(DOCS)) {
            s.filter(p -> p.toString().endsWith(".md")).forEach(docs::add);
        }
        return docs;
    }

    private Set<String> declaredTypeNames() throws IOException {
        Set<String> names = new HashSet<>();
        for (String root : List.of("src/main/java", "src/test/java", "scratch")) {
            Path dir = Path.of(root);
            if (!Files.exists(dir)) continue;
            try (Stream<Path> s = Files.walk(dir)) {
                for (Path p : (Iterable<Path>) s.filter(f -> f.toString().endsWith(".java"))::iterator) {
                    Matcher m = TYPE_DECL.matcher(Files.readString(p));
                    while (m.find()) names.add(m.group(1));
                }
            }
        }
        return names;
    }
}
