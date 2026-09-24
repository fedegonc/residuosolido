package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class CityOrgServiceTest {

    private UserRepository userRepository;
    private CityOrgService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CityOrgService(userRepository);
    }

    private User org(String id, City city) {
        return TestFixtures.organization(id, city, MaterialCategory.PAPEL);
    }

    @Test
    void findOrganizationByIdAndCity_blankId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity(" ", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_nullCity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", null));
    }

    @Test
    void findOrganizationByIdAndCity_notFound_throws() {
        when(userRepository.findById("org1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_notAnOrganization_throws() {
        User user = TestFixtures.citizen("u1", "+59899123456");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("u1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_wrongCity_throws() {
        User org = org("org1", City.LIVRAMENTO);
        when(userRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_orgWithoutCity_throwsNotInCity() {
        User org = org("org1", City.RIVERA);
        org.setCity(null);
        when(userRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_unavailableOrg_throws() {
        // Org existe y es de la ciudad, pero no está disponible
        // (perfil incompleto, sin materiales, teléfono inválido o inactiva).
        User org = org("org1", City.RIVERA);
        org.setAcceptedMaterials(List.of());
        when(userRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_inactiveOrg_throwsUnavailable() {
        User org = org("org1", City.RIVERA);
        org.setActive(false);
        when(userRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_orgWithoutPhone_throwsUnavailable() {
        // setPhone valida al setear: el único teléfono "inválido" persistible
        // es null (dato legacy anterior a la validación).
        User org = org("org1", City.RIVERA);
        org.setPhone(null);
        when(userRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void getOrganizationsByCity_filtersOutOrgWithoutMaterials() {
        User noMaterials = org("org3", City.RIVERA);
        noMaterials.setAcceptedMaterials(List.of());
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true))
                .thenReturn(List.of(noMaterials));
        assertTrue(service.getOrganizationsByCity(City.RIVERA).isEmpty());
    }

    @Test
    void findOrganizationByIdAndCity_valid_returnsOrg() {
        User org = org("org1", City.RIVERA);
        when(userRepository.findById("org1")).thenReturn(Optional.of(org));
        assertEquals(org, service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void getOrganizationsByCity_returnsOnlyAvailableOrgs() {
        User available = org("org1", City.RIVERA);
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true))
                .thenReturn(List.of(available));
        List<User> result = service.getOrganizationsByCity(City.RIVERA);
        assertEquals(1, result.size());
        assertEquals(available, result.get(0));
    }

    @Test
    void getOrganizationsByCity_emptyWhenNoActiveOrgs_doesNotFallBack() {
        // Regla de negocio: si no hay organizaciones activas y disponibles,
        // el resultado debe quedar vacío. NUNCA debe volver a organizaciones
        // inactivas o incompletas.
        User inactiveOrg = org("org2", City.RIVERA);
        inactiveOrg.setActive(false);
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true))
                .thenReturn(List.of());
        List<User> result = service.getOrganizationsByCity(City.RIVERA);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrganizationsByCity_filtersOutIncompleteOrgs() {
        User incomplete = org("org3", City.RIVERA);
        incomplete.setProfileCompleted(false);
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true))
                .thenReturn(List.of(incomplete));
        List<User> result = service.getOrganizationsByCity(City.RIVERA);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableCities_onlyReturnsCitiesWithOrgs() {
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true))
                .thenReturn(List.of(org("org1", City.RIVERA)));
        List<City> result = service.getAvailableCities();
        assertTrue(result.contains(City.RIVERA));
    }
}
