package com.residuosolido.app.controller;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GuestTrackingController {

    private final RequestService requestService;

    @Autowired
    public GuestTrackingController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/rastrear")
    public String trackGuestForm(@RequestParam(value = "phone", required = false) String phone, Model model) {
        List<Request> requests = requestService.getGuestRequestsByPhone(phone);
        boolean searched = phone != null && !phone.trim().isEmpty();
        model.addAttribute("phone", searched ? phone : "");
        model.addAttribute("requests", requests);
        model.addAttribute("searched", searched);
        return "users/track";
    }

    @PostMapping("/rastrear")
    public String trackGuestSubmit(@RequestParam("phone") String phone, Model model) {
        List<Request> requests = requestService.getGuestRequestsByPhone(phone);
        model.addAttribute("phone", phone);
        model.addAttribute("requests", requests);
        model.addAttribute("searched", true);
        return "users/track";
    }
}
