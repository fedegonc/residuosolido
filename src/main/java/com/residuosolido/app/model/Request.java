package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    
    private String description;

    @ManyToMany
    @JoinTable(
        name = "request_materials",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materials = new ArrayList<>();


    private String collectionAddress;
    private LocalDate scheduledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String notes;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addMaterial(Material material) {
        this.materials.add(material);
    }

    public void removeMaterial(Material material) {
        this.materials.remove(material);
    }

    // Campo legacy para compatibilidad con String
    @Transient
    public String getMaterialsAsString() {
        if (materials == null || materials.isEmpty()) {
            return "";
        }
        return materials.stream()
                .map(Material::getName)
                .collect(Collectors.joining(", "));
    }
    
    // Método adicional para compatibilidad con String materials
    public void setMaterials(String materialsString) {
        // Este método existe para compatibilidad pero no hace nada
        // Los materiales se manejan a través de la lista de Material
    }

    public enum RequestStatus {
        PENDING, ACCEPTED, REJECTED, COMPLETED
    }
}
