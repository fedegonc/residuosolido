package com.residuosolido.app.controller;

import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class MessagesTest {

    private MessageSource messageSource;
    private Messages messages;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        messages = new Messages(messageSource);
    }

    @Test
    void msg_withKey_delegatesToMessageSource() {
        String code = ServerMessage.FLASH_REQUEST_CREATED.code();
        when(messageSource.getMessage(eq(code), isNull(), eq(code), any(Locale.class)))
                .thenReturn("Listo");

        assertEquals("Listo", messages.msg(ServerMessage.FLASH_REQUEST_CREATED));
    }

    @Test
    void msg_withArgs_passesArgsToMessageSource() {
        String code = ServerMessage.FLASH_REQUEST_CREATED.code();
        when(messageSource.getMessage(eq(code), eq(new Object[]{5}), eq(code), any(Locale.class)))
                .thenReturn("hay 5");

        assertEquals("hay 5", messages.msg(ServerMessage.FLASH_REQUEST_CREATED, 5));
    }

    @Test
    void msg_keyedException_usesTypedKey() {
        ValidationException ex = new ValidationException(ServerMessage.FLASH_REQUEST_NOT_FOUND);
        String code = ex.key().code();
        when(messageSource.getMessage(eq(code), isNull(), eq(code), any(Locale.class)))
                .thenReturn("no existe");

        assertEquals("no existe", messages.msg(ex));
    }

    @Test
    void msg_plainException_fallsBackToGetMessage() {
        IllegalStateException ex = new IllegalStateException("error.generico");
        when(messageSource.getMessage(eq("error.generico"), isNull(), eq("error.generico"), any(Locale.class)))
                .thenReturn("falló");

        assertEquals("falló", messages.msg(ex));
    }

    @Test
    void flashSuccess_addsSuccessMessageAttribute() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        when(messageSource.getMessage(any(), any(), any(), any(Locale.class))).thenReturn("ok");

        messages.flashSuccess(ra, ServerMessage.FLASH_REQUEST_CREATED);

        assertEquals("ok", ra.getFlashAttributes().get("successMessage"));
    }

    @Test
    void flashError_withThrowable_resolvesAndAddsErrorMessage() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        when(messageSource.getMessage(any(), any(), any(), any(Locale.class))).thenReturn("mal");

        messages.flashError(ra, new IllegalStateException("error.x"));

        assertEquals("mal", ra.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void addFormAttributes_addsMaterialsAndTimeSlots() {
        Model model = new ExtendedModelMap();

        messages.addFormAttributes(model);

        assertEquals(MaterialCategory.values().length,
                ((MaterialCategory[]) model.getAttribute("materials")).length);
        assertEquals(TimeSlot.values().length,
                ((TimeSlot[]) model.getAttribute("timeSlots")).length);
    }
}
