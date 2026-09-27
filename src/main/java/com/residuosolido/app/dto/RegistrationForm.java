package com.residuosolido.app.dto;

import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * El teléfono usa el mismo selector binacional (país + nacional + DDD) que
 * request-form.html/org-profile.html — no un input suelto. Mismo patrón,
 * mismo componente JS (PHONE_PREFIXES en app.js ya incluye "phone").
 *
 * Bean Validation solo cubre forma (username/PIN) — el teléfono es compuesto
 * (countryCode+phoneNational+ddd, resuelto recién en toUser()) y la unicidad
 * de username necesita el repositorio, ninguna de las dos se presta a una
 * anotación declarativa simple. Esas dos siguen validándose en
 * UserRegistrationService.validateUserRegistration() como antes — esto es
 * una capa adicional que falla más rápido para los casos simples, no un
 * reemplazo del validador de servicio. Los mensajes usan el código
 * ServerMessage tal cual (`message = "error.register.username_required"`)
 * en vez del mecanismo de interpolación de Bean Validation, para no depender
 * de si Spring Boot conecta su MessageInterpolator con JsonMessageSource —
 * el controller resuelve ese código con el MessageSource real, mismo camino
 * que usa Messages.msg() para todo lo demás.
 */
@Getter
@Setter
public class RegistrationForm {
    @NotBlank(message = "error.register.username_required")
    @Size(max = 64, message = "error.register.username_too_long")
    private String username;
    private String countryCode;
    private String phoneNational;
    private String ddd;
    @Pattern(regexp = "\\d{4}", message = "error.register.pin_invalid")
    private String password;

    public User toUser() {
        User user = new User();
        user.setUsername(username);
        user.setPhone(PhoneNumber.resolve(countryCode, phoneNational, ddd, null));
        user.setPassword(password);
        return user;
    }
}
