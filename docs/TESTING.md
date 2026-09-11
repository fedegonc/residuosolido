# Testing — EcoSolicitud

Describe la suite de tests real del proyecto (216 tests, `mvn test`, `BUILD SUCCESS`), no un roadmap especulativo. Stack: JUnit 5 + Mockito + Spring Boot Test + Spring Security Test.

---

## 1. Cómo correr los tests

```bash
# Toda la suite
mvn test

# Un test específico
mvn test -Dtest=RequestServiceValidationTest

# Con reporte de cobertura (Jacoco, ya configurado en pom.xml)
mvn clean test
# Reporte en: target/site/jacoco/index.html
```

No requiere base de datos externa para los tests unitarios (repositorios mockeados con Mockito). Los tests de controller/seguridad usan `@SpringBootTest` + `MockMvc`. Los tests de integración de agregación (`MongoAggregationUtilsIT`) usan MongoDB real (`mongodb://localhost:27017/testdb`).

---

## 2. Estrategia por capa

### Unit tests (servicios) — repositorios mockeados con Mockito
La mayoría de la suite. Se instancia el servicio real con `new Service(mock(Repository.class), ...)` y se verifica comportamiento sin levantar contexto de Spring — rápidos (segundos, no minutos).

| Clase de test | Qué cubre |
|---|---|
| `RequestServiceValidationTest` (13) | Validación server-side de creación/actualización de solicitudes (RN-10: materiales obligatorios, dirección, ciudad; RN-11: borrado solo si `PENDING`) |
| `RequestQueryServiceTest` (10) | Ownership check de solicitudes por usuario (`getOwnedRequest`, `getEditableOwnedRequest`), rastreo por teléfono + código |
| `RequestOrgServiceTest` (8) | Ownership check por organización, filtros por estado, paginado |
| `RequestTransitionServiceTest` (13) | Transiciones de estado (`accept`/`reject`/`complete`), optimistic locking, envío de notificación WhatsApp |
| `CityOrgServiceTest` (9) | Resolución de organización por ciudad (RN-06), validaciones de organización inválida/ciudad incorrecta |
| `UserServiceTest` (17) | Registro, actualización de perfil, completar perfil de organización |
| `InformalCollectorServiceTest` (9) | CRUD de recolectores informales, ownership por organización |
| `RequestMetricsServiceTest` (6) | Agregación Mongo faceted para estadísticas de organización y usuario (counts por estado) |
| `LocalImageServiceTest` (7) | Validación de tipo/tamaño de imagen, guardado local |
| `BreadcrumbServiceTest` (6) | Construcción de breadcrumbs |
| `GuestRateLimiterTest` (5) | Rate limiting por IP (ventana deslizante), header `X-Forwarded-For`, limpieza de memoria |
| `LoginAttemptServiceTest` (7) | Bloqueo tras intentos fallidos de login, expiración, limpieza de memoria |
| `RoleBasedLoginTargetUrlResolverTest` (4) | Redirección post-login según rol (RN-05) |
| `PhoneNumberCountryCodeTest` (23) | Normalización E.164, códigos de país (UY/BR), DDD brasilero, validación de longitud |
| `MvpRegressionTest` (9) | Regresión de reglas críticas (password corto, org inactiva, etc.) |
| `PublicMetricsServiceTest` (6) | Métricas públicas por ciudad |

### Integration / Security tests — `@SpringBootTest` + `MockMvc`

| Clase de test | Qué cubre |
|---|---|
| `CriticalSecurityTest` (11) | Control de acceso por rol en rutas protegidas (`/usuarios/**`, `/acopio/**`), CSRF |
| `NewFlowsSecurityTest` (8) | Seguridad de flujos agregados recientemente (invitados, rastreo) |
| `OrganizationControllerTest` (5) | Flujo completo de organización vía `MockMvc` |
| `EndToEndFlowsTest` (14) | Flujos completos: registro → login → crear solicitud → aceptar/rechazar/completar |
| `I18nMessageResolutionTest` (8) | Resolución de mensajes en español/portugués |
| `DocsControllerTest` (17) | Páginas públicas `/documentos` y `/diagramas`, content-type de `.md`/`.drawio`, path traversal, accesibilidad sin auth |
| `MongoAggregationUtilsIT` (5) | Agregación faceted con MongoDB real — counts por estado, total, sin solicitudes, REJECTED incluido en total |

---

## 3. Patrones usados

- **Arrange-Act-Assert** en todos los tests.
- **Mocks de repositorio, no de base de datos real** — `mock(RequestRepository.class)`, sin H2 ni testcontainers (el proyecto usa MongoDB en producción, pero los tests unitarios no necesitan una instancia real).
- **`@WithMockUser`** para simular usuarios autenticados con rol específico en tests de `MockMvc`.
- **Regresión obligatoria en cada fix de bug**: cuando se corrigió `deleteOwnedRequest` (RN-11), se agregaron los tests `rn11_deleteOwnedRequest_notPending_throwsIllegalStateException` y `rn11_deleteOwnedRequest_pending_deletesSuccessfully` en el mismo commit.
- **Test de integración para pipelines de MongoDB**: `MongoAggregationUtilsIT` usa MongoDB real (no mock) para validar que el pipeline `$facet` produce los counts correctos. Este test detectó el bug donde 4 `$facet` stages separados hacían que `total` siempre fuera 1 (cada stage reemplazaba el documento anterior).

---

## 4. Cobertura por área (cualitativa, no hay reporte Jacoco versionado)

| Área | Cobertura |
|---|---|
| Reglas de negocio de `Request` (creación, edición, transición, borrado) | Alta — cubierta por 4 clases de test dedicadas |
| Seguridad por rol | Alta — 2 clases dedicadas + verificación implícita en `EndToEndFlowsTest` |
| Rate limiting / login attempts | Alta — ambas clases con tests de limpieza de memoria incluidos |
| `MongoAggregationUtils` (helper compartido de métricas) | Alta — test de integración propio (`MongoAggregationUtilsIT`, 5 tests con MongoDB real) + cubierto indirectamente vía `RequestMetricsServiceTest` |
| `PublicMetricsService` | Cobertura agregada — `PublicMetricsServiceTest` (6 tests) |

---

## 5. Comandos útiles adicionales

```bash
# Ver solo el resumen de resultados
mvn test 2>&1 | grep "Tests run"

# Saltar tests en un build de producción
mvn clean package -DskipTests
```
