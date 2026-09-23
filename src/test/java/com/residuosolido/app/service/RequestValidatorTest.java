package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestValidator")
class RequestValidatorTest {

    private final RequestValidator validator = new RequestValidator();

    @Test
    @DisplayName("validateCreate — usuario autenticado sin guest data")
    void createWithUser() {
        User user = new User();
        assertDoesNotThrow(() ->
            validator.validateCreate(user, City.RIVERA, "Calle 25 de Mayo",
                List.of(MaterialCategory.PLASTIC), null, null, "org-1")
        );
    }

    @Test
    @DisplayName("validateCreate — usuario guest valida nombre y teléfono")
    void createAsGuest() {
        assertDoesNotThrow(() ->
            validator.validateCreate(null, City.RIVERA, "Calle 25",
                List.of(MaterialCategory.PLASTIC), "Juan", "+59899123456", "org-1")
        );
    }

    @Test
    @DisplayName("validateCreate — guest sin nombre lanza excepción")
    void guestMissingNameFails() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateCreate(null, City.RIVERA, "Calle 25",
                List.of(MaterialCategory.PLASTIC), null, "+59899123456", "org-1")
        );
        assertEquals(ServerMessage.ERROR_GUEST_NAME_REQUIRED, ex.serverMessage);
    }

    @Test
    @DisplayName("validateEstimates — requiere peso o volumen")
    void estimatesRequired() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateEstimates(null, null)
        );
        assertEquals(ServerMessage.ERROR_ESTIMATES_REQUIRED, ex.serverMessage);
    }

    @Test
    @DisplayName("validateEstimates — acepta solo peso")
    void estimatesWeightOnly() {
        assertDoesNotThrow(() -> validator.validateEstimates("5kg", null));
    }

    @Test
    @DisplayName("validateMaterials — org no acepta ese material")
    void materialNotAccepted() {
        User org = new User();
        org.setAcceptedMaterials(List.of(MaterialCategory.PLASTIC));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateMaterials(org, List.of(MaterialCategory.GLASS))
        );
        assertEquals(ServerMessage.ERROR_ORG_DOES_NOT_ACCEPT_MATERIAL, ex.serverMessage);
    }
}
