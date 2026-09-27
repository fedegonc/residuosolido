package com.residuosolido.app.config;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.CityOrgService;
import com.residuosolido.app.service.UserService;
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
 * MEJORAS.md #208) y que UserService.updateUser realmente invalide el
 * cache — con el contexto real de Spring, no una instancia plana del
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
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getOrganizationsByCity_secondCall_hitsCacheNotRepository() {
        User org = TestFixtures.organization("o1", City.RIVERA, MaterialCategory.PLASTICO);
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true))
                .thenReturn(List.of(org));

        List<User> first = cityOrgService.getOrganizationsByCity(City.RIVERA);
        List<User> second = cityOrgService.getOrganizationsByCity(City.RIVERA);

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        verify(userRepository, times(1))
                .findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true);
    }

    @Test
    void updateUser_evictsCache_nextCallHitsRepositoryAgain() {
        User org = TestFixtures.organization("o2", City.LIVRAMENTO, MaterialCategory.VIDRIO);
        when(userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, City.LIVRAMENTO, true))
                .thenReturn(List.of(org));
        when(userRepository.findById("o2")).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        cityOrgService.getOrganizationsByCity(City.LIVRAMENTO);
        userService.updateUser(org);
        cityOrgService.getOrganizationsByCity(City.LIVRAMENTO);

        verify(userRepository, times(2))
                .findByRoleAndCityAndActive(eq(Role.ORGANIZATION), eq(City.LIVRAMENTO), eq(true));
    }
}
