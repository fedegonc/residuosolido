package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para MongoAggregationUtils.countByStatusFaceted.
 *
 * Regresión del bug: si se usan múltiples $facet stages separados, cada uno
 * reemplaza el documento anterior y solo el último sobrevive, produciendo
 * total=1 siempre (cuenta el documento del facet anterior, no las requests reales).
 *
 * Este test usa MongoDB real (no mock) para validar que el pipeline produce
 * los counts correctos.
 */
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class MongoAggregationUtilsIT {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("dummy");
        testUser.setRole(Role.USER);
        testUser.setFirstName("Test");
        testUser.setCity(City.RIVERA);
        testUser.setActive(true);
        testUser.setProfileCompleted(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void countByStatusFaceted_noRequests_returnsAllZeros() {
        Map<String, Long> stats = MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user").is(new ObjectId(testUser.getId())), true);

        assertEquals(0L, stats.get("total"), "total debe ser 0 cuando no hay solicitudes");
        assertEquals(0L, stats.get("pending"));
        assertEquals(0L, stats.get("inProgress"));
        assertEquals(0L, stats.get("completed"));
    }

    @Test
    void countByStatusFaceted_withRequests_returnsCorrectCounts() {
        // Crear 3 solicitudes: 1 PENDING, 1 IN_PROGRESS, 1 COMPLETED
        createRequest(RequestStatus.PENDING);
        createRequest(RequestStatus.IN_PROGRESS);
        createRequest(RequestStatus.COMPLETED);

        Map<String, Long> stats = MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user").is(new ObjectId(testUser.getId())), true);

        assertEquals(3L, stats.get("total"), "total debe ser 3");
        assertEquals(1L, stats.get("pending"), "pending debe ser 1");
        assertEquals(1L, stats.get("inProgress"), "inProgress debe ser 1");
        assertEquals(1L, stats.get("completed"), "completed debe ser 1");
    }

    @Test
    void countByStatusFaceted_rejectedNotCountedInStatuses_butIncludedInTotal() {
        createRequest(RequestStatus.PENDING);
        createRequest(RequestStatus.REJECTED);

        Map<String, Long> stats = MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user").is(new ObjectId(testUser.getId())), true);

        assertEquals(2L, stats.get("total"), "total incluye REJECTED");
        assertEquals(1L, stats.get("pending"));
        assertEquals(0L, stats.get("inProgress"));
        assertEquals(0L, stats.get("completed"));
    }

    @Test
    void countByStatusFaceted_withoutTotal_omitsTotalKey() {
        createRequest(RequestStatus.PENDING);

        Map<String, Long> stats = MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user").is(new ObjectId(testUser.getId())), false);

        assertFalse(stats.containsKey("total"));
        assertEquals(1L, stats.get("pending"));
    }

    @Test
    void countByStatusFaceted_multipleSameStatus_countsCorrectly() {
        createRequest(RequestStatus.PENDING);
        createRequest(RequestStatus.PENDING);
        createRequest(RequestStatus.PENDING);
        createRequest(RequestStatus.COMPLETED);

        Map<String, Long> stats = MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user").is(new ObjectId(testUser.getId())), true);

        assertEquals(4L, stats.get("total"));
        assertEquals(3L, stats.get("pending"));
        assertEquals(0L, stats.get("inProgress"));
        assertEquals(1L, stats.get("completed"));
    }

    private void createRequest(RequestStatus status) {
        Request r = new Request();
        r.setUser(testUser);
        r.setAddress("Test Address");
        r.setCity(City.RIVERA);
        r.setMaterials(List.of(MaterialCategory.PLASTICO));
        r.setStatus(status);
        if (status == RequestStatus.IN_PROGRESS || status == RequestStatus.COMPLETED) {
            r.setConfirmedSlot(TimeSlot.MANANA);
        }
        requestRepository.save(r);
    }
}
