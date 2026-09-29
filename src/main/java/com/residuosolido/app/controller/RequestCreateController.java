package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.config.RateLimiter;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Crea nuevas solicitudes de recolección (ciudadano o invitado con rate limiting). */
@Controller
public class RequestCreateController {

    private static final Logger logger = LoggerFactory.getLogger(RequestCreateController.class);

    private final RequestService requestService;
    private final CityOrgService cityOrgService;
    private final UserService userService;
    private final Messages messages;

    public RequestCreateController(RequestService requestService,
                                   CityOrgService cityOrgService,
                                   UserService userService,
                                   Messages messages) {
        this.requestService = requestService;
        this.cityOrgService = cityOrgService;
        this.userService = userService;
        this.messages = messages;
    }

    /** Muestra el formulario para crear una solicitud (solo usuarios registrados). */
    @GetMapping(Routes.REQUESTS_NEW)
    @PreAuthorize("hasRole('USER')")
    public String newRequestForm(@RequestParam(value = "ciudad", required = false) City ciudad,
                                  Model model, @CurrentUser User user) {
        Request request = new Request();
        model.addAttribute("request", request);
        model.addAttribute("isEdit", false);
        model.addAttribute("needsPhone", !user.hasPhone());
        model.addAttribute("cities", cityOrgService.getAvailableCities());
        messages.addFormAttributes(model);
        if (ciudad != null) {
            model.addAttribute("organizations", cityOrgService.getOrganizationsByCity(ciudad));
            model.addAttribute("selectedCity", ciudad);
        }
        return "users/request-form";
    }

    /** Devuelve las opciones de organización para una ciudad como HTML (fetch). */
    @GetMapping(Routes.ORG_OPTIONS)
    public String orgOptionsForCity(@RequestParam("ciudad") City ciudad, Model model) {
        model.addAttribute("organizations", cityOrgService.getOrganizationsByCity(ciudad));
        return "fragments/ui :: options";
    }

    /** Procesa la creación de una solicitud (solo usuarios registrados). */
    @PostMapping(Routes.REQUESTS_NEW)
    @PreAuthorize("hasRole('USER')")
    public String createRequest(@RequestParam("ciudad") City ciudad,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(value = "userCountryCode", required = false) String userCountryCode,
                                @RequestParam(value = "userPhoneNational", required = false) String userPhoneNational,
                                @RequestParam(value = "userDdd", required = false) String userDdd,
                                @RequestParam(value = "organizationId", required = false) String organizationId,
                                @CurrentUser User user,
                                RedirectAttributes redirectAttributes) {
        logger.info("=== POST /solicitar === ciudad={}, organizationId={}", ciudad, organizationId);
        try {
            if (!user.hasPhone()) {
                try {
                    String phone = PhoneNumber.normalize(userCountryCode, userPhoneNational, userDdd);
                    userService.updateProfile(user, null, null, phone, null);
                } catch (IllegalArgumentException e) {
                    messages.flashError(redirectAttributes, e);
                    return "redirect:" + Routes.REQUESTS_NEW;
                }
            }
            requestService.createRequestWithImage(user, ciudad, address, addressReference,
                    materials, organizationId, imageFile);

            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_REQUEST_CREATED);
            return "redirect:" + Routes.REQUESTS;
        } catch (IllegalStateException e) {
            logger.warn("ValidationException: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("warningMessage", messages.msg(e));
            return "redirect:" + Routes.REQUESTS;
        } catch (IllegalArgumentException e) {
            logger.warn("IllegalArgumentException: {}", e.getMessage());
            messages.flashError(redirectAttributes, e);
            return "redirect:" + Routes.REQUESTS_NEW;
        } catch (Exception e) {
            logger.error("Exception: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            messages.flashError(redirectAttributes, ServerMessage.FLASH_REQUEST_CREATE_ERROR);
            return "redirect:" + Routes.REQUESTS_NEW;
        }
    }
}
