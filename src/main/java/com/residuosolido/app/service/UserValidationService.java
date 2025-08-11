package com.residuosolido.app.service;

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


    public boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Validación integral del formulario de usuario
    public boolean validateUserForm(UserForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Formulario de usuario inválido");
        }

        // Crear nuevo usuario: contraseña obligatoria
        if (form.getId() == null) {
            String pw = form.getPassword();
            String confirm = form.getConfirmPassword();
            if (pw == null || pw.trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria");
            }
            if (confirm == null || confirm.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe confirmar la contraseña");
            }
            if (!pw.equals(confirm)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            if (!validatePassword(pw)) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
        } else {
            // Edición: el administrador puede escribir una nueva contraseña en un único campo
            String newPassword = form.getNewPassword();
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!validatePassword(newPassword)) {
                    throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
                }
            }
        }

        if (form.getUsername() == null || form.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (form.getFirstName() == null || form.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (form.getLastName() == null || form.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        
        if (form.getPreferredLanguage() == null || form.getPreferredLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("El idioma preferido es obligatorio");
        }
        // La imagen de perfil no es obligatoria; no lanzar excepción aquí.
        return true;
    }
}
