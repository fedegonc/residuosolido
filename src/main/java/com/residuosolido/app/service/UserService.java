package com.residuosolido.app.service;

import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para operaciones con la entidad User
 */
@Service
public class UserService extends GenericEntityService<User, Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return userRepository;
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
            Optional<User> existingUserByUsername = findByUsername(user.getUsername());
            if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
            
            Optional<User> existingUserByEmail = findByEmail(user.getEmail());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("El email ya existe");
            }
        }
    }
    
    /**
     * Crea un nuevo usuario con contraseña encriptada
     * @param user Usuario a crear
     * @param password Contraseña sin encriptar
     * @return Usuario creado
     */
    public User createUser(User user, String password) {
        validateUserUniqueness(user);
        
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage("es");
        }
        
        return save(user);
    }
    
    /**
     * Actualiza un usuario preservando campos críticos
     * @param user Usuario con los nuevos datos
     * @param newPassword Nueva contraseña (opcional)
     * @return Usuario actualizado
     */
    public User updateUser(User user, String newPassword) {
        validateUserUniqueness(user);
        
        Optional<User> existingUserOpt = findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Copiar propiedades preservando campos críticos
        BeanUtils.copyProperties(user, existingUser, "id", "password", "createdAt", "lastAccessAt");
        
        // Actualizar contraseña solo si se proporciona
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        
        return save(existingUser);
    }
}
