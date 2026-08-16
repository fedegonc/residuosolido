# Testing — EcoSolicitud

Describe la suite de tests real del proyecto (156 tests, `mvn test`, `BUILD SUCCESS`), no un roadmap especulativo. Stack: JUnit 5 + Mockito + Spring Boot Test + Spring Security Test.

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

No requiere base de datos externa para los tests unitarios (repositorios mockeados con Mockito). Los tests de controller/seguridad usan `@SpringBootTest` + `MockMvc`.

---

## 2. Estrategia por capa

### Unit tests (servicios) — repositorios mockeados con Mockito
La mayoría de la suite. Se instancia el servicio real con `new Service(mock(Repository.class), ...)` y se verifica comportamiento sin levantar contexto de Spring — rápidos (segundos, no minutos).

| Clase de test | Qué cubre |
|---|---|
| `RequestServiceValidationTest` (13) | Validación server-side de creación/actualización de solicitudes (RN-10: materiales obligatorios, dirección, ciudad; RN-11: borrado solo si `PENDING`) |
| `RequestQueryServiceTest` (9) | Ownership check de solicitudes por usuario (`getOwnedRequest`, `getEditableOwnedRequest`), rastreo por teléfono |
| `RequestOrgServiceTest` (8) | Ownership check por organización, filtros por estado, paginado |
| `RequestTransitionServiceTest` (13) | Transiciones de estado (`accept`/`reject`/`complete`), optimistic locking, envío de notificación WhatsApp |
| `CityOrgServiceTest` (9) | Resolución de organización por ciudad (RN-06), validaciones de organización inválida/ciudad incorrecta |
| `UserServiceTest` (17) | Registro, actualización de perfil, completar perfil de organización |
| `InformalCollectorServiceTest` (9) | CRUD de recolectores informales, ownership por organización |
| `DashboardServiceTest` (3) | Agregación Mongo faceted para estadísticas de organización |
| `LocalImageServiceTest` (7) | Validación de tipo/tamaño de imagen, guardado local |
| `BreadcrumbServiceTest` (6) | Construcción de breadcrumbs |
| `GuestRateLimiterTest` (5) | Rate limiting por IP (ventana deslizante), header `X-Forwarded-For`, limpieza de memoria |
| `LoginAttemptServiceTest` (7) | Bloqueo tras intentos fallidos de login, expiración, limpieza de memoria |
| `RoleBasedLoginTargetUrlResolverTest` (4) | Redirección post-login según rol (RN-05) |

### Integration / Security tests — `@SpringBootTest` + `MockMvc`

| Clase de test | Qué cubre |
|---|---|
| `CriticalSecurityTest` (11) | Control de acceso por rol en rutas protegidas (`/usuarios/**`, `/acopio/**`), CSRF |
| `NewFlowsSecurityTest` (8) | Seguridad de flujos agregados recientemente (invitados, rastreo) |
| `OrganizationControllerTest` (5) | Flujo completo de organización vía `MockMvc` |
| `EndToEndFlowsTest` (14) | Flujos completos: registro → login → crear solicitud → aceptar/rechazar/completar |
| `I18nMessageResolutionTest` (8) | Resolución de mensajes en español/portugués |

---

## 3. Patrones usados

- **Arrange-Act-Assert** en todos los tests.
- **Mocks de repositorio, no de base de datos real** — `mock(RequestRepository.class)`, sin H2 ni testcontainers (el proyecto usa MongoDB en producción, pero los tests unitarios no necesitan una instancia real).
- **`@WithMockUser`** para simular usuarios autenticados con rol específico en tests de `MockMvc`.
- **Regresión obligatoria en cada fix de bug**: cuando se corrigió `deleteOwnedRequest` (RN-11), se agregaron los tests `rn11_deleteOwnedRequest_notPending_throwsIllegalStateException` y `rn11_deleteOwnedRequest_pending_deletesSuccessfully` en el mismo commit.

---

## 4. Cobertura por área (cualitativa, no hay reporte Jacoco versionado)

| Área | Cobertura |
|---|---|
| Reglas de negocio de `Request` (creación, edición, transición, borrado) | Alta — cubierta por 4 clases de test dedicadas |
| Seguridad por rol | Alta — 2 clases dedicadas + verificación implícita en `EndToEndFlowsTest` |
| Rate limiting / login attempts | Alta — ambas clases con tests de limpieza de memoria incluidos |
| `MongoAggregationUtils` (helper compartido de métricas) | Sin test unitario propio — cubierta indirectamente vía `DashboardServiceTest` |
| `PublicMetricsService` | Sin test unitario detectado — punto débil real, candidato a agregar cobertura |

---

## 5. Comandos útiles adicionales

```bash
# Ver solo el resumen de resultados
mvn test 2>&1 | grep "Tests run"

# Saltar tests en un build de producción
mvn clean package -DskipTests
```
