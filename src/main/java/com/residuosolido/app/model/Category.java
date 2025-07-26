package com.residuosolido.app.model;

import jakarta.persistence.*;

@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waste_section_id")
    private WasteSection wasteSection;
    
    public Category() {}
    
    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Category(String name, WasteSection wasteSection) {
        this.name = name;
        this.wasteSection = wasteSection;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public WasteSection getWasteSection() { return wasteSection; }
    public void setWasteSection(WasteSection wasteSection) { this.wasteSection = wasteSection; }
}
