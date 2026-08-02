package com.residuosolido.app.controller;

import com.residuosolido.app.config.GuestRateLimiter;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CityOrganizationService;
import com.residuosolido.app.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RequestCreateController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RequestCreateController.class);

    private final RequestService requestService;
    private final CityOrganizationService cityOrganizationService;
    private final GuestRateLimiter guestRateLimiter;

    @Autowired
    public RequestCreateController(RequestService requestService,
                                   CityOrganizationService cityOrganizationService,
                                   GuestRateLimiter guestRateLimiter) {
        this.requestService = requestService;
        this.cityOrganizationService = cityOrganizationService;
        this.guestRateLimiter = guestRateLimiter;
    }

    @GetMapping("/solicitudes/nueva")
    public String newRequestForm(@RequestParam(value = "city", required = false) City city,
                                  Model model, Authentication authentication) {
        model.addAttribute("request", new Request());
        model.addAttribute("isEdit", false);
        model.addAttribute("isGuest", userService.isAnonymous(authentication));
        model.addAttribute("cities", cityOrganizationService.getAvailableCities());
        addFormAttributes(model);
        if (city != null) {
            model.addAttribute("organizations", cityOrganizationService.getOrganizationsByCity(city));
            model.addAttribute("selectedCity", city);
        }
        return "users/request-form";
    }

    @PostMapping("/solicitudes")
    public String createRequest(@RequestParam("city") City city,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "estimatedWeight", required = false) String estimatedWeight,
                                @RequestParam(value = "estimatedVolume", required = false) String estimatedVolume,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(value = "guestName", required = false) String guestName,
                                @RequestParam(value = "guestPhone", required = false) String guestPhone,
                                @RequestParam(value = "organizationId", required = false) String organizationId,
                                Authentication authentication,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.resolveUser(authentication);
            if (user == null && !guestRateLimiter.isAllowed(httpRequest)) {
                flashError(redirectAttributes, "flash.request.rate_limited");
                return "redirect:/solicitudes/nueva?error";
            }
            Request created = requestService.createRequestWithImage(user, city, address, addressReference,
                    materials, guestName, guestPhone, organizationId, estimatedWeight, estimatedVolume, imageFile);

            flashSuccess(redirectAttributes, "flash.request.created");
            redirectAttributes.addFlashAttribute("createdRequestId", created.getId());
            redirectAttributes.addFlashAttribute("createdRequestStatus", created.getStatus().name());
            redirectAttributes.addFlashAttribute("isGuest", user == null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("guestPhone", guestPhone);
            }
            return "redirect:/solicitudes/exito";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", msg(e.getMessage()));
            return "redirect:/solicitudes";
        } catch (IllegalArgumentException e) {
            flashError(redirectAttributes, e.getMessage());
            return "redirect:/solicitudes/nueva";
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            flashError(redirectAttributes, "flash.request.create_error");
            return "redirect:/solicitudes/nueva";
        }
    }
}
