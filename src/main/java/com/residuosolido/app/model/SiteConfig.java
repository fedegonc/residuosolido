package com.residuosolido.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "site_config")
public class SiteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String configKey;
    
    private String configValue;
    
    // Constructors
    public SiteConfig() {}
    
    public SiteConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
}
