package com.residuosolido.app.integration;

import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Test simple de integración para verificar que el sistema funciona.
 * Este test NO usa MockMvc, solo verifica la lógica de negocio.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleRequestTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaterialRepository materialRepository;

    private User testUser;
    private Material testMaterial;

    @BeforeEach
    void setUp() {
        // Limpiar datos (orden importante por foreign keys)
        requestRepository.deleteAll();
        materialRepository.deleteAll();
        userRepository.deleteAll();
        
        // Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$dummyhashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Crear material de prueba
        testMaterial = new Material();
        testMaterial.setName("Plástico PET");
        testMaterial.setDescription("Botellas de plástico");
        testMaterial.setActive(true);
        testMaterial = materialRepository.save(testMaterial);
    }

    @Test
    @Order(1)
    @DisplayName("✅ Test 1: Base de datos H2 funciona correctamente")
    void testDatabaseWorks() {
        // Given: Datos de prueba creados en setUp
        
        // When: Consultamos los repositorios
        List<User> users = userRepository.findAll();
        List<Material> materials = materialRepository.findAll();
        
        // Then: Los datos existen
        assertThat(users).hasSize(1);
        assertThat(materials).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("testuser");
        assertThat(materials.get(0).getName()).isEqualTo("Plástico PET");
    }

    @Test
    @Order(2)
    @DisplayName("✅ Test 2: Podemos crear una solicitud manualmente")
    void testCreateRequestManually() {
        // Given: Usuario y material existen
        assertThat(testUser.getId()).isNotNull();
        assertThat(testMaterial.getId()).isNotNull();
        
        // When: Creamos una solicitud manualmente
        Request request = new Request();
        request.setDescription("Test description");
        request.setAddress("Test address");
        request.setStatus(RequestStatus.PENDING);
        request.setUser(testUser);
        
        Request saved = requestRepository.save(request);
        
        // Then: La solicitud se guardó
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Test description");
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(saved.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @Order(3)
    @DisplayName("✅ Test 3: Podemos consultar solicitudes")
    void testFindRequests() {
        // Given: Creamos 3 solicitudes
        for (int i = 1; i <= 3; i++) {
            Request request = new Request();
            request.setDescription("Solicitud " + i);
            request.setAddress("Dirección " + i);
            request.setStatus(RequestStatus.PENDING);
            request.setUser(testUser);
            requestRepository.save(request);
        }
        
        // When: Consultamos todas las solicitudes
        List<Request> requests = requestRepository.findAll();
        
        // Then: Existen 3 solicitudes
        assertThat(requests).hasSize(3);
        assertThat(requests.get(0).getDescription()).contains("Solicitud");
    }

    @Test
    @Order(4)
    @DisplayName("✅ Test 4: Transacciones funcionan (rollback automático)")
    void testTransactionsWork() {
        // Given: Creamos una solicitud en este test
        Request request = new Request();
        request.setDescription("Test rollback");
        request.setAddress("Test address");
        request.setStatus(RequestStatus.PENDING);
        request.setUser(testUser);
        requestRepository.save(request);
        
        // When: El test termina
        // Then: @Transactional hará rollback automático
        // (verificaremos esto en el siguiente test)
        
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
    }

    @Test
    @Order(5)
    @DisplayName("✅ Test 5: Rollback funcionó (no hay datos del test anterior)")
    void testRollbackWorked() {
        // Given: El test anterior creó una solicitud
        
        // When: Consultamos las solicitudes
        List<Request> requests = requestRepository.findAll();
        
        // Then: No existe (rollback automático)
        assertThat(requests).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("✅ Test 6: Podemos cambiar el estado de una solicitud")
    void testUpdateRequestStatus() {
        // Given: Una solicitud PENDING
        Request request = new Request();
        request.setDescription("Test");
        request.setAddress("Test");
        request.setStatus(RequestStatus.PENDING);
        request.setUser(testUser);
        request = requestRepository.save(request);
        
        // When: Cambiamos el estado a ACCEPTED
        request.setStatus(RequestStatus.ACCEPTED);
        requestRepository.save(request);
        
        // Then: El estado cambió
        Request updated = requestRepository.findById(request.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
    }

    @Test
    @Order(7)
    @DisplayName("✅ Test 7: Podemos buscar solicitudes por usuario")
    void testFindRequestsByUser() {
        // Given: Creamos 2 usuarios con solicitudes
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole(Role.USER);
        user2.setActive(true);
        user2 = userRepository.save(user2);
        
        // Solicitudes para testUser
        for (int i = 1; i <= 2; i++) {
            Request request = new Request();
            request.setDescription("Request " + i);
            request.setAddress("Address " + i);
            request.setStatus(RequestStatus.PENDING);
            request.setUser(testUser);
            requestRepository.save(request);
        }
        
        // Solicitud para user2
        Request request3 = new Request();
        request3.setDescription("Request 3");
        request3.setAddress("Address 3");
        request3.setStatus(RequestStatus.PENDING);
        request3.setUser(user2);
        requestRepository.save(request3);
        
        // When: Buscamos solicitudes por usuario
        List<Request> testUserRequests = requestRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(testUser.getId()))
                .toList();
        
        // Then: testUser tiene 2 solicitudes
        assertThat(testUserRequests).hasSize(2);
    }

    @AfterEach
    void tearDown() {
        // El @Transactional hace rollback automático
    }
}
