package com.residuosolido.app.dto;

import com.residuosolido.app.model.Role;
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
    private String preferredLanguage;
    private boolean active;
    private String profileImage;
    private List<Material> materials = new ArrayList<>();
    private List<Long> materialIds = new ArrayList<>();
    
    // Campos de ubicación geográfica
    private String direccion; // Dirección completa en texto
    private java.math.BigDecimal latitud; // Coordenada latitud con precisión 10,8
    private java.math.BigDecimal longitud; // Coordenada longitud con precisión 11,8
    private String referencias; // Referencias adicionales de ubicación
    
    // Campos legacy para compatibilidad (deprecated)
    @Deprecated
    private Double latitude;
    @Deprecated
    private Double longitude;
    @Deprecated
    private String address;
    
    // Campos específicos del formulario
    private String password;
    private String newPassword;
    private String confirmPassword;
    // Admin: forzar cambio de contraseña (hace obligatoria la nueva contraseña)
    private boolean forcePasswordChange;
    
}
