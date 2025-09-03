package com.residuosolido.app.service;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para operaciones CRUD y de negocio con la entidad User
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
     * Obtiene usuarios paginados
     * @param pageable información de paginación
     * @return página de usuarios
     */
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Busca usuarios por texto (username, email, firstName, lastName) de forma paginada
     */
    public Page<User> search(String q, Pageable pageable) {
        return userRepository.search(q, pageable);
    }

    public Page<User> searchUsers(String search, Pageable pageable) {
        return userRepository.search(search, pageable);
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
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage("es");
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Actualiza un usuario preservando campos críticos
     * @param user Usuario con los nuevos datos
     * @param newPassword Nueva contraseña (opcional)
     * @return Usuario actualizado
     */
    public User updateUser(User user, String newPassword) {
        logger.info("[UserService] updateUser() called | id={}", user.getId());
        Optional<User> existingUserOpt = userRepository.findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Copiar propiedades preservando campos críticos
        logger.debug("[UserService] Before copy | username={}, email={}, firstName={}, lastName={}, role={}, active={}, lang={}",
                existingUser.getUsername(), existingUser.getEmail(), existingUser.getFirstName(), existingUser.getLastName(), existingUser.getRole(), existingUser.isActive(), existingUser.getPreferredLanguage());
        BeanUtils.copyProperties(user, existingUser, "id", "password", "createdAt", "lastAccessAt");
        logger.debug("[UserService] After copy  | username={}, email={}, firstName={}, lastName={}, role={}, active={}, lang={}",
                existingUser.getUsername(), existingUser.getEmail(), existingUser.getFirstName(), existingUser.getLastName(), existingUser.getRole(), existingUser.isActive(), existingUser.getPreferredLanguage());
        
        // Actualizar contraseña solo si se proporciona
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            existingUser.setPassword(passwordEncoder.encode(newPassword));
            logger.info("[UserService] Password updated for user id={}", existingUser.getId());
        }
        
        User saved = userRepository.save(existingUser);
        logger.info("[UserService] User updated and saved | id={}", saved.getId());
        return saved;
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
        validateUserForm(userForm);
        
        logger.info("[UserService] saveUser() | id={} | action={} | username={} | email={}",
                userForm.getId(), (userForm.getId() == null ? "CREATE" : "UPDATE"), userForm.getUsername(), userForm.getEmail());

        User user = new User();
        BeanUtils.copyProperties(userForm, user);
        
        // Copiar campos de ubicación geográfica
        copyLocationFields(userForm, user);
        
        if (user.getId() == null) {
            // Usuario nuevo
            logger.info("[UserService] Branch CREATE - encoding initial password and saving user");
            return createUser(user, userForm.getPassword());
        } else {
            // Usuario existente
            logger.info("[UserService] Branch UPDATE - applying field changes{}", (userForm.getNewPassword()!=null && !userForm.getNewPassword().trim().isEmpty())? " with password change":"");
            return updateUser(user, userForm.getNewPassword());
        }
    }
    
    private void validateUserForm(UserForm form) {
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
            if (pw.length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
        } else {
            // Edición: contraseña opcional
            String newPassword = form.getNewPassword();
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (newPassword.length() < 6) {
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
    }
    
    /**
     * Copia campos de ubicación del formulario a la entidad
     * @param userForm Formulario origen
     * @param user Entidad destino
     */
    private void copyLocationFields(UserForm userForm, User user) {
        logger.info("Copiando campos de ubicación:");
        logger.info("Dirección: {}", userForm.getDireccion());
        logger.info("Latitud: {}", userForm.getLatitud());
        logger.info("Longitud: {}", userForm.getLongitud());
        logger.info("Referencias: {}", userForm.getReferencias());
        logger.info("Legacy - Latitude: {}", userForm.getLatitude());
        logger.info("Legacy - Longitude: {}", userForm.getLongitude());
        
        user.setDireccion(userForm.getDireccion());
        user.setLatitud(userForm.getLatitud());
        user.setLongitud(userForm.getLongitud());
        user.setReferencias(userForm.getReferencias());
        
        // Mantener compatibilidad con campos legacy
        if (userForm.getLatitud() != null) {
            user.setLatitude(userForm.getLatitud().doubleValue());
        }
        if (userForm.getLongitud() != null) {
            user.setLongitude(userForm.getLongitud().doubleValue());
        }
        if (userForm.getDireccion() != null) {
            user.setAddress(userForm.getDireccion());
        }
        
        logger.info("Campos copiados a la entidad User:");
        logger.info("User - Latitud: {}", user.getLatitud());
        logger.info("User - Longitud: {}", user.getLongitud());
        logger.info("User - Legacy Latitude: {}", user.getLatitude());
        logger.info("User - Legacy Longitude: {}", user.getLongitude());
    }
    
    /**
     * Actualiza solo la ubicación geográfica de un usuario
     * @param userId ID del usuario
     * @param direccion Dirección completa
     * @param latitud Coordenada latitud
     * @param longitud Coordenada longitud
     * @param referencias Referencias adicionales
     * @return Usuario actualizado
     */
    public User updateUserLocation(Long userId, String direccion, 
                                   java.math.BigDecimal latitud, 
                                   java.math.BigDecimal longitud, 
                                   String referencias) {
        User user = getUserOrThrow(userId);
        
        user.setDireccion(direccion);
        user.setLatitud(latitud);
        user.setLongitud(longitud);
        user.setReferencias(referencias);
        
        // Mantener compatibilidad con campos legacy
        if (latitud != null) {
            user.setLatitude(latitud.doubleValue());
        }
        if (longitud != null) {
            user.setLongitude(longitud.doubleValue());
        }
        if (direccion != null) {
            user.setAddress(direccion);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Copia campos de ubicación de la entidad al formulario
     * @param user Entidad origen
     * @param userForm Formulario destino
     */
    public void copyLocationToForm(User user, UserForm userForm) {
        userForm.setDireccion(user.getDireccion());
        userForm.setLatitud(user.getLatitud());
        userForm.setLongitud(user.getLongitud());
        userForm.setReferencias(user.getReferencias());
        
        // Mantener compatibilidad con campos legacy
        userForm.setLatitude(user.getLatitude());
        userForm.setLongitude(user.getLongitude());
        userForm.setAddress(user.getAddress());
    }
    
    /**
     * Busca usuarios por proximidad geográfica
     * @param latitud Coordenada latitud central
     * @param longitud Coordenada longitud central
     * @param radioKm Radio de búsqueda en kilómetros
     * @return Lista de usuarios en el área
     */
    public List<User> findUsersByLocation(java.math.BigDecimal latitud, 
                                          java.math.BigDecimal longitud, 
                                          double radioKm) {
        // Implementación básica - se puede mejorar con consultas geoespaciales
        return userRepository.findAll().stream()
            .filter(user -> user.getLatitud() != null && user.getLongitud() != null)
            .filter(user -> {
                double distance = calculateDistance(
                    latitud.doubleValue(), longitud.doubleValue(),
                    user.getLatitud().doubleValue(), user.getLongitud().doubleValue());
                return distance <= radioKm;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine
     * @param lat1 Latitud punto 1
     * @param lon1 Longitud punto 1
     * @param lat2 Latitud punto 2
     * @param lon2 Longitud punto 2
     * @return Distancia en kilómetros
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Obtiene un usuario por su ID o lanza una excepción si no existe
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }
    
    
    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
    
    /**
     * Busca un usuario por su ID y lo devuelve directamente
     * @param id ID del usuario
     * @return Usuario encontrado o null si no existe
     */
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * Cuenta la cantidad de usuarios por rol
     * @param role Rol a contar
     * @return Cantidad de usuarios con ese rol
     */
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @PostConstruct
public void ensureDefaultAdmin() {
    if (userRepository.count() == 0) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@admin.com");
        admin.setFirstName("Admin");
        admin.setLastName("Principal");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setPreferredLanguage("es");
        admin.setPassword(passwordEncoder.encode("12345"));
        userRepository.save(admin);
    }
}

}
