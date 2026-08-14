package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Onboarding: primera configuración de perfil para organizaciones recién registradas. */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgOnboardingController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrgOnboardingController.class);

    @Autowired
    public OrgOnboardingController() {
    }

    /** Muestra el formulario para completar perfil (teléfono y ciudad). */
    @GetMapping("/acopio/completar-perfil")
    public String showCompleteProfileForm(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(authentication);

            if (currentUser.isProfileComplete()) {
                redirectAttributes.addFlashAttribute("infoMessage", msg("flash.org.profile_already_complete"));
                return "redirect:/acopio/inicio";
            }

            model.addAttribute("organization", currentUser);
            model.addAttribute("cities", City.values());
            return "org/complete-profile";

        } catch (Exception e) {
            logger.error("Error al cargar formulario de completar perfil: {}", e.getMessage(), e);
            flashError(redirectAttributes, "flash.org.profile_form_error");
            return "redirect:/auth/login";
        }
    }

    /** Guarda el perfil inicial de la organización. */
    @PostMapping("/acopio/completar-perfil")
    public String completeProfile(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) City city,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser(authentication);
            userService.completeOrgProfile(currentUser, phone, city);
            flashSuccess(redirectAttributes, "flash.org.profile_completed");
            return "redirect:/acopio/inicio";
        } catch (IllegalArgumentException e) {
            flashError(redirectAttributes, e.getMessage());
            return "redirect:/acopio/completar-perfil";
        } catch (Exception e) {
            logger.error("Error al completar perfil de organización: {}", e.getMessage(), e);
            flashError(redirectAttributes, "flash.org.profile_complete_error");
            return "redirect:/acopio/completar-perfil";
        }
    }
}
