package com.residuosolido.app.controller;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/list";
    }
    
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "users/form";
    }
    
    @PostMapping
    public String saveUser(@ModelAttribute UserForm userForm, RedirectAttributes redirectAttributes) {
        try {
            // Convertir DTO a entidad
            User user = new User();
            BeanUtils.copyProperties(userForm, user);
            
            if (user.getId() == null) {
                // Usuario nuevo
                if (userForm.getNewPassword() == null || userForm.getNewPassword().trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "La contraseña es requerida para usuarios nuevos");
                    return "redirect:/users/create";
                }
                userService.createUser(user, userForm.getNewPassword());
            } else {
                // Usuario existente
                userService.updateUser(user, userForm.getNewPassword());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            String redirectUrl = user.getId() == null ? "redirect:/users/create" : "redirect:/users/edit/" + user.getId();
            return redirectUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario: " + e.getMessage());
            logger.error("Error al guardar usuario: ", e);
        }
        return "redirect:/users";
    }
    
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            UserForm userForm = new UserForm();
            BeanUtils.copyProperties(user.get(), userForm);
            model.addAttribute("userForm", userForm);
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

    @GetMapping("/dashboard")
    public String userDashboard() {
        return "users/dashboard"; 
    }


}
