package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.config.RateLimiter;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.service.CityOrgService;
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

/** Crea nuevas solicitudes de recolección (ciudadano o invitado con rate limiting). */
@Controller
public class RequestCreateController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RequestCreateController.class);

    private final RequestService requestService;
    private final CityOrgService cityOrgService;
    private final RateLimiter guestRateLimiter;

    @Autowired
    public RequestCreateController(RequestService requestService,
                                   CityOrgService cityOrgService,
                                   RateLimiter guestRateLimiter) {
        this.requestService = requestService;
        this.cityOrgService = cityOrgService;
        this.guestRateLimiter = guestRateLimiter;
    }

    /** Muestra el formulario para crear una solicitud (acepta prefill de nombre/teléfono desde la home). */
    @GetMapping(Routes.REQUESTS_NEW)
    public String newRequestForm(@RequestParam(value = "ciudad", required = false) City ciudad,
                                  @RequestParam(value = "nombre", required = false) String nombre,
                                  @RequestParam(value = "telefono", required = false) String telefono,
                                  Model model, Authentication authentication) {
        User user = userService.resolveUser(authentication);
        Request request = new Request();
        if (nombre != null) request.setGuestName(nombre);
        if (telefono != null) request.setGuestPhone(telefono);
        model.addAttribute("request", request);
        model.addAttribute("isEdit", false);
        model.addAttribute("isGuest", user == null);
        model.addAttribute("needsPhone", user != null && !user.hasPhone());
        model.addAttribute("cities", cityOrgService.getAvailableCities());
        addFormAttributes(model);
        if (ciudad != null) {
            model.addAttribute("organizations", cityOrgService.getOrganizationsByCity(ciudad));
            model.addAttribute("selectedCity", ciudad);
        }
        return "users/request-form";
    }

    /** HTMX: devuelve las opciones de organización para una ciudad como HTML. */
    @GetMapping(Routes.HTMX_ORG_OPTIONS)
    public String orgOptionsForCity(@RequestParam("ciudad") City ciudad, Model model) {
        model.addAttribute("organizations", cityOrgService.getOrganizationsByCity(ciudad));
        return "fragments/ui :: options";
    }

    /** Procesa la creación de una solicitud (con imagen opcional y rate limit para invitados). */
    @PostMapping(Routes.REQUESTS_NEW)
    public String createRequest(@RequestParam("ciudad") City ciudad,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "estimatedWeight", required = false) String estimatedWeight,
                                @RequestParam(value = "estimatedVolume", required = false) String estimatedVolume,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(value = "guestName", required = false) String guestName,
                                @RequestParam(value = "guestPhone", required = false) String guestPhone,
                                @RequestParam(value = "guestCountryCode", required = false) String guestCountryCode,
                                @RequestParam(value = "guestPhoneNational", required = false) String guestPhoneNational,
                                @RequestParam(value = "guestDdd", required = false) String guestDdd,
                                @RequestParam(value = "userCountryCode", required = false) String userCountryCode,
                                @RequestParam(value = "userPhoneNational", required = false) String userPhoneNational,
                                @RequestParam(value = "userDdd", required = false) String userDdd,
                                @RequestParam(value = "organizationId", required = false) String organizationId,
                                Authentication authentication,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.resolveUser(authentication);
            if (user == null && !guestRateLimiter.isAllowed(httpRequest)) {
                flashError(redirectAttributes, "flash.request.rate_limited");
                return "redirect:" + Routes.REQUESTS_NEW + "?error";
            }
            String resolvedGuestPhone = guestPhone;
            if (guestPhoneNational != null && !guestPhoneNational.trim().isEmpty()
                    && guestCountryCode != null && !guestCountryCode.trim().isEmpty()) {
                resolvedGuestPhone = PhoneNumber.normalize(guestCountryCode, guestPhoneNational, guestDdd);
            }
            if (user != null && !user.hasPhone()) {
                String phone = PhoneNumber.normalize(userCountryCode, userPhoneNational, userDdd);
                userService.updateProfile(user, null, null, phone, null);
            }
            Request created = requestService.createRequestWithImage(user, ciudad, address, addressReference,
                    materials, guestName, resolvedGuestPhone, organizationId, estimatedWeight, estimatedVolume, imageFile);

            flashSuccess(redirectAttributes, "flash.request.created");
            if (user == null && resolvedGuestPhone != null && created.getTrackingCode() != null) {
                return "redirect:" + Routes.TRACK + "?telefono=" + resolvedGuestPhone + "&codigo=" + created.getTrackingCode();
            }
            return "redirect:" + Routes.REQUESTS;
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", msg(e.getMessage()));
            return "redirect:" + Routes.REQUESTS;
        } catch (IllegalArgumentException e) {
            flashError(redirectAttributes, e.getMessage());
            return "redirect:" + Routes.REQUESTS_NEW;
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            flashError(redirectAttributes, "flash.request.create_error");
            return "redirect:" + Routes.REQUESTS_NEW;
        }
    }
}
