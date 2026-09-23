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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        if (nombre != null || telefono != null) request.setGuestContact(nombre, telefono, null);
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

    /** Devuelve las opciones de organización para una ciudad como HTML (fetch). */
    @GetMapping(Routes.ORG_OPTIONS)
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
                flashError(redirectAttributes, ServerMessage.FLASH_REQUEST_RATE_LIMITED);
                return "redirect:" + Routes.REQUESTS_NEW + "?error";
            }
            String resolvedGuestPhone = PhoneNumber.resolve(guestCountryCode, guestPhoneNational, guestDdd, guestPhone);
            if (user != null && !user.hasPhone()) {
                String phone = PhoneNumber.normalize(userCountryCode, userPhoneNational, userDdd);
                userService.updateProfile(user, null, null, phone, null);
            }
            Request created = requestService.createRequestWithImage(user, ciudad, address, addressReference,
                    materials, guestName, resolvedGuestPhone, organizationId, imageFile);

            flashSuccess(redirectAttributes, ServerMessage.FLASH_REQUEST_CREATED);
            if (user == null && resolvedGuestPhone != null && created.getTrackingCode() != null) {
                // El "+" de un telefono E.164 sin codificar en una query string se lee como
                // espacio (application/x-www-form-urlencoded) -> PhoneNumber.normalize lo
                // rechaza en /rastrear. Bug real: rompia el 100% de las redirecciones de
                // exito de invitado (telefono siempre empieza con "+").
                String encodedPhone = URLEncoder.encode(resolvedGuestPhone, StandardCharsets.UTF_8);
                return "redirect:" + Routes.TRACK + "?telefono=" + encodedPhone + "&codigo=" + created.getTrackingCode();
            }
            return "redirect:" + Routes.REQUESTS;
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", msg(e));
            // "/mis-solicitudes" exige ROLE_USER — un invitado ahí rebota a login (Security),
            // no al mensaje de error. Mismo criterio que el resto del método: sin sesión -> REQUESTS_NEW.
            User currentUser = userService.resolveUser(authentication);
            return "redirect:" + (currentUser == null ? Routes.REQUESTS_NEW : Routes.REQUESTS);
        } catch (IllegalArgumentException e) {
            flashError(redirectAttributes, e);
            return "redirect:" + Routes.REQUESTS_NEW;
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            flashError(redirectAttributes, ServerMessage.FLASH_REQUEST_CREATE_ERROR);
            return "redirect:" + Routes.REQUESTS_NEW;
        }
    }
}
