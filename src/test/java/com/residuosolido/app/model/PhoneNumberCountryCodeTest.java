package com.residuosolido.app.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PhoneNumberCountryCodeTest {

    @Nested
    class Uruguay {
        @Test
        void validNationalNumber_producesE164() {
            String pn = PhoneNumber.normalize("+598", "99123456", null);
            assertEquals("+59899123456", pn);
        }

        @Test
        void stripsLeadingZero_domesticFormat() {
            String pn = PhoneNumber.normalize("+598", "092224955", null);
            assertEquals("+59892224955", pn);
        }

        @Test
        void stripsSpaces() {
            String pn = PhoneNumber.normalize("+598", "99 123 456", null);
            assertEquals("+59899123456", pn);
        }

        @Test
        void stripsParentheses_bugReportedByUser() {
            // Bug real: "(099) 123 456" tiraba error.phone.invalid ("debe incluir código
            // de país") aunque el usuario ya había elegido +598 del select — los paréntesis
            // no se limpiaban, quedaba texto no numérico y el mensaje no describía la causa real.
            String pn = PhoneNumber.normalize("+598", "(099) 123 456", null);
            assertEquals("+59899123456", pn);
        }

        @Test
        void stripsDialCodeIfIncluded() {
            String pn = PhoneNumber.normalize("+598", "+59899123456", null);
            assertEquals("+59899123456", pn);
        }

        @Test
        void tooShort_throwsInvalidLength() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598", "9912345", null));
            assertEquals("error.phone.invalid_length", ex.getMessage());
        }

        @Test
        void tooLong_throwsInvalidLength() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598", "991234567", null));
            assertEquals("error.phone.invalid_length", ex.getMessage());
        }

        @Test
        void firstDigitNot9_throwsInvalidFirstDigit() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598", "21234567", null));
            assertEquals("error.phone.invalid_first_digit", ex.getMessage());
        }

        @Test
        void emptyNumber_throwsRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+598", "", null));
            assertEquals("error.phone.required", ex.getMessage());
        }
    }

    @Nested
    class Brazil {
        @Test
        void validWithDefaultDdd_producesE164() {
            String pn = PhoneNumber.normalize("+55", "912345678", null);
            assertEquals("+5555912345678", pn);
        }

        @Test
        void validWithExplicitDdd55_producesE164() {
            String pn = PhoneNumber.normalize("+55", "912345678", "55");
            assertEquals("+5555912345678", pn);
        }

        @Test
        void validWithDdd51_producesE164() {
            String pn = PhoneNumber.normalize("+55", "912345678", "51");
            assertEquals("+5551912345678", pn);
        }

        @Test
        void stripsDialCodeIfIncluded() {
            String pn = PhoneNumber.normalize("+55", "+5555912345678", "55");
            assertEquals("+5555912345678", pn);
        }

        @Test
        void tooShortNational_throwsInvalidLength() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+55", "91234567", "55"));
            assertEquals("error.phone.invalid_length", ex.getMessage());
        }

        @Test
        void firstDigitNot9_throwsInvalidFirstDigit() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+55", "812345678", "55"));
            assertEquals("error.phone.invalid_first_digit", ex.getMessage());
        }

        @Test
        void stripsParenthesesInNationalAndDdd() {
            String pn = PhoneNumber.normalize("+55", "(912) 345-678", "(51)");
            assertEquals("+5551912345678", pn);
        }

        @Test
        void invalidDdd_throwsInvalidDdd() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+55", "912345678", "ABC"));
            assertEquals("error.phone.invalid_ddd", ex.getMessage());
        }

        @Test
        void dddOneDigit_throwsInvalidDdd() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.normalize("+55", "912345678", "5"));
            assertEquals("error.phone.invalid_ddd", ex.getMessage());
        }
    }

    @Nested
    class UnsupportedCountry {
        @Test
        void unsupportedDialCode_throws() {
            assertThrows(IllegalArgumentException.class, () -> PhoneNumber.normalize("+1", "912345678", null));
        }
    }

    @Nested
    class BackwardCompatibility {
        @Test
        void ofStringStillWorks() {
            assertEquals("+59899123456", PhoneNumber.normalize("+59899123456"));
        }

        @Test
        void ofStringWithSpacesStillWorks() {
            assertEquals("+59899123456", PhoneNumber.normalize(" +598 99 123 456 "));
        }

        @Test
        void ofStringWithDashesAndParensStillWorks() {
            assertEquals("+59899123456", PhoneNumber.normalize("+598 (99) 123-456"));
        }

        @Test
        void isValidStillWorks() {
            assertTrue(PhoneNumber.isValid("+59899123456"));
            assertFalse(PhoneNumber.isValid("invalid"));
        }

        @Test
        void equalityPreserved() {
            assertEquals(PhoneNumber.normalize("+59899123456"), PhoneNumber.normalize(" +598 99 123 456 "));
        }
    }
}
