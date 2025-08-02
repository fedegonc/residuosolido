package com.residuosolido.app.service;

import com.residuosolido.app.model.User;
import com.residuosolido.app.dto.UserForm;


import org.springframework.stereotype.Service;

@Service
public class UserValidationService {
    
    public boolean isValidUser(String username, String email) {
        return username != null && !username.trim().isEmpty() 
            && email != null && !email.trim().isEmpty();
    }
    
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean validateUserUniqueness(User user) {
        // lógica real aquí
        return true;
    }

    public boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean validateUserForm(UserForm form) {
        return form != null && form.getUsername() != null && form.getEmail() != null;
    }
}
