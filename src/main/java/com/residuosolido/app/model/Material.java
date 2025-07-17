package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing recyclable materials
 */
@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String category;

    private Boolean active;

    @ManyToMany(mappedBy = "acceptedMaterials")
    private List<Organization> organizations = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (active == null) {
            active = true;
        }
    }
}
