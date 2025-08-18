package com.residuosolido.app.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;



    @Column(nullable = false)
    private String preferredLanguage; // 'es' o 'pt'

    private String firstName;
    private String lastName;

    private String profileImage; // URL imagen de perfil Cloudinary
    
    // Campos de ubicación geográfica
    @Column(name = "direccion", length = 500)
    private String direccion; // Dirección completa en texto
    
    @Column(name = "latitud", precision = 10, scale = 8)
    private java.math.BigDecimal latitud; // Coordenada latitud con precisión 10,8
    
    @Column(name = "longitud", precision = 11, scale = 8)
    private java.math.BigDecimal longitud; // Coordenada longitud con precisión 11,8
    
    @Column(name = "referencias", length = 300)
    private String referencias; // Referencias adicionales de ubicación
    
    // Campos legacy para compatibilidad (deprecated)
    @Deprecated
    private Double latitude;  // Mantener para compatibilidad
    @Deprecated
    private Double longitude; // Mantener para compatibilidad
    @Deprecated
    private String address;   // Mantener para compatibilidad


    // --- Feedback relation temporarily disabled to avoid orphanRemoval issues during User updates ---
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Feedback> feedbacks = new ArrayList<>();
    @Transient
    private List<Feedback> feedbacks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "user_materials",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materials = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime lastAccessAt;
    private boolean active;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = true;

    }


    // @Data genera automáticamente todos los getters/setters
}
