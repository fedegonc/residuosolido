package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private Integer dailyCapacity;
    private Boolean active;

    @ManyToMany
    @JoinTable(
        name = "organization_materials",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> acceptedMaterials = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.active = true;
    }
    
    /**
     * Añade un material a la lista de materiales aceptados por la organización
     * @param material Material a añadir
     */
    public void addAcceptedMaterial(Material material) {
        this.acceptedMaterials.add(material);
        // Comentado temporalmente hasta que Material tenga getter
        // material.getOrganizations().add(this);
    }
    
    /**
     * Elimina un material de la lista de materiales aceptados por la organización
     * @param material Material a eliminar
     */
    public void removeAcceptedMaterial(Material material) {
        this.acceptedMaterials.remove(material);
        // Comentado temporalmente hasta que Material tenga getter
        // material.getOrganizations().remove(this);
    }
}
