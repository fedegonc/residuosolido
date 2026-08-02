package com.residuosolido.app.controller;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.InformalCollector;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.InformalCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class InformalCollectorController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(InformalCollectorController.class);

    private final InformalCollectorService informalCollectorService;

    @Autowired
    public InformalCollectorController(InformalCollectorService informalCollectorService) {
        this.informalCollectorService = informalCollectorService;
    }

    @GetMapping("/acopio/catadores")
    public String list(Authentication authentication, Model model) {
        User organization = getCurrentUser(authentication);
        List<InformalCollector> collectors = informalCollectorService.findByOrganization(organization);
        model.addAttribute("catadores", collectors);
        model.addAttribute("cities", City.values());
        model.addAttribute("materialCategories", MaterialCategory.values());
        return "org/catadores";
    }

    @GetMapping("/acopio/catadores/edit/{id}")
    public String edit(@PathVariable String id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User organization = getCurrentUser(authentication);
            InformalCollector collector = informalCollectorService.findOwnedById(id, organization);

            model.addAttribute("catador", collector);
            model.addAttribute("catadores", informalCollectorService.findByOrganization(organization));
            model.addAttribute("cities", City.values());
            model.addAttribute("materialCategories", MaterialCategory.values());
            return "org/catadores";
        } catch (Exception e) {
            logger.error("Error al cargar catador: {}", e.getMessage(), e);
            flashError(redirectAttributes, "flash.catador.load_error", e.getMessage());
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
            User organization = getCurrentUser(authentication);
            informalCollectorService.saveOrUpdate(id, organization, name, phone, city, materials, notes, active);
            String messageKey = (id != null && !id.isBlank()) ? "flash.catador.updated" : "flash.catador.created";
            flashSuccess(redirectAttributes, messageKey);
        } catch (Exception e) {
            logger.error("Error al guardar catador: {}", e.getMessage(), e);
            flashError(redirectAttributes, "flash.catador.save_error", e.getMessage());
        }
        return "redirect:/acopio/catadores";
    }

    @PostMapping("/acopio/catadores/{id}/delete")
    public String delete(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User organization = getCurrentUser(authentication);
            informalCollectorService.delete(id, organization);
            flashSuccess(redirectAttributes, "flash.catador.deleted");
        } catch (Exception e) {
            logger.error("Error al eliminar catador: {}", e.getMessage(), e);
            flashError(redirectAttributes, "flash.catador.delete_error", e.getMessage());
        }
        return "redirect:/acopio/catadores";
    }
}
