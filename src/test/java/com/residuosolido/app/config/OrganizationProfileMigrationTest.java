package com.residuosolido.app.config;

import com.residuosolido.app.EmbeddedMongoTest;
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
 * ver su efecto en un @SpringBootTest normal. Este test la instancia e invoca
 * directamente, con un documento sembrado a mano con la forma VIEJA, para probar
 * la lógica real contra Mongo real (no un mock).
 *
 * Corre contra el mongod embebido de EmbeddedMongoTest (Mongo real, no un
 * mock) — limpia el documento de prueba después de cada test.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class OrganizationProfileMigrationTest extends EmbeddedMongoTest {

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
        mongoTemplate.getCollection("organizations").deleteOne(eq("_id", testId.toString()));
    }

    @Test
    void migratesOldShapeToOrganizationProfile() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        users.insertOne(new Document("_id", testId)
                .append("username", "migration-test-" + testId)
                .append("firstName", "Cooperativa")
                .append("email", "coop@test.com")
                .append("role", "ORGANIZATION")
                .append("active", true)
                .append("city", "RIVERA")
                .append("phone", "+59899123456")
                .append("acceptedMaterials", List.of("PAPEL", "PLASTICO"))
                .append("profileCompleted", true));

        new OrganizationProfileMigration(mongoTemplate).run();

        Document migrated = users.find(eq("_id", testId)).first();
        assertNotNull(migrated, "El documento debería seguir existiendo después de migrar");
        assertNull(migrated.get("acceptedMaterials"), "El campo viejo top-level debe desaparecer");
        assertNull(migrated.get("profileCompleted"), "El campo viejo top-level debe desaparecer");

        Document org = mongoTemplate.getCollection("organizations")
                .find(eq("_id", testId.toString())).first();
        assertNotNull(org, "Debe crear el documento en la colección organizations");
        assertEquals(testId.toString(), org.getString("_id"),
                "El _id del org coincide con el del usuario (enlace implícito)");
        assertNull(org.get("userId"), "userId/email/username/active no se denormalizan — se leen del User");
        assertNull(org.get("username"));
        assertNull(org.get("email"));
        assertNull(org.get("active"));
        assertEquals(List.of("PAPEL", "PLASTICO"), org.getList("acceptedMaterials", String.class));
        assertTrue(org.getBoolean("profileCompleted"));
        assertEquals("Cooperativa", org.getString("name"));
        assertEquals("+59899123456", org.getString("phone"));
    }

    @Test
    void isIdempotent_secondRunDoesNothingToAlreadyMigratedDoc() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        users.insertOne(new Document("_id", testId)
                .append("username", "migration-test-" + testId)
                .append("role", "ORGANIZATION")
                .append("active", true)
                .append("city", "RIVERA"));
        MongoCollection<Document> organizations = mongoTemplate.getCollection("organizations");
        organizations.insertOne(new Document("_id", testId.toString())
                .append("userId", testId.toString())
                .append("acceptedMaterials", List.of("METAL"))
                .append("profileCompleted", false));

        assertDoesNotThrow(() -> new OrganizationProfileMigration(mongoTemplate).run());

        Document unchanged = organizations.find(eq("_id", testId.toString())).first();
        assertNotNull(unchanged);
        assertEquals(List.of("METAL"), unchanged.getList("acceptedMaterials", String.class));
    }
}
