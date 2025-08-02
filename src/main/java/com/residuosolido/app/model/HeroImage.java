package com.residuosolido.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hero_images")
public class HeroImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String title;
    private String description;
    private LocalDateTime uploadDate;
    private Boolean isActive;

    public HeroImage() {}
    
    public HeroImage(String imageUrl) {
        this.imageUrl = imageUrl;
        this.isActive = true;
        this.uploadDate = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive ;
    }
    
    public void setActive(boolean active) {
        this.isActive  = active;
    }

    

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}
