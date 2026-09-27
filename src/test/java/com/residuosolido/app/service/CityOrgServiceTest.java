package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.repository.OrganizationRepository;
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

    private OrganizationRepository organizationRepository;
    private CityOrgService service;

    @BeforeEach
    void setUp() {
        organizationRepository = mock(OrganizationRepository.class);
        service = new CityOrgService(organizationRepository);
    }

    private Organization org(String id, City city) {
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
        when(organizationRepository.findById("org1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_wrongCity_throws() {
        Organization org = org("org1", City.LIVRAMENTO);
        when(organizationRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_orgWithoutCity_throwsNotInCity() {
        Organization org = org("org1", City.RIVERA);
        org.setCity(null);
        when(organizationRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_unavailableOrg_throws() {
        Organization org = org("org1", City.RIVERA);
        org.setAcceptedMaterials(List.of());
        when(organizationRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_orgWithoutPhone_throwsUnavailable() {
        Organization org = org("org1", City.RIVERA);
        org.setPhone(null);
        when(organizationRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void findOrganizationByIdAndCity_orgIncompleteProfile_throwsUnavailable() {
        Organization org = org("org1", City.RIVERA);
        org.setProfileCompleted(false);
        when(organizationRepository.findById("org1")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void getOrganizationsByCity_nullCity_throwsValidation() {
        assertThrows(com.residuosolido.app.exception.ValidationException.class,
                () -> service.getOrganizationsByCity(null));
    }

    @Test
    void getOrganizationsByCity_filtersOutOrgWithoutMaterials() {
        Organization noMaterials = org("org3", City.RIVERA);
        noMaterials.setAcceptedMaterials(List.of());
        when(organizationRepository.findByCity(City.RIVERA))
                .thenReturn(List.of(noMaterials));
        assertTrue(service.getOrganizationsByCity(City.RIVERA).isEmpty());
    }

    @Test
    void findOrganizationByIdAndCity_valid_returnsOrg() {
        Organization org = org("org1", City.RIVERA);
        when(organizationRepository.findById("org1")).thenReturn(Optional.of(org));
        assertEquals(org, service.findOrganizationByIdAndCity("org1", City.RIVERA));
    }

    @Test
    void getOrganizationsByCity_returnsOnlyAvailableOrgs() {
        Organization available = org("org1", City.RIVERA);
        when(organizationRepository.findByCity(City.RIVERA))
                .thenReturn(List.of(available));
        List<Organization> result = service.getOrganizationsByCity(City.RIVERA);
        assertEquals(1, result.size());
        assertEquals(available, result.get(0));
    }

    @Test
    void getOrganizationsByCity_emptyWhenNoOrgs_doesNotFallBack() {
        when(organizationRepository.findByCity(City.RIVERA))
                .thenReturn(List.of());
        List<Organization> result = service.getOrganizationsByCity(City.RIVERA);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrganizationsByCity_filtersOutIncompleteOrgs() {
        Organization incomplete = org("org3", City.RIVERA);
        incomplete.setProfileCompleted(false);
        when(organizationRepository.findByCity(City.RIVERA))
                .thenReturn(List.of(incomplete));
        List<Organization> result = service.getOrganizationsByCity(City.RIVERA);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableCities_returnsAllCities() {
        List<City> result = service.getAvailableCities();
        assertTrue(result.contains(City.RIVERA));
        assertTrue(result.contains(City.LIVRAMENTO));
    }
}
