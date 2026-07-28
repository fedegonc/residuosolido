package com.residuosolido.app.controller;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.InformalCollector;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.InformalCollectorService;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class InformalCollectorController {

    private final InformalCollectorService informalCollectorService;
    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public InformalCollectorController(InformalCollectorService informalCollectorService, UserService userService, MessageSource messageSource) {
        this.informalCollectorService = informalCollectorService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/acopio/catadores")
    public String list(Authentication authentication, Model model) {
        User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
        List<InformalCollector> collectors = informalCollectorService.findByOrganization(organization);
        model.addAttribute("catadores", collectors);
        model.addAttribute("cities", City.values());
        model.addAttribute("materialCategories", MaterialCategory.values());
        return "org/catadores";
    }

    @GetMapping("/acopio/catadores/edit/{id}")
    public String edit(@PathVariable String id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
            InformalCollector collector = informalCollectorService.findOwnedById(id, organization);

            model.addAttribute("catador", collector);
            model.addAttribute("catadores", informalCollectorService.findByOrganization(organization));
            model.addAttribute("cities", City.values());
            model.addAttribute("materialCategories", MaterialCategory.values());
            return "org/catadores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.catador.load_error", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
            return "redirect:/acopio/catadores";
        }
    }

    @PostMapping("/acopio/catadores")
    public String save(@RequestParam(required = false) String id,
                       @RequestParam String name,
                       @RequestParam String phone,
                       @RequestParam City city,
                       @RequestParam(required = false) List<MaterialCategory> materials,
                       @RequestParam(required = false) String notes,
                       @RequestParam(required = false, defaultValue = "false") boolean active,
                       Authentication authentication,
                       RedirectAttributes redirectAttributes) {
        try {
            User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
            informalCollectorService.saveOrUpdate(id, organization, name, phone, city, materials, notes, active);
            String messageKey = (id != null && !id.isBlank()) ? "flash.catador.updated" : "flash.catador.created";
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.catador.save_error", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
        return "redirect:/acopio/catadores";
    }

    @PostMapping("/acopio/catadores/{id}/delete")
    public String delete(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
            informalCollectorService.delete(id, organization);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.catador.deleted", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.catador.delete_error", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
        return "redirect:/acopio/catadores";
    }
}
