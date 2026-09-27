package com.residuosolido.app.model;

import com.residuosolido.app.enums.MaterialCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Campos que solo tienen sentido para un {@link User} con role ORGANIZATION —
 * antes vivían sueltos en User, con valores sin significado para un ciudadano
 * (siempre null/false/vacío). Extraído para que el modelo documente la
 * asimetría real del dominio en vez de esconderla en un comentario.
 *
 * No es un @Document propio: se guarda embebido dentro de User (subdocumento
 * en Mongo). Ver docs/TRADEOFFS.md §38 y OrganizationProfileMigration para
 * la migración de los documentos existentes (antes tenían estos campos en
 * el nivel superior).
 */
@Getter
@Setter
@NoArgsConstructor
public class OrganizationProfile {

    private List<MaterialCategory> acceptedMaterials = new ArrayList<>();
    private Boolean profileCompleted = false;
}
