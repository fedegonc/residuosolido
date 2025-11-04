package com.residuosolido.app.manualintake.model;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad para registros manuales de materiales recibidos
 * Permite a las organizaciones registrar materiales que reciben
 * directamente de vecinos u otras fuentes independientes de solicitudes
 */
@Entity
@Table(name = "manual_intakes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualIntake {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Organización que registra el ingreso manual
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private User organization;

    /**
     * Material recibido
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    /**
     * Cantidad en kilogramos
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityKg;

    /**
     * Fecha del registro (puede ser diferente a la fecha de creación)
     * Permite registrar ingresos de fechas pasadas
     */
    @Column(name = "intake_date", nullable = false)
    private LocalDate intakeDate;

    /**
     * Notas adicionales sobre el ingreso
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Origen del material (ej: "Vecino", "Donación", "Recolección directa")
     */
    @Column(length = 200)
    private String source;

    /**
     * Timestamp de creación del registro
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp de última actualización
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.intakeDate == null) {
            this.intakeDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualIntake that = (ManualIntake) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
