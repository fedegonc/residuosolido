package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }
    
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/form";
    }
    
    @PostMapping
    public String saveUser(@ModelAttribute User user, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Si es un usuario nuevo, establecer la contraseña
            if (user.getId() == null) {
                String newPassword = request.getParameter("newPassword");
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "La contraseña es requerida para usuarios nuevos");
                    return "redirect:/users/create";
                }
                // Establecer valores predeterminados para usuarios nuevos
                user.setActive(true);
                if (user.getPreferredLanguage() == null) {
                    user.setPreferredLanguage("es");
                }
            }
            
            userService.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario: " + e.getMessage());
        }
        return "redirect:/users";
    }
    
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "users/form";
        }
        return "redirect:/users";
    }
    
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        logger.info("Accediendo a viewUser con id: {}", id);
        
        try {
            Optional<User> user = userService.findById(id);
            logger.info("¿Usuario encontrado?: {}", user.isPresent());
            
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                logger.info("Retornando vista users/view para usuario: {}", user.get().getUsername());
                return "users/view";
            }
            
            logger.warn("Usuario no encontrado para id: {}", id);
            return "redirect:/users";
        } catch (Exception e) {
            logger.error("Error al visualizar el usuario: {}", e.getMessage(), e);
            return "redirect:/users";
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/users";
    }
}
