package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String category;

    private Boolean active;



    @PrePersist
    public void prePersist() {
        if (active == null) {
            active = true;
        }
    }
    
}
