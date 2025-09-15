package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    // ========== DASHBOARD & DOCS ==========
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/documentation")
    public String documentation() {
        return "admin/documentation";
    }

    @GetMapping("/admin/documentation/uml")
    public String documentationUml() {
        return "admin/uml";
    }
    
    @GetMapping("/admin/documentation/layouts")
    public String documentationLayouts() {
        return "admin/documentation-layouts";
    }

    @GetMapping("/admin/documentation/details/{section}")
    public String showDocumentationDetails(@PathVariable String section, Model model) {
        model.addAttribute("section", section);
        return "admin/documentation-details";
    }

    @GetMapping("/admin/documentation/details/visual-system")
    public String showVisualSystem(Model model) {
        model.addAttribute("section", "visual-system");
        return "admin/documentation-details";
    }

    @GetMapping("/admin/statistics")
    public String statistics() {
        return "admin/statistics";
    }

    // ========== CONFIG ==========
    @GetMapping("/admin/config")
    public String config() {
        return "admin/config";
    }

    @PostMapping("/admin/config/hero-image")
    public String updateHeroImage(@RequestParam("heroImageFile") MultipartFile heroImageFile, 
                                 RedirectAttributes redirectAttributes) {
        try {
            if (heroImageFile != null && !heroImageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(heroImageFile);
                // TODO: Guardar imageUrl en configuración/base de datos
                redirectAttributes.addFlashAttribute("successMessage", "Imagen hero actualizada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se seleccionó ninguna imagen");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar imagen: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    // ========= USERS: HTMX demo autocompletar =========
    @GetMapping("/admin/users/form-demo")
    public String userFormDemo(Model model) {
        User demo = new User();
        demo.setUsername("demo_user");
        demo.setEmail("demo@example.com");
        demo.setFirstName("Demo");
        demo.setLastName("User");
        demo.setRole(Role.USER);
        demo.setActive(true);
        demo.setPreferredLanguage("es");
        demo.setAddress("Av. Siempre Viva 742");
        // Nota: el campo password es plano aquí solo para prellenar el input; se encripta al guardar
        demo.setPassword("Demo1234");
        demo.setAddressReferences("Frente a la plaza principal");
        demo.setLatitude(new java.math.BigDecimal("-34.9055"));
        demo.setLongitude(new java.math.BigDecimal("-56.1850"));
        demo.setProfileImage("https://res.cloudinary.com/demo/image/upload/v123456789/avatar.png");

        model.addAttribute("user", demo);
        model.addAttribute("roles", Role.values());
        model.addAttribute("isEdit", false);
        return "admin/users :: userFormFields";
    }
}
