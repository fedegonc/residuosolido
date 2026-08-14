package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void resolve_delegatesToCityOrgService() {
        User org = org("org1", City.RIVERA);
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA)).thenReturn(org);
        assertEquals(org, resolver.resolve("org1", City.RIVERA));
    }

    @Test
    void resolve_blankOrganizationId_propagatesValidationErrorFromCityOrgService() {
        when(cityOrgService.findOrganizationByIdAndCity("", City.RIVERA))
                .thenThrow(new IllegalArgumentException("error.request.organization_required"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("", City.RIVERA));
    }

    @Test
    void resolve_organizationNotInCity_propagatesError() {
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA))
                .thenThrow(new IllegalArgumentException("error.request.organization_not_in_city"));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("org1", City.RIVERA));
    }
}
