package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    // ========== HOME ==========
    @GetMapping({"/", "/index"})
    public String home(Model model) {
        model.addAttribute("title", "Residuos Sólidos - Inicio");
        return "index";
    }

    // ========== ADMIN USER CRUD ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public String adminUsers(@RequestParam(required = false) String action,
                            @RequestParam(required = false) Long id,
                            Model model) {
        
        List<User> allUsers = userService.findAll();
        model.addAttribute("users", allUsers);
        model.addAttribute("totalUsers", allUsers != null ? allUsers.size() : 0);
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users")
    public String adminSaveUser(@RequestParam(required = false) String action,
                               @ModelAttribute User user, 
                               RedirectAttributes redirectAttributes) {
        
        if ("delete".equals(action) && user.getId() != null) {
            return adminDeleteUser(user.getId(), redirectAttributes);
        }
        try {
            if (user.getId() != null) {
                userService.updateUser(user, null);
                redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado");
            } else {
                userService.createUser(user, user.getPassword());
                redirectAttributes.addFlashAttribute("successMessage", "Usuario creado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
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
    @GetMapping("/users/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            List<Request> recentRequests = requestService.getRecentRequestsByUser(currentUser, 5);
            
            model.addAttribute("user", currentUser);
            model.addAttribute("recentRequests", recentRequests);
            model.addAttribute("totalRequests", recentRequests.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar dashboard de usuario: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al cargar el dashboard");
        }
        
        return "users/dashboard";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/profile")
    public String userProfile(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            model.addAttribute("user", currentUser);
        } catch (Exception e) {
            logger.error("Error al cargar perfil: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al cargar el perfil");
        }
        
        return "users/profile";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/users/profile")
    public String updateUserProfile(@ModelAttribute User user,
                                   @RequestParam(value = "profileImage", required = false) MultipartFile profileImageFile,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            currentUser.setEmail(user.getEmail());
            
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(profileImageFile);
                currentUser.setProfileImage(imageUrl);
            }
            
            userService.updateUser(currentUser, null);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al actualizar perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil");
        }
        
        return "redirect:/users/profile";
    }

    // ========== USER REQUESTS ==========
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/requests")
    public String userRequests(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            List<Request> userRequests = requestService.getRequestsByUser(currentUser);
            model.addAttribute("requests", userRequests);
            
        } catch (Exception e) {
            logger.error("Error al cargar solicitudes: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al cargar las solicitudes");
        }
        
        return "users/requests";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/users/requests/new")
    public String newUserRequest() {
        return "users/request-form";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/users/requests")
    public String createUserRequest(@RequestParam("description") String description,
                                   @RequestParam("address") String address,
                                   @RequestParam("materials") String materials,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            requestService.createRequest(currentUser, description, materials, address);
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud enviada exitosamente!");
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud. Inténtalo de nuevo.");
        }
        
        return "redirect:/users/requests";
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
