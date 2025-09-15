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
        java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
    
       // Listas básicas sin tildes
        String[] firstNames = {"Ana","Bruno","Carla","Diego","Eva","Fabio","Gina","Hugo","Iris","Juan","Maria","Sofia","Andres"};
        String[] lastNames  = {"Pereira","Silva","Garcia","Lopez","Fernandez","Santos","Rodriguez","Ramos","Costa","Diaz","Martinez","Gonzalez"};
        String[] streets    = {"Artigas","Sarandi","Rivera","Brasil","Uruguay","Ansina","Yaguaron"};
        String[] refs       = {"Frente a la plaza","Cerca del liceo","Esquina farmacia","Junto a la terminal","A una cuadra del hospital"};

        // Nombre + apellido 
        String fn = firstNames[rnd.nextInt(firstNames.length)];
        String ln = lastNames[rnd.nextInt(lastNames.length)];
        int suffix = rnd.nextInt(1000, 9999);
    
        // Usuario y email únicos
        String username = (fn.substring(0,1) + ln + suffix).toLowerCase();
        String email = (fn + "." + ln + suffix + "@example.com").toLowerCase();
    
        // Dirección
        String address = streets[rnd.nextInt(streets.length)] + " " + rnd.nextInt(100, 999);
        String addressRef = refs[rnd.nextInt(refs.length)];
    
        // Coordenadas simples (zona Rivera/Sant’Ana)
        java.math.BigDecimal lat = java.math.BigDecimal.valueOf(-30.90 + rnd.nextDouble(-0.05, 0.05));
        java.math.BigDecimal lng = java.math.BigDecimal.valueOf(-55.55 + rnd.nextDouble(-0.05, 0.05));
    
        // Idioma aleatorio
        String[] langs = {"es", "pt"};
        String lang = langs[rnd.nextInt(langs.length)];
    
        // Rol aleatorio (mayoría USER)
        Role role = Role.USER;    
        // Armado del user
        User demo = new User();
        demo.setUsername(username);
        demo.setEmail(email);
        demo.setFirstName(fn);
        demo.setLastName(ln);
        demo.setRole(role);
        demo.setActive(rnd.nextDouble() > 0.1); // 90% activos
        demo.setPreferredLanguage(lang);
        demo.setAddress(address);
        demo.setAddressReferences(addressRef);
        demo.setLatitude(lat);
        demo.setLongitude(lng);
        demo.setProfileImage("https://res.cloudinary.com/demo/image/upload/v1690000000/sample-avatar.png");
        demo.setPassword("Demo" + suffix);
    
        model.addAttribute("user", demo);
        model.addAttribute("roles", Role.values());
        model.addAttribute("isEdit", false);
        return "admin/users :: userFormFields";
    }
}
