package com.residuosolido.app.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Migración de índices de Mongo ejecutada al arrancar, en vez de depender de
 * spring.data.mongodb.auto-index-creation (deshabilitado a propósito, ver
 * application.properties).
 *
 * Motivo: el índice único de "email" venía sin sparse (de cuando el registro
 * pedía email obligatorio). Ahora el registro pide teléfono en su lugar
 * (ver UserRegistrationService, docs/DEFENSA.md §24) — con 2+ usuarios en
 * email=null, ese índice viejo choca y tira abajo el arranque de la app
 * (MongoCommandException IndexKeySpecsConflict).
 *
 * Esta clase reemplaza la creación automática por una explícita e idempotente:
 * en el primer arranque corrige el índice roto, en los siguientes no hace
 * nada porque ya está bien. También recrea los otros 3 índices @Indexed del
 * proyecto (antes cubiertos por auto-index-creation) para que una base nueva
 * (fresh install, CI, otra máquina) quede igual de bien indexada.
 */
@Component
public class MongoIndexMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexMigration.class);

    private final MongoTemplate mongoTemplate;

    public MongoIndexMigration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        fixEmailIndex();
        ensureUsernameIndex();
        ensureRequestIndexes();
    }

    /** El único índice que puede estar en un estado incompatible — el resto son inofensivos de recrear siempre. */
    private void fixEmailIndex() {
        MongoCollection<Document> users = mongoTemplate.getCollection("users");
        boolean hasStaleIndex = false;
        for (Document idx : users.listIndexes()) {
            if ("email".equals(idx.getString("name")) && !Boolean.TRUE.equals(idx.getBoolean("sparse"))) {
                hasStaleIndex = true;
                break;
            }
        }
        if (!hasStaleIndex) {
            return;
        }
        logger.info("Migración Mongo: el índice 'email' es viejo (sin sparse), recreándolo...");
        users.dropIndex("email");
        users.createIndex(Indexes.ascending("email"), new IndexOptions()
                .name("email")
                .unique(true)
                .sparse(true)
                .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build()));
        logger.info("Migración Mongo: índice 'email' recreado con sparse=true.");
    }

    private void ensureUsernameIndex() {
        mongoTemplate.getCollection("users")
                .createIndex(Indexes.ascending("username"), new IndexOptions().name("username").unique(true));
    }

    private void ensureRequestIndexes() {
        MongoCollection<Document> requests = mongoTemplate.getCollection("requests");
        requests.createIndex(Indexes.ascending("organization"), new IndexOptions().name("organization"));
        requests.createIndex(Indexes.ascending("status"), new IndexOptions().name("status"));
    }
}
