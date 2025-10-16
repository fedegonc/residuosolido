package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Request Controller - Controlador unificado para gestión de solicitudes
 * 
 * Estructura:
 * - /requests - Endpoint base
 * - /requests/{id} - Detalle de solicitud
 * - /requests/new - Crear nueva solicitud
 * 
 * Prefijos por rol:
 * - /admin/requests - Admin (gestión completa)
 * - /user/requests - User (crear y ver propias)
 * - /org/requests - Organization (ver pendientes, aceptar/rechazar)
 */
@Controller
public class RequestController {

    @Autowired
    private RequestService requestService;
    
    @Autowired
    private UserService userService;
    
    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de solicitudes
     */
    private void prepareRequestModel(Model model, List<Request> requests, String viewType) {
        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", viewType);
    }
    
    /**
     * Maneja errores comunes en operaciones de solicitudes
     */
    private void handleRequestError(Exception e, RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message + ": " + e.getMessage());
    }

    // ========== ADMIN ENDPOINTS ==========
    
    /**
     * Lista todas las solicitudes (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/requests")
    public String adminRequests(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long id,
            @RequestParam(value = "q", required = false) String query,
            Model model) {
        
        // Modo creación (nuevo)
        if ("new".equals(action)) {
            model.addAttribute("request", new Request());
            model.addAttribute("viewType", "form");
            // Lista de usuarios finales para seleccionar en el form de alta
            model.addAttribute("users", userService.findByRole(com.residuosolido.app.model.Role.USER));
            // Lista de organizaciones activas para seleccionar en el form de alta
            model.addAttribute("organizations", requestService.getActiveOrganizations());
            model.addAttribute("isCreate", true);
            return "admin/requests";
        }

        // Modo edición
        if ("edit".equals(action) && id != null) {
            Optional<Request> requestOpt = requestService.findById(id);
            if (requestOpt.isPresent()) {
                model.addAttribute("request", requestOpt.get());
                model.addAttribute("viewType", "form");
                model.addAttribute("isCreate", false);
                return "admin/requests";
            }
        }

        // Modo lista (por defecto)
        List<Request> allRequests = requestService.findAll();
        // Filtro de búsqueda (usuario, dirección, materiales, estado, fecha)
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            allRequests = allRequests.stream().filter(r -> {
                String username = (r.getUser() != null && r.getUser().getUsername() != null) ? r.getUser().getUsername().toLowerCase() : "";
                String address = r.getCollectionAddress() != null ? r.getCollectionAddress().toLowerCase() : "";
                String materials = r.getMaterialsAsString() != null ? r.getMaterialsAsString().toLowerCase() : "";
                String status = r.getStatus() != null ? r.getStatus().name().toLowerCase() : "";
                String created = r.getCreatedAt() != null ? r.getCreatedAt().toString().toLowerCase() : "";
                return username.contains(q) || address.contains(q) || materials.contains(q) || status.contains(q) || created.contains(q);
            }).toList();
        }
        prepareRequestModel(model, allRequests, "list");
        model.addAttribute("query", query);
        // Mostrar botón 'Nueva Solicitud' en la lista (misma ubicación que 'Alta de usuario')
        model.addAttribute("showCreateButton", true);
        return "admin/requests";
    }

    /**
     * Actualiza el estado de una solicitud (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests")
    public String adminUpdateRequest(
            @ModelAttribute Request request,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "organizationId", required = false) Long organizationId,
            @RequestParam(value = "requestImageFile", required = false) MultipartFile requestImageFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Crear nueva
            if (request.getId() == null) {
                // Validación: debe existir al menos una organización activa
                if (!requestService.hasActiveOrganizations()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "No es posible crear la solicitud: no hay organizaciones disponibles para realizar la recolección en este momento.");
                    return "redirect:/admin/requests?action=new";
                }
                if (userId == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Seleccione un usuario para crear la solicitud.");
                    return "redirect:/admin/requests?action=new";
                }
                User user = userService.getUserOrThrow(userId);
                request.setUser(user);
                
                // Manejar imagen si se subió
                if (requestImageFile != null && !requestImageFile.isEmpty() && cloudinaryService != null) {
                    String imageUrl = cloudinaryService.uploadFile(requestImageFile);
                    request.setImageUrl(imageUrl);
                }
                
                // Organización seleccionada (opcional mientras no exista relación en el modelo)
                if (organizationId != null) {
                    try {
                        User org = userService.getUserOrThrow(organizationId);
                        // Validación básica de rol
                        // Nota: Asignación real se realizará cuando exista relación en el modelo.
                        String orgName = org.getUsername() != null ? org.getUsername() : String.valueOf(org.getId());
                        String prefix = request.getNotes() != null ? request.getNotes() + "\n" : "";
                        request.setNotes(prefix + "Organización seleccionada: " + orgName);
                    } catch (Exception ignored) { }
                }
                if (request.getStatus() == null) request.setStatus(RequestStatus.PENDING);
                requestService.save(request);
                redirectAttributes.addFlashAttribute("successMessage", "Solicitud creada correctamente");
            } else {
                // Actualizar existente: preservar campos no editables
                Optional<Request> originalRequest = requestService.findById(request.getId());
                if (originalRequest.isPresent()) {
                    Request updatedRequest = originalRequest.get();
                    
                    // Manejar imagen si se subió
                    if (requestImageFile != null && !requestImageFile.isEmpty() && cloudinaryService != null) {
                        String imageUrl = cloudinaryService.uploadFile(requestImageFile);
                        updatedRequest.setImageUrl(imageUrl);
                    }
                    
                    updatedRequest.setStatus(request.getStatus());
                    updatedRequest.setNotes(request.getNotes());
                    updatedRequest.setScheduledDate(request.getScheduledDate());
                    requestService.save(updatedRequest);
                    redirectAttributes.addFlashAttribute("successMessage", "Solicitud actualizada correctamente");
                }
            }
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al actualizar solicitud");
        }
        return "redirect:/admin/requests";
    }

    // ========== HTMX FORM DEMO (ADMIN) ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/requests/form-demo")
    public String getRequestFormDemo(Model model) {
        Request demo = new Request();
        demo.setCollectionAddress("Av. Principal 123, Rivera");
        demo.setDescription("Residuos reciclables: plásticos y cartón.");
        demo.setScheduledDate(java.time.LocalDate.now().plusDays(3));
        demo.setStatus(RequestStatus.PENDING);
        demo.setNotes("Prioridad normal. Coordinar con organización disponible.");
        model.addAttribute("request", demo);
        // También se necesita la lista de usuarios para el select en modo crear
        model.addAttribute("users", userService.findByRole(com.residuosolido.app.model.Role.USER));
        // y la lista de organizaciones activas
        model.addAttribute("organizations", requestService.getActiveOrganizations());
        model.addAttribute("isCreate", true);
        return "admin/requests :: requestFormFields";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/form-demo")
    public String createRequestDemo(RedirectAttributes redirectAttributes) {
        try {
            // Usar el primer usuario ROLE.USER disponible para la demo
            java.util.List<User> users = userService.findByRole(com.residuosolido.app.model.Role.USER);
            if (users == null || users.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No hay usuarios disponibles para crear una solicitud demo.");
                return "redirect:/admin/requests";
            }
            User u = users.get(0);
            Request demo = new Request();
            demo.setUser(u);
            demo.setCollectionAddress("Av. Principal 123, Rivera");
            demo.setDescription("Residuos reciclables: plásticos y cartón.");
            demo.setScheduledDate(java.time.LocalDate.now().plusDays(3));
            demo.setStatus(RequestStatus.PENDING);
            demo.setNotes("Solicitud creada automáticamente para demo.");
            requestService.save(demo);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud demo creada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al crear solicitud demo");
        }
        return "redirect:/admin/requests";
    }

    /**
     * Actualiza estado específico de una solicitud (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/update-status/{id}")
    public String adminUpdateRequestStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            requestService.updateStatus(id, RequestStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al actualizar estado");
        }
        return "redirect:/admin/requests";
    }

    /**
     * Elimina una solicitud (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/delete/{id}")
    public String adminDeleteRequest(
            @PathVariable Long id, 
            RedirectAttributes redirectAttributes) {
        try {
            requestService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud eliminada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al eliminar solicitud");
        }
        return "redirect:/admin/requests";
    }

    // ========== USER ENDPOINTS ==========
    
    /**
     * Lista solicitudes del usuario actual
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests")
    public String userRequests(Authentication authentication, Model model) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            List<Request> userRequests = requestService.getRequestsByUser(currentUser);
            prepareRequestModel(model, userRequests, "list");
            return "users/requests";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar solicitudes: " + e.getMessage());
            return "users/requests";
        }
    }

    /**
     * Muestra formulario para nueva solicitud
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests/new")
    public String userNewRequestForm(Model model) {
        model.addAttribute("request", new Request());
        // Flags de organizaciones disponibles para la UI
        boolean noOrgs = !requestService.hasActiveOrganizations();
        model.addAttribute("noOrganizationsAvailable", noOrgs);
        if (!noOrgs) {
            model.addAttribute("availableOrganizations", requestService.getActiveOrganizationNames());
            model.addAttribute("organizations", requestService.getActiveOrganizations());
        }
        return "users/request-form";
    }

    /**
     * Crea una nueva solicitud
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/user/requests")
    public String userCreateRequest(
            @ModelAttribute Request request,
            @RequestParam(value = "materials", required = false) List<String> materialNames,
            @RequestParam(value = "organizationId", required = false) Long organizationId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Validación: debe existir al menos una organización activa
            if (!requestService.hasActiveOrganizations()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No es posible crear la solicitud: no hay organizaciones disponibles para realizar la recolección en este momento.");
                return "redirect:/user/requests/new";
            }

            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            request.setUser(currentUser);
            request.setStatus(RequestStatus.PENDING);
            
            // Construir notas con materiales y organización seleccionada
            StringBuilder notesBuilder = new StringBuilder();
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                notesBuilder.append(request.getNotes()).append("\n");
            }
            
            // Guardar materiales seleccionados
            if (materialNames != null && !materialNames.isEmpty()) {
                String materialsStr = String.join(", ", materialNames);
                notesBuilder.append("Materiales: ").append(materialsStr).append("\n");
            }
            
            // Guardar organización seleccionada
            if (organizationId != null) {
                try {
                    User org = userService.getUserOrThrow(organizationId);
                    String orgName = org.getUsername() != null ? org.getUsername() : String.valueOf(org.getId());
                    notesBuilder.append("Organización preferida: ").append(orgName);
                } catch (Exception e) {
                    // Si hay error al obtener la organización, continuar sin ella
                }
            }
            
            request.setNotes(notesBuilder.toString().trim());
            requestService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud creada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al crear solicitud");
            return "redirect:/user/requests/new";
        }
        return "redirect:/user/requests";
    }

    // ========== ORGANIZATION ENDPOINTS ==========
    
    /**
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/requests")
    public String orgRequests(Model model) {
        List<Request> pendingRequests = requestService.getPendingRequests();
        prepareRequestModel(model, pendingRequests, "list");
        return "org/requests";
    }

    /**
     * Acepta una solicitud
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/requests/accept/{id}")
    public String orgAcceptRequest(
            @PathVariable Long id, 
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Asignar la organización actual a la solicitud
            User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.approveRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud aceptada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al aceptar solicitud");
        }
        return "redirect:/org/requests";
    }

    /**
     * Rechaza una solicitud
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/requests/reject/{id}")
    public String orgRejectRequest(
            @PathVariable Long id, 
            RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud rechazada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al rechazar solicitud");
        }
        return "redirect:/org/requests";
    }

    /**
     * Muestra detalle de una solicitud (organización)
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/requests/{id}")
    public String orgRequestDetail(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Request> requestOpt = requestService.findById(id);
            if (requestOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Solicitud no encontrada");
                return "redirect:/acopio/requests";
            }
            
            Request request = requestOpt.get();
            model.addAttribute("request", request);
            model.addAttribute("viewType", "detail");
            return "org/requests";
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al cargar solicitud");
            return "redirect:/acopio/requests";
        }
    }
}
