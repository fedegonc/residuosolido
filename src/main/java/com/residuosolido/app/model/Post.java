package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    private String sourceUrl;
    private String sourceName;
    
   
    
    // Métodos de conveniencia
    @Transient
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
    
    @Transient
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
    
    // El nombre de categoría se obtiene a través de la relación con Category
}
