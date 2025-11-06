package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.Role;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrganizationAdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private MaterialService materialService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/organizations")
    public String organizations(@RequestParam(required = false) String action,
                                @RequestParam(required = false) Long id,
                                @RequestParam(required = false, name = "q") String query,
                                Model model) {
        // Lista base: solo organizaciones
        List<User> orgs = userService.findByRole(Role.ORGANIZATION);
        if (query != null && !query.trim().isEmpty()) {
            String qLower = query.trim().toLowerCase();
            orgs = orgs.stream()
                    .filter(u -> {
                        String username = u.getUsername() != null ? u.getUsername().toLowerCase() : "";
                        String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                        String fullName = u.getFullName() != null ? u.getFullName().toLowerCase() : "";
                        return username.contains(qLower) || email.contains(qLower) || fullName.contains(qLower);
                    })
                    .toList();
        }
        model.addAttribute("organizations", orgs);
        model.addAttribute("totalOrganizations", orgs != null ? orgs.size() : 0);
        model.addAttribute("query", query);
        
        // Obtener usuarios disponibles para asignar como organizaciones (solo usuarios regulares, no organizaciones ni admins)
        List<User> availableUsers = userService.findAll().stream()
                .filter(u -> u.getRole() == Role.USER) // Solo usuarios regulares
                .toList();
        model.addAttribute("availableUsers", availableUsers);

        if (action != null) {
            switch (action) {
                case "view":
                    if (id != null) {
                        User user = userService.findByIdWithMaterials(id).orElse(null);
                        if (user != null && user.getRole() == Role.ORGANIZATION) {
                            model.addAttribute("organization", user);
                            model.addAttribute("viewType", "view");
                            return "admin/organizations";
                        }
                    }
                    break;
                case "edit":
                    if (id != null) {
                        User user = userService.findById(id).orElse(null);
                        if (user != null && user.getRole() == Role.ORGANIZATION) {
                            model.addAttribute("organization", user);
                            model.addAttribute("isEdit", true);
                            model.addAttribute("viewType", "form");
                            return "admin/organizations";
                        }
                    }
                    break;
                case "new":
                    User newOrg = new User();
                    newOrg.setRole(Role.ORGANIZATION);
                    newOrg.setPreferredLanguage("es");
                    newOrg.setActive(true);
                    model.addAttribute("organization", newOrg);
                    model.addAttribute("isEdit", false);
                    model.addAttribute("viewType", "form");
                    return "admin/organizations";
            }
        }

        model.addAttribute("viewType", "list");
        return "admin/organizations";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/organizations")
    public String saveOrganization(@RequestParam(required = false) String action,
                                   @ModelAttribute("organization") User organization,
                                   @RequestParam(value = "newPassword", required = false) String newPassword,
                                   RedirectAttributes redirectAttributes) {
        if ("delete".equals(action) && organization.getId() != null) {
            return deleteOrganization(organization.getId(), redirectAttributes);
        }
        try {
            organization.setRole(Role.ORGANIZATION); // Enforce role
            if (organization.getId() != null) {
                // Converting existing user to organization - don't change password
                userService.updateUser(organization, newPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Usuario convertido a organización exitosamente");
            } else {
                // Creating new organization - use a default password if none provided
                String password = organization.getPassword();
                if (password == null || password.trim().isEmpty()) {
                    password = "temp123"; // Default temporary password
                }
                userService.createUser(organization, password);
                redirectAttributes.addFlashAttribute("successMessage", "Organización creada exitosamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/organizations";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/organizations/form-demo")
    public String getOrganizationFormDemo(Model model) {
        // Demo organization data
        User demo = new User();
        demo.setUsername("org_demo");
        demo.setEmail("org_demo@example.com");
        demo.setFirstName("Org");
        demo.setLastName("Demo");
        demo.setPreferredLanguage("es");
        demo.setActive(true);
        demo.setRole(Role.ORGANIZATION);
        demo.setAddress("Av. Principal 123, Rivera");
        demo.setAddressReferences("Frente a la plaza");

        model.addAttribute("organization", demo);
        // Provide available users list for the selector on 'new' flow
        List<User> availableUsers = userService.findAll().stream()
                .filter(u -> u.getRole() == Role.USER)
                .toList();
        model.addAttribute("availableUsers", availableUsers);
        // Important so the fragment can evaluate conditions like !isEdit
        model.addAttribute("isEdit", false);

        return "admin/organizations :: orgFormFields";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/organizations/delete/{id}")
    public String deleteOrganization(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Organización eliminada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/organizations";
    }
    // ========== ORG DASHBOARD & PROFILE ==========
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/inicio")
    public String orgDashboard(Authentication authentication, Model model,
                               @RequestParam(name = "debugAll", required = false, defaultValue = "false") boolean debugAll) {
        long startTime = System.currentTimeMillis();
        System.out.println("=== INICIO CARGA DASHBOARD ORGANIZACIÓN ===");
        int queryCount = 0;
        
        try {
            long beforeAuth = System.currentTimeMillis();
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            queryCount++; // Auth query
            System.out.println("⏱️ Tiempo autenticación: " + (System.currentTimeMillis() - beforeAuth) + "ms");
            
            // Obtener IDs de materiales que acepta esta organización
            List<Long> acceptedMaterialIds = currentOrg.getMaterials() != null 
                ? currentOrg.getMaterials().stream().map(Material::getId).toList()
                : new ArrayList<>();
            System.out.println("📦 Materiales aceptados por organización: " + acceptedMaterialIds.size() + " (" + acceptedMaterialIds + ")");
            
            // DEBUG: Ver cuántas solicitudes pendientes hay en total en el sistema
            long totalPendingInSystem = requestService.countByStatus(RequestStatus.PENDING);
            System.out.println("🔍 DEBUG: Total solicitudes PENDING en sistema: " + totalPendingInSystem);
            // Evitar consulta pesada en cada carga: sólo si se pide explícitamente
            if (debugAll) {
                requestService.logPendingRequestsDebug();
            }

            // PENDIENTES: Contar directamente sin traer listas
            long pendingCount;
            List<Request> pendingRequestsList = new ArrayList<>();
            
            if (acceptedMaterialIds.isEmpty()) {
                // Si no hay materiales configurados, contar todas las pendientes
                long countStart = System.currentTimeMillis();
                pendingCount = requestService.countByStatus(RequestStatus.PENDING);
                queryCount++;
                System.out.println("  ⏱️ Conteo pendientes: " + (System.currentTimeMillis() - countStart) + "ms");
                
                // Traer solo los primeros 5 para mostrar
                long listStart = System.currentTimeMillis();
                pendingRequestsList = requestService.getPendingRequests().stream().limit(5).toList();
                queryCount++;
                System.out.println("  ⏱️ Lista pendientes (top 5): " + (System.currentTimeMillis() - listStart) + "ms");
            } else {
                // Con materiales configurados, contar solo los que coinciden
                long countStart = System.currentTimeMillis();
                pendingCount = requestService.countByStatusAndMaterials(RequestStatus.PENDING, acceptedMaterialIds);
                queryCount++;
                System.out.println("  ⏱️ Conteo pendientes (filtrado): " + (System.currentTimeMillis() - countStart) + "ms");
                System.out.println("  📊 PENDIENTES encontradas: " + pendingCount);
                
                // Traer solo los primeros 5 filtrados directamente desde BD (OPTIMIZADO)
                long listStart = System.currentTimeMillis();
                pendingRequestsList = requestService.getTopPendingByMaterials(acceptedMaterialIds, 5);
                queryCount++;
                System.out.println("  ⏱️ Lista pendientes (filtrado, top 5): " + (System.currentTimeMillis() - listStart) + "ms");
                System.out.println("  📋 Lista cargada: " + pendingRequestsList.size() + " solicitudes");
            }
            
            model.addAttribute("pendingRequests", pendingCount);
            model.addAttribute("pendingRequestsList", pendingRequestsList);
            System.out.println("✅ PENDIENTES: " + pendingCount + " (mostrando " + pendingRequestsList.size() + " en lista)");
            
            // EN PROCESO + COMPLETADAS: Consolidar en una sola consulta agrupada
            long groupedStart = System.currentTimeMillis();
            java.util.Map<RequestStatus, Long> groupedCounts = requestService.countGroupedByOrganizationAndStatuses(
                currentOrg,
                java.util.List.of(RequestStatus.IN_PROGRESS, RequestStatus.ACCEPTED, RequestStatus.COMPLETED)
            );
            queryCount++;
            long groupedElapsed = System.currentTimeMillis() - groupedStart;
            long inProgressCount = groupedCounts.getOrDefault(RequestStatus.IN_PROGRESS, 0L)
                    + groupedCounts.getOrDefault(RequestStatus.ACCEPTED, 0L);
            long completedCount = groupedCounts.getOrDefault(RequestStatus.COMPLETED, 0L);
            System.out.println("  ⏱️ Conteo agrupado en proceso/completadas: " + groupedElapsed + "ms");
            System.out.println("✅ EN PROCESO: " + inProgressCount);
            System.out.println("✅ COMPLETADAS: " + completedCount);
            model.addAttribute("inProgressRequests", inProgressCount);
            model.addAttribute("completedRequests", completedCount);
            
            // Total de kg reciclados (placeholder - se puede implementar después)
            model.addAttribute("totalKgRecycled", 0);
            
            // Materiales aceptados por la organización
            model.addAttribute("managedMaterials", acceptedMaterialIds.size());
            
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("📈 RESUMEN ESTADOS → Pendientes: " + pendingCount
                    + " | En proceso: " + inProgressCount
                    + " | Completadas: " + completedCount);
            System.out.println("📊 TOTAL QUERIES: " + queryCount);
            System.out.println("⏱️ TIEMPO TOTAL DASHBOARD: " + totalTime + "ms");
            System.out.println("=== FIN CARGA DASHBOARD ORGANIZACIÓN ===\n");
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            System.err.println("❌ ERROR en dashboard después de " + totalTime + "ms: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("pendingRequests", 0);
            model.addAttribute("inProgressRequests", 0);
            model.addAttribute("completedRequests", 0);
            model.addAttribute("totalKgRecycled", 0);
            model.addAttribute("managedMaterials", 0);
        }
        return "org/dashboard";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/perfil")
    public String orgProfile(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            model.addAttribute("organization", currentOrg);
            
            // Cargar todos los materiales disponibles
            List<Material> availableMaterials = materialService.findAll();
            model.addAttribute("availableMaterials", availableMaterials);
            
            // IDs de materiales que la organización ya acepta
            List<Long> selectedMaterialIds = currentOrg.getMaterials() != null 
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
            @RequestParam(required = false) List<Long> materialIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Actualizar campos
            if (email != null && !email.trim().isEmpty()) {
                currentOrg.setEmail(email.trim());
            }
            if (firstName != null) {
                currentOrg.setFirstName(firstName.trim());
            }
            if (lastName != null) {
                currentOrg.setLastName(lastName.trim());
            }
            if (phone != null) {
                currentOrg.setPhone(phone.trim());
            }
            if (address != null) {
                currentOrg.setAddress(address.trim());
            }
            if (addressReferences != null) {
                currentOrg.setAddressReferences(addressReferences.trim());
            }
            if (latitude != null && !latitude.trim().isEmpty()) {
                try {
                    currentOrg.setLatitude(new java.math.BigDecimal(latitude.trim()));
                } catch (NumberFormatException e) {
                    // Ignorar si no es un número válido
                }
            }
            if (longitude != null && !longitude.trim().isEmpty()) {
                try {
                    currentOrg.setLongitude(new java.math.BigDecimal(longitude.trim()));
                } catch (NumberFormatException e) {
                    // Ignorar si no es un número válido
                }
            }
            
            // Actualizar materiales seleccionados
            if (materialIds != null && !materialIds.isEmpty()) {
                List<Material> selectedMaterials = new ArrayList<>();
                for (Long materialId : materialIds) {
                    Material material = materialService.findById(materialId).orElse(null);
                    if (material != null && material.getActive()) {
                        selectedMaterials.add(material);
                    }
                }
                currentOrg.setMaterials(selectedMaterials);
            } else {
                // Si no se seleccionó ningún material, limpiar la lista
                currentOrg.setMaterials(new ArrayList<>());
            }
            
            userService.updateUser(currentOrg, null);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
        }
        return "redirect:/acopio/perfil";
    }
    
    /**
     * Gestión de materiales que acepta la organización
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/materials")
    public String orgMaterials(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Obtener todos los materiales activos disponibles
            List<Material> allMaterials = materialService.findAllActive();
            model.addAttribute("allMaterials", allMaterials);
            
            // Obtener materiales que la organización ya acepta
            List<Material> acceptedMaterials = currentOrg.getMaterials() != null 
                ? currentOrg.getMaterials() 
                : new ArrayList<>();
            model.addAttribute("acceptedMaterials", acceptedMaterials);
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar materiales: " + e.getMessage());
            model.addAttribute("allMaterials", new ArrayList<>());
            model.addAttribute("acceptedMaterials", new ArrayList<>());
        }
        return "org/materials";
    }
    
    /**
     * Actualiza los materiales que acepta la organización
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/materials/update")
    public String updateOrgMaterials(
            @RequestParam(required = false) List<Long> materialIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Actualizar materiales seleccionados
            if (materialIds != null && !materialIds.isEmpty()) {
                List<Material> selectedMaterials = new ArrayList<>();
                for (Long materialId : materialIds) {
                    Material material = materialService.findById(materialId).orElse(null);
                    if (material != null && material.getActive()) {
                        selectedMaterials.add(material);
                    }
                }
                currentOrg.setMaterials(selectedMaterials);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Materiales actualizados correctamente. Ahora aceptas " + selectedMaterials.size() + " tipos de materiales.");
            } else {
                // Si no se seleccionó ningún material, limpiar la lista
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
    
    /**
     * Estadísticas de la organización
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/estadisticas")
    public String orgStatistics(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Obtener IDs de materiales que acepta esta organización
            List<Long> acceptedMaterialIds = currentOrg.getMaterials() != null 
                ? currentOrg.getMaterials().stream().map(Material::getId).toList()
                : new ArrayList<>();
            
            long totalRequests = requestService.countRequestsForMaterials(acceptedMaterialIds);
            Map<RequestStatus, Long> statusCounts = requestService.getRequestStatusCountsForMaterials(acceptedMaterialIds);

            long pending = statusCounts.getOrDefault(RequestStatus.PENDING, 0L);
            long accepted = statusCounts.getOrDefault(RequestStatus.ACCEPTED, 0L);
            long inProgress = statusCounts.getOrDefault(RequestStatus.IN_PROGRESS, 0L);
            long cancelled = statusCounts.getOrDefault(RequestStatus.REJECTED, 0L);
            long completed = statusCounts.getOrDefault(RequestStatus.COMPLETED, 0L);

            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("pendingRequests", pending);
            model.addAttribute("acceptedRequests", accepted);
            model.addAttribute("inProgressRequests", inProgress);
            model.addAttribute("cancelledRequests", cancelled);
            model.addAttribute("completedRequests", completed);
            
            // Materiales gestionados
            int materialsCount = acceptedMaterialIds.size();
            model.addAttribute("managedMaterials", materialsCount);
            
            // Total de materiales disponibles
            model.addAttribute("totalMaterials", materialService.findAllActive().size());
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar estadísticas: " + e.getMessage());
        }
        return "org/statistics";
    }
}
