package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller unificado de solicitudes del ciudadano:
 * lista, elimina, edita y muestra el formulario de edición.
 *
 * Antes estaba dividido en RequestController + RequestEditController.
 * La creación de solicitudes (con rate limiting de invitados) vive en RequestCreateController.
 */
@Controller
public class RequestController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    private final RequestService requestService;
    private final RequestMetricsService requestMetricsService;
    private final CityOrgService cityOrgService;

    @Autowired
    public RequestController(RequestService requestService,
                             RequestMetricsService requestMetricsService,
                             CityOrgService cityOrgService) {
        this.requestService = requestService;
        this.requestMetricsService = requestMetricsService;
        this.cityOrgService = cityOrgService;
    }

    /** Dashboard unificado: lista de solicitudes + stats del usuario. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping(Routes.USER_HOME)
    public String userHome() {
        return "redirect:/solicitudes";
    }

    /** Lista las solicitudes del usuario autenticado con stats. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping(Routes.REQUESTS)
    public String listUserRequests(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("requests", requestService.getRequestsByUser(user, page, size));
        model.addAttribute("requestStats", requestMetricsService.getUserRequestStats(user));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "users/requests";
    }

    /** Elimina una solicitud del usuario (solo si está pendiente). */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(Routes.REQUEST_DELETE)
    public String deleteRequest(@PathVariable String id, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            requestService.deleteOwnedRequest(id, user);
            flashSuccess(redirectAttributes, "flash.request.deleted");
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.request.not_owned");
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage());
            flashError(redirectAttributes, "flash.request.delete_error");
        }
        return "redirect:/solicitudes";
    }

    /** Muestra el formulario de edición con los datos actuales. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping(Routes.REQUEST_EDIT)
    public String editRequestForm(@PathVariable String id, Authentication authentication, Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            Request request = requestService.getEditableOwnedRequest(id, user);
            model.addAttribute("request", request);
            model.addAttribute("isEdit", true);
            model.addAttribute("isGuest", false);
            model.addAttribute("cities", cityOrgService.getAvailableCities());
            model.addAttribute("organizations", cityOrgService.getOrganizationsByCity(request.getCity()));
            addFormAttributes(model);
            return "users/request-form";
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.request.not_owned");
            return "redirect:/solicitudes";
        } catch (IllegalStateException e) {
            flashError(redirectAttributes, "flash.request.edit.pending_only");
            return "redirect:/solicitudes";
        } catch (Exception e) {
            logger.error("Error al cargar formulario de edición: {}", e.getMessage());
            flashError(redirectAttributes, "flash.request.load_error");
            return "redirect:/solicitudes";
        }
    }

    /** Actualiza una solicitud existente (ciudad, dirección, materiales, imagen). */
    @PreAuthorize("hasRole('USER')")
    @PostMapping(Routes.REQUEST_EDIT)
    public String updateRequest(@PathVariable String id,
                                @RequestParam("city") City city,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "organizationId", required = false) String organizationId,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            requestService.updateRequest(id, user, city, address, addressReference, materials, organizationId, imageFile);
            flashSuccess(redirectAttributes, "flash.request.updated");
            return "redirect:/solicitud/" + id;
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.request.not_owned");
            return "redirect:/solicitudes";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", msg(e.getMessage()));
            return "redirect:/solicitud/" + id;
        } catch (Exception e) {
            logger.error("Error al actualizar solicitud: {}", e.getMessage());
            flashError(redirectAttributes, "flash.request.update_error");
            return "redirect:/solicitud/" + id;
        }
    }
}
