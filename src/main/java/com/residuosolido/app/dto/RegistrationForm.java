package com.residuosolido.app.dto;

import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;
import lombok.Getter;
import lombok.Setter;

/**
 * El teléfono usa el mismo selector binacional (país + nacional + DDD) que
 * request-form.html/org-profile.html — no un input suelto. Mismo patrón,
 * mismo componente JS (PHONE_PREFIXES en app.js ya incluye "phone").
 */
@Getter
@Setter
public class RegistrationForm {
    private String username;
    private String countryCode;
    private String phoneNational;
    private String ddd;
    private String password;

    public User toUser() {
        User user = new User();
        user.setUsername(username);
        user.setPhone(PhoneNumber.resolve(countryCode, phoneNational, ddd, null));
        user.setPassword(password);
        return user;
    }
}
