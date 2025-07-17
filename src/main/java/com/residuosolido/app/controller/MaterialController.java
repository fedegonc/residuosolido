package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.service.GenericEntityService;
import com.residuosolido.app.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/materials")
public class MaterialController extends GenericEntityController<Material, Long> {

    private final MaterialService materialService;

    @Autowired
    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @Override
    protected GenericEntityService<Material, Long> getService() {
        return materialService;
    }

    @Override
    protected String getEntityName() {
        return "material";
    }

    @Override
    protected String getEntityNamePlural() {
        return "materials";
    }

    @Override
    protected String getBasePath() {
        return "/materials";
    }

    @Override
    protected Material createNewEntity() {
        Material material = new Material();
        material.setActive(true);
        return material;
    }

    @Override
    protected Long getEntityId(Material entity) {
        return entity.getId();
    }

    @GetMapping
    public String listMaterials(Model model) {
        model.addAttribute("materials", materialService.findAll());
        return "materials/list";
    }
}
