package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Lista y formulario en la misma página
    @GetMapping
    public String users(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long id,
            Model model) {
        
        List<User> allUsers = userService.findAll();
        model.addAttribute("users", allUsers);
        model.addAttribute("totalUsers", allUsers != null ? allUsers.size() : 0);
        model.addAttribute("roles", Role.values());
        
        // Determinar el tipo de vista basado en la acción
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
        
        // Vista por defecto: lista
        model.addAttribute("viewType", "list");
        return "admin/users";
    }

    // Guardar usuario
    @PostMapping
    public String save(
            @RequestParam(required = false) String action,
            @ModelAttribute User user, 
            RedirectAttributes redirectAttributes) {
        
        if ("delete".equals(action) && user.getId() != null) {
            return delete(user.getId(), redirectAttributes);
        }
        try {
            if (user.getId() != null) {
                // Actualiza sin cambio de contraseña desde este formulario
                userService.updateUser(user, null);
                redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado");
            } else {
                // Crear usando la contraseña proporcionada en el formulario
                userService.createUser(user, user.getPassword());
                redirectAttributes.addFlashAttribute("successMessage", "Usuario creado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Eliminar usuario
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}