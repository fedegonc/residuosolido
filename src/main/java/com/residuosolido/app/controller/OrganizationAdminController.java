package com.residuosolido.app.controller;

import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class OrganizationAdminController {

    @Autowired
    private UserService userService;

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
                        User user = userService.findById(id).orElse(null);
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
                                   RedirectAttributes redirectAttributes) {
        if ("delete".equals(action) && organization.getId() != null) {
            return deleteOrganization(organization.getId(), redirectAttributes);
        }
        try {
            organization.setRole(Role.ORGANIZATION); // Enforce role
            if (organization.getId() != null) {
                // Converting existing user to organization - don't change password
                userService.updateUser(organization, null);
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
    @GetMapping("/org/dashboard")
    public String orgDashboard(Model model) {
        model.addAttribute("totalOrganizations", 15);
        model.addAttribute("activeRequests", 8);
        model.addAttribute("managedMaterials", 6);
        model.addAttribute("usersServed", 42);
        return "org/dashboard";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/org/profile")
    public String orgProfile(Model model) {
        model.addAttribute("currentZone", "Barrio Centro (Rivera)");
        model.addAttribute("currentPhone", "098 123 456");
        return "org/profile";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/profile")
    public String updateOrgProfile(@RequestParam("zone") String zone,
                                  @RequestParam("phone") String phone,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Lógica de actualización
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil");
        }
        return "redirect:/org/profile";
    }
}
