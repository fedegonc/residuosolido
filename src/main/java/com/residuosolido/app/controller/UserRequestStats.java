package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import com.residuosolido.app.service.notification.SystemNotificationService;
import com.residuosolido.app.model.notification.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserRequestStats {

    private static final Logger logger = LoggerFactory.getLogger(UserRequestStats.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private PostService postService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    // ========== HOME ==========
    // NOTA: Las rutas "/" y "/index" ahora son manejadas por AuthController
    // para redirigir automáticamente según el rol del usuario autenticado

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/inicio")
    public String userDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);

            // Cargar todas las solicitudes del usuario para estadísticas
            List<Request> allUserRequests = requestService.getRequestsByUser(currentUser);
            List<Request> recentRequests = allUserRequests.stream()
                    .sorted(Comparator.comparing(Request::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5)
                    .toList();

            // Calcular estadísticas reales
            long totalRequests = allUserRequests.size();
            long completedRequests = allUserRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                    .count();
            long pendingRequests = allUserRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .count();
            long inProgressRequests = allUserRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS || r.getStatus() == RequestStatus.ACCEPTED)
                    .count();

            // Próxima recolección programada (considerar pendientes/aceptadas/en progreso)
            Request nextRequest = allUserRequests.stream()
                    .filter(r -> r.getScheduledDate() != null)
                    .filter(r -> (r.getStatus() == RequestStatus.PENDING
                            || r.getStatus() == RequestStatus.ACCEPTED
                            || r.getStatus() == RequestStatus.IN_PROGRESS))
                    .filter(r -> !r.getScheduledDate().isBefore(LocalDate.now()))
                    .sorted(Comparator.comparing(Request::getScheduledDate))
                    .findFirst()
                    .orElse(null);

            // Posts educativos
            List<Post> educationalPosts = postService.findRecentPosts(3);
            model.addAttribute("educationalPosts", educationalPosts);

            model.addAttribute("user", currentUser);
            model.addAttribute("recentRequests", recentRequests);
            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("completedRequests", completedRequests);
            model.addAttribute("pendingRequests", pendingRequests);
            model.addAttribute("inProgressRequests", inProgressRequests);
            model.addAttribute("nextRequest", nextRequest);
            model.addAttribute("hasRequests", !allUserRequests.isEmpty());

            // Cuando no hay solicitudes, mostrar contenido educativo adicional
            if (recentRequests == null || recentRequests.isEmpty()) {
                model.addAttribute("recentPosts", educationalPosts);
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
            
            boolean requestUpdated = false;
            // Subir imagen si se proporcionó
            if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    request.setImageUrl(imageUrl);
                    requestUpdated = true;
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
                    requestUpdated = true;
                } catch (Exception e) {
                    logger.warn("Error al asignar organización: {}", e.getMessage());
                }
            }

            if (requestUpdated) {
                requestService.save(request);
            }

            // Notificar al usuario que la solicitud fue registrada
            try {
                systemNotificationService.sendCustomNotification(
                        currentUser,
                        "Solicitud registrada",
                        "Tu solicitud de recolección se registró correctamente y está pendiente de procesamiento.",
                        NotificationType.SUCCESS,
                        "/usuarios/solicitud/" + request.getId()
                );
            } catch (Exception e) {
                logger.warn("No se pudo enviar la notificación de registro al usuario {}: {}", currentUser.getId(), e.getMessage());
            }

            // Notificar a la organización asignada, si corresponde
            if (request.getOrganization() != null) {
                try {
                    systemNotificationService.notifyNewRequestToOrganization(request);
                } catch (Exception e) {
                    logger.warn("No se pudo notificar a la organización {} sobre la solicitud {}: {}",
                            request.getOrganization().getId(), request.getId(), e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud enviada exitosamente!");
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud. Inténtalo de nuevo.");
        }
        
        return "redirect:/usuarios/solicitudes";
    }
}
