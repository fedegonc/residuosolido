package com.residuosolido.app.service;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
     * Busca usuarios por rol con eager loading para evitar LazyInitializationException
     * @param role Rol para filtrar
     * @return Lista de usuarios con el rol especificado
     */
    @Transactional(readOnly = true)
    public List<User> findByRole(com.residuosolido.app.model.Role role) {
        List<User> users = userRepository.findByRole(role);
        // Forzar la inicialización de propiedades lazy
        users.forEach(user -> {
            user.getUsername(); // Forzar carga
            user.getFirstName();
            user.getLastName();
            user.getEmail();
            user.getAddress();
        });
        return users;
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

    public long count() {
        return userRepository.count();
    }

    /**
     * Busca un usuario por su ID
     * @param id ID del usuario
     * @return Optional con el usuario encontrado
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Busca usuarios por texto (username, email, firstName, lastName) de forma paginada
     */
    public Page<User> search(String q, Pageable pageable) {
        return userRepository.search(q, pageable);
    }

    
    
    /**
     * Busca un usuario por su nombre de usuario autenticado
     * @param username Nombre de usuario
     * @return Usuario encontrado
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public User findAuthenticatedUserByUsername(String username) {
        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
        
        // Forzar inicialización de colecciones lazy para evitar LazyInitializationException
        if (user.getMaterials() != null) {
            user.getMaterials().size();
        }
        
        return user;
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
    @Transactional
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
        logger.info("[UserService] saveUser() | id={} | action={} | username={} | email={}",
                userForm.getId(), (userForm.getId() == null ? "CREATE" : "UPDATE"), userForm.getUsername(), userForm.getEmail());

        User user = new User();
        BeanUtils.copyProperties(userForm, user);
        
        // Copiar campos de ubicación geográfica (esquema español)
        if (userForm.getDireccion() != null) {
            user.setAddress(userForm.getDireccion());
        }
        if (userForm.getReferencias() != null) {
            user.setAddressReferences(userForm.getReferencias());
        }
        if (userForm.getLatitud() != null) {
            user.setLatitude(userForm.getLatitud());
        }
        if (userForm.getLongitud() != null) {
            user.setLongitude(userForm.getLongitud());
        }
        
        // Compatibilidad con campos legacy (deprecated)
        if (userForm.getAddress() != null) {
            user.setAddress(userForm.getAddress());
        }
        if (userForm.getLatitude() != null) {
            user.setLatitude(BigDecimal.valueOf(userForm.getLatitude()));
        }
        if (userForm.getLongitude() != null) {
            user.setLongitude(BigDecimal.valueOf(userForm.getLongitude()));
        }
        
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
     * Cuenta la cantidad de usuarios por rol
     * @param role Rol a contar
     * @return Cantidad de usuarios con ese rol
     */
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    /**
     * Crea un usuario demo con datos de ejemplo completos (incluyendo contraseña)
     * Útil para llenar formularios de demostración
     * @return Usuario demo con todos los campos llenos y valores únicos
     */
    public User createDemoUser() {
        User demo = new User();
        
        // Generar un timestamp para hacer el usuario único
        long timestamp = System.currentTimeMillis() % 10000;
        
        // Usar valores únicos para evitar duplicados en la base de datos
        demo.setUsername("user_demo" + timestamp);
        demo.setEmail("user_demo" + timestamp + "@example.com");
        demo.setFirstName("User");
        demo.setLastName("Demo");
        demo.setPreferredLanguage("es");
        demo.setActive(true);
        demo.setRole(Role.USER);
        demo.setAddress("Av. Principal 123, Rivera");
        demo.setAddressReferences("Puntos de referencia cercanos, indicaciones adicionales");
        
        // Coordenadas de ejemplo para Rivera, Uruguay
        demo.setLatitude(new BigDecimal("-34.3055"));
        demo.setLongitude(new BigDecimal("-56.1850"));
        
        // Imagen de perfil de ejemplo
        demo.setProfileImage("https://example.com/profile.jpg");
        
        // Establecer contraseña predeterminada para el formulario
        demo.setPassword("demo123456");
        
        logger.info("Creando usuario demo con username={} y email={}", demo.getUsername(), demo.getEmail());
        
        return demo;
    }
    
    /**
     * Crea un usuario rápidamente con contraseña generada automáticamente
     * @param user Usuario base con los datos
     * @return Usuario creado con contraseña generada
     */
    public User createQuickUser(User user) {
        // Generar contraseña temporal
        String tempPassword = "temp123456";
        logger.info("[UserService] Creating quick user with temporary password");
        return createUser(user, tempPassword);
    }

    // Eliminado el @PostConstruct ensureDefaultAdmin para evitar conflicto con
    // StartupAdminInitializer. El administrador por defecto se crea únicamente
    // desde StartupAdminInitializer con credenciales consistentes.
}