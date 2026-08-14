package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrgResolverTest {

    private CityOrgService cityOrgService;
    private OrgResolver resolver;

    @BeforeEach
    void setUp() {
        cityOrgService = mock(CityOrgService.class);
        resolver = new OrgResolver(cityOrgService);
    }

    private User org(String id, City city) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.ORGANIZATION);
        u.setCity(city);
        return u;
    }

    @Test
    void resolve_withOrganizationId_delegatesToCityOrgService() {
        User org = org("org1", City.RIVERA);
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA)).thenReturn(org);
        assertEquals(org, resolver.resolve("org1", City.RIVERA));
    }

    @Test
    void resolve_withoutOrganizationId_autoAssignsFirstOrgForCity() {
        User org = org("org1", City.RIVERA);
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of(org));
        assertEquals(org, resolver.resolve(null, City.RIVERA));
    }

    @Test
    void resolve_blankOrganizationId_autoAssignsFirstOrgForCity() {
        User org = org("org1", City.RIVERA);
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of(org));
        assertEquals(org, resolver.resolve("  ", City.RIVERA));
    }

    @Test
    void resolve_noOrgsAvailable_throws() {
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null, City.RIVERA));
    }

    @Test
    void reassignIfNeeded_sameCity_keepsCurrentOrg() {
        User currentOrg = org("org1", City.RIVERA);
        User result = resolver.reassignIfNeeded(currentOrg, City.RIVERA);
        assertEquals(currentOrg, result);
    }

    @Test
    void reassignIfNeeded_differentCity_reassigns() {
        User currentOrg = org("org1", City.RIVERA);
        User newOrg = org("org2", City.LIVRAMENTO);
        when(cityOrgService.getOrganizationsByCity(City.LIVRAMENTO)).thenReturn(List.of(newOrg));
        User result = resolver.reassignIfNeeded(currentOrg, City.LIVRAMENTO);
        assertEquals(newOrg, result);
    }

    @Test
    void reassignIfNeeded_nullCurrentOrg_assignsFirstForCity() {
        User newOrg = org("org1", City.RIVERA);
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of(newOrg));
        assertEquals(newOrg, resolver.reassignIfNeeded(null, City.RIVERA));
    }
}
