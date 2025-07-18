package com.residuosolido.app.service;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio para validaciones relacionadas con usuarios
 */
@Service
public class UserValidationService {

    private final UserRepository userRepository;
    
    @Autowired
    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Optional con el usuario encontrado
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Busca un usuario por su nombre de usuario
     * @param username Nombre de usuario
     * @return Optional con el usuario encontrado
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     * @param username Nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    /**
     * Valida que el username y email sean únicos para un usuario
     * @param user Usuario a validar
     * @throws IllegalArgumentException si hay duplicados
     */
    public void validateUserUniqueness(User user) {
        if (user.getId() == null) {
            // Usuario nuevo - validar duplicados
            if (existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
            if (existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("El email ya existe");
            }
        } else {
            // Usuario existente - validar duplicados excluyendo el usuario actual
            findByUsername(user.getUsername()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new IllegalArgumentException("El nombre de usuario ya existe");
                }
            });
            
            findByEmail(user.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new IllegalArgumentException("El email ya existe");
                }
            });
        }
    }
    
    /**
     * Valida que la contraseña cumpla con los requisitos mínimos
     * @param password Contraseña a validar
     * @throws IllegalArgumentException si la contraseña no cumple los requisitos
     */
    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }
    
    /**
     * Valida los datos de un formulario de usuario
     * @param userForm Formulario a validar
     * @throws IllegalArgumentException si hay errores de validación
     */
    public void validateUserForm(UserForm userForm) {
        // Validar campos obligatorios
        if (userForm.getUsername() == null || userForm.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        
        if (userForm.getEmail() == null || userForm.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        
        // Validar formato de email
        if (!userForm.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        
        // Validar contraseña para usuarios nuevos
        if (userForm.getId() == null) {
            if (userForm.getNewPassword() == null || userForm.getNewPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("La contraseña es requerida para usuarios nuevos");
            }
            validatePassword(userForm.getNewPassword());
        } else if (userForm.getNewPassword() != null && !userForm.getNewPassword().trim().isEmpty()) {
            // Validar contraseña solo si se proporciona para usuarios existentes
            validatePassword(userForm.getNewPassword());
        }
    }
}
