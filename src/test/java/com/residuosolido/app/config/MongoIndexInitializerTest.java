package com.residuosolido.app.config;

import com.residuosolido.app.EmbeddedMongoTest;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato de índices gestionados explícitamente: auto-index-creation está en
 * false, así que cada @Indexed/@CompoundIndex del modelo debe tener su
 * createIndex acá. Regresión: notifications.user no tenía índice — la bandeja
 * y el badge del navbar hacían collection scan.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.database=residuosolido_test_indexes",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class MongoIndexInitializerTest extends EmbeddedMongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoIndexInitializer initializer;

    @Test
    void createsNotificationUserCreatedAtIndex() {
        initializer.run();
        List<String> indexNames = new ArrayList<>();
        for (Document idx : mongoTemplate.getCollection("notifications").listIndexes()) {
            indexNames.add(idx.getString("name"));
        }
        assertTrue(indexNames.contains("user_createdAt"),
                "notifications debe tener índice compuesto user_createdAt; tiene: " + indexNames);
    }

    @Test
    void isIdempotent() {
        initializer.run();
        initializer.run(); // segunda pasada: no debe explotar ni duplicar
        List<String> indexNames = new ArrayList<>();
        for (Document idx : mongoTemplate.getCollection("notifications").listIndexes()) {
            indexNames.add(idx.getString("name"));
        }
        assertEquals(1, indexNames.stream().filter("user_createdAt"::equals).count());
    }
}
