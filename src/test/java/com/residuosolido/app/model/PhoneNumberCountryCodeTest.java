package com.residuosolido.app.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberCountryCodeTest {

    @Nested
    class Uruguay {
        @Test
        void validNationalNumber_producesE164() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.URUGUAY, "99123456", null);
            assertEquals("+59899123456", pn.value());
        }

        @Test
        void stripsLeadingZero_domesticFormat() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.URUGUAY, "092224955", null);
            assertEquals("+59892224955", pn.value());
        }

        @Test
        void stripsSpaces() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.URUGUAY, "99 123 456", null);
            assertEquals("+59899123456", pn.value());
        }

        @Test
        void stripsDialCodeIfIncluded() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.URUGUAY, "+59899123456", null);
            assertEquals("+59899123456", pn.value());
        }

        @Test
        void tooShort_throwsInvalidLength() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.URUGUAY, "9912345", null));
            assertEquals("error.phone.invalid_length", ex.getMessage());
        }

        @Test
        void tooLong_throwsInvalidLength() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.URUGUAY, "991234567", null));
            assertEquals("error.phone.invalid_length", ex.getMessage());
        }

        @Test
        void firstDigitNot9_throwsInvalidFirstDigit() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.URUGUAY, "21234567", null));
            assertEquals("error.phone.invalid_first_digit", ex.getMessage());
        }

        @Test
        void emptyNumber_throwsRequired() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.URUGUAY, "", null));
            assertEquals("error.phone.required", ex.getMessage());
        }
    }

    @Nested
    class Brazil {
        @Test
        void validWithDefaultDdd_producesE164() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.BRAZIL, "912345678", null);
            assertEquals("+5555912345678", pn.value());
        }

        @Test
        void validWithExplicitDdd55_producesE164() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.BRAZIL, "912345678", "55");
            assertEquals("+5555912345678", pn.value());
        }

        @Test
        void validWithDdd51_producesE164() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.BRAZIL, "912345678", "51");
            assertEquals("+5551912345678", pn.value());
        }

        @Test
        void stripsDialCodeIfIncluded() {
            PhoneNumber pn = PhoneNumber.of(CountryCode.BRAZIL, "+5555912345678", "55");
            assertEquals("+5555912345678", pn.value());
        }

        @Test
        void tooShortNational_throwsInvalidLength() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.BRAZIL, "91234567", "55"));
            assertEquals("error.phone.invalid_length", ex.getMessage());
        }

        @Test
        void firstDigitNot9_throwsInvalidFirstDigit() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.BRAZIL, "812345678", "55"));
            assertEquals("error.phone.invalid_first_digit", ex.getMessage());
        }

        @Test
        void invalidDdd_throwsInvalidDdd() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.BRAZIL, "912345678", "ABC"));
            assertEquals("error.phone.invalid_ddd", ex.getMessage());
        }

        @Test
        void dddOneDigit_throwsInvalidDdd() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> PhoneNumber.of(CountryCode.BRAZIL, "912345678", "5"));
            assertEquals("error.phone.invalid_ddd", ex.getMessage());
        }
    }

    @Nested
    class CountryCodeEnum {
        @Test
        void fromDialCodeUruguay() {
            assertEquals(CountryCode.URUGUAY, CountryCode.fromDialCode("+598"));
        }

        @Test
        void fromDialCodeBrazil() {
            assertEquals(CountryCode.BRAZIL, CountryCode.fromDialCode("+55"));
        }

        @Test
        void fromDialCodeUnsupported_throws() {
            assertThrows(IllegalArgumentException.class, () -> CountryCode.fromDialCode("+1"));
        }
    }

    @Nested
    class BackwardCompatibility {
        @Test
        void ofStringStillWorks() {
            assertEquals("+59899123456", PhoneNumber.of("+59899123456").value());
        }

        @Test
        void ofStringWithSpacesStillWorks() {
            assertEquals("+59899123456", PhoneNumber.of(" +598 99 123 456 ").value());
        }

        @Test
        void isValidStillWorks() {
            assertTrue(PhoneNumber.isValid("+59899123456"));
            assertFalse(PhoneNumber.isValid("invalid"));
        }

        @Test
        void equalityPreserved() {
            assertEquals(PhoneNumber.of("+59899123456"), PhoneNumber.of(" +598 99 123 456 "));
        }
    }
}
