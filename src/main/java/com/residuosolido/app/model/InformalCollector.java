package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
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

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
@Document(collection = "informal_collectors")
public class InformalCollector {

    @Id
    private String id;

    private String organizationId;

    private String name;

    private String phone;

    private City city;

    private List<MaterialCategory> materials = new ArrayList<>();

    private String notes;

    private boolean active = true;

    private LocalDateTime createdAt;

    public static InformalCollector create(String organizationId, String name, String phone, City city,
                                           List<MaterialCategory> materials, String notes) {
        InformalCollector collector = new InformalCollector();
        collector.organizationId = organizationId;
        collector.name = name.trim();
        collector.phone = phone.trim();
        collector.city = city;
        collector.materials = materials != null ? materials : List.of();
        collector.notes = notes;
        collector.active = true;
        collector.createdAt = LocalDateTime.now();
        return collector;
    }

    public void updateDetails(String name, String phone, City city, List<MaterialCategory> materials,
                              String notes, boolean active) {
        this.name = name.trim();
        this.phone = phone.trim();
        this.city = city;
        this.materials = materials != null ? materials : List.of();
        this.notes = notes;
        this.active = active;
    }

    public boolean belongsTo(String orgId) {
        return orgId != null && orgId.equals(this.organizationId);
    }

    public boolean belongsTo(User organization) {
        return organization != null && belongsTo(organization.getId());
    }

}
