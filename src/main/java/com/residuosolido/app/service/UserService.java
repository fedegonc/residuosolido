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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
    @PersistenceContext
    private EntityManager entityManager;
    
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


    @SuppressWarnings("deprecation")
    private void applyLegacyLocationFields(UserForm userForm, User user) {
        if (userForm.getAddress() != null) {
            user.setAddress(userForm.getAddress());
        }
        if (userForm.getLatitude() != null) {
            user.setLatitude(BigDecimal.valueOf(userForm.getLatitude()));
        }
        if (userForm.getLongitude() != null) {
            user.setLongitude(BigDecimal.valueOf(userForm.getLongitude()));
        }
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
     * Busca un usuario por su ID cargando también sus materiales (JOIN FETCH)
     * Evita LazyInitializationException al acceder a la colección materials
     * @param id ID del usuario
     * @return Optional con el usuario y sus materiales cargados
     */
    public Optional<User> findByIdWithMaterials(Long id) {
        return userRepository.findByIdWithMaterials(id);
    }

    /**
     * Busca usuarios por texto (username, email, firstName, lastName) de forma paginada
     */
    public Page<User> search(String q, Pageable pageable) {
        return userRepository.search(q, pageable);
    }

    
    
    /**
     * Encuentra un usuario autenticado por username y carga sus materiales en una sola query.
     * Optimizado para evitar N+1 queries.
     * 
     * @param username Nombre de usuario
     * @return Usuario encontrado con toda la información cargada
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public User findAuthenticatedUserByUsername(String username) {
        // Usar método optimizado que carga usuario + materiales en una sola query
        User user = userRepository.findByUsernameWithMaterials(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
        
        // Las propiedades básicas ya están cargadas, solo forzar inicialización de lazy simples
        user.getUsername();
        user.getFirstName();
        user.getLastName();
        user.getEmail();
        user.getRole();
        user.getProfileCompleted();
        user.getPreferredLanguage();
        user.getAddress();
        user.getPhone();
        user.getProfileImage();
        
        // Materials ya están cargados por el JOIN FETCH, no necesita .size()
        
        // Si es una organización, inicializar propiedades específicas
        if (user.getRole() == Role.ORGANIZATION) {
            user.getLatitude();
            user.getLongitude();
            user.getAddressReferences();
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
                "feedbacks",
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
    
    /**
     * Guarda un usuario directamente sin usar BeanUtils.copyProperties
     * Útil cuando el objeto ya está managed por Hibernate y solo queremos persistir cambios
     * @param user Usuario a guardar
     * @return Usuario guardado
     */
    @Transactional
    public User saveDirectly(User user) {
        User saved = userRepository.save(user);
        // Forzar flush para asegurar que se persiste inmediatamente
        userRepository.flush();
        return saved;
    }
    
    /**
     * Marca el perfil de un usuario como completado utilizando la consulta nativa del repositorio.
     * @param userId identificador del usuario a actualizar
     * @throws IllegalArgumentException si no se encuentra el usuario
     */
    @Transactional
    public void markProfileAsCompleted(Long userId) {
        int updatedRows = userRepository.markProfileAsCompleted(userId);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
    }

    /**
     * Marca el perfil de una organización como completado usando query nativa
     * @return Usuario guardado
     * @throws IllegalArgumentException si hay errores de validación
     */
    public User saveUser(UserForm userForm) {

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
        
        applyLegacyLocationFields(userForm, user);
        
        if (user.getId() == null) {
            // Usuario nuevo
            return createUser(user, userForm.getPassword());
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
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }
    
    
    /**
     * Desactiva un usuario por su ID (soft delete)
     * En lugar de eliminar físicamente el usuario, lo marca como inactivo
     * para preservar la integridad referencial con solicitudes y otros datos
     * @param id ID del usuario a desactivar
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        // Soft delete: marcar como inactivo en lugar de eliminar
        user.setActive(false);
        userRepository.save(user);
    }
    
    /**
     * Elimina permanentemente un usuario y TODAS sus solicitudes asociadas (hard delete)
     * ⚠️ ADVERTENCIA: Esta acción es IRREVERSIBLE y eliminará:
     * - El usuario
     * - Todas sus solicitudes (requests)
     * - Todos sus feedbacks
     * - Todas las relaciones con materiales
     * 
     * Usar solo cuando estés 100% seguro de que quieres eliminar todo el historial del usuario
     * 
     * @param id ID del usuario a eliminar permanentemente
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional
    public void hardDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        // Hard delete: eliminar físicamente el usuario y todas sus relaciones
        // Debido a las restricciones de foreign key, debemos eliminar en orden:
        
        logger.info("Iniciando eliminación permanente del usuario ID: {}", id);
        
        // 1. Eliminar relaciones request_materials (tabla intermedia)
        // Incluir solicitudes donde el usuario es creador O organización asignada
        int deletedRequestMaterials = entityManager.createNativeQuery(
                "DELETE FROM request_materials WHERE request_id IN " +
                "(SELECT id FROM requests WHERE user_id = :userId OR organization_id = :userId)")
                .setParameter("userId", id)
                .executeUpdate();
        logger.info("Eliminados {} registros de request_materials", deletedRequestMaterials);
        
        // 2. Eliminar solicitudes donde el usuario es creador O organización asignada
        int deletedRequests = entityManager.createNativeQuery(
                "DELETE FROM requests WHERE user_id = :userId OR organization_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        logger.info("Eliminadas {} solicitudes", deletedRequests);
        
        // 3. Eliminar relaciones con materiales del usuario
        int deletedUserMaterials = entityManager.createNativeQuery("DELETE FROM user_materials WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        logger.info("Eliminados {} registros de user_materials", deletedUserMaterials);
        
        // 4. Flush para asegurar que se ejecuten los DELETE
        entityManager.flush();
        
        // 5. Ahora sí podemos eliminar el usuario
        userRepository.delete(user);
        logger.info("Usuario ID: {} eliminado permanentemente", id);
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
        return createUser(user, tempPassword);
    }

    // Eliminado el @PostConstruct ensureDefaultAdmin para evitar conflicto con
    // StartupAdminInitializer. El administrador por defecto se crea únicamente
    // desde StartupAdminInitializer con credenciales consistentes.
}