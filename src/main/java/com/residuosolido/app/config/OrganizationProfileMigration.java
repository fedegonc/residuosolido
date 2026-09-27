package com.residuosolido.app.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

/**
 * Mueve acceptedMaterials/profileCompleted de campos top-level en "users" a
 * un subdocumento organizationProfile — ver {@link com.residuosolido.app.model.OrganizationProfile}
 * y docs/TRADEOFFS.md §38.
 *
 * Idempotente por diseño (igual que MongoIndexMigration): solo toca
 * documentos con role=ORGANIZATION que todavía NO tienen organizationProfile
 * (primer arranque tras el deploy). En arranques siguientes el filtro no
 * matchea nada y esto es un no-op instantáneo.
 *
 * Opera con BSON crudo (MongoCollection<Document>), no con el repositorio
 * mapeado a User — el modelo Java ya espera la forma NUEVA (organizationProfile
 * anidado), así que leer/escribir documentos viejos vía el mapper los dejaría
 * con acceptedMaterials vacío antes de poder migrarlos.
 */
@Component
public class OrganizationProfileMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationProfileMigration.class);

    private final MongoTemplate mongoTemplate;

    public OrganizationProfileMigration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");

        Bson pendingFilter = and(
                eq("role", "ORGANIZATION"),
                exists("organizationProfile", false));

        List<Document> pending = users.find(pendingFilter).into(new ArrayList<>());
        if (pending.isEmpty()) {
            return;
        }

        logger.info("Migración Mongo: {} organizaciones sin organizationProfile, migrando...", pending.size());

        List<WriteModel<Document>> writes = new ArrayList<>();
        for (Document doc : pending) {
            List<?> materials = doc.getList("acceptedMaterials", Object.class, List.of());
            Boolean profileCompleted = doc.getBoolean("profileCompleted", false);

            Document organizationProfile = new Document("acceptedMaterials", materials)
                    .append("profileCompleted", profileCompleted);

            Bson update = combine(
                    set("organizationProfile", organizationProfile),
                    unset("acceptedMaterials"),
                    unset("profileCompleted"));

            writes.add(new UpdateOneModel<>(eq("_id", doc.getObjectId("_id")), update, new UpdateOptions()));
        }
        users.bulkWrite(writes);

        logger.info("Migración Mongo: {} organizaciones migradas a organizationProfile.", writes.size());
    }
}
