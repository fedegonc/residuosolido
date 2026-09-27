package com.residuosolido.app.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
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
 * Migra perfiles de organización desde la colección {@code users} a la
 * colección separada {@code organizations}. Es idempotente: solo crea un
 * documento en {@code organizations} si no existe uno con el mismo _id.
 *
 * Soporta tres formas históricas de persistencia:
 * 1. Campos top-level {@code acceptedMaterials} / {@code profileCompleted}.
 * 2. Subdocumento embebido {@code organizationProfile}.
 * 3. Usuarios organización ya migrados (sin organizationProfile): copia teléfono/ciudad.
 *
 * Los documentos de organización usan el mismo {@code _id} que el usuario para
 * no invalidar referencias de solicitudes existentes (Request.organization).
 * Email y username se leen del User, no se denormalizan.
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
        MongoCollection<Document> organizations = mongoTemplate.getCollection("organizations");

        List<Document> orgUsers = users.find(eq("role", "ORGANIZATION")).into(new ArrayList<>());
        if (orgUsers.isEmpty()) {
            return;
        }

        List<WriteModel<Document>> inserts = new ArrayList<>();
        List<WriteModel<Document>> userCleanups = new ArrayList<>();

        for (Document user : orgUsers) {
            String userId = user.getObjectId("_id").toString();
            if (organizations.find(eq("_id", userId)).first() != null) {
                // Ya migrado: solo limpiar subdocumento residual si existe.
                if (user.containsKey("organizationProfile") || user.containsKey("acceptedMaterials")) {
                    userCleanups.add(cleanup(userId));
                }
                continue;
            }

            Document org = buildOrganization(user, userId);
            inserts.add(new InsertOneModel<>(org));

            if (user.containsKey("organizationProfile") || user.containsKey("acceptedMaterials")) {
                userCleanups.add(cleanup(userId));
            }
        }

        if (!inserts.isEmpty()) {
            organizations.bulkWrite(inserts);
            logger.info("Migración Mongo: {} organizaciones movidas a colección 'organizations'.", inserts.size());
        }
        if (!userCleanups.isEmpty()) {
            users.bulkWrite(userCleanups);
            logger.info("Migración Mongo: {} documentos 'users' limpiados de campos de perfil de org.", userCleanups.size());
        }
    }

    private Document buildOrganization(Document user, String userId) {
        Document org = new Document();
        org.put("_id", userId);
        org.put("name", user.getString("firstName"));
        org.put("phone", normalizePhone(user.getString("phone")));
        org.put("city", cityName(user));
        org.put("profileCompleted", profileCompleted(user));
        org.put("acceptedMaterials", acceptedMaterials(user));
        return org;
    }

    private List<?> acceptedMaterials(Document user) {
        Document profile = user.get("organizationProfile", Document.class);
        if (profile != null && profile.containsKey("acceptedMaterials")) {
            return profile.getList("acceptedMaterials", Object.class, List.of());
        }
        return user.getList("acceptedMaterials", Object.class, List.of());
    }

    private boolean profileCompleted(Document user) {
        Document profile = user.get("organizationProfile", Document.class);
        if (profile != null && profile.containsKey("profileCompleted")) {
            return Boolean.TRUE.equals(profile.getBoolean("profileCompleted"));
        }
        return Boolean.TRUE.equals(user.getBoolean("profileCompleted"));
    }

    private String cityName(Document user) {
        Object city = user.get("city");
        return city != null ? city.toString() : null;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return com.residuosolido.app.model.PhoneNumber.normalize(phone);
    }

    private WriteModel<Document> cleanup(String userId) {
        Bson update = combine(
                unset("organizationProfile"),
                unset("acceptedMaterials"),
                unset("profileCompleted"));
        return new UpdateOneModel<>(eq("_id", new org.bson.types.ObjectId(userId)), update, new UpdateOptions());
    }
}
