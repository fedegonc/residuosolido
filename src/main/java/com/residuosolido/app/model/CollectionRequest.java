package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    private String address;
    private String city;
    private Double quantity;
    private String unit; // kg, bolsas, unidades
    private LocalDate preferredDate;
    private String preferredTime; // manana, tarde, noche
    private String notes;
    private String contactPhone;
    private String imageUrl; // URL de imagen en Cloudinary

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum RequestStatus {
        PENDING, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
