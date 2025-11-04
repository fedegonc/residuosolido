package com.residuosolido.app.manualintake.controller;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.MaterialService;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para gestión de registros manuales de materiales
 * Solo accesible para organizaciones (ROLE_ORGANIZATION)
 * Usa la entidad Request con isManualIntake=true
 */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
@RequestMapping("/acopio/registro-manual")
public class ManualIntakeController {

    private final RequestService requestService;
    private final UserService userService;
    private final MaterialService materialService;

    @Autowired
    public ManualIntakeController(RequestService requestService,
                                  UserService userService,
                                  MaterialService materialService) {
        this.requestService = requestService;
        this.userService = userService;
        this.materialService = materialService;
    }

    /**
     * Lista todos los registros manuales de la organización
     */
    @GetMapping
    public String listIntakes(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long id,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());

        // Modo creación (nuevo)
        if ("new".equals(action)) {
            Request newIntake = new Request();
            newIntake.setIsManualIntake(true);
            model.addAttribute("intake", newIntake);
            model.addAttribute("viewType", "form");
            model.addAttribute("isCreate", true);
            model.addAttribute("materials", materialService.findAllActive());
            return "org/manual-intake";
        }

        // Modo edición
        if ("edit".equals(action) && id != null) {
            Optional<Request> intakeOpt = requestService.findById(id);
            if (intakeOpt.isPresent()) {
                Request intake = intakeOpt.get();
                // Verificar que es manual y pertenece a la organización actual
                if (Boolean.TRUE.equals(intake.getIsManualIntake()) && 
                    intake.getOrganization().getId().equals(currentOrg.getId())) {
                    model.addAttribute("intake", intake);
                    model.addAttribute("viewType", "form");
                    model.addAttribute("isCreate", false);
                    model.addAttribute("materials", materialService.findAllActive());
                    return "org/manual-intake";
                }
            }
        }

        // Modo lista (por defecto) - Obtener solo registros manuales de esta organización
        List<Request> allIntakes = requestService.findAll().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsManualIntake()) && 
                        r.getOrganization() != null &&
                        r.getOrganization().getId().equals(currentOrg.getId()))
            .sorted((r1, r2) -> r2.getScheduledDate().compareTo(r1.getScheduledDate()))
            .toList();
        
        model.addAttribute("intakes", allIntakes);
        model.addAttribute("totalIntakes", allIntakes.size());
        model.addAttribute("query", query);
        model.addAttribute("viewType", "list");
        
        // Calcular total de kg
        BigDecimal totalKg = allIntakes.stream()
            .map(r -> r.getQuantityKg() != null ? r.getQuantityKg() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalKg", totalKg);

        return "org/manual-intake";
    }

    /**
     * Crea o actualiza un registro manual
     */
    @PostMapping
    public String saveIntake(
            @RequestParam(required = false) Long id,
            @RequestParam Long materialId,
            @RequestParam BigDecimal quantityKg,
            @RequestParam String intakeDate,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            Material material = materialService.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));
            
            // Parsear fecha
            LocalDate date = LocalDate.parse(intakeDate, DateTimeFormatter.ISO_LOCAL_DATE);

            if (id == null) {
                // Crear nuevo registro manual
                Request intake = new Request();
                intake.setIsManualIntake(true);
                intake.setOrganization(currentOrg);
                intake.setUser(currentOrg); // El usuario es la misma organización
                intake.getMaterials().add(material);
                intake.setQuantityKg(quantityKg);
                intake.setScheduledDate(date);
                intake.setDescription(source != null ? source : "Registro manual");
                intake.setNotes(notes);
                intake.setStatus(RequestStatus.COMPLETED); // Los registros manuales se marcan como completados
                
                requestService.save(intake);
                redirectAttributes.addFlashAttribute("successMessage", "Registro creado correctamente");
            } else {
                // Actualizar registro existente
                Optional<Request> intakeOpt = requestService.findById(id);
                if (intakeOpt.isPresent() && 
                    Boolean.TRUE.equals(intakeOpt.get().getIsManualIntake()) &&
                    intakeOpt.get().getOrganization().getId().equals(currentOrg.getId())) {
                    
                    Request intake = intakeOpt.get();
                    intake.getMaterials().clear();
                    intake.getMaterials().add(material);
                    intake.setQuantityKg(quantityKg);
                    intake.setScheduledDate(date);
                    intake.setDescription(source != null ? source : "Registro manual");
                    intake.setNotes(notes);
                    
                    requestService.save(intake);
                    redirectAttributes.addFlashAttribute("successMessage", "Registro actualizado correctamente");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para editar este registro");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar registro: " + e.getMessage());
        }

        return "redirect:/acopio/registro-manual";
    }

    /**
     * Elimina un registro manual
     */
    @PostMapping("/delete/{id}")
    public String deleteIntake(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
            Optional<Request> intakeOpt = requestService.findById(id);
            
            if (intakeOpt.isPresent() && 
                Boolean.TRUE.equals(intakeOpt.get().getIsManualIntake()) &&
                intakeOpt.get().getOrganization().getId().equals(currentOrg.getId())) {
                requestService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Registro eliminado correctamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para eliminar este registro");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar registro: " + e.getMessage());
        }

        return "redirect:/acopio/registro-manual";
    }
}
