package com.residuosolido.app.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ramas de PhoneNumber no ejercitadas por PhoneNumberCountryCodeTest:
 * normalize(raw) límites, resolve() fallbacks y errores residuales.
 */
@Tag("unit")
class PhoneNumberEdgeTest {

    @Nested
    class NormalizeRaw {

        @Test
        void nullRaw_throwsRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize(null));
            assertEquals("error.phone.required", ex.getMessage());
        }

        @Test
        void blankRaw_throwsRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("   "));
            assertEquals("error.phone.required", ex.getMessage());
        }

        @Test
        void longerThan32Chars_throwsInvalid() {
            // El check de largo se hace sobre el RAW (con separadores): un input
            // gigante se rechaza aunque sus dígitos limpios serían válidos.
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598 99 123 456 789 012 345 678 901"));
            assertEquals("error.phone.invalid", ex.getMessage());
        }

        @Test
        void leadingZeroDialCode_throwsInvalid() {
            // "+0..." no matchea el patrón E.164 ([1-9] después del +).
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+01234567"));
            assertEquals("error.phone.invalid", ex.getMessage());
        }

        @Test
        void missingPlus_throwsInvalid() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("59899123456"));
            assertEquals("error.phone.invalid", ex.getMessage());
        }

        @Test
        void lettersAfterCleaning_throwsInvalid() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598abc123456"));
            assertEquals("error.phone.invalid", ex.getMessage());
        }

        @Test
        void tooFewDigits_throwsInvalid() {
            // E.164 exige mínimo 7 dígitos tras el +.
            assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+5989"));
        }
    }

    @Nested
    class Resolve {

        @Test
        void noNational_returnsRawUnchanged() {
            assertEquals("+59899123456",
                    PhoneNumber.resolve("+598", null, null, "+59899123456"));
            assertNull(PhoneNumber.resolve("+598", null, null, null));
        }

        @Test
        void blankNational_returnsRawUnchanged() {
            assertEquals("crudo", PhoneNumber.resolve("+598", "  ", "55", "crudo"));
        }

        @Test
        void nationalWithoutDialCode_returnsRaw() {
            // Tiene número nacional pero no eligió país: no se puede normalizar
            // sin dialCode → devuelve el raw (que luego se valida en normalize).
            assertEquals("99123456", PhoneNumber.resolve(null, "99123456", null, "99123456"));
            assertEquals("99123456", PhoneNumber.resolve(" ", "99123456", null, "99123456"));
        }

        @Test
        void dialCodeAndNational_normalizes() {
            assertEquals("+59899123456",
                    PhoneNumber.resolve("+598", "99123456", null, "ignorado"));
        }
    }

    @Nested
    class CountryNormalizeEdges {

        @Test
        void nullDialCode_throwsUnsupportedCountry() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize(null, "99123456", null));
            assertEquals("error.phone.unsupported_country", ex.getMessage());
        }

        @Test
        void nullNational_throwsRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598", null, null));
            assertEquals("error.phone.required", ex.getMessage());
        }

        @Test
        void uruguayNationalWithLetters_throwsInvalid() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598", "99abc456", null));
            assertEquals("error.phone.invalid", ex.getMessage());
        }

        @Test
        void brazilDialCodeWithLetters_throwsInvalid() {
            // "+55abc..." → tras quitar +55 quedan letras → invalid antes de validar largo.
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+55", "+55abc91234567", null));
            assertEquals("error.phone.invalid", ex.getMessage());
        }

        @Test
        void brazilBlankDdd_usesDefault55() {
            // DDD vacío → default "55" (frontera: Livramento es 55).
            assertEquals("+5555912345678",
                    PhoneNumber.normalize("+55", "912345678", "  "));
        }
    }

    @Nested
    class IsValid {

        @Test
        void isValid_null_returnsFalse() {
            assertFalse(PhoneNumber.isValid(null));
        }

        @Test
        void isValid_blank_returnsFalse() {
            assertFalse(PhoneNumber.isValid("  "));
        }
    }
}
