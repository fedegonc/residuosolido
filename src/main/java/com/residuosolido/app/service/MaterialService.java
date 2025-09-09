package com.residuosolido.app.service;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public long count() {
        return materialRepository.count();
    }

    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    /**
     * Obtiene los materiales que acepta una organización específica
     * TODO: Implementar relación Many-to-Many entre User y Material
     * Por ahora retorna lista vacía como placeholder
     */
    public List<Material> getAcceptedMaterialsByOrganization(User organization) {
        // TODO: Implementar consulta real cuando se cree la tabla de relación
        // return materialRepository.findByAcceptedByOrganizationsContaining(organization);
        return new ArrayList<>();
    }

    /**
     * Actualiza los materiales aceptados por una organización
     * TODO: Implementar relación Many-to-Many entre User y Material
     */
    public void updateAcceptedMaterials(User organization, List<Long> materialIds) {
        // TODO: Implementar lógica real cuando se cree la tabla de relación
        // 1. Limpiar materiales actuales de la organización
        // 2. Agregar nuevos materiales seleccionados
        System.out.println("TODO: Actualizar materiales aceptados para organización " + organization.getUsername());
    }

    /**
     * Alterna la aceptación de un material por una organización
     * TODO: Implementar relación Many-to-Many entre User y Material
     */
    public boolean toggleMaterialAcceptance(User organization, Long materialId) {
        // TODO: Implementar lógica real cuando se cree la tabla de relación
        // 1. Verificar si la organización ya acepta este material
        // 2. Si lo acepta, removerlo; si no lo acepta, agregarlo
        // 3. Retornar true si se agregó, false si se removió
        System.out.println("TODO: Toggle material " + materialId + " para organización " + organization.getUsername());
        return true; // Placeholder
    }
}
