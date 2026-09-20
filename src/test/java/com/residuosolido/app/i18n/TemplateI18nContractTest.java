package com.residuosolido.app.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.TimeSlot;

import java.util.Locale;
import java.util.Map;

/**
 * Contrato data-i18n (cliente) <-> i18n/{lang}.json — mismo patrón que
 * ServerMessageContractTest pero para las claves client-side de los templates.
 *
 * Cubre claves LITERALES (data-i18n="key", data-i18n-attr="attr:key") y también
 * las DINÁMICAS interpoladas vía th:attr ('mat_' + mat.name(), 'req_city_' +
 * c.name()...): cada prefijo encontrado debe estar registrado en
 * DYNAMIC_PREFIX_ENUMS con su enum, y cada clave expandida
 * (prefijo + enum.name().toLowerCase()) debe existir en ambos JSON.
 * Bug real que motiva esto: 'req_city_livramento' se renderizaba crudo
 * (LIVRAMENTO) porque la clave nunca existió y ningún test la verificaba.
 */
class TemplateI18nContractTest {

    private static final Path TEMPLATES_DIR = Path.of("src/main/resources/templates");
    private static final Pattern DATA_I18N = Pattern.compile("data-i18n=\"([^\"]+)\"");
    private static final Pattern DATA_I18N_ATTR = Pattern.compile("data-i18n-attr=\"([^\"]+)\"");
    private static final Pattern DYNAMIC_PREFIX = Pattern.compile("'([a-z_]+)'\\s*\\+");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Prefijo dinámico -> enum del que salen sus valores (registro explícito:
     * un prefijo nuevo en un template sin registrar rompe el test). */
    private static final Map<String, Class<? extends Enum<?>>> DYNAMIC_PREFIX_ENUMS = Map.of(
            "mat_", MaterialCategory.class,
            "city_", City.class,
            "req_city_", City.class,
            "req_form_slot_", TimeSlot.class);

    @Test
    void everyTemplateKey_existsInSpanish() throws Exception {
        assertAllKeysPresent("es");
    }

    @Test
    void everyTemplateKey_existsInPortuguese() throws Exception {
        assertAllKeysPresent("pt");
    }

    private void assertAllKeysPresent(String lang) throws Exception {
        Set<String> keys = extractKeysFromTemplates();
        keys.addAll(expandDynamicKeys());
        JsonNode json;
        try (InputStream in = getClass().getResourceAsStream("/static/i18n/" + lang + ".json")) {
            json = MAPPER.readTree(in);
        }
        List<String> missing = new ArrayList<>();
        for (String key : keys) {
            if (!json.has(key)) missing.add(key);
        }
        assertTrue(missing.isEmpty(),
                "Claves data-i18n sin traducción en " + lang + ".json (" + missing.size() + "): " + missing);
    }

    /** Extrae prefijos dinámicos ('x_' + enum.name()) y los expande contra el enum registrado. */
    private Set<String> expandDynamicKeys() throws Exception {
        Set<String> keys = new TreeSet<>();
        Set<String> unregistered = new TreeSet<>();
        try (Stream<Path> paths = Files.walk(TEMPLATES_DIR)) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".html"))::iterator) {
                Matcher dyn = DYNAMIC_PREFIX.matcher(Files.readString(path));
                while (dyn.find()) {
                    String prefix = dyn.group(1);
                    Class<? extends Enum<?>> enumClass = DYNAMIC_PREFIX_ENUMS.get(prefix);
                    if (enumClass == null) { unregistered.add(prefix); continue; }
                    for (Enum<?> value : enumClass.getEnumConstants())
                        keys.add(prefix + value.name().toLowerCase(Locale.ROOT));
                }
            }
        }
        assertTrue(unregistered.isEmpty(),
                "Prefijos data-i18n dinámicos sin enum registrado en DYNAMIC_PREFIX_ENUMS: " + unregistered);
        return keys;
    }

    private Set<String> extractKeysFromTemplates() throws Exception {
        Set<String> keys = new TreeSet<>();
        try (Stream<Path> paths = Files.walk(TEMPLATES_DIR)) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".html"))::iterator) {
                String content = Files.readString(path);

                Matcher direct = DATA_I18N.matcher(content);
                while (direct.find()) keys.add(direct.group(1));

                Matcher attr = DATA_I18N_ATTR.matcher(content);
                while (attr.find()) {
                    String pair = attr.group(1);
                    int sep = pair.indexOf(':');
                    if (sep > 0) keys.add(pair.substring(sep + 1));
                }
            }
        }
        return keys;
    }
}
