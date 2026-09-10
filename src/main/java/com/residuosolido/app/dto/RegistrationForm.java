package com.residuosolido.app.dto;

import com.residuosolido.app.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {
    private String username;
    private String email;
    private String password;

    public User toUser() {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
