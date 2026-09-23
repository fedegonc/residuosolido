package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.util.LandingCardLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * Controller unificado de solicitudes del ciudadano:
 * lista, elimina, edita y muestra el formulario de edición.
 *
 * Antes estaba dividido en RequestController + RequestEditController.
 * La creación de solicitudes (con rate limiting de invitados) vive en RequestCreateController.
 */
@Controller
public class RequestController {

    private final RequestService requestService;
    private final RequestMetricsService requestMetricsService;
    private final CityOrgService cityOrgService;
    private final LocaleResolver localeResolver;
    private final Messages messages;

    public RequestController(RequestService requestService,
                             RequestMetricsService requestMetricsService,
                             CityOrgService cityOrgService,
                             LocaleResolver localeResolver,
                             Messages messages) {
        this.requestService = requestService;
        this.requestMetricsService = requestMetricsService;
        this.cityOrgService = cityOrgService;
        this.localeResolver = localeResolver;
        this.messages = messages;
    }

    /** Lista las solicitudes del usuario autenticado con stats. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping(Routes.REQUESTS)
    public String listUserRequests(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @CurrentUser User user, Model model,
                                    HttpServletRequest request) {
        model.addAttribute("user", user);
        model.addAttribute("requests", requestService.getRequestsByUser(user, page, size));
        model.addAttribute("requestStats", requestMetricsService.getUserRequestStats(user));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        Locale locale = localeResolver.resolveLocale(request);
        model.addAttribute("cards", LandingCardLoader.loadCards(locale.getLanguage()));
        return "users/requests";
    }

    /** Elimina una solicitud del usuario (solo si está pendiente). */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(Routes.REQUEST)
    public String deleteRequest(@PathVariable String id, @CurrentUser User user,
                                RedirectAttributes redirectAttributes) {
        requestService.deleteOwnedRequest(id, user);
        messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_REQUEST_DELETED);
        return "redirect:" + Routes.REQUESTS;
    }

    /** Muestra el formulario de edición con los datos actuales. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping(Routes.REQUEST_EDIT)
    public String editRequestForm(@PathVariable String id, @CurrentUser User user, Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Request request = requestService.getEditableOwnedRequest(id, user);
            model.addAttribute("request", request);
            model.addAttribute("isEdit", true);
            model.addAttribute("isGuest", false);
            model.addAttribute("cities", cityOrgService.getAvailableCities());
            model.addAttribute("organizations", cityOrgService.getOrganizationsByCity(request.getCity()));
            messages.addFormAttributes(model);
            return "users/request-form";
        } catch (IllegalStateException e) {
            messages.flashError(redirectAttributes, ServerMessage.FLASH_REQUEST_EDIT_PENDING_ONLY);
            return "redirect:" + Routes.REQUESTS;
        }
    }

    /** Actualiza una solicitud existente (ciudad, dirección, materiales, imagen). */
    @PreAuthorize("hasRole('USER')")
    @PutMapping(Routes.REQUEST)
    public String updateRequest(@PathVariable String id,
                                @RequestParam("ciudad") City ciudad,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "organizationId", required = false) String organizationId,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @CurrentUser User user,
                                RedirectAttributes redirectAttributes) {
        try {
            requestService.updateRequest(id, user, ciudad, address, addressReference, materials, organizationId, imageFile);
            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_REQUEST_UPDATED);
            return "redirect:" + Routes.REQUESTS;
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", messages.msg(e));
            return "redirect:" + Routes.REQUESTS;
        }
    }

    /** Toda operación de este controller que falle por no ser dueño de la solicitud cae acá. */
    @ExceptionHandler(SecurityException.class)
    public String handleNotOwned(RedirectAttributes redirectAttributes) {
        messages.flashError(redirectAttributes, ServerMessage.FLASH_REQUEST_NOT_OWNED);
        return "redirect:" + Routes.REQUESTS;
    }
}
