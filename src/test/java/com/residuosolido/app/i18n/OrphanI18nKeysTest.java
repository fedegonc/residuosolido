package com.residuosolido.app.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.enums.TimeSlot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato INVERSO a ServerMessageContractTest/TemplateI18nContractTest: en vez de
 * "toda clave declarada existe traducida", verifica "toda clave traducida se usa en
 * algún lado" — mismo tipo de limpieza que se hizo a mano una vez (ver MEJORAS.md #24:
 * 136→91 claves), ahora automática y repetible.
 *
 * "Usada" significa: (a) es el serverKey() de un ServerMessage, (b) aparece como
 * data-i18n/data-i18n-attr literal en un template, (c) coincide con un prefijo
 * dinámico ('mat_' + enum.name().toLowerCase(), etc.) expandido contra TODOS los
 * enums de dominio, o (d) aparece como literal citado en cualquier lado de un
 * template (cubre los casos pasados como argumento a fragments, ej.
 * forms::submit(icon, 'auth_login_button', ...)).
 *
 * También cuenta como "usada" una clave que aparece en un locator Playwright de
 * src/test/java (data-i18n='key') — si un browser test depende de ella, borrarla
 * no es responsabilidad de este contrato aunque el template real ya no la tenga
 * (eso es un problema aparte: template y test desincronizados, no traducción muerta).
 */
class OrphanI18nKeysTest {

    private static final Path TEMPLATES_DIR = Path.of("src/main/resources/templates");
    private static final Path TESTS_DIR = Path.of("src/test/java");
    private static final Pattern DATA_I18N = Pattern.compile("data-i18n=\"([^\"]+)\"");
    private static final Pattern DATA_I18N_ATTR = Pattern.compile("data-i18n-attr=\"([^\"]+)\"");
    private static final Pattern DYNAMIC_PREFIX = Pattern.compile("'([a-z_]+)'\\s*\\+");
    private static final Pattern TEST_LOCATOR = Pattern.compile("data-i18n='([a-zA-Z0-9_]+)'");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void everyKeyInSpanishJson_isUsedSomewhere() throws Exception {
        assertNoOrphans("es");
    }

    @Test
    void everyKeyInPortugueseJson_isUsedSomewhere() throws Exception {
        assertNoOrphans("pt");
    }

    private void assertNoOrphans(String lang) throws Exception {
        String allTemplates = readAllTemplates();
        Set<String> used = usedKeys(allTemplates);

        JsonNode json;
        try (InputStream in = getClass().getResourceAsStream("/static/i18n/" + lang + ".json")) {
            json = MAPPER.readTree(in);
        }

        List<String> orphans = new ArrayList<>();
        Iterator<String> fields = json.fieldNames();
        while (fields.hasNext()) {
            String key = fields.next();
            boolean literalMatch = allTemplates.contains("'" + key + "'") || allTemplates.contains("\"" + key + "\"");
            if (!used.contains(key) && !literalMatch) {
                orphans.add(key);
            }
        }
        orphans.sort(String::compareTo);

        assertTrue(orphans.isEmpty(),
                "Claves en " + lang + ".json sin ningún uso detectado (" + orphans.size() + "): " + orphans);
    }

    private Set<String> usedKeys(String allTemplates) throws Exception {
        Set<String> used = new HashSet<>();

        for (ServerMessage m : ServerMessage.values()) {
            used.add(m.serverKey());
        }

        Matcher direct = DATA_I18N.matcher(allTemplates);
        while (direct.find()) used.add(direct.group(1));

        Matcher attr = DATA_I18N_ATTR.matcher(allTemplates);
        while (attr.find()) {
            String pair = attr.group(1);
            int sep = pair.indexOf(':');
            if (sep > 0) used.add(pair.substring(sep + 1));
        }

        Set<String> dynamicPrefixes = new TreeSet<>();
        Matcher dyn = DYNAMIC_PREFIX.matcher(allTemplates);
        while (dyn.find()) dynamicPrefixes.add(dyn.group(1));

        List<String> allEnumValues = new ArrayList<>();
        for (City c : City.values()) allEnumValues.add(c.name().toLowerCase(Locale.ROOT));
        for (MaterialCategory m : MaterialCategory.values()) allEnumValues.add(m.name().toLowerCase(Locale.ROOT));
        for (TimeSlot s : TimeSlot.values()) allEnumValues.add(s.name().toLowerCase(Locale.ROOT));
        for (RequestStatus s : RequestStatus.values()) allEnumValues.add(s.name().toLowerCase(Locale.ROOT));
        for (Role r : Role.values()) allEnumValues.add(r.name().toLowerCase(Locale.ROOT));

        for (String prefix : dynamicPrefixes) {
            for (String value : allEnumValues) used.add(prefix + value);
        }

        Matcher testLocators = TEST_LOCATOR.matcher(readAllJavaTests());
        while (testLocators.find()) used.add(testLocators.group(1));

        return used;
    }

    private String readAllTemplates() throws Exception {
        return readAllFilesWithExtension(TEMPLATES_DIR, ".html");
    }

    private String readAllJavaTests() throws Exception {
        return readAllFilesWithExtension(TESTS_DIR, ".java");
    }

    private String readAllFilesWithExtension(Path dir, String extension) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(extension))::iterator) {
                sb.append(Files.readString(path)).append('\n');
            }
        }
        return sb.toString();
    }
}
