package com.residuosolido.app.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.residuosolido.app.model.Username;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Updates.set;

/**
 * Normaliza los usernames existentes a su forma canónica (trim + lowercase).
 *
 * Es idempotente: solo actualiza documentos cuyo username actual no coincide
 * con la forma canónica. Si la normalización generaría un duplicado de índice
 * único, Mongo rechaza ese update individual y se registra el error sin romper
 * el arranque.
 */
@Component
public class UsernameNormalizationMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UsernameNormalizationMigration.class);

    private final MongoTemplate mongoTemplate;

    public UsernameNormalizationMigration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        List<Document> toFix = users.find(ne("username", null)).into(new ArrayList<>());

        List<WriteModel<Document>> updates = new ArrayList<>();
        for (Document user : toFix) {
            Object raw = user.get("username");
            if (!(raw instanceof String current)) continue;
            String canonical = Username.canonical(current);
            if (canonical.equals(current)) continue;

            Bson filter = eq("_id", user.getObjectId("_id"));
            updates.add(new UpdateOneModel<>(filter, set("username", canonical), new UpdateOptions()));
        }

        if (updates.isEmpty()) {
            return;
        }

        try {
            users.bulkWrite(updates);
            logger.info("Migración de usernames: {} documentos normalizados.", updates.size());
        } catch (Exception e) {
            logger.error("Migración de usernames: falló la normalización (posible duplicado). Error: {}", e.getMessage());
        }
    }
}
