package com.residuosolido.app.service;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    
    @Autowired(required = false)
    private CloudinaryService cloudinaryService;
    
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
     * Busca usuarios por rol con eager loading para evitar LazyInitializationException
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
     * Busca un usuario por su ID
     * @param id ID del usuario
     * @return Optional con el usuario encontrado
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Determina si el perfil de una organización está completo en base a campos obligatorios.
     * Si el usuario no es ORGANIZATION, se considera completo por defecto.
     */
    public boolean isOrganizationProfileComplete(User user) {
        if (user == null) return false;
        if (user.getRole() != Role.ORGANIZATION) return true;

        boolean hasAddress = user.getAddress() != null && !user.getAddress().trim().isEmpty() && user.getAddress().trim().length() >= 10;
        boolean hasPhone = user.getPhone() != null && !user.getPhone().trim().isEmpty() && user.getPhone().trim().length() >= 8;
        boolean hasCoords = user.getLatitude() != null && user.getLongitude() != null;
        boolean hasMaterials = user.getMaterials() != null && !user.getMaterials().isEmpty();

        boolean complete = hasAddress && hasPhone && hasCoords && hasMaterials;
        logger.debug("Eval perfil ORG -> address={}, phone={}, coords={}, materials={} => complete={}", hasAddress, hasPhone, hasCoords, hasMaterials, complete);
        return complete;
    }

    /**
     * Carga el usuario por username, evalúa si su perfil está completo y sincroniza el flag profileCompleted si difiere.
     * Retorna el estado final (true si completo).
     */
    public boolean syncOrganizationProfileCompletion(String username) {
        User user = findAuthenticatedUserByUsername(username);
        boolean complete = isOrganizationProfileComplete(user);
        Boolean flag = user.getProfileCompleted();
        if (flag == null || flag.booleanValue() != complete) {
            logger.info("Sincronizando profileCompleted (antes: {}, ahora: {}) para usuario {}", flag, complete, username);
            user.setProfileCompleted(complete);
            userRepository.save(user);
        }
        return complete;
    }

    /**
     * Encuentra un usuario autenticado por username y carga sus materiales en una sola query.
     * Optimizado para evitar N+1 queries.
     * 
     * @param username Nombre de usuario
     * @return Usuario encontrado con toda la información cargada
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User findAuthenticatedUserByUsername(String username) {
        return userRepository.findByUsername(username)
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
        Optional<User> existingUserOpt = userRepository.findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        User existingUser = existingUserOpt.get();
        Boolean profileCompletedValue = user.getProfileCompleted();
        
        // Copiar propiedades preservando campos críticos
        BeanUtils.copyProperties(user, existingUser,
                "id",
                "password",
                "createdAt",
                "lastAccessAt",
                "requests",
                "materials");
        
        // Asegurar que profileCompleted se mantenga si fue seteado
        if (profileCompletedValue != null) {
            existingUser.setProfileCompleted(profileCompletedValue);
        }
        
        // Actualizar contraseña si se proporciona
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        
        User saved = userRepository.save(existingUser);
        return saved;
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Actualiza usuario con imagen de perfil
     * @param user Usuario a actualizar
     * @param imageFile Archivo de imagen (opcional)
     * @param newPassword Nueva contraseña (opcional)
     * @return Usuario actualizado
     */
    public User updateUserWithImage(User user, MultipartFile imageFile, String newPassword) {
        if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
            try {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                user.setProfileImage(imageUrl);
            } catch (Exception e) {
                logger.warn("Error al subir imagen de perfil: {}", e.getMessage());
            }
        }
        return updateUser(user, newPassword);
    }
    
    /**
     * Marca el perfil de un usuario como completado utilizando la consulta nativa del repositorio.
     * @param userId identificador del usuario a actualizar
     * @throws IllegalArgumentException si no se encuentra el usuario
     */
    public void markProfileAsCompleted(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
        user.setProfileCompleted(true);
        userRepository.save(user);
    }

    
    
    
    /**
     * Obtiene un usuario por su ID o lanza una excepción si no existe
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws IllegalArgumentException si el usuario no existe
     */
    public User getUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    public String validateUserRegistration(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "El nombre de usuario es obligatorio";
        }
        if (user.getUsername().matches(".*\\s+.*")) {
            return "El nombre de usuario no puede contener espacios";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "El nombre de usuario ya existe";
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "El email ya está registrado";
        }
        return null;
    }

    public User registerUser(User user) {
        return registerUser(user, false);
    }

    public User registerUser(User user, boolean isOrganization) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(isOrganization ? Role.ORGANIZATION : Role.USER);
        user.setActive(true);
        user.setPreferredLanguage("es");
        return userRepository.save(user);
    }
}