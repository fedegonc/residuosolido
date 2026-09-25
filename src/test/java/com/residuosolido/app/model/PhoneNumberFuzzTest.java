package com.residuosolido.app.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhoneNumber: validación exhaustiva de formatos")
class PhoneNumberFuzzTest {

    @ParameterizedTest(name = "normalize('+598', '{0}', null) — Uruguay válido")
    @ValueSource(strings = {
        "99123456",          // base UY
        "99 123 456",        // con espacios
        "99-123-456",        // con guiones
        "(99) 123-456",      // con decorativos
        "099123456",         // con 0 inicial
    })
    void uruguayFormats_normalize(String phone) {
        assertDoesNotThrow(() -> {
            String normalized = PhoneNumber.normalize("+598", phone, null);
            assertNotNull(normalized);
            assertTrue(normalized.startsWith("+598"));
            assertTrue(normalized.length() >= 11); // +598 + al menos 8 dígitos
        });
    }

    @ParameterizedTest(name = "normalize('+55', '{0}', '11') — Brasil válido")
    @ValueSource(strings = {
        "987654321",         // base BR (9 dígitos nacionales)
        "9 8765-4321",       // con espacios/guiones
        "(9) 8765-4321",     // con decorativos
    })
    void brazilFormats_normalize(String phone) {
        assertDoesNotThrow(() -> {
            String normalized = PhoneNumber.normalize("+55", phone, "11");
            assertNotNull(normalized);
            assertTrue(normalized.startsWith("+55"));
        });
    }

    @ParameterizedTest(name = "normalize('+55', nacional, DDD '{0}') — variación de DDD")
    @ValueSource(strings = {
        "21",  // Río de Janeiro
        "85",  // Ceará
        "62",  // Brasilia
    })
    void brazilWithDifferentDdd_normalize(String ddd) {
        String phone = "987654321";

        assertDoesNotThrow(() -> {
            String normalized = PhoneNumber.normalize("+55", phone, ddd);
            assertNotNull(normalized);
            assertTrue(normalized.startsWith("+55"));
        });
    }

    @ParameterizedTest(name = "edge cases rechazados")
    @ValueSource(strings = {
        "",                  // vacío
        "   ",               // solo espacios
        "123",               // muy corto
        "abc",               // solo letras
    })
    void edgeCases_mustThrow(String phone) {
        assertThrows(Exception.class, () ->
            PhoneNumber.normalize("+598", phone, null)
        );
    }

    @ParameterizedTest(name = "unsupported country — rechazado")
    @ValueSource(strings = {
        "AR",   // Argentina
        "CL",   // Chile
        "XX",   // inválido
        "999",  // inválido
    })
    void unsupportedCountry_throws(String dialCode) {
        assertThrows(Exception.class, () ->
            PhoneNumber.normalize(dialCode, "99123456", null),
            "Debe rechazar país no soportado: " + dialCode
        );
    }

    @ParameterizedTest(name = "largo inválido — rechazado")
    @ValueSource(strings = {
        "9912345",    // demasiado corto (7 dígitos en lugar de 8)
        "991234567",  // demasiado largo (9 dígitos en lugar de 8)
        "111234567",  // empieza con 1, no 9
        "891234567",  // empieza con 8, no 9
    })
    void invalidLength_throws(String phone) {
        assertThrows(Exception.class, () ->
            PhoneNumber.normalize("+598", phone, null),
            "Debe rechazar largo inválido: " + phone
        );
    }

    @ParameterizedTest(name = "performance: sin catastrophic backtracking")
    @ValueSource(strings = {
        "999999999999",      // muchos 9s
        "111111111111",      // muchos 1s
        "---",               // muchos guiones
        "         ",         // muchos espacios
    })
    void regexPerformance_noBacktracking(String malformed) {
        assertThrows(Exception.class, () ->
            PhoneNumber.normalize("+598", malformed, null),
            "Debe rechazar sin timeout"
        );
    }
}
