package com.residuosolido.app.model;

import jakarta.persistence.*;
import java.util.Objects;
import java.text.Normalizer;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Campos adicionales para reemplazar WasteSection
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean active = true; // Por defecto las categorías están activas

    // === Constructores ===
    public Category() {}

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    /**
     * Método conveniente para verificar si la categoría está activa
     * @return true si la categoría está activa, false en caso contrario
     */
    public boolean isActive() {
        return active != null && active;
    }


    @Transient
    public String getSlug() {
        if (name == null) return "";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // === equals & hashCode (clave para comparaciones, colecciones) ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // === toString (evita imprimir relaciones para prevenir ciclos infinitos) ===
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
