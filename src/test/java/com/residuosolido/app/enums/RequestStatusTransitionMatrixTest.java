package com.residuosolido.app.enums;

import com.residuosolido.app.exception.StateException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cobertura exhaustiva de la matriz 4×4 de RequestStatus (4 estados × 3
 * transiciones = 12 casos) — hueco encontrado en la auditoría de máquinas de
 * estados: antes solo había tests de la transición "happy" + alguna inválida
 * suelta, no la matriz completa. No usa jqwik (property-based): el espacio
 * de estados es finito y chico (4 valores), exhaustivo es más simple y más
 * legible que property-based para este caso — jqwik se justificaría con un
 * dominio de entrada grande/infinito, que acá no existe.
 */
@Tag("unit")
class RequestStatusTransitionMatrixTest {

    /** Tabla de verdad: para cada transición, el único estado de origen válido y su destino. */
    private record Case(RequestStatus from, Function<RequestStatus, RequestStatus> transition,
                         String transitionName, RequestStatus expectedTarget) {}

    private static Stream<Case> allCases() {
        return Stream.of(RequestStatus.values()).flatMap(from -> Stream.of(
                new Case(from, RequestStatus::transitionAccept, "transitionAccept", RequestStatus.IN_PROGRESS),
                new Case(from, RequestStatus::transitionComplete, "transitionComplete", RequestStatus.COMPLETED),
                new Case(from, RequestStatus::transitionReject, "transitionReject", RequestStatus.REJECTED)
        ));
    }

    /** Único estado de origen permitido por transición — la fuente de verdad de este test. */
    private static final Map<String, RequestStatus> SINGLE_ALLOWED = Map.of(
            "transitionAccept", RequestStatus.PENDING,
            "transitionComplete", RequestStatus.IN_PROGRESS);
    /** transitionReject permite 2 orígenes, se maneja aparte. */

    @ParameterizedTest(name = "[{index}] transición sobre el estado dado")
    @MethodSource("allCases")
    void transition_from12Combinations_matchesTruthTable(Case c) {
        boolean shouldSucceed = "transitionReject".equals(c.transitionName())
                ? (c.from() == RequestStatus.PENDING || c.from() == RequestStatus.IN_PROGRESS)
                : c.from() == SINGLE_ALLOWED.get(c.transitionName());

        if (shouldSucceed) {
            assertEquals(c.expectedTarget(), c.transition().apply(c.from()),
                    c.from() + "." + c.transitionName() + "() debía llegar a " + c.expectedTarget());
        } else {
            assertThrows(StateException.class, () -> c.transition().apply(c.from()),
                    c.from() + "." + c.transitionName() + "() debía rechazar la transición, no la permitió");
        }
    }

    @Test
    void allCases_cover12Combinations() {
        // 4 estados x 3 transiciones = 12 — si esto cambia, alguien agregó un
        // estado o una transición y hay que revisar la tabla de verdad de arriba.
        assertEquals(12, allCases().count());
    }
}
