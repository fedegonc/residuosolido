package com.residuosolido.app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar que los estados de solicitud están correctamente traducidos
 */
public class RequestStatusTest {

    @Test
    public void testRequestStatusTranslations() {
        // Verificar que todos los estados tengan nombres en español
        assertEquals("Pendiente", RequestStatus.PENDING.getDisplayName());
        assertEquals("Aceptada", RequestStatus.ACCEPTED.getDisplayName());
        assertEquals("En Proceso", RequestStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Rechazada", RequestStatus.REJECTED.getDisplayName());
        assertEquals("Completada", RequestStatus.COMPLETED.getDisplayName());
    }

    @Test
    public void testAllStatusesHaveDisplayNames() {
        // Verificar que ningún estado tenga un displayName nulo o vacío
        for (RequestStatus status : RequestStatus.values()) {
            assertNotNull(status.getDisplayName(), "El estado " + status.name() + " no tiene displayName");
            assertFalse(status.getDisplayName().isEmpty(), "El estado " + status.name() + " tiene displayName vacío");
            // Verificar que no contiene palabras en inglés
            assertFalse(status.getDisplayName().toUpperCase().contains("PENDING"), 
                "El estado " + status.name() + " contiene 'PENDING' en inglés");
            assertFalse(status.getDisplayName().toUpperCase().contains("ACCEPTED"), 
                "El estado " + status.name() + " contiene 'ACCEPTED' en inglés");
            assertFalse(status.getDisplayName().toUpperCase().contains("PROGRESS"), 
                "El estado " + status.name() + " contiene 'PROGRESS' en inglés");
            assertFalse(status.getDisplayName().toUpperCase().contains("REJECTED"), 
                "El estado " + status.name() + " contiene 'REJECTED' en inglés");
            assertFalse(status.getDisplayName().toUpperCase().contains("COMPLETED"), 
                "El estado " + status.name() + " contiene 'COMPLETED' en inglés");
        }
    }

    @Test
    public void testStatusEnumValues() {
        // Verificar que existan todos los estados esperados
        assertNotNull(RequestStatus.PENDING);
        assertNotNull(RequestStatus.ACCEPTED);
        assertNotNull(RequestStatus.IN_PROGRESS);
        assertNotNull(RequestStatus.REJECTED);
        assertNotNull(RequestStatus.COMPLETED);
    }

    @Test
    public void testStatusCount() {
        // Verificar que hay exactamente 5 estados
        assertEquals(5, RequestStatus.values().length);
    }
}
