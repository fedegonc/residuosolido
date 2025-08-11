package com.residuosolido.app.controller;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CloudinaryService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

 
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('USER')")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    // Dashboard para usuarios normales
    @GetMapping("/dashboard")
    
    public String userDashboard(Model model) {
        return "users/dashboard";
    }
    
    // Solicitudes del usuario
    @GetMapping("/requests")
    public String userRequests(Model model) {
        return "users/requests";
    }
    
    // Crear nueva solicitud
    @GetMapping("/requests/create")
    public String createRequest(Model model) {
        return "users/request-form";
    }
    
    
    // Notificaciones del usuario
    @GetMapping("/notifications")
    public String userNotifications(Model model) {
        return "users/notifications";
    }
    
    // Estadísticas del usuario
    @GetMapping("/stats")
    public String userStats(Model model) {
        return "users/stats";
    }
    
    // Perfil de usuario
    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findAuthenticatedUserByUsername(username);
        model.addAttribute("user", currentUser);
        model.addAttribute("username", username);
        return "users/profile";
    }
    
    // Editar perfil de usuario
    @GetMapping("/edit")
    public String editUserProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findAuthenticatedUserByUsername(username);
        UserForm userForm = new UserForm();
        userForm.setId(currentUser.getId());
        userForm.setUsername(currentUser.getUsername());
        userForm.setEmail(currentUser.getEmail());
        userForm.setFirstName(currentUser.getFirstName());
        userForm.setLastName(currentUser.getLastName());
        userForm.setRole(currentUser.getRole());
        userForm.setPreferredLanguage(currentUser.getPreferredLanguage() != null ? currentUser.getPreferredLanguage() : "es");
        userForm.setProfileImage(currentUser.getProfileImage());
        model.addAttribute("userForm", userForm);
        model.addAttribute("isProfile", true);
        return "users/edit";
    }
    
    // Guardar cambios del perfil
    @PostMapping("/save-profile")
    public String saveUserProfile(@ModelAttribute UserForm userForm, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            userForm.setId(currentUser.getId());
            userForm.setRole(currentUser.getRole());
            userForm.setPreferredLanguage(currentUser.getPreferredLanguage() != null ? currentUser.getPreferredLanguage() : "es");
            // Subida de imagen si hay archivo
            if (imageFile != null && !imageFile.isEmpty()) {
                String url = cloudinaryService.uploadFile(imageFile);
                userForm.setProfileImage(url);
            } else {
                userForm.setProfileImage(currentUser.getProfileImage());
            }
            userService.saveUser(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente");
            return "redirect:/users/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
            return "redirect:/users/edit";
        }
    }
    
    // (Rutas de administración movidas a com.residuosolido.app.controller.admin.AdminUserController)
    
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        logger.info("Accediendo a viewUser con id: {}", id);
        
        try {
            User user = userService.getUserOrThrow(id);
            model.addAttribute("user", user);
            logger.info("Retornando vista users/view para usuario: {}", user.getUsername());
            return "users/view";
        } catch (IllegalArgumentException e) {
            logger.warn("Usuario no encontrado para id: {}", id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.error("Error al visualizar el usuario: {}", e.getMessage(), e);
            return "redirect:/admin/users";
        }
    }
    
    
    
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/users";
    }
    

    

    @Autowired
    private RequestService requestService;
    
    
    
    @GetMapping("/new")
    public String newRequestForm(Model model) {
        return "requests/form";
    }
    
    @PostMapping("/requests")
    public String createRequest(@RequestParam Long organizationId, 
                               @RequestParam String address,
                               @RequestParam String description,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User currentUser = userService.findAuthenticatedUserByUsername(username);
            
            requestService.createRequest(currentUser, description, address);
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud de recolección enviada exitosamente!");
        } catch (Exception e) {
            logger.error("Error al crear solicitud de recolección: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud. Inténtalo de nuevo.");
        }
        
        return "redirect:/users/dashboard";
    }

}
