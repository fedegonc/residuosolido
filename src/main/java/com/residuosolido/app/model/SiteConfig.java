package com.residuosolido.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "site_config")
public class SiteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", unique = true)
    private String key;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String value;
    
    // Constructors
    public SiteConfig() {}
    
    public SiteConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
