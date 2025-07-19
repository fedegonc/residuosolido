package com.residuosolido.app.model;

public class Category {
    private Long id;
    private String name;
    
    public Category() {}
    
    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
