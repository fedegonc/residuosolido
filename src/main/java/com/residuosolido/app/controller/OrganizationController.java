package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private MaterialService materialService;

    // ========== ORG DASHBOARD ==========

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/inicio")
    public String orgDashboard(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());

            List<String> acceptedMaterialIds = currentOrg.getMaterials() != null
                ? currentOrg.getMaterials().stream().map(Material::getId).toList()
                : new ArrayList<>();

            Map<RequestStatus, Long> pendingGrouped = requestService.countGroupedByOrganizationAndStatuses(
                currentOrg, List.of(RequestStatus.PENDING));
            long pendingCount = pendingGrouped.getOrDefault(RequestStatus.PENDING, 0L);

            List<Request> pendingRequestsList = requestService.getRequestsByOrganization(currentOrg).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .limit(5)
                    .toList();

            model.addAttribute("pendingRequests", pendingCount);
            model.addAttribute("pendingRequestsList", pendingRequestsList);

            Map<RequestStatus, Long> groupedCounts = requestService.countGroupedByOrganizationAndStatuses(
                currentOrg, List.of(RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED));
            long inProgressCount = groupedCounts.getOrDefault(RequestStatus.IN_PROGRESS, 0L);
            long completedCount = groupedCounts.getOrDefault(RequestStatus.COMPLETED, 0L);
            model.addAttribute("inProgressRequests", inProgressCount);
            model.addAttribute("completedRequests", completedCount);
            model.addAttribute("totalKgRecycled", 0);
            model.addAttribute("managedMaterials", acceptedMaterialIds.size());

        } catch (Exception e) {
            logger.error("Error en dashboard de organización: {}", e.getMessage(), e);
            model.addAttribute("pendingRequests", 0);
            model.addAttribute("inProgressRequests", 0);
            model.addAttribute("completedRequests", 0);
            model.addAttribute("totalKgRecycled", 0);
            model.addAttribute("managedMaterials", 0);
        }
        return "org/dashboard";
    }

    // ========== ORG PROFILE ==========

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/perfil")
    public String orgProfile(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            model.addAttribute("organization", currentOrg);
            model.addAttribute("availableMaterials", materialService.findAll());

            List<String> selectedMaterialIds = currentOrg.getMaterials() != null
                ? currentOrg.getMaterials().stream().map(Material::getId).toList()
                : new ArrayList<>();
            model.addAttribute("selectedMaterialIds", selectedMaterialIds);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar perfil");
            model.addAttribute("availableMaterials", new ArrayList<>());
            model.addAttribute("selectedMaterialIds", new ArrayList<>());
        }
        return "org/profile";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/perfil")
    public String updateOrgProfile(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String addressReferences,
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude,
            @RequestParam(required = false) List<String> materialIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());

            if (email != null && !email.trim().isEmpty()) currentOrg.setEmail(email.trim());
            if (firstName != null) currentOrg.setFirstName(firstName.trim());
            if (lastName != null) currentOrg.setLastName(lastName.trim());
            if (phone != null) currentOrg.setPhone(phone.trim());
            if (address != null) currentOrg.setAddress(address.trim());
            if (addressReferences != null) currentOrg.setAddressReferences(addressReferences.trim());
            if (latitude != null && !latitude.trim().isEmpty()) {
                try { currentOrg.setLatitude(new BigDecimal(latitude.trim())); } catch (NumberFormatException e) { }
            }
            if (longitude != null && !longitude.trim().isEmpty()) {
                try { currentOrg.setLongitude(new BigDecimal(longitude.trim())); } catch (NumberFormatException e) { }
            }

            if (materialIds != null && !materialIds.isEmpty()) {
                List<Material> selectedMaterials = new ArrayList<>();
                for (String materialId : materialIds) {
                    Material material = materialService.findById(materialId).orElse(null);
                    if (material != null && material.getActive()) selectedMaterials.add(material);
                }
                currentOrg.setMaterials(selectedMaterials);
            } else {
                currentOrg.setMaterials(new ArrayList<>());
            }

            userService.updateUser(currentOrg, null);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
        }
        return "redirect:/acopio/perfil";
    }

    // ========== ORG MATERIALS ==========

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/materials")
    public String orgMaterials(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            model.addAttribute("allMaterials", materialService.findAllActive());
            model.addAttribute("acceptedMaterials", currentOrg.getMaterials() != null
                ? currentOrg.getMaterials() : new ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar materiales: " + e.getMessage());
            model.addAttribute("allMaterials", new ArrayList<>());
            model.addAttribute("acceptedMaterials", new ArrayList<>());
        }
        return "org/materials";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/materials/update")
    public String updateOrgMaterials(@RequestParam(required = false) List<String> materialIds,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            if (materialIds != null && !materialIds.isEmpty()) {
                List<Material> selectedMaterials = new ArrayList<>();
                for (String materialId : materialIds) {
                    Material material = materialService.findById(materialId).orElse(null);
                    if (material != null && material.getActive()) selectedMaterials.add(material);
                }
                currentOrg.setMaterials(selectedMaterials);
                redirectAttributes.addFlashAttribute("successMessage",
                    "Materiales actualizados correctamente. Ahora aceptas " + selectedMaterials.size() + " tipos de materiales.");
            } else {
                currentOrg.setMaterials(new ArrayList<>());
                redirectAttributes.addFlashAttribute("successMessage",
                    "Lista de materiales actualizada. No aceptas ningún material en este momento.");
            }
            userService.updateUser(currentOrg, null);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar materiales: " + e.getMessage());
        }
        return "redirect:/acopio/materials";
    }

    // ========== ORG REQUESTS ==========

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/requests")
    public String orgRequests(@RequestParam(required = false) String status,
            Authentication authentication, Model model) {
        User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
        List<Request> allOrgRequests = requestService.getRequestsByOrganization(currentOrg);
        List<Request> requests;

        if (status != null && !status.trim().isEmpty()) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    requests = allOrgRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).toList();
                    model.addAttribute("statusFilter", "Pendientes");
                    break;
                case "IN_PROGRESS":
                    requests = allOrgRequests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).toList();
                    model.addAttribute("statusFilter", "En Proceso");
                    break;
                case "COMPLETED":
                    requests = allOrgRequests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).toList();
                    model.addAttribute("statusFilter", "Completadas");
                    break;
                default:
                    requests = allOrgRequests;
                    break;
            }
        } else {
            requests = allOrgRequests;
        }

        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", "list");
        model.addAttribute("currentStatus", status);
        return "org/requests";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/requests/{id}")
    public String orgRequestDetail(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Request> requestOpt = requestService.findById(id);
            if (requestOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Solicitud no encontrada");
                return "redirect:/acopio/requests";
            }
            model.addAttribute("request", requestOpt.get());
            model.addAttribute("viewType", "detail");
            return "org/requests";
        } catch (Exception e) {
            logger.error("Error al cargar solicitud {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar solicitud: " + e.getMessage());
            return "redirect:/acopio/requests";
        }
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/requests/accept/{id}")
    public String orgAcceptRequest(@PathVariable String id, Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
            Request request = requestService.findById(id).orElseThrow();
            request.setOrganization(organization);
            request.accept();
            requestService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud aceptada y en proceso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar solicitud: " + e.getMessage());
        }
        return "redirect:/acopio/inicio";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/requests/reject/{id}")
    public String orgRejectRequest(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Request request = requestService.findById(id).orElseThrow();
            request.reject();
            requestService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud rechazada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar solicitud: " + e.getMessage());
        }
        return "redirect:/acopio/requests";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/requests/complete/{id}")
    public String orgCompleteRequest(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Request request = requestService.findById(id).orElseThrow();
            request.complete();
            requestService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud completada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al completar solicitud: " + e.getMessage());
        }
        return "redirect:/acopio/inicio";
    }

    // ========== ORG ONBOARDING ==========

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/completar-perfil")
    public String showCompleteProfileForm(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());

            if (currentUser.getProfileCompleted() != null && currentUser.getProfileCompleted()) {
                redirectAttributes.addFlashAttribute("infoMessage", "Tu perfil ya está completado");
                return "redirect:/acopio/inicio";
            }

            List<Material> availableMaterials = materialService.findAllActive();
            model.addAttribute("organization", currentUser);
            model.addAttribute("availableMaterials", availableMaterials);

            return "org/complete-profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/completar-perfil")
    public String completeProfile(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) List<String> materialIds,
            @RequestParam(name = "preferredDays", required = false) List<String> preferredDays,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());

            String trimmedAddress = address != null ? address.trim() : null;
            String trimmedPhone = phone != null ? phone.trim() : null;
            List<String> safeMaterialIds = materialIds != null ? materialIds : List.of();
            List<String> safePreferredDays = preferredDays != null ? preferredDays : List.of();

            List<String> errors = new ArrayList<>();

            if (trimmedAddress == null || trimmedAddress.isEmpty()) {
                errors.add("La dirección es obligatoria");
            } else if (trimmedAddress.length() < 10) {
                errors.add("La dirección debe tener al menos 10 caracteres");
            }

            if (trimmedPhone == null || trimmedPhone.isEmpty()) {
                errors.add("El teléfono es obligatorio");
            } else if (trimmedPhone.length() < 8) {
                errors.add("El teléfono debe tener al menos 8 dígitos");
            }

            if (safeMaterialIds.isEmpty()) {
                errors.add("Debes seleccionar al menos un material que aceptas");
            }

            if (latitude == null || longitude == null) {
                errors.add("Debes seleccionar una ubicación en el mapa");
            }

            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", String.join(". ", errors));
                return "redirect:/acopio/completar-perfil";
            }

            currentUser.setAddress(trimmedAddress);
            currentUser.setPhone(trimmedPhone);
            currentUser.setLatitude(latitude);
            currentUser.setLongitude(longitude);
            if (!safePreferredDays.isEmpty()) {
                currentUser.setPreferredCollectionDays(String.join(",", safePreferredDays));
            } else {
                currentUser.setPreferredCollectionDays(null);
            }

            List<Material> selectedMaterials = new ArrayList<>();
            for (String materialId : safeMaterialIds) {
                Material material = materialService.findById(materialId).orElse(null);
                if (material != null && material.getActive()) {
                    selectedMaterials.add(material);
                }
            }
            currentUser.setMaterials(selectedMaterials);
            currentUser.setProfileCompleted(true);

            userService.save(currentUser);
            userService.markProfileAsCompleted(currentUser.getId());

            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, authentication);

            redirectAttributes.addFlashAttribute("successMessage",
                "¡Perfil completado exitosamente! Por favor, inicia sesión nuevamente para acceder al sistema.");

            return "redirect:/auth/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al completar perfil: " + e.getMessage());
            return "redirect:/acopio/completar-perfil";
        }
    }
}
