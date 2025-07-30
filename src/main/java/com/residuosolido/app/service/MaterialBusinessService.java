package com.residuosolido.app.service;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialBusinessService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UserService userService;

    public Optional<Material> findMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    public List<Material> getActiveMaterials() {
        return materialRepository.findByActiveTrue();
    }

    public List<Material> getMaterialsByOrganization(Long organizationId) {
        return materialRepository.findByOrganizationsId(organizationId);
    }

    public List<Material> getMaterialsForOrganization(String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }

        List<Material> allMaterials = getActiveMaterials();
        List<Material> acceptedMaterials = getMaterialsByOrganization(user.getId());

        // Marcar materiales aceptados
        for (Material material : allMaterials) {
            material.setAccepted(acceptedMaterials.contains(material));
        }

        return allMaterials;
    }

    public void toggleMaterialAcceptance(String username, Long materialId) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }

        Optional<Material> materialOpt = findMaterialById(materialId);
        if (materialOpt.isEmpty()) {
            throw new RuntimeException("Material no encontrado: " + materialId);
        }

        Material material = materialOpt.get();
        
        if (material.getOrganizations().contains(user)) {
            material.getOrganizations().remove(user);
        } else {
            material.getOrganizations().add(user);
        }

        materialRepository.save(material);
    }
}
