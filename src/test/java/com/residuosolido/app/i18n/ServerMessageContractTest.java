package com.residuosolido.app.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.enums.ServerMessage;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato ServerMessage ↔ i18n/{lang}.json.
 *
 * Cada clave que el servidor puede emitir (throws de dominio, flashes de
 * controllers) debe existir traducida en ambos idiomas — si no, el usuario
 * vería la clave cruda (bug real encontrado dos veces: login.error, mat_*).
 * Es un test unitario puro: sin contexto Spring ni Mongo.
 */
class ServerMessageContractTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void everyServerMessageKey_existsInSpanish() throws Exception {
        assertAllKeysPresent("es");
    }

    @Test
    void everyServerMessageKey_existsInPortuguese() throws Exception {
        assertAllKeysPresent("pt");
    }

    @Test
    void serverKeyFollowsJsonConvention() {
        for (ServerMessage m : ServerMessage.values()) {
            assertTrue(m.serverKey().equals("_server_" + m.code().replace('.', '_')),
                    "serverKey() debe seguir la convención de JsonMessageSource.toServerKey");
        }
    }

    private void assertAllKeysPresent(String lang) throws Exception {
        JsonNode json;
        try (InputStream in = getClass().getResourceAsStream("/static/i18n/" + lang + ".json")) {
            json = MAPPER.readTree(in);
        }
        List<String> missing = new ArrayList<>();
        for (ServerMessage m : ServerMessage.values()) {
            if (!json.has(m.serverKey())) {
                missing.add(m.name() + " → " + m.serverKey());
            }
        }
        assertTrue(missing.isEmpty(),
                "Claves de ServerMessage sin traducción en " + lang + ".json: " + missing);
    }
}
