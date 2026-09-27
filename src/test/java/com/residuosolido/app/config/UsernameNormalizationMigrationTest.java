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

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class UsernameNormalizationMigrationTest {

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
    void normalizesUsernameToCanonical() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        users.insertOne(new Document("_id", testId)
                .append("username", "  JuAn_PeReZ  ")
                .append("role", "USER"));

        new UsernameNormalizationMigration(mongoTemplate).run();

        Document migrated = users.find(eq("_id", testId)).first();
        assertNotNull(migrated);
        assertEquals("juan_perez", migrated.getString("username"));
    }

    @Test
    void leavesAlreadyCanonicalUsernameAlone() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        users.insertOne(new Document("_id", testId)
                .append("username", "already_canonical")
                .append("role", "USER"));

        assertDoesNotThrow(() -> new UsernameNormalizationMigration(mongoTemplate).run());

        Document unchanged = users.find(eq("_id", testId)).first();
        assertNotNull(unchanged);
        assertEquals("already_canonical", unchanged.getString("username"));
    }
}
