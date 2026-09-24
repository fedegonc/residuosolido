package com.residuosolido.app.controller;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.dto.OrganizationDto;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.CityOrgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
class OrgApiControllerTest {

    private CityOrgService cityOrgService;
    private OrgApiController controller;

    @BeforeEach
    void setUp() {
        cityOrgService = mock(CityOrgService.class);
        controller = new OrgApiController(cityOrgService);
    }

    @Test
    void getOrganizationsByCity_withoutMaterial_mapsAllOrgsToDto() {
        User org = TestFixtures.organization("o1", City.RIVERA, MaterialCategory.PLASTICO);
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of(org));

        List<OrganizationDto> result = controller.getOrganizationsByCity(City.RIVERA, null);

        assertEquals(1, result.size());
        OrganizationDto dto = result.get(0);
        assertEquals("o1", dto.id());
        assertEquals(org.getDisplayName(), dto.displayName());
        assertEquals(List.of(MaterialCategory.PLASTICO), dto.acceptedMaterials());
    }

    @Test
    void getOrganizationsByCity_withMaterial_filtersOrgsThatDoNotAcceptIt() {
        User accepts = TestFixtures.organization("o1", City.RIVERA, MaterialCategory.PLASTICO, MaterialCategory.VIDRIO);
        User rejects = TestFixtures.organization("o2", City.RIVERA, MaterialCategory.CARTON);
        when(cityOrgService.getOrganizationsByCity(City.RIVERA)).thenReturn(List.of(accepts, rejects));

        List<OrganizationDto> result = controller.getOrganizationsByCity(City.RIVERA, MaterialCategory.VIDRIO);

        assertEquals(1, result.size());
        assertEquals("o1", result.get(0).id());
    }

    @Test
    void getOrganizationsByCity_noMatches_returnsEmptyList() {
        when(cityOrgService.getOrganizationsByCity(City.LIVRAMENTO)).thenReturn(List.of());

        assertTrue(controller.getOrganizationsByCity(City.LIVRAMENTO, null).isEmpty());
    }
}
