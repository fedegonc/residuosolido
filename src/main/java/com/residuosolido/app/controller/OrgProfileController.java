package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.CountryCode;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Gestión del perfil de la organización: datos de contacto, ciudad y materiales aceptados. */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgProfileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrgProfileController.class);

    @Autowired
    public OrgProfileController() {
    }

    /** Muestra el formulario de edición del perfil. */
    @GetMapping("/acopio/perfil")
    public String orgProfile(Authentication authentication, Model model) {
        try {
            User currentOrg = getCurrentUser(authentication);
            model.addAttribute("organization", currentOrg);
            model.addAttribute("cities", City.values());
            model.addAttribute("materials", MaterialCategory.values());
        } catch (Exception e) {
            logger.error("Error al cargar perfil de organización: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", msg("flash.org.profile_load_error"));
        }
        return "org/profile";
    }

    /** Actualiza los datos del perfil de la organización. */
    @PostMapping("/acopio/perfil")
    public String updateOrgProfile(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String phoneNational,
            @RequestParam(required = false) String ddd,
            @RequestParam(required = false) City city,
            @RequestParam(required = false) List<MaterialCategory> materials,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentOrg = getCurrentUser(authentication);
            String resolvedPhone = resolvePhone(phone, countryCode, phoneNational, ddd);
            userService.updateProfile(currentOrg, email, firstName, resolvedPhone, city,
                    materials != null ? materials : List.of());
            flashSuccess(redirectAttributes, "flash.profile.updated");
        } catch (Exception e) {
            logger.error("Error al actualizar perfil de organización: {}", e.getMessage(), e);
            flashError(redirectAttributes, e.getMessage() != null && e.getMessage().startsWith("error.") ? e.getMessage() : "flash.profile.update_error");
        }
        return "redirect:/acopio/perfil";
    }

    private String resolvePhone(String rawPhone, String countryCode, String phoneNational, String ddd) {
        if (phoneNational != null && !phoneNational.trim().isEmpty() && countryCode != null && !countryCode.trim().isEmpty()) {
            return PhoneNumber.of(CountryCode.fromDialCode(countryCode), phoneNational, ddd).value();
        }
        return rawPhone;
    }
}
