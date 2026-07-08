package com.residuosolido.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document(collection = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    private String id;

    private String name;
    private String description;

    private MaterialCategory category;

    private Boolean active;
}
