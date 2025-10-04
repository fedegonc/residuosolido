package com.residuosolido.app.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.Request;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private String preferredLanguage = "es"; // Default español

    private String firstName;
    private String lastName;
    private String profileImage; // URL Cloudinary
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    // Ubicación geográfica (nombres en inglés para consistencia)
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "address_references", length = 300)
    private String addressReferences;

    @ManyToMany
    @JoinTable(
        name = "user_materials",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materials = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Request> requests = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime lastAccessAt;
    private boolean active = true;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastAccessAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastAccessAt = LocalDateTime.now();
    }

    // Métodos de negocio
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
        feedback.setUser(this);
    }

    public void removeFeedback(Feedback feedback) {
        feedbacks.remove(feedback);
        feedback.setUser(null);
    }

    public void addRequest(Request request) {
        requests.add(request);
        request.setUser(this);
    }

    public void removeRequest(Request request) {
        requests.remove(request);
        request.setUser(null);
    }

    // equals/hashCode solo con ID para evitar problemas JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
