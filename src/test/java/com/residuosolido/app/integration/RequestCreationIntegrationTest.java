package com.residuosolido.app.integration;

import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para la creación de solicitudes de recolección.
 * Este es el test más crítico del sistema ya que valida el flujo principal del negocio.
 * 
 * Casos de uso cubiertos:
 * - CU-03: Crear Solicitud de Recolección
 * - Validaciones de seguridad (autenticación requerida)
 * - Validaciones de datos (campos obligatorios)
 * - Estados de solicitud (PENDING por defecto)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequestCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
        // Limpiar datos de prueba
        requestRepository.deleteAll();
        
        // Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$dummyhashedpassword"); // Password hasheado
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
    @DisplayName("✅ Test 1: Usuario autenticado puede crear solicitud exitosamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestSuccess() throws Exception {
        // Given: Datos válidos para crear una solicitud
        String description = "Necesito recolección de botellas plásticas";
        String address = "Calle Principal 123";

        // When: Se envía el formulario de creación
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", description)
                .param("address", address)
                .param("materials", testMaterial.getName()))
                
        // Then: Se redirige a la lista de solicitudes
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/requests"));

        // And: La solicitud se guardó en la base de datos
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
        
        Request savedRequest = requests.get(0);
        assertThat(savedRequest.getDescription()).isEqualTo(description);
        assertThat(savedRequest.getCollectionAddress()).isEqualTo(address);
        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(savedRequest.getMaterials()).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("🔒 Test 2: Usuario no autenticado no puede crear solicitud")
    void testCreateRequestUnauthorized() throws Exception {
        // When: Usuario no autenticado intenta crear solicitud
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "Test")
                .param("address", "Test Address"))
                
        // Then: Es redirigido al login
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));

        // And: No se creó ninguna solicitud
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).isEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("⚠️ Test 3: Solicitud sin descripción es rechazada")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestWithoutDescription() throws Exception {
        int initialCount = requestRepository.findAll().size();

        // When: Se intenta crear solicitud sin descripción
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "")
                .param("address", "Calle Test 123")
                .param("materials", testMaterial.getName()));

        // Then: No se creó la solicitud
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(initialCount);
    }

    @Test
    @Order(4)
    @DisplayName("⚠️ Test 4: Solicitud sin dirección es rechazada")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestWithoutAddress() throws Exception {
        int initialCount = requestRepository.findAll().size();

        // When: Se intenta crear solicitud sin dirección
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "Descripción válida")
                .param("address", "")
                .param("materials", testMaterial.getName()));

        // Then: No se creó la solicitud
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(initialCount);
    }

    @Test
    @Order(5)
    @DisplayName("📋 Test 5: Solicitud se crea con estado PENDING por defecto")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testRequestCreatedWithPendingStatus() throws Exception {
        // When: Se crea una solicitud
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "Test description")
                .param("address", "Test address")
                .param("materials", testMaterial.getName()))
                .andExpect(status().is3xxRedirection());

        // Then: El estado es PENDING
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @Order(6)
    @DisplayName("📊 Test 6: Usuario puede crear múltiples solicitudes")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateMultipleRequests() throws Exception {
        // When: Se crean 3 solicitudes
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/users/requests")
                    .with(csrf())
                    .param("description", "Solicitud " + i)
                    .param("address", "Dirección " + i)
                    .param("materials", testMaterial.getName()))
                    .andExpect(status().is3xxRedirection());
        }

        // Then: Se crearon las 3 solicitudes
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(3);
    }

    @Test
    @Order(7)
    @DisplayName("🔢 Test 7: Solicitud con múltiples materiales se guarda correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestWithMultipleMaterials() throws Exception {
        // Given: Crear segundo material
        Material material2 = new Material();
        material2.setName("Vidrio");
        material2.setDescription("Botellas de vidrio");
        material2.setActive(true);
        material2 = materialRepository.save(material2);

        // When: Se crea solicitud con 2 materiales
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "Múltiples materiales")
                .param("address", "Calle Test 456")
                .param("materials", testMaterial.getName())
                .param("materials", material2.getName()))
                .andExpect(status().is3xxRedirection());

        // Then: La solicitud tiene materiales asociados
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getMaterials()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(8)
    @DisplayName("🔐 Test 8: Usuario con rol ORGANIZATION no puede crear solicitudes de usuario")
    @WithMockUser(username = "orguser", roles = {"ORGANIZATION"})
    void testOrganizationCannotCreateUserRequest() throws Exception {
        // When: Usuario con rol ORGANIZATION intenta crear solicitud de usuario
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "Test")
                .param("address", "Test Address")
                .param("materials", testMaterial.getName()))
                
        // Then: Acceso denegado
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    @DisplayName("✅ Test 9: Formulario de nueva solicitud se muestra correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testNewRequestFormDisplays() throws Exception {
        // When: Se accede al formulario de nueva solicitud
        mockMvc.perform(get("/users/requests/new"))
                
        // Then: Se muestra el formulario
                .andExpect(status().isOk())
                .andExpect(view().name("users/request-form"));
    }

    @Test
    @Order(10)
    @DisplayName("📋 Test 10: Lista de solicitudes se muestra correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testRequestListDisplays() throws Exception {
        // Given: Crear una solicitud
        mockMvc.perform(post("/users/requests")
                .with(csrf())
                .param("description", "Test")
                .param("address", "Test Address")
                .param("materials", testMaterial.getName()));

        // When: Se accede a la lista de solicitudes
        mockMvc.perform(get("/users/requests"))
                
        // Then: Se muestra la lista
                .andExpect(status().isOk())
                .andExpect(view().name("users/user-requests"))
                .andExpect(model().attributeExists("requests"));
    }

    @AfterEach
    void tearDown() {
        // Limpieza adicional si es necesaria
        // El @Transactional ya hace rollback automático
    }
}
