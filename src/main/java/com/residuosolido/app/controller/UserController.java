package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private PostService postService;

    @Autowired
    private MaterialService materialService;

    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    // ========== USER DASHBOARD & PROFILE ==========
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/inicio")
    public String userDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);

            List<Request> allUserRequests = requestService.getRequestsByUser(currentUser);
            List<Request> recentRequests = allUserRequests.stream()
                    .sorted(Comparator.comparing(Request::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5)
                    .toList();

            long totalRequests = allUserRequests.size();
            long completedRequests = allUserRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                    .count();
            long pendingRequests = allUserRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .count();
            long inProgressRequests = allUserRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS)
                    .count();

            Request nextRequest = allUserRequests.stream()
                    .filter(r -> r.getScheduledDate() != null)
                    .filter(r -> (r.getStatus() == RequestStatus.PENDING
                            || r.getStatus() == RequestStatus.IN_PROGRESS))
                    .filter(r -> !r.getScheduledDate().isBefore(LocalDate.now()))
                    .sorted(Comparator.comparing(Request::getScheduledDate))
                    .findFirst()
                    .orElse(null);

            List<Post> educationalPosts = postService.getRecentPosts(3);
            model.addAttribute("educationalPosts", educationalPosts);

            model.addAttribute("user", currentUser);
            model.addAttribute("recentRequests", recentRequests);
            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("completedRequests", completedRequests);
            model.addAttribute("pendingRequests", pendingRequests);
            model.addAttribute("inProgressRequests", inProgressRequests);
            model.addAttribute("nextRequest", nextRequest);
            model.addAttribute("hasRequests", !allUserRequests.isEmpty());

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
    public String userProfile(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            model.addAttribute("user", currentUser);
            model.addAttribute("userForm", currentUser);

            try {
                List<Request> userRequests = requestService.getRequestsByUser(currentUser);
                Map<String, Long> requestStats = new HashMap<>();
                requestStats.put("pending", userRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count());
                requestStats.put("inProgress", userRequests.stream().filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).count());
                requestStats.put("completed", userRequests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count());
                requestStats.put("total", (long) userRequests.size());
                model.addAttribute("requestStats", requestStats);
            } catch (Exception e) {
                logger.warn("Error al cargar estadísticas de solicitudes: {}", e.getMessage());
                Map<String, Long> requestStats = new HashMap<>();
                requestStats.put("pending", 0L);
                requestStats.put("inProgress", 0L);
                requestStats.put("completed", 0L);
                requestStats.put("total", 0L);
                model.addAttribute("requestStats", requestStats);
            }

            

        } catch (Exception e) {
            logger.error("Error al cargar perfil: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al cargar el perfil: " + e.getMessage());
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

            currentUser.setFirstName(userForm.getFirstName());
            currentUser.setLastName(userForm.getLastName());
            currentUser.setEmail(userForm.getEmail());
            currentUser.setPhone(userForm.getPhone());
            currentUser.setAddress(userForm.getAddress());

            userService.updateUserWithImage(currentUser, imageFile, null);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");

        } catch (Exception e) {
            logger.error("Error al actualizar perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }

        return "redirect:/usuarios/perfil";
    }

    // ========== USER REQUESTS CRUD ==========

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/solicitudes")
    public String userRequestsList(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            List<Request> requests = requestService.getRequestsByUser(currentUser);
            model.addAttribute("requests", requests);
            model.addAttribute("user", currentUser);
        } catch (Exception e) {
            logger.error("Error al cargar solicitudes: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al cargar las solicitudes");
        }
        return "users/requests";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/solicitud/{id}")
    public String requestDetail(@PathVariable String id, Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
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
    @GetMapping("/usuarios/solicitudes/nueva")
    public String newUserRequest(Model model) {
        model.addAttribute("request", new Request());
        model.addAttribute("isEdit", false);
        boolean noOrgs = !requestService.hasActiveOrganizations();
        model.addAttribute("noOrganizationsAvailable", noOrgs);
        if (!noOrgs) {
            model.addAttribute("availableOrganizations", requestService.getActiveOrganizations().stream()
                .map(u -> u.getUsername() != null ? u.getUsername() : (u.getFullName() != null ? u.getFullName() : "Organización"))
                .toList());
        }
        model.addAttribute("organizations", requestService.getActiveOrganizations());
        model.addAttribute("materials", materialService.findAllActive());
        return "users/request-form";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/usuarios/solicitudes")
    public String createUserRequest(@RequestParam("description") String description,
                                   @RequestParam("collectionAddress") String collectionAddress,
                                   @RequestParam(value = "materials", required = false) List<String> materials,
                                   @RequestParam(value = "organizationId", required = false) String organizationId,
                                   @RequestParam(value = "collectionLatitude", required = false) String collectionLatitude,
                                   @RequestParam(value = "collectionLongitude", required = false) String collectionLongitude,
                                   @RequestParam(value = "quantityKg", required = false) String quantityKg,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            Request request = requestService.createRequest(currentUser, description, "", collectionAddress);

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

            if (collectionLatitude != null && !collectionLatitude.trim().isEmpty()) {
                try { request.setCollectionLatitude(new java.math.BigDecimal(collectionLatitude.trim())); } catch (NumberFormatException e) { }
            }
            if (collectionLongitude != null && !collectionLongitude.trim().isEmpty()) {
                try { request.setCollectionLongitude(new java.math.BigDecimal(collectionLongitude.trim())); } catch (NumberFormatException e) { }
            }
            if (quantityKg != null && !quantityKg.trim().isEmpty()) {
                try { request.setQuantityKg(new java.math.BigDecimal(quantityKg.trim())); } catch (NumberFormatException e) { }
            }

            boolean requestUpdated = (request.getMaterials() != null && !request.getMaterials().isEmpty())
                || request.getCollectionLatitude() != null || request.getCollectionLongitude() != null
                || request.getQuantityKg() != null;

            if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    request.setImageUrl(imageUrl);
                    requestUpdated = true;
                } catch (Exception e) {
                    logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
                }
            }
            if (organizationId != null && !organizationId.trim().isEmpty()) {
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
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud enviada exitosamente!");
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud. Inténtalo de nuevo.");
        }
        return "redirect:/usuarios/solicitudes";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/usuarios/solicitud/{id}/editar")
    public String editRequestForm(@PathVariable String id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para editar esta solicitud");
            }
            model.addAttribute("request", request);
            model.addAttribute("isEdit", true);
            model.addAttribute("organizations", requestService.getActiveOrganizations());
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
    public String updateRequest(@PathVariable String id,
                               @RequestParam("description") String description,
                               @RequestParam("collectionAddress") String collectionAddress,
                               @RequestParam(value = "materials", required = false) List<String> materials,
                               @RequestParam(value = "organizationId", required = false) String organizationId,
                               @RequestParam(value = "collectionLatitude", required = false) String collectionLatitude,
                               @RequestParam(value = "collectionLongitude", required = false) String collectionLongitude,
                               @RequestParam(value = "quantityKg", required = false) String quantityKg,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para editar esta solicitud");
            }
            request.setDescription(description);
            request.setCollectionAddress(collectionAddress);
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
            if (collectionLatitude != null && !collectionLatitude.trim().isEmpty()) {
                try { request.setCollectionLatitude(new java.math.BigDecimal(collectionLatitude.trim())); } catch (NumberFormatException e) { }
            }
            if (collectionLongitude != null && !collectionLongitude.trim().isEmpty()) {
                try { request.setCollectionLongitude(new java.math.BigDecimal(collectionLongitude.trim())); } catch (NumberFormatException e) { }
            }
            if (quantityKg != null && !quantityKg.trim().isEmpty()) {
                try { request.setQuantityKg(new java.math.BigDecimal(quantityKg.trim())); } catch (NumberFormatException e) { }
            }
            if (organizationId != null && !organizationId.trim().isEmpty()) {
                User org = userService.getUserOrThrow(organizationId);
                request.setOrganization(org);
            }
            if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    request.setImageUrl(imageUrl);
                } catch (Exception e) {
                    logger.warn("Error al subir imagen: {}", e.getMessage());
                }
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
    @PostMapping("/usuarios/solicitud/{id}/eliminar")
    public String deleteRequest(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            Request request = requestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            if (!request.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("No tienes permiso para eliminar esta solicitud");
            }
            requestService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la solicitud: " + e.getMessage());
        }
        return "redirect:/usuarios/solicitudes";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/organizations/{orgId}/materials")
    @ResponseBody
    public ResponseEntity<List<Material>> getOrganizationMaterials(@PathVariable String orgId) {
        try {
            User organization = userService.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));
            List<Material> materials = organization.getMaterials();
            if (materials == null) {
                materials = new ArrayList<>();
            }
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            logger.error("Error al obtener materiales de organización {}: {}", orgId, e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}
