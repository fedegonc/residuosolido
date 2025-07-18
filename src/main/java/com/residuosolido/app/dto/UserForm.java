package com.residuosolido.app.dto;

import com.residuosolido.app.model.Role;
import lombok.Data;

/**
 * DTO para el formulario de usuario
 * Incluye campos adicionales que no pertenecen a la entidad User
 */
@Data
public class UserForm {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String preferredLanguage;
    private boolean active;
    
    // Campos específicos del formulario
    private String newPassword;
    private String confirmPassword;
}
