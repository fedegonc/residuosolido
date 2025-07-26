package com.residuosolido.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "waste_sections")
public class WasteSection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String icon;
    
    @Column(name = "action_text")
    private String actionText;
    
    @Column(name = "action_icon")
    private String actionIcon;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @OneToMany(mappedBy = "wasteSection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Category> categories = new java.util.ArrayList<>();
    
    // Constructors
    public WasteSection() {}
    
    public WasteSection(String title, String description, String icon, String actionText, String actionIcon, Integer displayOrder) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.actionText = actionText;
        this.actionIcon = actionIcon;
        this.displayOrder = displayOrder;
    }
    
    // Getters and Setters
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
    
    public String getActionIcon() { return actionIcon; }
    public void setActionIcon(String actionIcon) { this.actionIcon = actionIcon; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public java.util.List<Category> getCategories() { return categories; }
    public void setCategories(java.util.List<Category> categories) { this.categories = categories; }
}
