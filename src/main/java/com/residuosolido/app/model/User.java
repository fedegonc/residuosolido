package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserType userType;

    @Column(nullable = false)
    private String preferredLanguage; // 'es' o 'pt'

    private String firstName;
    private String lastName;

    private String profileImage; // URL imagen de perfil Cloudinary

    @ManyToMany
    @JoinTable(
        name = "user_phones",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "phone_id")
    )
    private List<Phone> phones = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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
        if (this.userType == null) {
            this.userType = UserType.COMUN;
        }
    }

    /**
     * Añade un teléfono a la lista de teléfonos del usuario
     * @param phone Teléfono a añadir
     */
    public void addPhone(Phone phone) {
        this.phones.add(phone);
        phone.getUsers().add(this);
    }

    /**
     * Elimina un teléfono de la lista de teléfonos del usuario
     * @param phone Teléfono a eliminar
     */
    public void removePhone(Phone phone) {
        this.phones.remove(phone);
        phone.getUsers().remove(this);
    }
}
