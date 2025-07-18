package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la sección de administración
 * Maneja las rutas y vistas específicas para usuarios con rol ADMIN
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @Autowired
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Muestra el dashboard principal de administración
     * @param model Modelo para pasar datos a la vista
     * @return Vista del dashboard de administración
     */
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        // Obtener estadísticas para el dashboard
        long totalUsers = userRepository.count();
        
        // Agregar datos al modelo
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pageTitle", "Panel de Administración");
        
        return "admin/dashboard";
    }
}
