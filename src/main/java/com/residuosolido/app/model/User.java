package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "password")
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String password;

    private Role role;

    private String firstName;
    private String phone;

    private City city;

    private LocalDateTime createdAt;
    private boolean active = true;

    private Boolean profileCompleted = false;

    private List<MaterialCategory> acceptedMaterials = new ArrayList<>();

    public String getDisplayName() {
        return firstName != null && !firstName.isBlank() ? firstName : username;
    }

    public boolean isOrganization() {
        return role == Role.ORGANIZATION;
    }

    public boolean isUser() {
        return role == Role.USER;
    }

    public boolean isProfileComplete() {
        return role == null || role.isProfileComplete(this);
    }

    public boolean hasPhone() {
        return phone != null && !phone.isBlank();
    }

    public boolean hasCity() {
        return city != null;
    }

    public String getAcceptedMaterialsCsv() {
        return acceptedMaterials.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    public void completeProfile() {
        if (!hasPhone()) {
            throw new IllegalStateException("error.profile.phone_required");
        }
        if (!hasCity()) {
            throw new IllegalStateException("error.profile.city_required");
        }
        this.profileCompleted = true;
    }

    public boolean needsProfileCompletion() {
        return !isProfileComplete();
    }

    public void updateProfileDetails(String email, String firstName, String phone, City city) {
        if (email != null) this.email = email.trim();
        if (firstName != null) this.firstName = firstName.trim();
        if (phone != null) this.phone = phone.trim();
        if (city != null) this.city = city;
    }
}
