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

    public Material findByName(String name) {
        return materialRepository.findByName(name);
    }

    /**
     * Obtiene los materiales que acepta una organización específica.
     * La relación Many-to-Many entre User y Material aún no está modelada,
     * por lo que este método devuelve un placeholder vacío hasta que se agreguen
     * las entidades y repositorios necesarios.
     */
    public List<Material> getAcceptedMaterialsByOrganization(User organization) {
        // Cuando exista la tabla intermedia, realizar la consulta real, por ejemplo:
        // return materialRepository.findByAcceptedByOrganizationsContaining(organization);
        return new ArrayList<>();
    }

    /**
     * Actualiza los materiales aceptados por una organización. La lógica real depende de la
     * relación Many-to-Many pendiente, por lo que se mantiene como stub informativo.
     */
    public void updateAcceptedMaterials(User organization, List<Long> materialIds) {
        // Implementar cuando se defina la relación:
        // 1. Limpiar materiales actuales de la organización
        // 2. Agregar nuevos materiales seleccionados
    }

    /**
     * Alterna la aceptación de un material por una organización. Este método actúa como
     * recordatorio del comportamiento esperado una vez exista la relación Many-to-Many.
     */
    public boolean toggleMaterialAcceptance(User organization, Long materialId) {
        // Próximos pasos cuando la relación esté disponible:
        // 1. Verificar si la organización ya acepta este material
        // 2. Si lo acepta, removerlo; si no lo acepta, agregarlo
        // 3. Retornar true si se agregó, false si se removió
        return true; // Placeholder
    }
}
