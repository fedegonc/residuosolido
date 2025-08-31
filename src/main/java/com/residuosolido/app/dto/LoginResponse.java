package com.residuosolido.app.dto;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean active;
    private String preferredLanguage;

    public static LoginResponse error(String message) {
        LoginResponse resp = new LoginResponse();
        resp.setSuccess(false);
        resp.setMessage(message);
        return resp;
    }

    public static LoginResponse fromUser(User user) {
        LoginResponse resp = new LoginResponse();
        resp.setSuccess(true);
        resp.setMessage("OK");
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setRole(user.getRole());
        resp.setActive(user.isActive());
        resp.setPreferredLanguage(user.getPreferredLanguage());
        return resp;
    }
}
