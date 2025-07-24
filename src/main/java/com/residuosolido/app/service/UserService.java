package com.residuosolido.app.service;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.Role;
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
 * Servicio para operaciones CRUD y de negocio con la entidad User
 */
@Service
public class UserService extends GenericEntityService<User, Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService validationService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidationService validationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
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
     * Busca usuarios por rol
     * @param role Rol para filtrar
     * @return Lista de usuarios con el rol especificado
     */
    public List<User> findByRole(com.residuosolido.app.model.Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Obtiene todos los usuarios
     * @return Lista de todos los usuarios
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * Obtiene todos los usuarios, opcionalmente filtrados por rol
     * @param role Rol para filtrar (opcional)
     * @return Lista de usuarios
     */
    public List<User> getAllUsers(String roleName) {
        if (roleName != null && !roleName.isEmpty()) {
            try {
                com.residuosolido.app.model.Role roleEnum = com.residuosolido.app.model.Role.valueOf(roleName);
                return userRepository.findByRole(roleEnum);
            } catch (IllegalArgumentException e) {
                return userRepository.findAll();
            }
        } else {
            return userRepository.findAll();
        }
    }
    
    /**
     * Busca un usuario por su nombre de usuario autenticado
     * @param username Nombre de usuario
     * @return Usuario encontrado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User findAuthenticatedUserByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
    }
    
    
    /**
     * Crea un nuevo usuario con contraseña encriptada
     * @param user Usuario a crear
     * @param password Contraseña sin encriptar
     * @return Usuario creado
     */
    public User createUser(User user, String password) {
        validationService.validateUserUniqueness(user);
        
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
        validationService.validateUserUniqueness(user);
        
        Optional<User> existingUserOpt = findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Copiar propiedades preservando campos críticos
        BeanUtils.copyProperties(user, existingUser, "id", "password", "createdAt", "lastAccessAt");
        
        // Actualizar contraseña solo si se proporciona
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            validationService.validatePassword(newPassword);
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        
        return save(existingUser);
    }
    
    /**
     * Guarda un usuario a partir de un DTO UserForm
     * Maneja tanto la creación como la actualización
     * @param userForm DTO con los datos del formulario
     * @return Usuario guardado
     * @throws IllegalArgumentException si hay errores de validación
     */
    public User saveUser(UserForm userForm) {
        // Validar datos del formulario
        validationService.validateUserForm(userForm);
        
        User user = new User();
        BeanUtils.copyProperties(userForm, user);
        
        if (user.getId() == null) {
            // Usuario nuevo
            return createUser(user, userForm.getNewPassword());
        } else {
            // Usuario existente
            return updateUser(user, userForm.getNewPassword());
        }
    }
    
    /**
     * Obtiene un usuario por su ID o lanza una excepción si no existe
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User getUserOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }
    
    
    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void deleteUser(Long id) {
        if (!existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        deleteById(id);
    }
    
    /**
     * Busca un usuario por su ID y lo devuelve directamente
     * @param id ID del usuario
     * @return Usuario encontrado o null si no existe
     */
    public User findUserById(Long id) {
        return super.findById(id).orElse(null);
    }
    
    /**
     * Cuenta la cantidad de usuarios por rol
     * @param role Rol a contar
     * @return Cantidad de usuarios con ese rol
     */
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
