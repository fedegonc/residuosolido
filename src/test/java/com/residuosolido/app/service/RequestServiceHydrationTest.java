package com.residuosolido.app.service;

import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.residuosolido.app.EmbeddedMongoTest;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regresión del N+1 de @DocumentReference(lazy): cada card del kanban llama
 * {@link Request#getContactName()}, que dereferencia el proxy lazy del usuario
 * → un findById por card. La lista debe devolver los usuarios ya hidratados
 * en batch, de modo que acceder a los datos del contacto cueste CERO queries
 * adicionales.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.database=residuosolido_test_hydration",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class RequestServiceHydrationTest extends EmbeddedMongoTest {

    /** Cuenta los comandos find sobre la colección "users" — el contador del N+1. */
    @TestConfiguration
    static class UserFindCounter {
        static final AtomicInteger USER_FINDS = new AtomicInteger();
        static final AtomicInteger REQUEST_FINDS = new AtomicInteger();

        @Bean
        MongoClientSettingsBuilderCustomizer userFindCounter() {
            return builder -> builder.addCommandListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    if ("find".equals(event.getCommandName())
                            && "users".equals(event.getCommand().getString("find").getValue())) {
                        USER_FINDS.incrementAndGet();
                    }
                    if ("find".equals(event.getCommandName())
                            && "requests".equals(event.getCommand().getString("find").getValue())) {
                        REQUEST_FINDS.incrementAndGet();
                    }
                }
            });
        }
    }

    @Autowired
    private RequestService requestService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private RequestRepository requestRepository;

    private Organization org;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
        UserFindCounter.USER_FINDS.set(0);

        User orgUser = new User();
        orgUser.setUsername("org");
        orgUser.setPassword("x");
        orgUser = userRepository.save(orgUser);
        org = new Organization();
        org.setId(orgUser.getId());
        org.setName("Org Test");
        org = organizationRepository.save(org);

        for (int i = 0; i < 5; i++) {
            User citizen = new User();
            citizen.setUsername("citizen" + i);
            citizen.setFirstName("Ciudadano " + i);
            citizen.setPassword("x");
            citizen = userRepository.save(citizen);
            Request r = Request.forCitizen(citizen);
            r.updateDraft(City.RIVERA, "Dir " + i, null, List.of(MaterialCategory.PAPEL));
            r.assignOrganization(org);
            requestRepository.save(r);
        }
        UserFindCounter.USER_FINDS.set(0);
    }

    @Test
    void citizenListReusesKnownUserWithOnlyOneRequestQuery() {
        User citizen = userRepository.findByUsername("citizen0").orElseThrow();
        for (int i = 0; i < 2; i++) {
            Request request = Request.forCitizen(citizen);
            request.updateDraft(City.RIVERA, "Otra dir " + i, null, List.of(MaterialCategory.PAPEL));
            request.assignOrganization(org);
            requestRepository.save(request);
        }
        UserFindCounter.USER_FINDS.set(0);
        UserFindCounter.REQUEST_FINDS.set(0);

        List<Request> requests = requestService.getRequestsByUser(citizen, 0, 20);

        assertEquals(3, requests.size());
        for (Request request : requests) {
            assertEquals(citizen.getDisplayName(), request.getContactName());
            assertEquals(citizen.getPhone(), request.getContactPhone());
        }
        assertEquals(0, UserFindCounter.USER_FINDS.get());
        assertEquals(1, UserFindCounter.REQUEST_FINDS.get());
        requests.forEach(request -> assertSame(citizen, request.getUser()));
    }

    @Test
    void citizenEmptyPageDoesNotFetchUsers() {
        User citizen = userRepository.findByUsername("citizen0").orElseThrow();
        UserFindCounter.USER_FINDS.set(0);
        UserFindCounter.REQUEST_FINDS.set(0);

        assertTrue(requestService.getRequestsByUser(citizen, 1, 20).isEmpty());
        assertEquals(0, UserFindCounter.USER_FINDS.get());
        assertEquals(1, UserFindCounter.REQUEST_FINDS.get());
    }

    @Test
    void orgListHydratesUsersWithoutExtraQueriesPerCard() {
        List<Request> requests = requestService.getRequestsByOrganization(org, 0, 20);
        assertEquals(5, requests.size());

        // Render-equivalente: el kanban toca contactName de cada card.
        UserFindCounter.USER_FINDS.set(0);
        for (Request r : requests) {
            assertNotNull(r.getContactName());
            assertNotEquals("N/A", r.getContactName());
        }
        assertEquals(0, UserFindCounter.USER_FINDS.get(),
                "Los usuarios deben venir hidratados: 0 finds extra en 'users'");
    }
}
