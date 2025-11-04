package com.residuosolido.app.manualintake.service;

import com.residuosolido.app.manualintake.model.ManualIntake;
import com.residuosolido.app.manualintake.repository.ManualIntakeRepository;
import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar registros manuales de materiales
 */
@Service
public class ManualIntakeService {

    private final ManualIntakeRepository manualIntakeRepository;
    private final MaterialRepository materialRepository;

    @Autowired
    public ManualIntakeService(ManualIntakeRepository manualIntakeRepository,
                               MaterialRepository materialRepository) {
        this.manualIntakeRepository = manualIntakeRepository;
        this.materialRepository = materialRepository;
    }

    /**
     * Crea un nuevo registro manual de ingreso
     */
    @Transactional
    public ManualIntake createIntake(User organization, Long materialId, BigDecimal quantityKg,
                                     LocalDate intakeDate, String source, String notes) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));

        ManualIntake intake = new ManualIntake();
        intake.setOrganization(organization);
        intake.setMaterial(material);
        intake.setQuantityKg(quantityKg);
        intake.setIntakeDate(intakeDate != null ? intakeDate : LocalDate.now());
        intake.setSource(source);
        intake.setNotes(notes);

        return manualIntakeRepository.save(intake);
    }

    /**
     * Obtiene todos los registros de una organización
     */
    @Transactional(readOnly = true)
    public List<ManualIntake> getIntakesByOrganization(User organization) {
        List<ManualIntake> intakes = manualIntakeRepository.findByOrganizationOrderByIntakeDateDesc(organization);
        // Forzar carga de relaciones lazy
        intakes.forEach(intake -> {
            intake.getMaterial().getName();
            intake.getOrganization().getUsername();
        });
        return intakes;
    }

    /**
     * Obtiene registros paginados de una organización
     */
    @Transactional(readOnly = true)
    public Page<ManualIntake> getIntakesByOrganization(User organization, Pageable pageable) {
        Page<ManualIntake> intakes = manualIntakeRepository.findByOrganizationOrderByIntakeDateDesc(organization, pageable);
        // Forzar carga de relaciones lazy
        intakes.forEach(intake -> {
            intake.getMaterial().getName();
            intake.getOrganization().getUsername();
        });
        return intakes;
    }

    /**
     * Busca registros por query
     */
    @Transactional(readOnly = true)
    public Page<ManualIntake> searchIntakes(User organization, String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getIntakesByOrganization(organization, pageable);
        }
        Page<ManualIntake> intakes = manualIntakeRepository.searchByOrganization(organization, query, pageable);
        // Forzar carga de relaciones lazy
        intakes.forEach(intake -> {
            intake.getMaterial().getName();
            intake.getOrganization().getUsername();
        });
        return intakes;
    }

    /**
     * Obtiene un registro por ID
     */
    @Transactional(readOnly = true)
    public Optional<ManualIntake> findById(Long id) {
        Optional<ManualIntake> intake = manualIntakeRepository.findById(id);
        intake.ifPresent(i -> {
            i.getMaterial().getName();
            i.getOrganization().getUsername();
        });
        return intake;
    }

    /**
     * Actualiza un registro existente
     */
    @Transactional
    public ManualIntake updateIntake(Long id, Long materialId, BigDecimal quantityKg,
                                     LocalDate intakeDate, String source, String notes) {
        ManualIntake intake = manualIntakeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));

        if (materialId != null) {
            Material material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));
            intake.setMaterial(material);
        }

        if (quantityKg != null) {
            intake.setQuantityKg(quantityKg);
        }

        if (intakeDate != null) {
            intake.setIntakeDate(intakeDate);
        }

        if (source != null) {
            intake.setSource(source);
        }

        if (notes != null) {
            intake.setNotes(notes);
        }

        return manualIntakeRepository.save(intake);
    }

    /**
     * Elimina un registro
     */
    @Transactional
    public void deleteIntake(Long id) {
        manualIntakeRepository.deleteById(id);
    }

    /**
     * Obtiene registros por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<ManualIntake> getIntakesByDateRange(User organization, LocalDate startDate, LocalDate endDate) {
        List<ManualIntake> intakes = manualIntakeRepository.findByOrganizationAndDateRange(organization, startDate, endDate);
        // Forzar carga de relaciones lazy
        intakes.forEach(intake -> {
            intake.getMaterial().getName();
            intake.getOrganization().getUsername();
        });
        return intakes;
    }

    /**
     * Cuenta registros de una organización
     */
    public long countByOrganization(User organization) {
        return manualIntakeRepository.countByOrganization(organization);
    }

    /**
     * Calcula el total de kg registrados por una organización
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalKgByOrganization(User organization) {
        List<ManualIntake> intakes = manualIntakeRepository.findByOrganizationOrderByIntakeDateDesc(organization);
        return intakes.stream()
                .map(ManualIntake::getQuantityKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
