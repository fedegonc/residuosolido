package com.residuosolido.app.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String password;

    private Role role;

    private String preferredLanguage = "es";

    private String firstName;
    private String lastName;
    private String profileImage;

    private String phone;

    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String addressReferences;

    private String preferredCollectionDays;

    @DBRef
    private List<Material> materials = new ArrayList<>();

    @Transient
    private List<Request> requests = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime lastAccessAt;
    private boolean active = true;

    private Boolean profileCompleted = false;

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
