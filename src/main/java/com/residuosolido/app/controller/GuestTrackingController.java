package com.residuosolido.app.controller;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.RequestQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Permite a invitados (sin cuenta) rastrear sus solicitudes mediante
 * teléfono + código privado de rastreo. El teléfono solo no es suficiente,
 * porque cualquier persona podría conocerlo. El código se entrega al
 * invitado en la pantalla de confirmación al crear la solicitud.
 */
@Controller
public class GuestTrackingController {

    private final RequestQueryService requestQueryService;

    @Autowired
    public GuestTrackingController(RequestQueryService requestQueryService) {
        this.requestQueryService = requestQueryService;
    }

    /** Muestra el formulario de rastreo con resultados opcionales. */
    @GetMapping("/rastrear")
    public String trackGuestForm(@RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "code", required = false) String code,
                                Model model) {
        boolean searched = phone != null && !phone.trim().isEmpty()
                && code != null && !code.trim().isEmpty();
        List<Request> requests = searched
                ? requestQueryService.getGuestRequests(phone, code)
                : List.of();
        model.addAttribute("phone", phone != null ? phone : "");
        model.addAttribute("code", code != null ? code : "");
        model.addAttribute("requests", requests);
        model.addAttribute("searched", searched);
        return "users/track";
    }

    /** Busca solicitudes por teléfono + código (POST desde el formulario). */
    @PostMapping("/rastrear")
    public String trackGuestSubmit(@RequestParam("phone") String phone,
                                   @RequestParam("code") String code,
                                   Model model) {
        List<Request> requests = requestQueryService.getGuestRequests(phone, code);
        model.addAttribute("phone", phone);
        model.addAttribute("code", code);
        model.addAttribute("requests", requests);
        model.addAttribute("searched", true);
        return "users/track";
    }
}
