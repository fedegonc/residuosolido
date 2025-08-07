package com.residuosolido.app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "waste_sections")
public class WasteSection {

    // === Identificador ===
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Contenido visual ===
    @Column(nullable = false)
    private String title;

    private String description;
    @Column(nullable = true)
    private String icon;
    private String actionText;
    private String imageUrl;

    // === Control de visualización ===
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean active;

    // === Relación con categorías ===
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "waste_section_categories",
        joinColumns = @JoinColumn(name = "waste_section_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @OrderBy("name ASC")
    private List<Category> categories = new ArrayList<>();

    // === Constructor ===
    public WasteSection() {}

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    // === toString (opcional) ===
    @Override
    public String toString() {
        return "WasteSection{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", active=" + active +
                '}';
    }
}
