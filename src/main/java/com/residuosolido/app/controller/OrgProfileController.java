package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgProfileController {

    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public OrgProfileController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/acopio/perfil")
    public String orgProfile(Authentication authentication, Model model) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            model.addAttribute("organization", currentOrg);
            model.addAttribute("cities", City.values());
        } catch (Exception e) {
            model.addAttribute("errorMessage", messageSource.getMessage("flash.org.profile_load_error", null, LocaleContextHolder.getLocale()));
        }
        return "org/profile";
    }

    @PostMapping("/acopio/perfil")
    public String updateOrgProfile(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) City city,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            userService.updateProfile(currentOrg, email, firstName, phone, city);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.profile.updated", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.profile.update_error", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/acopio/perfil";
    }

    @GetMapping("/acopio/completar-perfil")
    public String showCompleteProfileForm(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());

            if (currentUser.isProfileComplete()) {
                redirectAttributes.addFlashAttribute("infoMessage", messageSource.getMessage("flash.org.profile_already_complete", null, LocaleContextHolder.getLocale()));
                return "redirect:/acopio/inicio";
            }

            model.addAttribute("organization", currentUser);
            model.addAttribute("cities", City.values());
            return "org/complete-profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.profile_form_error", null, LocaleContextHolder.getLocale()));
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/acopio/completar-perfil")
    public String completeProfile(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) City city,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            userService.completeOrgProfile(currentUser, phone, city);
            redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("flash.org.profile_completed", null, LocaleContextHolder.getLocale()));
            return "redirect:/acopio/inicio";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/acopio/completar-perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                messageSource.getMessage("flash.org.profile_complete_error", null, LocaleContextHolder.getLocale()));
            return "redirect:/acopio/completar-perfil";
        }
    }
}
