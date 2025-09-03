package com.residuosolido.app.service;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para operaciones con la entidad Material
 */
@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    @Autowired
    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    /**
     * Encuentra todos los materiales activos
     */
    public List<Material> findAllActive() {
        return materialRepository.findByActiveTrue();
    }

    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    public Material save(Material material) {
        return materialRepository.save(material);
    }

    public void deleteById(Long id) {
        materialRepository.deleteById(id);
    }
}
