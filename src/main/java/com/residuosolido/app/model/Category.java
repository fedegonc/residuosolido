package com.residuosolido.app.model;

import jakarta.persistence.*;

@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    @ManyToMany(mappedBy = "categories", fetch = FetchType.EAGER)
    private java.util.List<WasteSection> wasteSections = new java.util.ArrayList<>();
    
    public Category() {}
    
    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Category(String name) {
        this.name = name;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public java.util.List<WasteSection> getWasteSections() { return wasteSections; }
    public void setWasteSections(java.util.List<WasteSection> wasteSections) { this.wasteSections = wasteSections; }
}
