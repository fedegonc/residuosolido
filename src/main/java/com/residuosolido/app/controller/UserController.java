package com.residuosolido.app.controller;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
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

import java.util.Optional;

@Controller
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    // Dashboard para usuarios normales
    @GetMapping("/users/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(Model model) {
        return "users/dashboard";
    }
    
    // Perfil de usuario
    @GetMapping("/users/profile")
   
    @PreAuthorize("hasRole('USER')")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findAuthenticatedUserByUsername(username);
        model.addAttribute("user", currentUser);
        model.addAttribute("username", username);
        return "users/profile";
    }
    
    // Editar perfil de usuario
    @GetMapping("/users/edit")
    @PreAuthorize("hasRole('USER')")
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
    @PostMapping("/users/save-profile")
    @PreAuthorize("hasRole('USER')")
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
    
    // Rutas de administración
    @GetMapping("/admin/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "users/form";
    }
    
    @PostMapping("/admin/users/save")
    public String saveUser(@ModelAttribute UserForm userForm, RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            String redirectUrl = userForm.getId() == null ? "redirect:/admin/users/create" : "redirect:/admin/users/edit/" + userForm.getId();
            return redirectUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario: " + e.getMessage());
            logger.error("Error al guardar usuario: ", e);
            return "redirect:/admin/users";
        }
    }
    
    @GetMapping("/admin/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserOrThrow(id);
            UserForm userForm = new UserForm();
            BeanUtils.copyProperties(user, userForm);
            model.addAttribute("userForm", userForm);
            return "users/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/users";
        }
    }
    
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
    
    @GetMapping("/admin/users/view/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewUserAdmin(@PathVariable Long id, Model model) {
        return viewUser(id, model);
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
    

    @GetMapping("/dashboard")
    public String userDashboard() {
        return "users/dashboard"; 
    }

    @Autowired
    private RequestService requestService;
    
    @GetMapping("/requests/create")
    public String createRequestForm(Model model) {
        model.addAttribute("organizations", requestService.getOrganizations());
        return "requests/form";
    }
    
    @GetMapping("/new")
    public String newRequestForm(Model model) {
        model.addAttribute("organizations", requestService.getOrganizations());
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
            
            requestService.createRequest(organizationId, address, description, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud de recolección enviada exitosamente!");
        } catch (Exception e) {
            logger.error("Error al crear solicitud de recolección: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la solicitud. Inténtalo de nuevo.");
        }
        
        return "redirect:/users/dashboard";
    }

}
