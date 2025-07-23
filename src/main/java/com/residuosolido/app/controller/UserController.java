package com.residuosolido.app.controller;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    // Dashboard para usuarios normales
    @GetMapping("/users/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard(Model model) {
        return "users/dashboard";
    }
    
    // Rutas de administración
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(@RequestParam(required = false) String role, Model model) {
        model.addAttribute("users", userService.getAllUsers(role));
        return "users/list";
    }
    
    @GetMapping("/admin/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "users/form";
    }
    
    @PostMapping("/save")
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
    
    @GetMapping("/edit/{id}")
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

    @GetMapping("/perfil")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findAuthenticatedUserByUsername(username);
        model.addAttribute("user", currentUser);
        return "users/profile";
    }

    @Autowired
    private RequestService requestService;
    
    @GetMapping("/new")
    public String newRequestForm(Model model) {
        model.addAttribute("organizations", requestService.getOrganizations());
        return "requests/form";
    }
    
    @PostMapping("/requests")
    public String createRequest(@RequestParam Long organizationId, 
                               @RequestParam String address,
                               @RequestParam String description) {
        requestService.createRequest(organizationId, address, description);
        return "redirect:/";
    }

}
