package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import com.residuosolido.app.service.notification.SystemNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    
    @Autowired
    private MaterialService materialService;
    
    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de solicitudes
     */
    private void prepareRequestModel(Model model, Page<Request> requestsPage, String viewType, String query) {
        model.addAttribute("requestsPage", requestsPage);
        model.addAttribute("requests", requestsPage.getContent());
        model.addAttribute("totalRequests", requestsPage.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("viewType", viewType);
    }

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
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
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
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Request> pageResult = requestService.searchAll(query, pageable);
        prepareRequestModel(model, pageResult, "list", query);
        model.addAttribute("pageSize", safeSize);
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
                
                // Organización seleccionada
                if (organizationId != null) {
                    try {
                        User org = userService.getUserOrThrow(organizationId);
                        request.setOrganization(org);
                    } catch (Exception ignored) { }
                }
                if (request.getStatus() == null) request.setStatus(RequestStatus.PENDING);
                Request savedRequest = requestService.save(request);

                // Notificar al usuario si la solicitud queda asignada a una organización
                if (savedRequest.getOrganization() != null) {
                    try {
                        systemNotificationService.notifyRequestAssigned(savedRequest);
                        systemNotificationService.notifyRequestStatusChange(savedRequest, RequestStatus.PENDING);
                    } catch (Exception ex) {
                        // Registrar pero no interrumpir el flujo del admin
                        org.slf4j.LoggerFactory.getLogger(RequestController.class)
                                .warn("No se pudo notificar la creación de solicitud {}: {}", savedRequest.getId(), ex.getMessage());
                    }
                }
                redirectAttributes.addFlashAttribute("successMessage", "Solicitud creada correctamente");
            } else {
                // Actualizar existente: preservar campos no editables
                Optional<Request> originalRequest = requestService.findById(request.getId());
                if (originalRequest.isPresent()) {
                    Request updatedRequest = originalRequest.get();
                    RequestStatus previousStatus = updatedRequest.getStatus();
                    User previousOrganization = updatedRequest.getOrganization();
                    
                    // Manejar imagen si se subió
                    if (requestImageFile != null && !requestImageFile.isEmpty() && cloudinaryService != null) {
                        String imageUrl = cloudinaryService.uploadFile(requestImageFile);
                        updatedRequest.setImageUrl(imageUrl);
                    }
                    
                    updatedRequest.setStatus(request.getStatus());
                    updatedRequest.setNotes(request.getNotes());
                    updatedRequest.setScheduledDate(request.getScheduledDate());
                    Request savedRequest = requestService.save(updatedRequest);

                    try {
                        if (previousOrganization == null && savedRequest.getOrganization() != null) {
                            systemNotificationService.notifyRequestAssigned(savedRequest);
                        }
                        if (previousStatus != savedRequest.getStatus()) {
                            systemNotificationService.notifyRequestStatusChange(savedRequest, previousStatus);
                        }
                    } catch (Exception ex) {
                        org.slf4j.LoggerFactory.getLogger(RequestController.class)
                                .warn("No se pudieron enviar notificaciones al actualizar solicitud {}: {}", savedRequest.getId(), ex.getMessage());
                    }
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
        // Cargar materiales activos desde la BD
        model.addAttribute("materials", materialService.findAllActive());
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
            
            // Construir notas con materiales seleccionados
            StringBuilder notesBuilder = new StringBuilder();
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                notesBuilder.append(request.getNotes()).append("\n");
            }
            
            // Guardar materiales seleccionados
            if (materialNames != null && !materialNames.isEmpty()) {
                String materialsStr = String.join(", ", materialNames);
                notesBuilder.append("Materiales: ").append(materialsStr);
            }
            
            request.setNotes(notesBuilder.toString().trim());
            
            // Asignar organización seleccionada
            if (organizationId != null) {
                try {
                    User org = userService.getUserOrThrow(organizationId);
                    request.setOrganization(org);
                } catch (Exception e) {
                    // Si hay error al obtener la organización, continuar sin ella
                }
            }
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
     * Muestra las solicitudes de la organización
     * - Sin parámetro 'status': muestra TODAS las solicitudes de la organización
     * - Con parámetro 'status=PENDING': muestra solo solicitudes PENDIENTES
     * - Con parámetro 'status=IN_PROGRESS': muestra solo solicitudes EN PROCESO (incluye ACCEPTED)
     * - Con parámetro 'status=COMPLETED': muestra solo solicitudes COMPLETADAS
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/requests")
    public String orgRequests(
            @RequestParam(required = false) String status,
            Authentication authentication, 
            Model model) {
        User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
        List<Request> requests;
        
        // Obtener todas las solicitudes de la organización
        List<Request> allOrgRequests = requestService.getRequestsByOrganization(currentOrg);
        
        // Filtrar por estado si se proporciona el parámetro
        if (status != null && !status.trim().isEmpty()) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    // Mostrar solo solicitudes pendientes
                    requests = allOrgRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.PENDING)
                        .toList();
                    model.addAttribute("statusFilter", "Pendientes");
                    break;
                    
                case "IN_PROGRESS":
                    // Mostrar solicitudes en proceso (incluye ACCEPTED e IN_PROGRESS)
                    requests = allOrgRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS || 
                                   r.getStatus() == RequestStatus.ACCEPTED)
                        .toList();
                    model.addAttribute("statusFilter", "En Proceso");
                    break;
                    
                case "COMPLETED":
                    // Mostrar solo solicitudes completadas
                    requests = allOrgRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                        .toList();
                    model.addAttribute("statusFilter", "Completadas");
                    break;
                    
                default:
                    // Si el estado no es válido, mostrar todas
                    requests = allOrgRequests;
                    break;
            }
        } else {
            // Sin filtro, mostrar todas las solicitudes
            requests = allOrgRequests;
        }
        
        prepareRequestModel(model, requests, "list");
        model.addAttribute("currentStatus", status);
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
            Request request = requestService.findById(id).orElseThrow();
            RequestStatus previousStatus = request.getStatus();
            User previousOrg = request.getOrganization();
            request.setOrganization(organization);
            request.setStatus(RequestStatus.IN_PROGRESS);
            Request savedRequest = requestService.save(request);

            try {
                if (previousOrg == null && savedRequest.getOrganization() != null) {
                    systemNotificationService.notifyRequestAssigned(savedRequest);
                }
                systemNotificationService.notifyRequestStatusChange(savedRequest, previousStatus);
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger(RequestController.class)
                        .warn("No se pudo notificar la aceptación de la solicitud {}: {}", id, ex.getMessage());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud aceptada y en proceso");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al aceptar solicitud");
        }
        return "redirect:/acopio/inicio";
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
            Request request = requestService.findById(id).orElseThrow();
            RequestStatus previousStatus = request.getStatus();
            request.setStatus(RequestStatus.REJECTED);
            Request savedRequest = requestService.save(request);

            try {
                systemNotificationService.notifyRequestStatusChange(savedRequest, previousStatus);
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger(RequestController.class)
                        .warn("No se pudo notificar el rechazo de la solicitud {}: {}", id, ex.getMessage());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud rechazada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al rechazar solicitud");
        }
        return "redirect:/acopio/requests";
    }

    /**
     * Completa una solicitud
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/requests/complete/{id}")
    public String orgCompleteRequest(
            @PathVariable Long id, 
            RedirectAttributes redirectAttributes) {
        try {
            Request request = requestService.findById(id).orElseThrow();
            RequestStatus previousStatus = request.getStatus();
            request.setStatus(RequestStatus.COMPLETED);
            Request savedRequest = requestService.save(request);

            try {
                systemNotificationService.notifyRequestStatusChange(savedRequest, previousStatus);
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger(RequestController.class)
                        .warn("No se pudo notificar la finalización de la solicitud {}: {}", id, ex.getMessage());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud completada exitosamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al completar solicitud");
        }
        return "redirect:/acopio/inicio";
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
        long startTime = System.currentTimeMillis();
        System.out.println("=== INICIO CARGA DETALLE SOLICITUD ID: " + id + " ===");
        
        try {
            long beforeQuery = System.currentTimeMillis();
            Optional<Request> requestOpt = requestService.findById(id);
            long afterQuery = System.currentTimeMillis();
            System.out.println("⏱️ Tiempo consulta DB: " + (afterQuery - beforeQuery) + "ms");
            
            if (requestOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Solicitud no encontrada");
                return "redirect:/acopio/requests";
            }
            
            long beforeModel = System.currentTimeMillis();
            Request request = requestOpt.get();
            
            // Log de acceso a propiedades lazy
            if (request.getUser() != null) {
                System.out.println("✓ Usuario cargado: " + request.getUser().getUsername());
            }
            if (request.getOrganization() != null) {
                System.out.println("✓ Organización cargada: " + request.getOrganization().getUsername());
            }
            if (request.getMaterials() != null) {
                System.out.println("✓ Materiales cargados: " + request.getMaterials().size() + " items");
            }
            
            model.addAttribute("request", request);
            model.addAttribute("viewType", "detail");
            long afterModel = System.currentTimeMillis();
            System.out.println("⏱️ Tiempo preparación modelo: " + (afterModel - beforeModel) + "ms");
            
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("⏱️ TIEMPO TOTAL: " + totalTime + "ms");
            System.out.println("=== FIN CARGA DETALLE SOLICITUD ===\n");
            
            return "org/requests";
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            System.err.println("❌ ERROR después de " + totalTime + "ms: " + e.getMessage());
            e.printStackTrace();
            handleRequestError(e, redirectAttributes, "Error al cargar solicitud");
            return "redirect:/acopio/requests";
        }
    }
}
