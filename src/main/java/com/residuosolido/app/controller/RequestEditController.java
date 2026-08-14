package com.residuosolido.app.controller;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestQueryService;
import com.residuosolido.app.service.RequestUpdateService;
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

/** Edición de solicitudes existentes (solo pendientes, solo del dueño). */
@Controller
public class RequestEditController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RequestEditController.class);

    private final RequestUpdateService requestUpdateService;
    private final RequestQueryService requestQueryService;
    private final CityOrgService cityOrgService;

    @Autowired
    public RequestEditController(RequestUpdateService requestUpdateService,
                                 RequestQueryService requestQueryService,
                                 CityOrgService cityOrgService) {
        this.requestUpdateService = requestUpdateService;
        this.requestQueryService = requestQueryService;
        this.cityOrgService = cityOrgService;
    }

    /** Muestra el formulario de edición con los datos actuales. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/solicitud/{id}/editar")
    public String editRequestForm(@PathVariable String id, Authentication authentication, Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            Request request = requestQueryService.getEditableOwnedRequest(id, user);
            model.addAttribute("request", request);
            model.addAttribute("isEdit", true);
            model.addAttribute("cities", cityOrgService.getAvailableCities());
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
    @PostMapping("/solicitud/{id}/editar")
    public String updateRequest(@PathVariable String id,
                                @RequestParam("city") City city,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            requestUpdateService.updateRequest(id, user, city, address, addressReference, materials, imageFile);
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
