package com.residuosolido.app.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TrackingCodeTest {

    @Test
    void canonicalTrimsAndUppercases() {
        assertEquals("ABC123DE", TrackingCode.canonical("  abc123de  "));
    }

    @Test
    void canonicalIsIdempotent() {
        assertEquals("XYZW2345", TrackingCode.canonical("XYZW2345"));
    }

    @Test
    void canonicalReturnsNullForNull() {
        assertNull(TrackingCode.canonical(null));
    }
}
