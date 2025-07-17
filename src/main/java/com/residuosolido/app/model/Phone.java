package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing phone numbers in the system
 */
@Entity
@Table(name = "phones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private PhoneType type;
    
    @ManyToMany(mappedBy = "phones")
    private List<User> users = new ArrayList<>();
    
    private boolean active;
    
    /**
     * Enum that defines the possible phone types
     */
    public enum PhoneType {
        MOBILE("Móvil"),
        HOME("Casa"),
        WORK("Trabajo"),
        OTHER("Otro");
        
        private final String displayName;
        
        PhoneType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    public void prePersist() {
        this.active = true;
    }
}
