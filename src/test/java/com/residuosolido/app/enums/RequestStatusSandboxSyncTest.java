package com.residuosolido.app.enums;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Cierra el gap de sincronización encontrado en la auditoría "máquinas de
 * estados y mecanismos de invariantes": {@code scratch/sim/Domain.java}
 * declara (comentario textual) que su enum RequestStatus está "portado tal
 * cual" de {@link RequestStatus} real — pero nada verificaba eso
 * automáticamente. scratch/ no es un source root de Maven, así que no se
 * puede importar directo; este test lee ambos archivos como texto y compara
 * la tabla de transiciones semántica (qué estados de origen habilita cada
 * transitionX() y a qué estado destino va), ignorando diferencias
 * cosméticas (tipo/mensaje de excepción, visibilidad public/package-private).
 *
 * Si esto falla, alguien cambió una de las dos copias sin espejar la otra —
 * exactamente el escenario que la convención manual ("sandbox como fuente
 * de verdad, se espeja a mano") no detectaba.
 *
 * scratch/ está gitignoreado (ver .gitignore) — no existe en un clon limpio
 * (CI, otra máquina). Si el archivo no está, el test se SALTA (Assumptions),
 * no falla: es una verificación local-only, igual que scratch/ mismo.
 */
@Tag("unit")
class RequestStatusSandboxSyncTest {

    private static final Path SANDBOX_FILE = Path.of("scratch/sim/Domain.java");

    private static final Pattern ENUM_CONSTANTS = Pattern.compile(
            "enum\\s+RequestStatus\\s*\\{\\s*([A-Z_]+(?:\\s*,\\s*[A-Z_]+)*)\\s*;");

    private static final Pattern TRANSITION_METHOD = Pattern.compile(
            "RequestStatus\\s+(transition\\w+)\\(\\)\\s*\\{(.*?)return\\s+(\\w+);",
            Pattern.DOTALL);

    private static final Pattern FORBIDDEN_STATE = Pattern.compile("this\\s*!=\\s*(\\w+)");

    @Test
    void sandboxDomain_hasSameStatesAsRealEnum() throws IOException {
        Assumptions.assumeTrue(Files.exists(SANDBOX_FILE), "scratch/ no presente (gitignoreado) — salteando");
        Set<String> realStates = new TreeSet<>();
        for (RequestStatus s : RequestStatus.values()) realStates.add(s.name());

        Set<String> sandboxStates = extractEnumConstants(readSandbox());

        assertEquals(realStates, sandboxStates,
                "scratch/sim/Domain.java declara estados distintos a enums/RequestStatus.java real");
    }

    @Test
    void sandboxDomain_hasSameTransitionTableAsRealEnum() throws IOException {
        Assumptions.assumeTrue(Files.exists(SANDBOX_FILE), "scratch/ no presente (gitignoreado) — salteando");
        Map<String, TransitionRule> real = extractTransitionTable(readRealSource());
        Map<String, TransitionRule> sandbox = extractTransitionTable(readSandbox());

        assertFalse(real.isEmpty(), "No se pudo extraer ninguna transición del RequestStatus real — regex desactualizada");
        assertEquals(real, sandbox,
                "La tabla de transiciones de scratch/sim/Domain.java divergió de enums/RequestStatus.java real. "
                        + "El comentario \"portado tal cual\" ya no es cierto — actualizar una de las dos copias.");
    }

    private record TransitionRule(Set<String> allowedFrom, String target) {}

    private Map<String, TransitionRule> extractTransitionTable(String source) {
        Map<String, TransitionRule> table = new LinkedHashMap<>();
        Matcher m = TRANSITION_METHOD.matcher(source);
        while (m.find()) {
            String methodName = m.group(1);
            String condition = m.group(2);
            String target = m.group(3);
            Set<String> allowedFrom = new TreeSet<>();
            Matcher forbidden = FORBIDDEN_STATE.matcher(condition);
            while (forbidden.find()) allowedFrom.add(forbidden.group(1));
            table.put(methodName, new TransitionRule(allowedFrom, target));
        }
        return table;
    }

    private Set<String> extractEnumConstants(String source) {
        Matcher m = ENUM_CONSTANTS.matcher(source);
        if (!m.find()) return Set.of();
        Set<String> constants = new TreeSet<>();
        for (String c : m.group(1).split("\\s*,\\s*")) constants.add(c.trim());
        return constants;
    }

    private String readSandbox() throws IOException {
        return Files.readString(SANDBOX_FILE);
    }

    /** Lee el propio RequestStatus.java real como texto (mismo criterio que DocsContractTest). */
    private String readRealSource() throws IOException {
        return Files.readString(Path.of("src/main/java/com/residuosolido/app/enums/RequestStatus.java"));
    }
}
