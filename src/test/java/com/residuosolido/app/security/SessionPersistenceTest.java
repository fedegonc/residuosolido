package com.residuosolido.app.security;

import com.residuosolido.app.EmbeddedMongoTest;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Session con store MongoDB: al autenticar, la sesión debe persistirse
 * como documento en la colección "sessions" — no solo en memoria de Tomcat.
 * Es lo que permite que el login sobreviva a réplicas y redeploys.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.database=residuosolido_test_session",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
@AutoConfigureMockMvc
class SessionPersistenceTest extends EmbeddedMongoTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mongoTemplate.getCollection("sessions").drop();
        userRepository.deleteAll();
        User u = new User();
        u.setUsername("sessionuser");
        u.setPassword(passwordEncoder.encode("5678"));
        u.setActive(true);
        userRepository.save(u);
    }

    @Test
    void loginPersistsSessionInMongo() throws Exception {
        mockMvc.perform(post("/entrar")
                        .param("username", "sessionuser")
                        .param("password", "5678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTrue(mongoTemplate.getCollection("sessions").countDocuments() > 0,
                "la sesión debe persistirse en la colección 'sessions' de Mongo");
    }
}
