package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(length = 1000)
    private String description;

    @ManyToMany
    @JoinTable(
        name = "request_materials",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materials = new ArrayList<>();

    @Column(name = "collection_address", length = 500)
    private String collectionAddress;
    
    private LocalDate scheduledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(length = 2000)
    private String notes;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void addMaterial(Material material) {
        this.materials.add(material);
    }

    public void removeMaterial(Material material) {
        this.materials.remove(material);
    }

    // Legacy compatibility methods - TODO: remove when migration is complete
    /**
     * @deprecated Use collectionAddress field directly
     */
    @Deprecated
    public String getAddress() {
        return collectionAddress;
    }
    
    /**
     * @deprecated Use collectionAddress field directly  
     */
    @Deprecated
    public void setAddress(String address) {
        this.collectionAddress = address;
    }

    /**
     * Legacy method for String materials compatibility
     * @deprecated Materials are managed through List<Material> relationship
     */
    @Deprecated
    public void setMaterials(String materialsString) {
        // Empty implementation - materials managed through entity relationship
    }
    
    /**
     * @return Materials as comma-separated string for display purposes
     */
    @Transient
    public String getMaterialsAsString() {
        if (materials == null || materials.isEmpty()) {
            return "";
        }
        return materials.stream()
                .map(Material::getName)
                .collect(Collectors.joining(", "));
    }

    // JPA-safe equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
