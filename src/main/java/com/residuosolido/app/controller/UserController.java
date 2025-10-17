package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PostService postService;

    // ========== HOME ==========
    // NOTA: Las rutas "/" y "/index" ahora son manejadas por AuthController
    // para redirigir automáticamente según el rol del usuario autenticado

    // ========== ADMIN USER CRUD ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public String adminUsers(@RequestParam(required = false) String action,
                            @RequestParam(required = false) Long id,
                            @RequestParam(required = false, name = "q") String query,
                            Model model) {
        
        List<User> allUsers = userService.findAll();
        if (query != null && !query.trim().isEmpty()) {
            String qLower = query.trim().toLowerCase();
            allUsers = allUsers.stream()
                    .filter(u -> {
                        String username = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                        String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                        String fullName = u.getFullName() != null ? u.getFullName().toLowerCase() : "";
                        return username.contains(qLower) || email.contains(qLower) || fullName.contains(qLower);
                    })
                    .toList();
        }
        model.addAttribute("users", allUsers);
        Map<Long, UserRequestStats> userRequestsStats = buildUserRequestStats(allUsers);
        model.addAttribute("userRequestsStats", userRequestsStats);
        model.addAttribute("totalUsers", allUsers != null ? allUsers.size() : 0);
        model.addAttribute("query", query);
        model.addAttribute("roles", Role.values());
        
        if (action != null) {
            switch (action) {
                case "view":
                    if (id != null) {
                        User user = userService.findById(id).orElse(null);
                        if (user != null) {
                            model.addAttribute("user", user);
                            model.addAttribute("viewType", "view");
                            return "admin/users";
                        }
                    }
                    break;
                    
                case "edit":
                    if (id != null) {
                        User user = userService.findById(id).orElse(null);
                        if (user != null) {
                            model.addAttribute("user", user);
                            model.addAttribute("isEdit", true);
                            model.addAttribute("viewType", "form");
                            return "admin/users";
                        }
                    }
                    break;
                    
                case "new":
                    User newUser = new User();
                    newUser.setRole(Role.USER);
                    newUser.setPreferredLanguage("es");
                    newUser.setActive(true);
                    model.addAttribute("user", newUser);
                    model.addAttribute("isEdit", false);
                    model.addAttribute("viewType", "form");
                    return "admin/users";
            }
        }
        
        model.addAttribute("viewType", "list");
        return "admin/users";
    }

    private Map<Long, UserRequestStats> buildUserRequestStats(List<User> users) {
        Map<Long, UserRequestStats> statsMap = new HashMap<>();
        if (users == null || users.isEmpty()) {
            return statsMap;
        }

        List<Long> userIds = users.stream()
                .map(User::getId)
                .filter(id -> id != null)
                .toList();

        Map<Long, Map<RequestStatus, Long>> rawStats = requestService.getRequestStatsByUserIds(userIds);
        rawStats.forEach((userId, statusCounts) -> {
            long assigned = statusCounts.getOrDefault(RequestStatus.ACCEPTED, 0L);
            long inProgress = statusCounts.getOrDefault(RequestStatus.PENDING, 0L);
            long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
            statsMap.put(userId, new UserRequestStats(assigned, inProgress, total));
        });

        return statsMap;
    }

    // (Rutas de organizaciones movidas a OrganizationAdminController)

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users")
    public String adminSaveUser(@RequestParam(required = false) String action,
                               @ModelAttribute User user,
                               @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                               RedirectAttributes redirectAttributes) {
        
        if ("delete".equals(action) && user.getId() != null) {
            return adminDeleteUser(user.getId(), redirectAttributes);
        }
        try {
            if (user.getId() != null) {
                // Manejar imagen de perfil si se subió
                if (profileImageFile != null && !profileImageFile.isEmpty() && cloudinaryService != null) {
                    String imageUrl = cloudinaryService.uploadFile(profileImageFile);
                    user.setProfileImage(imageUrl);
                }
                
                userService.updateUser(user, null);
                redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente");
            } else {
                // Verificar si ya existe un usuario con ese email o username
                if (userService.existsByEmail(user.getEmail())) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Error: Ya existe un usuario con el email " + user.getEmail() + ". Por favor utilice otro email.");
                    return "redirect:/admin/users?action=new";
                }
                
                if (userService.existsByUsername(user.getUsername())) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Error: Ya existe un usuario con el nombre de usuario " + user.getUsername() + ". Por favor utilice otro nombre de usuario.");
                    return "redirect:/admin/users?action=new";
                }
                
                userService.createUser(user, user.getPassword());
                redirectAttributes.addFlashAttribute("successMessage", "Usuario creado exitosamente");
            }
        } catch (Exception e) {
            logger.error("Error al guardar usuario: {}", e.getMessage(), e);
            String errorMsg = "Error al procesar la solicitud";
            
            // Mejorar el mensaje de error para hacerlo más amigable
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                if (e.getMessage().contains("email")) {
                    errorMsg = "Ya existe un usuario con ese email. Por favor utilice otro email.";
                } else if (e.getMessage().contains("username")) {
                    errorMsg = "Ya existe un usuario con ese nombre de usuario. Por favor utilice otro nombre de usuario.";
                } else {
                    errorMsg = "Ya existe un usuario con esos datos. Por favor verifique la información.";
                }
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/admin/users";
    }

    // (Operaciones POST de organizaciones movidas a OrganizationAdminController)

    // ========== HTMX ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/form-demo")
    public String getUserFormDemo(Model model, HttpServletResponse response) {
        // Disable caching for HTMX fragment fetches
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Usar el método del servicio que NO incluye contraseña por seguridad
        User demo = userService.createDemoUser();
        
        model.addAttribute("user", demo);
        model.addAttribute("roles", Role.values());
        // Importante: para que el fragmento pueda evaluar "!isEdit" sin error
        model.addAttribute("isEdit", false);
        return "admin/users :: userFormFields";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/create-quick")
    public String createQuickUser(RedirectAttributes redirectAttributes) {
        try {
            // Crear usuario demo con contraseña generada automáticamente
            User demoUser = userService.createDemoUser();
            
            // Verificar si ya existe un usuario con ese email o username (aunque debería ser único por el timestamp)
            if (userService.existsByEmail(demoUser.getEmail())) {
                // Intentar con otro timestamp
                demoUser.setEmail("user_demo" + System.currentTimeMillis() + "@example.com");
            }
            
            if (userService.existsByUsername(demoUser.getUsername())) {
                // Intentar con otro timestamp
                demoUser.setUsername("user_demo" + System.currentTimeMillis());
            }
            
            User createdUser = userService.createQuickUser(demoUser);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Usuario creado exitosamente. Username: " + createdUser.getUsername() + 
                ", Contraseña temporal: temp123456 (cambiar al primer login)");
        } catch (Exception e) {
            logger.error("Error al crear usuario rápido: {}", e.getMessage(), e);
            String errorMsg = "Error al crear usuario";
            
            // Mejorar el mensaje de error para hacerlo más amigable
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                errorMsg = "Error: Ya existe un usuario con esos datos. Intente nuevamente.";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        }
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/delete/{id}")
    public String adminDeleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ========== USER DASHBOARD & PROFILE ==========
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/inicio")
    public String userDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            // Cargar todas las solicitudes del usuario para estadísticas
            List<Request> allUserRequests = requestService.getRequestsByUser(currentUser);
            List<Request> recentRequests = allUserRequests.stream().limit(5).toList();
            
            // Calcular estadísticas reales
            long totalRequests = allUserRequests.size();
            long completedRequests = allUserRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();
            
            // Contar tipos únicos de materiales reciclados
            long materialTypes = allUserRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .flatMap(r -> r.getMaterials().stream())
                .map(m -> m.getId())
                .distinct()
                .count();
            
            // Solicitudes del mes actual
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            long monthRequests = allUserRequests.stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(monthStart))
                .count();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("recentRequests", recentRequests);
            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("completedRequests", completedRequests);
            model.addAttribute("materialTypes", materialTypes);
            model.addAttribute("monthRequests", monthRequests);
            
            // Si no hay solicitudes, cargar posts recientes para mostrar contenido educativo
            if (recentRequests == null || recentRequests.isEmpty()) {
                List<Post> recentPosts = postService.findRecentPosts(4);
                model.addAttribute("recentPosts", recentPosts);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar dashboard de usuario: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al cargar el dashboard");
        }
        
        return "users/dashboard";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/perfil")
    @Transactional(readOnly = true)
    public String userProfile(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            model.addAttribute("user", currentUser);
            model.addAttribute("userForm", currentUser);
            
            // Cargar estadísticas de solicitudes con manejo seguro
            try {
                List<Request> userRequests = requestService.getRequestsByUser(currentUser);
                Map<String, Long> requestStats = new HashMap<>();
                requestStats.put("pending", userRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count());
                requestStats.put("inProgress", userRequests.stream().filter(r -> r.getStatus() == RequestStatus.ACCEPTED).count());
                requestStats.put("completed", userRequests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count());
                requestStats.put("total", (long) userRequests.size());
                model.addAttribute("requestStats", requestStats);
            } catch (Exception e) {
                logger.warn("Error al cargar estadísticas de solicitudes: {}", e.getMessage());
                // Valores por defecto si falla
                Map<String, Long> requestStats = new HashMap<>();
                requestStats.put("pending", 0L);
                requestStats.put("inProgress", 0L);
                requestStats.put("completed", 0L);
                requestStats.put("total", 0L);
                model.addAttribute("requestStats", requestStats);
            }
            
            // Cargar contador de feedback con manejo seguro
            try {
                long feedbackCount = currentUser.getFeedbacks() != null ? currentUser.getFeedbacks().size() : 0;
                model.addAttribute("feedbackCount", feedbackCount);
            } catch (Exception e) {
                logger.warn("Error al cargar contador de feedback: {}", e.getMessage());
                model.addAttribute("feedbackCount", 0L);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar perfil: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al cargar el perfil: " + e.getMessage());
            // Asegurar que al menos el user esté disponible para evitar errores en la vista
            try {
                String username = authentication.getName();
                User currentUser = userService.findAuthenticatedUserByUsername(username);
                model.addAttribute("user", currentUser);
                model.addAttribute("userForm", currentUser);
            } catch (Exception ex) {
                logger.error("Error crítico al cargar usuario: {}", ex.getMessage(), ex);
            }
        }
        
        return "users/profile";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/users/profile")
    public String updateUserProfile(@ModelAttribute("userForm") User userForm,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            // Actualizar campos editables
            currentUser.setFirstName(userForm.getFirstName());
            currentUser.setLastName(userForm.getLastName());
            currentUser.setEmail(userForm.getEmail());
            currentUser.setPhone(userForm.getPhone());
            currentUser.setAddress(userForm.getAddress());
            
            // Subir imagen de perfil si se proporciona
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                currentUser.setProfileImage(imageUrl);
            }
            
            userService.updateUser(currentUser, null);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al actualizar perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }
        
        return "redirect:/usuarios/perfil";
    }

    // ========== USER REQUESTS ==========
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/solicitud/{id}")
    public String requestDetail(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            
            // Verificar que la solicitud pertenece al usuario actual
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para ver esta solicitud");
            }
            
            model.addAttribute("request", request);
            
        } catch (Exception e) {
            logger.error("Error al cargar detalle de solicitud: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al cargar la solicitud");
            return "redirect:/usuarios/solicitudes";
        }
        
        return "users/request-detail";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/usuarios/solicitud/{id}/eliminar")
    public String deleteRequest(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            
            // Verificar que la solicitud pertenece al usuario actual
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para eliminar esta solicitud");
            }
            
            // Eliminar la solicitud
            requestService.deleteById(id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud eliminada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la solicitud: " + e.getMessage());
        }
        
        return "redirect:/usuarios/solicitudes";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/solicitud/{id}/editar")
    public String editRequestForm(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            
            // Verificar que la solicitud pertenece al usuario actual
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para editar esta solicitud");
            }
            
            model.addAttribute("request", request);
            model.addAttribute("isEdit", true);
            
            // Organizaciones disponibles
            model.addAttribute("organizations", requestService.getActiveOrganizations());
            
            // Materiales activos
            model.addAttribute("materials", materialService.findAllActive());
            
            return "users/request-form";
            
        } catch (Exception e) {
            logger.error("Error al cargar formulario de edición: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar la solicitud");
            return "redirect:/usuarios/solicitudes";
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/usuarios/solicitud/{id}/editar")
    public String updateRequest(@PathVariable Long id,
                               @RequestParam("description") String description,
                               @RequestParam("collectionAddress") String collectionAddress,
                               @RequestParam(value = "materials", required = false) List<String> materials,
                               @RequestParam(value = "organizationId", required = false) Long organizationId,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            logger.info("Actualizando solicitud {} con organizationId: {}", id, organizationId);
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            
            // Verificar que la solicitud pertenece al usuario actual
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para editar esta solicitud");
            }
            
            // Actualizar campos
            request.setDescription(description);
            request.setCollectionAddress(collectionAddress);
            
            // Actualizar materiales si se proporcionaron
            if (materials != null && !materials.isEmpty()) {
                List<Material> materialList = new ArrayList<>();
                for (String materialName : materials) {
                    Material material = materialService.findByName(materialName);
                    if (material != null) {
                        materialList.add(material);
                    }
                }
                request.setMaterials(materialList);
            }
            
            // Actualizar organización si se proporcionó
            if (organizationId != null && organizationId > 0) {
                User org = userService.getUserOrThrow(organizationId);
                request.setOrganization(org);
            }
            
            // Actualizar imagen si se proporcionó
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFile);
                request.setImageUrl(imageUrl);
            }
            
            requestService.save(request);
            
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud actualizada exitosamente");
            return "redirect:/usuarios/solicitud/" + id;
            
        } catch (Exception e) {
            logger.error("Error al actualizar solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar la solicitud: " + e.getMessage());
            return "redirect:/usuarios/solicitud/" + id;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/solicitudes/nueva")
    public String newUserRequest(Model model) {
        // Modelo base de la solicitud
        model.addAttribute("request", new Request());
        model.addAttribute("isEdit", false);
        
        // Organizaciones disponibles (activa primero, fallback a todas)
        boolean noOrgs = !requestService.hasActiveOrganizations();
        model.addAttribute("noOrganizationsAvailable", noOrgs);
        if (!noOrgs) {
            model.addAttribute("availableOrganizations", requestService.getActiveOrganizationNames());
        }
        model.addAttribute("organizations", requestService.getActiveOrganizations());
        
        // Materiales activos desde la BD
        model.addAttribute("materials", materialService.findAllActive());
        
        return "users/request-form";
    }

    /**
     * API endpoint to get materials accepted by an organization
     */
    @GetMapping("/api/organizations/{orgId}/materials")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<List<Material>> getOrganizationMaterials(@PathVariable Long orgId) {
        try {
            User organization = userService.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));
            
            // Forzar inicialización de la colección de materiales (lazy loading)
            List<Material> materials = organization.getMaterials();
            if (materials == null) {
                materials = new ArrayList<>();
            }
            // Forzar la carga accediendo al tamaño
            materials.size();
            
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            logger.error("Error al obtener materiales de organización {}: {}", orgId, e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping({"/users/requests", "/usuarios/solicitudes"})
    public String createUserRequest(@RequestParam("description") String description,
                                   @RequestParam("collectionAddress") String collectionAddress,
                                   @RequestParam(value = "materials", required = false) List<String> materials,
                                   @RequestParam(value = "organizationId", required = false) Long organizationId,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            // Crear solicitud básica
            String materialsString = materials != null ? String.join(", ", materials) : "";
            Request request = requestService.createRequest(currentUser, description, materialsString, collectionAddress);
            
            // Subir imagen si se proporcionó
            if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    request.setImageUrl(imageUrl);
                    requestService.save(request);
                } catch (Exception e) {
                    logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
                    // Continuar sin imagen
                }
            }
            
            // Asignar organización si se seleccionó
            if (organizationId != null) {
                try {
                    User org = userService.getUserOrThrow(organizationId);
                    request.setOrganization(org);
                    requestService.save(request);
                } catch (Exception e) {
                    logger.warn("Error al asignar organización: {}", e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud enviada exitosamente!");
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud. Inténtalo de nuevo.");
        }
        
        return "redirect:/usuarios/solicitudes";
    }

    static class UserRequestStats {
        private final long assigned;
        private final long inProgress;
        private final long total;

        public UserRequestStats(long assigned, long inProgress, long total) {
            this.assigned = assigned;
            this.inProgress = inProgress;
            this.total = total;
        }

        public long getAssigned() {
            return assigned;
        }

        public long getInProgress() {
            return inProgress;
        }

        public long getTotal() {
            return total;
        }
    }
}
