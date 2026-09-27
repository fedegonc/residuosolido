package com.residuosolido.app.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class UsernameTest {

    @Test
    void canonicalTrimsAndLowercases() {
        assertEquals("juan pérez", Username.canonical("  Juan Pérez  "));
    }

    @Test
    void canonicalIsIdempotent() {
        String already = "maria_garcia";
        assertEquals(already, Username.canonical(already));
    }

    @Test
    void canonicalReturnsNullForNull() {
        assertNull(Username.canonical(null));
    }
}
