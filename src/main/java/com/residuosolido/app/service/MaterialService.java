package com.residuosolido.app.service;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para operaciones con la entidad Material
 */
@Service
public class MaterialService extends GenericEntityService<Material, Long> {

    private final MaterialRepository materialRepository;

    @Autowired
    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    protected JpaRepository<Material, Long> getRepository() {
        return materialRepository;
    }
    
    /**
     * Encuentra todos los materiales activos
     */
    public List<Material> findAllActive() {
        return materialRepository.findByActiveTrue();
    }
}
