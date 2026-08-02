# 🧪 Estrategia de Testing - Sistema de Gestión de Residuos Sólidos

## 🎯 Objetivo
Establecer una estrategia de testing progresiva y práctica que garantice la calidad del sistema sin sobrecargar el desarrollo inicial.

---

## 📊 Pirámide de Testing Recomendada

```
        /\
       /  \      E2E Tests (5%)
      /----\     - Flujos críticos completos
     /      \    
    /--------\   Integration Tests (25%)
   /          \  - Controllers + Services + DB
  /------------\ 
 /              \ Unit Tests (70%)
/________________\ - Services, Utils, Models
```

---

## 🥇 PRIMER TEST MÁS IMPORTANTE

### **Test de Creación de Solicitud (Integration Test)**

**¿Por qué este test primero?**
1. ✅ **Flujo crítico del negocio**: Es la funcionalidad principal del sistema
2. ✅ **Cubre múltiples capas**: Controller → Service → Repository → Database
3. ✅ **Valida seguridad**: Verifica que solo usuarios autenticados pueden crear solicitudes
4. ✅ **Detecta errores comunes**: Validación de datos, relaciones entre entidades
5. ✅ **Alta confianza**: Si este test pasa, el core del sistema funciona

---

## 📝 Implementación del Primer Test

### **Archivo:** `RequestCreationIntegrationTest.java`

```java
package com.residuosolido.app.integration;

import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import com.residuosolido.app.service.*;
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
    private RequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private MaterialService materialService;

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
        testUser.setPassword("password123");
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
    @DisplayName("Test 1: Usuario autenticado puede crear solicitud exitosamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestSuccess() throws Exception {
        // Given: Datos válidos para crear una solicitud
        String description = "Necesito recolección de botellas plásticas";
        String address = "Calle Principal 123";

        // When: Se envía el formulario de creación
        mockMvc.perform(post("/usuarios/solicitudes")
                .with(csrf())
                .param("description", description)
                .param("address", address)
                .param("materials", String.valueOf(testMaterial.getId())))
                
        // Then: Se redirige a la lista de solicitudes
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/solicitudes"));

        // And: La solicitud se guardó en la base de datos
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
        
        Request savedRequest = requests.get(0);
        assertThat(savedRequest.getDescription()).isEqualTo(description);
        assertThat(savedRequest.getAddress()).isEqualTo(address);
        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(savedRequest.getMaterials()).hasSize(1);
        assertThat(savedRequest.getMaterials().get(0).getName()).isEqualTo("Plástico PET");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Usuario no autenticado no puede crear solicitud")
    void testCreateRequestUnauthorized() throws Exception {
        // When: Usuario no autenticado intenta crear solicitud
        mockMvc.perform(post("/usuarios/solicitudes")
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
    @DisplayName("Test 3: Solicitud sin descripción es rechazada")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestWithoutDescription() throws Exception {
        // When: Se intenta crear solicitud sin descripción
        mockMvc.perform(post("/usuarios/solicitudes")
                .with(csrf())
                .param("description", "")
                .param("address", "Calle Test 123"))
                
        // Then: Se muestra error de validación
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("request", "description"));

        // And: No se creó la solicitud
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Solicitud sin dirección es rechazada")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestWithoutAddress() throws Exception {
        // When: Se intenta crear solicitud sin dirección
        mockMvc.perform(post("/usuarios/solicitudes")
                .with(csrf())
                .param("description", "Descripción válida")
                .param("address", ""))
                
        // Then: Se muestra error de validación
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("request", "address"));

        // And: No se creó la solicitud
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Solicitud se crea con estado PENDING por defecto")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testRequestCreatedWithPendingStatus() throws Exception {
        // When: Se crea una solicitud
        mockMvc.perform(post("/usuarios/solicitudes")
                .with(csrf())
                .param("description", "Test description")
                .param("address", "Test address")
                .param("materials", String.valueOf(testMaterial.getId())))
                .andExpect(status().is3xxRedirection());

        // Then: El estado es PENDING
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Usuario puede crear múltiples solicitudes")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateMultipleRequests() throws Exception {
        // When: Se crean 3 solicitudes
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/usuarios/solicitudes")
                    .with(csrf())
                    .param("description", "Solicitud " + i)
                    .param("address", "Dirección " + i)
                    .param("materials", String.valueOf(testMaterial.getId())))
                    .andExpect(status().is3xxRedirection());
        }

        // Then: Se crearon las 3 solicitudes
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(3);
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Solicitud con múltiples materiales se guarda correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateRequestWithMultipleMaterials() throws Exception {
        // Given: Crear segundo material
        Material material2 = new Material();
        material2.setName("Vidrio");
        material2.setDescription("Botellas de vidrio");
        material2.setActive(true);
        material2 = materialRepository.save(material2);

        // When: Se crea solicitud con 2 materiales
        mockMvc.perform(post("/usuarios/solicitudes")
                .with(csrf())
                .param("description", "Múltiples materiales")
                .param("address", "Calle Test 456")
                .param("materials", String.valueOf(testMaterial.getId()))
                .param("materials", String.valueOf(material2.getId())))
                .andExpect(status().is3xxRedirection());

        // Then: La solicitud tiene 2 materiales
        List<Request> requests = requestRepository.findAll();
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getMaterials()).hasSize(2);
    }
}
```

---

## 🔧 Configuración Necesaria

### **1. Archivo de Configuración de Test**

**Archivo:** `src/test/resources/application-test.properties`

```properties
# Database H2 en memoria para tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Logging
logging.level.org.springframework.security=DEBUG
logging.level.com.residuosolido.app=DEBUG

# Cloudinary mock (no hacer llamadas reales en tests)
cloudinary.cloud-name=test
cloudinary.api-key=test
cloudinary.api-secret=test
```

### **2. Dependencias en pom.xml**

```xml
<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 📈 Roadmap de Testing (Próximos Tests)

### **Fase 1: Tests Críticos (Semana 1-2)** ✅
1. ✅ **RequestCreationIntegrationTest** - Creación de solicitudes
2. ⏳ **AuthenticationIntegrationTest** - Login/Logout
3. ⏳ **RequestStatusWorkflowTest** - Cambios de estado (PENDING → ACCEPTED → COMPLETED)

### **Fase 2: Tests de Servicios (Semana 3-4)**
4. ⏳ **UserServiceTest** - CRUD de usuarios
5. ⏳ **MaterialServiceTest** - Gestión de materiales
6. ⏳ **RequestServiceTest** - Lógica de negocio de solicitudes

### **Fase 3: Tests de Seguridad (Semana 5)**
7. ⏳ **SecurityConfigTest** - Configuración de seguridad
8. ⏳ **RoleBasedAccessTest** - Permisos por rol
9. ⏳ **CSRFProtectionTest** - Protección CSRF

### **Fase 4: Tests E2E (Semana 6)**
10. ⏳ **UserJourneyE2ETest** - Flujo completo de ciudadano
11. ⏳ **OrganizationJourneyE2ETest** - Flujo completo de centro de acopio

---

## 🎯 Métricas de Éxito

### **Cobertura de Código Objetivo:**
- **Mínimo aceptable:** 60%
- **Objetivo:** 75%
- **Ideal:** 85%+

### **Áreas Prioritarias:**
1. **Services:** 80%+ (lógica de negocio crítica)
2. **Controllers:** 70%+ (endpoints principales)
3. **Models:** 60%+ (validaciones)
4. **Repositories:** 50%+ (queries custom)

---

## 🚀 Comandos para Ejecutar Tests

### **Ejecutar todos los tests:**
```bash
mvn test
```

### **Ejecutar un test específico:**
```bash
mvn test -Dtest=RequestCreationIntegrationTest
```

### **Ejecutar con reporte de cobertura:**
```bash
mvn clean test jacoco:report
```

### **Ver reporte de cobertura:**
```
target/site/jacoco/index.html
```

---

## 💡 Mejores Prácticas

### **1. Nomenclatura de Tests**
```java
// ✅ BIEN: Descriptivo y claro
@Test
@DisplayName("Usuario autenticado puede crear solicitud exitosamente")
void testCreateRequestSuccess() { }

// ❌ MAL: Poco descriptivo
@Test
void test1() { }
```

### **2. Estructura AAA (Arrange-Act-Assert)**
```java
@Test
void testExample() {
    // Given (Arrange): Preparar datos
    User user = createTestUser();
    
    // When (Act): Ejecutar acción
    Request request = requestService.createRequest(user, data);
    
    // Then (Assert): Verificar resultado
    assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
}
```

### **3. Usar @Transactional en Tests de Integración**
```java
@SpringBootTest
@Transactional  // ✅ Rollback automático después de cada test
class MyIntegrationTest {
    // Los cambios en BD se revierten automáticamente
}
```

### **4. Datos de Prueba Aislados**
```java
@BeforeEach
void setUp() {
    // ✅ Limpiar datos antes de cada test
    requestRepository.deleteAll();
    userRepository.deleteAll();
    
    // Crear datos frescos para cada test
    testUser = createTestUser();
}
```

---

## 🔍 Debugging de Tests

### **Ver logs detallados:**
```properties
# En application-test.properties
logging.level.org.springframework.test=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### **Pausar ejecución para inspeccionar BD:**
```java
@Test
void testDebug() {
    // Crear datos
    Request request = createRequest();
    
    // Pausar para inspeccionar H2 console
    System.out.println("Inspeccionar en: http://localhost:8080/h2-console");
    Thread.sleep(60000); // Esperar 1 minuto
}
```

---

## 📚 Recursos Adicionales

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Security Testing](https://docs.spring.io/spring-security/reference/servlet/test/index.html)

---

## ✅ Checklist de Implementación

- [ ] Agregar dependencias de testing en `pom.xml`
- [ ] Crear `application-test.properties`
- [ ] Implementar `RequestCreationIntegrationTest`
- [ ] Ejecutar tests y verificar que pasan
- [ ] Configurar Jacoco para cobertura de código
- [ ] Agregar tests al CI/CD pipeline
- [ ] Documentar tests en README.md

---

**Última actualización:** 11/10/2025  
**Versión:** 1.0.0  
**Próximo paso:** Implementar `RequestCreationIntegrationTest`
