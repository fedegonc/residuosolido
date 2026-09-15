# Endpoints — EcoSolicitud

Extraído directamente de las anotaciones `@GetMapping`/`@PostMapping` en `src/main/java/com/residuosolido/app/controller`. No incluye rutas hipotéticas ni planificadas — solo lo que existe y compila.

---

## Público / Invitado (sin autenticación)

| Método | Ruta | Controller | Descripción |
|---|---|---|---|
| GET | `/`, `/index` | `AuthController` | Landing page pública |
| GET | `/auth/register` | `AuthController` | Formulario de registro |
| POST | `/auth/register` | `AuthController` | Procesa registro (Usuario u Organización) |
| GET | `/auth/login` | `AuthController` | Formulario de login |
| GET | `/rastrear` | `GuestTrackingController` | Formulario de rastreo por teléfono + código privado |
| POST | `/rastrear` | `GuestTrackingController` | Busca solicitudes por teléfono + código |
| GET | `/metricas` | `PublicMetricsController` | Métricas públicas (total completadas, por ciudad) — **sin protección**, ver backlog en `RF-RN.md` |
| GET | `/blog` | `BlogController` | Blog estático con los 3 artículos expandidos (historias del reciclaje) |
| GET | `/documentos` | `DocsController` | Índice de documentación técnica (docs/*.md) |
| GET | `/diagramas` | `DocsController` | Índice de diagramas UML (docs/diagrams/*.drawio) |
| GET | `/docs/**` | `WebConfig` (resource handler) | Archivos estáticos de docs/ y docs/diagrams/ |
| GET | `/api/organizations/by-city?city={City}` | `OrgApiController` | JSON de organizaciones activas en una ciudad (usado por el selector del formulario) |

## Usuario (rol `USER`)

| Método | Ruta | Controller | Descripción |
|---|---|---|---|
| GET | `/usuarios/inicio` | `UserProfileController` | Dashboard con estadísticas y solicitudes recientes |
| GET | `/usuarios/perfil` | `UserProfileController` | Formulario de perfil |
| POST | `/usuarios/perfil` | `UserProfileController` | Actualiza email/nombre/teléfono/ciudad |
| GET | `/solicitudes` | `RequestController` | Lista de solicitudes propias |
| GET | `/solicitudes/nueva` | `RequestCreateController` | Formulario de nueva solicitud (también accesible sin login) |
| POST | `/solicitudes/nueva` | `RequestCreateController` | Crea la solicitud (con imagen opcional) |
| GET | `/solicitudes/exito` | `RequestController` | Página de confirmación tras crear |
| GET | `/solicitud/{id}` | `RequestController` | Detalle de una solicitud propia |
| GET | `/solicitud/{id}/editar` | `RequestEditController` | Formulario de edición (solo si `PENDING`) |
| POST | `/solicitud/{id}/editar` | `RequestEditController` | Actualiza la solicitud |
| POST | `/solicitud/{id}/eliminar` | `RequestController` | Elimina la solicitud (solo si `PENDING`) |

## Organización (rol `ORGANIZATION`)

| Método | Ruta | Controller | Descripción |
|---|---|---|---|
| GET | `/acopio/inicio` | `OrgDashboardController` | Dashboard con Kanban integrado (4 columnas por estado) + estadísticas |
| GET | `/acopio/completar-perfil` | `OrgOnboardingController` | Formulario de onboarding forzado (teléfono + ciudad) |
| POST | `/acopio/completar-perfil` | `OrgOnboardingController` | Guarda el perfil inicial |
| GET | `/acopio/perfil` | `OrgProfileController` | Formulario de edición de perfil |
| POST | `/acopio/perfil` | `OrgProfileController` | Actualiza datos de la organización |
| GET | `/acopio/requests` | `OrgRequestController` | Lista de solicitudes asignadas, con filtro por estado y paginado |
| GET | `/acopio/requests/{id}` | `OrgRequestDetailController` | Detalle de una solicitud asignada |
| POST | `/acopio/requests/{id}/transition` | `OrgRequestController` | Cambia estado: `action=accept\|reject\|complete` |
| GET | `/acopio/catadores` | `InformalCollectorController` | CRUD de recolectores — **sin link en sidebar** (ver docs/DEFENSA.md) |
| GET | `/acopio/catadores/edit/{id}` | `InformalCollectorController` | Carga un recolector para editar |
| POST | `/acopio/catadores` | `InformalCollectorController` | Crea o actualiza un recolector (según si llega `id`) |
| POST | `/acopio/catadores/{id}/delete` | `InformalCollectorController` | Elimina un recolector |

---

## Notas

- No existen rutas `/admin/**` — no hay rol Admin ni panel de administración general.
- No existe la ruta `/acopio/kanban` — el tablero Kanban se integró al dashboard (`/acopio/inicio`).
- No existen rutas `/posts`, `/categories`, `/feedback` — el blog es estático, sin CMS.
- Las rutas están en español (`/usuarios`, `/acopio`, `/solicitudes`) por decisión de diseño, sin alias en inglés.
- `PublicMetricsController` (`/metricas`) es la única ruta pública que expone datos agregados sin protección — pendiente en el backlog (`docs/DIAGRAMAS.md` sección 6) renombrarlo/protegerlo si se agregan métricas privadas por organización.
- Las rutas físicas están centralizadas en `com.residuosolido.app.config.Routes` para evitar URLs hardcodeadas en controllers, seguridad y tests.
- OpenAPI/Swagger UI está disponible en `/swagger-ui.html` y `/v3/api-docs` (público en `SecurityConfig`).

---

# Testing (anexo)


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

No requiere base de datos externa para los tests unitarios (repositorios mockeados con Mockito). Los tests de controller/seguridad usan `@SpringBootTest` + `MockMvc`. Los tests de integración de agregación (`MongoAggregationUtilsIntegrationTest`) usan MongoDB real (`mongodb://localhost:27017/testdb`).

---

## 2. Estrategia por capa

### Unit tests (servicios) — repositorios mockeados con Mockito
La mayoría de la suite. Se instancia el servicio real con `new Service(mock(Repository.class), ...)` y se verifica comportamiento sin levantar contexto de Spring — rápidos (segundos, no minutos).

| Clase de test | Qué cubre |
|---|---|
| `RequestServiceValidationTest` (13) | Validación server-side de creación/actualización de solicitudes (RN-10: materiales obligatorios, dirección, ciudad; RN-11: borrado solo si `PENDING`) |
| `RequestQueryServiceTest` (10) | Ownership check de solicitudes por usuario (`getOwnedRequest`, `getEditableOwnedRequest`), rastreo por teléfono + código |
| `RequestOrgServiceTest` (8) | Ownership check por organización, filtros por estado, paginado |
| `RequestTransitionServiceTest` (13) | Transiciones de estado (`accept`/`reject`/`complete`) y optimistic locking |
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
| `MongoAggregationUtilsIntegrationTest` (5) | Agregación faceted con MongoDB real — counts por estado, total, sin solicitudes, REJECTED incluido en total |

---

## 3. Patrones usados

- **Arrange-Act-Assert** en todos los tests.
- **Mocks de repositorio, no de base de datos real** — `mock(RequestRepository.class)`, sin H2 ni testcontainers (el proyecto usa MongoDB en producción, pero los tests unitarios no necesitan una instancia real).
- **`@WithMockUser`** para simular usuarios autenticados con rol específico en tests de `MockMvc`.
- **Regresión obligatoria en cada fix de bug**: cuando se corrigió `deleteOwnedRequest` (RN-11), se agregaron los tests `rn11_deleteOwnedRequest_notPending_throwsIllegalStateException` y `rn11_deleteOwnedRequest_pending_deletesSuccessfully` en el mismo commit.
- **Test de integración para pipelines de MongoDB**: `MongoAggregationUtilsIntegrationTest` usa MongoDB real (no mock) para validar que el pipeline `$facet` produce los counts correctos. Este test detectó el bug donde 4 `$facet` stages separados hacían que `total` siempre fuera 1 (cada stage reemplazaba el documento anterior).

---

## 4. Cobertura por área (cualitativa, no hay reporte Jacoco versionado)

| Área | Cobertura |
|---|---|
| Reglas de negocio de `Request` (creación, edición, transición, borrado) | Alta — cubierta por 4 clases de test dedicadas |
| Seguridad por rol | Alta — 2 clases dedicadas + verificación implícita en `EndToEndFlowsTest` |
| Rate limiting / login attempts | Alta — ambas clases con tests de limpieza de memoria incluidos |
| `MongoAggregationUtils` (helper compartido de métricas) | Alta — test de integración propio (`MongoAggregationUtilsIntegrationTest`, 5 tests con MongoDB real) + cubierto indirectamente vía `RequestMetricsServiceTest` |
| `PublicMetricsService` | Cobertura agregada — `PublicMetricsServiceTest` (6 tests) |

---

## 5. Comandos útiles adicionales

```bash
# Ver solo el resumen de resultados
mvn test 2>&1 | grep "Tests run"

# Saltar tests en un build de producción
mvn clean package -DskipTests
```
