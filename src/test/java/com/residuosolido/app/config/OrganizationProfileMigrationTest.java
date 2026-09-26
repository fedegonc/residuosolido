package com.residuosolido.app.config;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OrganizationProfileMigration corre una sola vez al arrancar el contexto,
 * antes de que cualquier test method se ejecute — no se puede "esperar" a
 * ver su efecto en un @SpringBootTest normal (la suite completa no mostró
 * ninguna línea de log suya: podía ser que no había nada pendiente, o que
 * nunca corrió). Este test la instancia e invoca directamente, con un
 * documento sembrado a mano con la forma VIEJA, para probar la lógica real
 * contra Mongo real (no un mock) sin depender del timing de arranque.
 *
 * Usa la base real (Atlas en dev) — limpia el documento de prueba después
 * de cada test para no dejar basura.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class OrganizationProfileMigrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private ObjectId testId;

    @BeforeEach
    void setUp() {
        testId = new ObjectId();
    }

    @AfterEach
    void cleanUp() {
        mongoTemplate.getCollection("users").deleteOne(eq("_id", testId));
    }

    @Test
    void migratesOldShapeToOrganizationProfile() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        users.insertOne(new Document("_id", testId)
                .append("username", "migration-test-" + testId)
                .append("role", "ORGANIZATION")
                .append("acceptedMaterials", List.of("PAPEL", "PLASTICO"))
                .append("profileCompleted", true));

        new OrganizationProfileMigration(mongoTemplate).run();

        Document migrated = users.find(eq("_id", testId)).first();
        assertNotNull(migrated, "El documento debería seguir existiendo después de migrar");
        assertNull(migrated.get("acceptedMaterials"), "El campo viejo top-level debe desaparecer");
        assertNull(migrated.get("profileCompleted"), "El campo viejo top-level debe desaparecer");

        Document profile = migrated.get("organizationProfile", Document.class);
        assertNotNull(profile, "Debe existir el subdocumento organizationProfile");
        assertEquals(List.of("PAPEL", "PLASTICO"), profile.getList("acceptedMaterials", String.class));
        assertTrue(profile.getBoolean("profileCompleted"));
    }

    @Test
    void isIdempotent_secondRunDoesNothingToAlreadyMigratedDoc() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        users.insertOne(new Document("_id", testId)
                .append("username", "migration-test-" + testId)
                .append("role", "ORGANIZATION")
                .append("organizationProfile", new Document("acceptedMaterials", List.of("METAL"))
                        .append("profileCompleted", false)));

        // No debe tocar este documento — ya tiene organizationProfile, el filtro
        // de "pendientes" (exists organizationProfile == false) lo excluye.
        assertDoesNotThrow(() -> new OrganizationProfileMigration(mongoTemplate).run());

        Document unchanged = users.find(eq("_id", testId)).first();
        Document profile = unchanged.get("organizationProfile", Document.class);
        assertEquals(List.of("METAL"), profile.getList("acceptedMaterials", String.class));
    }
}
