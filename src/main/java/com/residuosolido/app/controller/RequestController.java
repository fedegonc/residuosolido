package com.residuosolido.app.controller;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.service.LocalImageService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RequestController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    private final RequestService requestService;
    private final UserService userService;
    private final LocalImageService imageService;
    private final MessageSource messageSource;

    @Autowired
    public RequestController(RequestService requestService, UserService userService,
                             LocalImageService imageService, MessageSource messageSource) {
        this.requestService = requestService;
        this.userService = userService;
        this.imageService = imageService;
        this.messageSource = messageSource;
    }

    @GetMapping("/solicitudes/nueva")
    public String newRequestForm(Model model, Authentication authentication) {
        model.addAttribute("request", new Request());
        model.addAttribute("isEdit", false);
        model.addAttribute("isGuest", userService.isAnonymous(authentication));
        model.addAttribute("cities", requestService.getAvailableCities());
        model.addAttribute("materials", MaterialCategory.values());
        model.addAttribute("timeSlots", TimeSlot.values());
        return "users/request-form";
    }

    @PostMapping("/solicitudes")
    public String createRequest(@RequestParam("city") City city,
                                @RequestParam("address") String address,
                                @RequestParam(value = "addressReference", required = false) String addressReference,
                                @RequestParam(value = "materials", required = false) List<MaterialCategory> materials,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(value = "guestName", required = false) String guestName,
                                @RequestParam(value = "guestPhone", required = false) String guestPhone,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.resolveUser(authentication);
            requestService.createRequestWithImage(user, city, address, addressReference,
                    materials, guestName, guestPhone, imageFile, imageService);

            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.request.created", null, LocaleContextHolder.getLocale()));
            if (user == null) {
                if (guestPhone != null && !guestPhone.trim().isEmpty()) {
                    return "redirect:/rastrear?phone=" + guestPhone.trim();
                }
                return "redirect:/solicitudes/nueva?success";
            }
            return "redirect:/solicitudes";
        } catch (Exception e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            return "redirect:/solicitudes/nueva?error";
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/solicitudes")
    public String listUserRequests(Authentication authentication, Model model) {
        User user = userService.findAuthenticatedUserByUsername(authentication.getName());
        model.addAttribute("requests", requestService.getRequestsByUser(user));
        return "users/requests";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/solicitud/{id}")
    public String requestDetail(@PathVariable String id, Authentication authentication, Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findAuthenticatedUserByUsername(authentication.getName());
            Request request = requestService.getOwnedRequest(id, user);
            model.addAttribute("request", request);
            return "users/request-detail";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.not_owned", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitudes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.load_error", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitudes";
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/solicitud/{id}/editar")
    public String editRequestForm(@PathVariable String id, Authentication authentication, Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findAuthenticatedUserByUsername(authentication.getName());
            Request request = requestService.getEditableOwnedRequest(id, user);
            model.addAttribute("request", request);
            model.addAttribute("isEdit", true);
            model.addAttribute("cities", requestService.getAvailableCities());
            model.addAttribute("materials", MaterialCategory.values());
            model.addAttribute("timeSlots", TimeSlot.values());
            return "users/request-form";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.not_owned", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitudes";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.edit.pending_only", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitudes";
        } catch (Exception e) {
            logger.error("Error al cargar formulario de edición: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.load_error", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitudes";
        }
    }

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
            User user = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.updateRequest(id, user, city, address, addressReference, materials, imageFile, imageService);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.request.updated", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitud/" + id;
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.not_owned", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitudes";
        } catch (Exception e) {
            logger.error("Error al actualizar solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.update_error", null, LocaleContextHolder.getLocale()));
            return "redirect:/solicitud/" + id;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/solicitud/{id}/eliminar")
    public String deleteRequest(@PathVariable String id, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.deleteOwnedRequest(id, user);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.request.deleted", null, LocaleContextHolder.getLocale()));
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.not_owned", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.request.delete_error", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/solicitudes";
    }

}
