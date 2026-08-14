package com.residuosolido.app.dto;

import com.residuosolido.app.enums.MaterialCategory;
import java.util.List;

public record OrganizationDto(String id, String displayName, List<MaterialCategory> acceptedMaterials) {
}
