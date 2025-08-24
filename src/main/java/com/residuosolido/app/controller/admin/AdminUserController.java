package com.residuosolido.app.controller.admin;

import com.residuosolido.app.dto.UserForm;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "admin/form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute UserForm userForm, RedirectAttributes redirectAttributes) {
        // Log de entrada con snapshot seguro (sin contraseñas)
        logger.info("[ADMIN] Guardar usuario - Inicio | id={}, username={}, email={}, role={}, active={}, preferredLanguage={}",
                userForm.getId(), userForm.getUsername(), userForm.getEmail(), userForm.getRole(), userForm.isActive(), userForm.getPreferredLanguage());
        logger.info("[ADMIN] Guardar usuario - Nueva contraseña provista? {} | Confirm provisto? {} | forzado? {}",
                userForm.getNewPassword() != null && !userForm.getNewPassword().trim().isEmpty(),
                userForm.getConfirmPassword() != null && !userForm.getConfirmPassword().trim().isEmpty(),
                userForm.isForcePasswordChange());
        try {
            logger.debug("[ADMIN] Llamando a userService.saveUser(userForm)");
            userService.saveUser(userForm);
            logger.info("[ADMIN] Guardar usuario - Éxito | id={}", userForm.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Usuario guardado exitosamente");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN] Validación fallida al guardar usuario: {} | id={}, username={}, email={}", e.getMessage(), userForm.getId(), userForm.getUsername(), userForm.getEmail());
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
            logger.info("[ADMIN] Editar usuario - GET | id={}", id);
            User user = userService.getUserOrThrow(id);
            UserForm userForm = new UserForm();
            BeanUtils.copyProperties(user, userForm);
            userService.copyLocationToForm(user, userForm);
            model.addAttribute("userForm", userForm);
            logger.info("[ADMIN] Renderizando vista de edición: admin/form.html | id={}", id);
            return "admin/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/users";
        }
    }

    /**
     * Endpoint específico para actualizar solo la ubicación de un usuario
     */
    @PostMapping("/{id}/location")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUserLocation(
            @PathVariable Long id,
            @RequestParam String direccion,
            @RequestParam BigDecimal latitud,
            @RequestParam BigDecimal longitud,
            @RequestParam(required = false) String referencias) {
        try {
            User updatedUser = userService.updateUserLocation(id, direccion, latitud, longitud, referencias);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ubicación actualizada exitosamente");
            response.put("user", Map.of(
                    "id", updatedUser.getId(),
                    "direccion", updatedUser.getDireccion(),
                    "latitud", updatedUser.getLatitud(),
                    "longitud", updatedUser.getLongitud(),
                    "referencias", updatedUser.getReferencias()
            ));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return ResponseEntity.ok("Usuario eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar el usuario: " + e.getMessage());
        }
    }   

    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserOrThrow(id);
            model.addAttribute("user", user);
            // Cantidad de feedbacks del usuario (por ID, sin cargar la lista)
            long userFeedbackCount = feedbackService.countByUserId(user.getId());
            model.addAttribute("userFeedbackCount", userFeedbackCount);
            return "admin/view";

        } catch (IllegalArgumentException e) {
            return "redirect:/admin/users";
        }
    }   
    

}
