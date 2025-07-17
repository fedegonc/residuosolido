package com.residuosolido.app.controller;

import com.residuosolido.app.service.GenericEntityService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador genérico para operaciones CRUD básicas en cualquier entidad
 * @param <T> Tipo de entidad
 * @param <ID> Tipo del ID de la entidad
 */
public abstract class GenericEntityController<T, ID> {

    protected abstract GenericEntityService<T, ID> getService();
    
    /**
     * Nombre de la entidad en singular (ej: "material")
     */
    protected abstract String getEntityName();
    
    /**
     * Nombre de la entidad en plural (ej: "materials")
     */
    protected abstract String getEntityNamePlural();
    
    /**
     * Ruta base para las operaciones de esta entidad (ej: "/materials")
     */
    protected abstract String getBasePath();
    
    /**
     * Crea una nueva instancia de la entidad
     */
    protected abstract T createNewEntity();
    
    /**
     * Obtiene el ID de una entidad
     */
    protected abstract ID getEntityId(T entity);
    
    /**
     * Muestra el detalle de una entidad
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable ID id, Model model) {
        return getService().findById(id)
                .map(entity -> {
                    model.addAttribute(getEntityName(), entity);
                    return "entities/" + getEntityName() + "/detail";
                })
                .orElse("redirect:" + getBasePath());
    }
    
    /**
     * Muestra el formulario para editar una entidad
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable ID id, Model model) {
        return getService().findById(id)
                .map(entity -> {
                    model.addAttribute(getEntityName(), entity);
                    return "entities/" + getEntityName() + "/form";
                })
                .orElse("redirect:" + getBasePath());
    }
    
    /**
     * Muestra el formulario para crear una nueva entidad
     */
    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute(getEntityName(), createNewEntity());
        return "entities/" + getEntityName() + "/form";
    }
    
    /**
     * Guarda una entidad (nueva o existente)
     */
    @PostMapping("/save")
    public String save(@ModelAttribute T entity, RedirectAttributes redirectAttributes) {
        T savedEntity = getService().save(entity);
        redirectAttributes.addFlashAttribute("message", getEntityName() + " saved successfully");
        return "redirect:" + getBasePath() + "/" + getEntityId(savedEntity);
    }
    
    /**
     * Elimina una entidad
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable ID id, RedirectAttributes redirectAttributes) {
        if (getService().existsById(id)) {
            getService().deleteById(id);
            redirectAttributes.addFlashAttribute("message", getEntityName() + " deleted successfully");
        }
        return "redirect:" + getBasePath();
    }
}
