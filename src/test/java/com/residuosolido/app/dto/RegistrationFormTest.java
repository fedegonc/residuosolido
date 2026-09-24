package com.residuosolido.app.dto;

import com.residuosolido.app.model.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class RegistrationFormTest {

    @Test
    void toUser_mapsFormFieldsAndResolvesPhone() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("nuevo");
        form.setPassword("1234");
        form.setCountryCode("+598");
        form.setPhoneNational("99123456");
        form.setDdd("");

        User user = form.toUser();

        assertEquals("nuevo", user.getUsername());
        assertEquals("1234", user.getPassword());
        assertNotNull(user.getPhone());
        assertTrue(user.getPhone().startsWith("+598"));
    }

    @Test
    void toUser_withoutSelector_keepsNullPhone() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("nuevo");
        form.setPassword("1234");

        User user = form.toUser();

        assertNull(user.getPhone());
    }

    @Test
    void getters_returnSettersValues() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("u");
        form.setCountryCode("+55");
        form.setPhoneNational("99123");
        form.setDdd("55");
        form.setPassword("pw");

        assertEquals("u", form.getUsername());
        assertEquals("+55", form.getCountryCode());
        assertEquals("99123", form.getPhoneNational());
        assertEquals("55", form.getDdd());
        assertEquals("pw", form.getPassword());
    }
}
