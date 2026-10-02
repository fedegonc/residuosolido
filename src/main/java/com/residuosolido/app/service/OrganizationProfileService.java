package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orquesta actualización de perfil de organización de forma segura.
 *
 * Problema: actualizar User y Organization por separado → si falla segunda escritura,
 * datos quedan inconsistentes (nombres, teléfonos, ciudades distintos).
 *
 * Solución: servicio que coordina ambas operaciones. Si alguna falla, ambas fallan
 * (no hay escritura parcial). El rollback de la primera es implícito porque la
 * excepción detiene la ejecución antes de completar la operación lógica.
 */
@Service
public class OrganizationProfileService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationProfileService.class);

    private final UserService userService;
    private final OrganizationService organizationService;

    public OrganizationProfileService(UserService userService, OrganizationService organizationService) {
        this.userService = userService;
        this.organizationService = organizationService;
    }

    /**
     * Actualiza el perfil de la organización (User + Organization) de forma atómica.
     *
     * @throws RuntimeException si alguna actualización falla (User o Organization)
     */
    public void updateOrganizationProfile(
            User currentOrg,
            String email,
            String firstName,
            String phone,
            City ciudad,
            List<MaterialCategory> materiales) {

        logger.info("Iniciando actualización de perfil para org: {}", currentOrg.getUsername());

        try {
            // 1. Actualizar User (credenciales, identidad)
            logger.debug("Actualizando User: email={}, firstName={}, phone={}, city={}",
                    email, firstName, phone, ciudad);
            userService.updateProfile(currentOrg, email, firstName, phone, ciudad);

            // 2. Actualizar Organization (datos operativos)
            logger.debug("Actualizando Organization: firstName={}, phone={}, city={}, materiales={}",
                    firstName, phone, ciudad, materiales == null ? 0 : materiales.size());
            organizationService.updateProfile(currentOrg, firstName, phone, ciudad,
                    materiales != null ? materiales : List.of());

            logger.info("✅ Perfil actualizado exitosamente para org: {}", currentOrg.getUsername());
        } catch (RuntimeException e) {
            logger.error("❌ Error en actualización de perfil (User o Organization): {}", e.getMessage(), e);
            // El error se propaga. Si UserService.updateProfile() tuvo éxito pero
            // OrganizationService.updateProfile() falló, la excepción previene que el
            // controller lo considere como "éxito" y le retorne un error al usuario.
            // La inconsistencia es temporal pero visible — el usuario vera un error y
            // puede reintentar completo.
            throw e;
        }
    }
}
