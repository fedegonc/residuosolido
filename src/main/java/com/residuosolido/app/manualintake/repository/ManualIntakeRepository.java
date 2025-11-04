package com.residuosolido.app.manualintake.repository;

import com.residuosolido.app.manualintake.model.ManualIntake;
import com.residuosolido.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManualIntakeRepository extends JpaRepository<ManualIntake, Long> {

    /**
     * Encuentra todos los registros de una organización específica
     */
    List<ManualIntake> findByOrganizationOrderByIntakeDateDesc(User organization);

    /**
     * Encuentra registros de una organización con paginación
     */
    Page<ManualIntake> findByOrganizationOrderByIntakeDateDesc(User organization, Pageable pageable);

    /**
     * Encuentra registros por rango de fechas para una organización
     */
    @Query("SELECT mi FROM ManualIntake mi WHERE mi.organization = :organization " +
           "AND mi.intakeDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mi.intakeDate DESC")
    List<ManualIntake> findByOrganizationAndDateRange(
            @Param("organization") User organization,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Cuenta registros por organización
     */
    long countByOrganization(User organization);

    /**
     * Búsqueda por material, fuente o notas
     */
    @Query("SELECT mi FROM ManualIntake mi WHERE mi.organization = :organization " +
           "AND (LOWER(mi.material.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(mi.source) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(mi.notes) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY mi.intakeDate DESC")
    Page<ManualIntake> searchByOrganization(
            @Param("organization") User organization,
            @Param("query") String query,
            Pageable pageable
    );
}
