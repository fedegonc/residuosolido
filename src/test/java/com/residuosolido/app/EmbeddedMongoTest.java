package com.residuosolido.app;

import org.springframework.test.context.TestPropertySource;

/**
 * Base para tests de integración: MongoDB embebido (flapdoodle) en vez de la
 * URI de Atlas/localhost.
 *
 * El auto-config de flapdoodle levanta un mongod real en puerto aleatorio y
 * registra un customizer que reemplaza el host:port del cliente — por eso la
 * URI acá es solo un placeholder sintáctico: tiene que ser esquema
 * {@code mongodb://} (NO {@code +srv}, que haría lookup DNS) y su path define
 * el nombre de la base ({@code testdb}).
 *
 * Por qué embebido y no Atlas: tests herméticos sin credenciales ni red,
 * sin contaminar la base compartida, y ~100ms menos por query. La app no usa
 * features Atlas-only (sin $search, sin transacciones multi-doc) — standalone
 * es suficiente. Si eso cambia, flapdoodle soporta modo replica-set.
 */
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost/testdb",
        "de.flapdoodle.mongodb.embedded.version=8.0.4"
})
public abstract class EmbeddedMongoTest {
}
