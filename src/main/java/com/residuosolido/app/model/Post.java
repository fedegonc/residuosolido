package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    
    // Estado del post (activo/inactivo)
    private Boolean active = true;
    
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * Método conveniente para verificar si el post está activo
     * @return true si el post está activo, false en caso contrario
     */
    public boolean isActive() {
        return active != null && active;
    }
    
    // Métodos de conveniencia
    @Transient
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
    
    @Transient
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
    
}
