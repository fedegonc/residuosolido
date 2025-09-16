package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.model.Role;
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

    // (Rutas de organizaciones movidas a OrganizationAdminController)

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

    // (Operaciones POST de organizaciones movidas a OrganizationAdminController)

    // ========== HTMX ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/form-demo")
    public String getUserFormDemo(Model model) {
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
        model.addAttribute("user", demo);
        model.addAttribute("roles", Role.values());
        return "admin/users :: userFormFields";
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

    

    // (Método duplicado eliminado - se usa getUserFormDemo arriba)
}
