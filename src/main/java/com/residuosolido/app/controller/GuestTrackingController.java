package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

    private final RequestService requestService;

    @Autowired
    public GuestTrackingController(RequestService requestService) {
        this.requestService = requestService;
    }

    /** Muestra el formulario de rastreo con resultados opcionales. */
    @GetMapping(Routes.TRACK)
    public String trackGuestForm(@RequestParam(value = "telefono", required = false) String telefono,
                                @RequestParam(value = "codigo", required = false) String codigo,
                                @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                Model model) {
        // HTMX: devolver solo el modal cuando no hay búsqueda
        if ("true".equals(hxRequest) && (telefono == null || telefono.isBlank()) && (codigo == null || codigo.isBlank())) {
            return "fragments/track-modal :: modal";
        }
        boolean searched = telefono != null && !telefono.trim().isEmpty()
                && codigo != null && !codigo.trim().isEmpty();
        List<Request> requests = searched
                ? requestService.getGuestRequests(telefono, codigo)
                : List.of();
        model.addAttribute("telefono", telefono != null ? telefono : "");
        model.addAttribute("codigo", codigo != null ? codigo : "");
        model.addAttribute("requests", requests);
        model.addAttribute("searched", searched);
        return "users/track";
    }
}
