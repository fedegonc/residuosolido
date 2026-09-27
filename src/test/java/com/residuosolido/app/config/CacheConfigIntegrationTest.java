package com.residuosolido.app.config;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.OrganizationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica que @Cacheable en CityOrgService.getOrganizationsByCity NO sea
 * decorativo (misma clase de bug que @Async/@Transactional, ver
 * MEJORAS.md #208) y que OrganizationService.updateProfile realmente invalide
 * el cache — con el contexto real de Spring, no una instancia plana del
 * service (un mock/new directo nunca pasa por el proxy de @Cacheable).
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class CacheConfigIntegrationTest {

    @Autowired
    private CityOrgService cityOrgService;

    @Autowired
    private OrganizationService organizationService;

    @MockBean
    private OrganizationRepository organizationRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getOrganizationsByCity_secondCall_hitsCacheNotRepository() {
        Organization org = TestFixtures.organization("o1", City.RIVERA, MaterialCategory.PLASTICO);
        when(organizationRepository.findByCity(City.RIVERA))
                .thenReturn(List.of(org));

        List<Organization> first = cityOrgService.getOrganizationsByCity(City.RIVERA);
        List<Organization> second = cityOrgService.getOrganizationsByCity(City.RIVERA);

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        verify(organizationRepository, times(1))
                .findByCity(City.RIVERA);
    }

    @Test
    void updateProfile_evictsCache_nextCallHitsRepositoryAgain() {
        Organization org = TestFixtures.organization("o2", City.LIVRAMENTO, MaterialCategory.VIDRIO);
        User user = new User();
        user.setId("o2");
        user.setUsername("coop");

        when(organizationRepository.findByCity(City.LIVRAMENTO))
                .thenReturn(List.of(org));
        when(organizationRepository.findById("o2")).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById("o2")).thenReturn(Optional.of(user));

        cityOrgService.getOrganizationsByCity(City.LIVRAMENTO);
        organizationService.updateProfile(user, "Coop", "+59899123456", City.LIVRAMENTO, List.of(MaterialCategory.VIDRIO));
        cityOrgService.getOrganizationsByCity(City.LIVRAMENTO);

        verify(organizationRepository, times(2))
                .findByCity(eq(City.LIVRAMENTO));
    }
}
