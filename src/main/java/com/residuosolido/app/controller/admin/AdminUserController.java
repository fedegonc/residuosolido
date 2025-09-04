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
    public String users(@RequestParam(required = false) Long edit, Model model) {
        // Siempre mostrar lista
        model.addAttribute("users", userService.findAll());
        
        // Si hay ID para editar, cargar ese usuario
        if (edit != null) {
            model.addAttribute("editUser", userService.findById(edit).orElse(null));
        }
        
        // Usuario vacío para formulario de nuevo
        User newUser = new User();
        newUser.setRole(Role.USER);
        newUser.setPreferredLanguage("es");
        model.addAttribute("newUser", newUser);
        model.addAttribute("roles", Role.values());
        
        return "admin/users";
    }

    // Guardar usuario
    @PostMapping
    public String save(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() != null) {
                // Actualiza sin cambio de contraseña desde este formulario
                userService.updateUser(user, null);
                redirectAttributes.addFlashAttribute("success", "Usuario actualizado");
            } else {
                // Crear usando la contraseña proporcionada en el formulario
                userService.createUser(user, user.getPassword());
                redirectAttributes.addFlashAttribute("success", "Usuario creado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Eliminar usuario
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}