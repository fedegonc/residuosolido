package com.residuosolido.app.dto;

import com.residuosolido.app.enums.MaterialCategory;
import java.util.List;

public class OrganizationDto {
    private String id;
    private String displayName;
    private List<MaterialCategory> acceptedMaterials;

    public OrganizationDto() {}

    public OrganizationDto(String id, String displayName, List<MaterialCategory> acceptedMaterials) {
        this.id = id;
        this.displayName = displayName;
        this.acceptedMaterials = acceptedMaterials;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<MaterialCategory> getAcceptedMaterials() { return acceptedMaterials; }
    public void setAcceptedMaterials(List<MaterialCategory> acceptedMaterials) { this.acceptedMaterials = acceptedMaterials; }
}
