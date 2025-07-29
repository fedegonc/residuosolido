package com.residuosolido.app.dto;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.UserType;
import com.residuosolido.app.model.Material;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

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
    private UserType userType;
    private String preferredLanguage;
    private boolean active;
    private String profileImage;
    private List<Material> materials = new ArrayList<>();
    private List<Long> materialIds = new ArrayList<>();
    
    // Campos de ubicación
    private Double latitude;
    private Double longitude;
    private String address;
    
    // Campos específicos del formulario
    private String password;
    private String newPassword;
    private String confirmPassword;
}
