package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.Keyed;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.service.OrganizationProfileService;
import com.residuosolido.app.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Perfil de organización: vista y edición de datos de contacto, ciudad y
 * materiales aceptados. Cuando el perfil está incompleto, el template abre
 * el formulario de edición directamente (funciona como onboarding).
 *
 * El modelo expone ambas entidades: {@code organization} para los datos de
 * negocio (ciudad, teléfono, materiales) y {@code user} para la identidad
 * (username, email) — username/email viven solo en User, no se denormalizan.
 */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgProfileController {

    private static final Logger logger = LoggerFactory.getLogger(OrgProfileController.class);

    private final OrganizationProfileService organizationProfileService;
    private final OrganizationService organizationService;
    private final Messages messages;

    public OrgProfileController(OrganizationProfileService organizationProfileService,
                               OrganizationService organizationService, Messages messages) {
        this.organizationProfileService = organizationProfileService;
        this.organizationService = organizationService;
        this.messages = messages;
    }

    /** Muestra el perfil de la organización (o el formulario si está incompleto). */
    @GetMapping(Routes.ORG_PROFILE)
    public String orgProfile(@CurrentUser User currentOrg, Model model) {
        logger.info("🔍 OrgProfileController.orgProfile() — usuario: {}", currentOrg.getUsername());
        Organization organization = organizationService.findByUser(currentOrg);
        logger.info("📦 Org: profileCompleted={}, phone={}, city={}", organization.isProfileComplete(), organization.getPhone(), organization.getCity());
        model.addAttribute("organization", organization);
        model.addAttribute("user", currentOrg);
        model.addAttribute("cities", City.values());
        model.addAttribute("materials", MaterialCategory.values());
        model.addAttribute("breadcrumbs", List.of(
                java.util.Map.of("label", "Inicio", "href", "/"),
                java.util.Map.of("label", "Panel de acopio", "href", Routes.ORG_REQUESTS),
                java.util.Map.of("label", "Perfil", "href", "")
        ));
        logger.info("✅ Renderizando org/profile.html");
        return "org/profile";
    }

    /** Actualiza los datos del perfil de la organización (User + Organization atomicamente). */
    @PutMapping(Routes.ORG_PROFILE)
    public String updateOrgProfile(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String phoneNational,
            @RequestParam(required = false) String ddd,
            @RequestParam(value = "ciudad", required = false) City ciudad,
            @RequestParam(value = "materiales", required = false) List<MaterialCategory> materiales,
            @CurrentUser User currentOrg,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Organization organization = organizationService.findByUser(currentOrg);
            City oldCity = organization.getCity();
            String resolvedPhone = PhoneNumber.resolve(countryCode, phoneNational, ddd, phone);
            // Ciudad y telefono son obligatorios SIEMPRE
            if (resolvedPhone == null || resolvedPhone.isBlank()) {
                throw new ValidationException(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED);
            }
            if (ciudad == null) {
                throw new ValidationException(ServerMessage.ERROR_PROFILE_CITY_REQUIRED);
            }
            // Actualiza User y Organization de forma coordinada (sin escritura parcial)
            organizationProfileService.updateOrganizationProfile(currentOrg, email, firstName,
                    resolvedPhone, ciudad, materiales);
            if (ciudad != null && !ciudad.equals(oldCity)) {
                session.removeAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            }
            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_PROFILE_UPDATED);
        } catch (RuntimeException e) {
            logger.error("Error al actualizar perfil de organización: {}", e.getMessage(), e);
            messages.flashError(redirectAttributes, e instanceof Keyed k ? k.key() : ServerMessage.FLASH_PROFILE_UPDATE_ERROR);
        }
        return "redirect:" + Routes.ORG_PROFILE;
    }
}
