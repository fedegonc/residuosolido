# Requerimientos aplicados — Trazabilidad

Complemento de `docs/REQUISITOS.md`: donde el catálogo define **qué** se
requiere, este documento responde **dónde se aplica** (clase/archivo) y
**cómo se verifica** (test JUnit o escenario ejecutable de `scratch/sim`).

Es el artefacto de trazabilidad de la tesis: cada RF/RN tiene una fila con
su implementación real y la evidencia que lo ejercita. Si una fila apunta a
código que no existe, `DocsContractTest` falla (los nombres entre backticks
se verifican contra `src/` y `scratch/`).

---

## 1. Requisitos funcionales

| RF | Implementación | Verificación |
|---|---|---|
| RF-1 Registrarse | `AuthController` + `UserRegistrationService` (nombre, teléfono, PIN 4 dígitos) | `UserServiceTest`, `NewFlowsSecurityTest`; sandbox `register.*` |
| RF-2 Iniciar sesión | `AuthController` + `SecurityConfig` + `AuthenticationEventHandler` (lockout 3 intentos → 15 min, `RateLimiter`) | `CriticalSecurityTest`, `RateLimiterTest`; sandbox `login.*` |
| RF-3 Crear solicitud | `RequestCreateController` + `RequestService.createRequest` + `Request.forCitizen`/`forGuest` + `LocalImageService` | `RequestServiceTest`, `RequestServiceValidationTest`, `EndToEndFlowsTest`; sandbox `create.*` |
| RF-4 Consultar/rastrear | Invitado: `GuestTrackingController` (`/rastrear`, teléfono + código). Usuario: `/mis-solicitudes` + detalle `GET /solicitudes/{id}` | `RequestServiceTest`, `RequestControllerTest`; sandbox `getGuestRequests.*`, `detail.*` |
| RF-5 Gestionar solicitudes propias | `RequestController` (listar, detalle, editar, eliminar) + `Request.canBeEdited`/`canBeDeleted` | `RequestServiceTest`, `RequestControllerTest`; sandbox `update.*`, `delete.*`, `detail.*` |
| RF-6 Gestionar solicitudes asignadas | `OrgRequestController` + `RequestService.acceptRequest`/`rejectRequest`/`completeRequest` + `RequestStatus` (máquina de estados) | `OrganizationControllerTest`, `RequestServiceTest`; sandbox `accept.*`/`reject.*`/`complete.*`, colisiones `actores` |
| RF-7 Completar/editar perfil | `OrgProfileController` (`/mi-organizacion` absorbe onboarding) + `UserService.updateProfile` + `User.isProfileComplete` | `UserServiceTest`, `OrganizationControllerTest`; sandbox `profile.*` |
| RF-8 Recolectores informales | Descartado — CRUD planificado y no implementado (`docs/TRADEOFFS.md` §6) | — |
| RF-9 Notificar al solicitante | `NotificationService.notifyRequester` tras `acceptRequest`/`rejectRequest` persistidos + bandeja `NotificationController` (`/notificaciones`) + badge `GlobalModelAttributes.unreadNotifications` | `RequestServiceTest` (notify + conflicto sin notificar); sandbox `notify.*` |

## 2. Reglas de negocio

| RN | Regla | Implementación | Verificación |
|---|---|---|---|
| RN-1 | Org no modifica solicitudes ajenas | `RequestService.getOwnedOrgRequest` | `RequestServiceTest`; sandbox `org-2 no puede aceptar` |
| RN-2 | No se completa desde `PENDING` | `RequestStatus.transitionComplete` | `RequestServiceTest`; sandbox `no se puede completar sin haber aceptado` |
| RN-3 | Rastreo exige teléfono + código | `RequestService.getGuestRequests` | sandbox `getGuestRequests.blankPhone/blankCode` |
| RN-4 | Solo `PENDING` se edita/elimina | `Request.canBeEdited`/`canBeDeleted` + `getEditableOwnedRequest` | `RequestServiceTest`; sandbox `no se puede editar una solicitud ya aceptada` |
| RN-5 | Org asignable: activa + rol + perfil completo + teléfono + ciudad + materiales | `CityOrgService.findOrganizationByIdAndCity` | `CityOrgServiceTest` (10 tests); sandbox `findOrg.*` |
| RN-6 | Materiales ⊆ aceptados por la org | `RequestService.validateMaterials` | `RequestServiceValidationTest`; sandbox create con material no aceptado |
| RN-7 | Transiciones con locking optimista | `Request.version` (`@Version`) + `saveWithOptimisticLock` | `RequestServiceTest.acceptRequest_concurrentConflict`; sandbox `concurrency.*`, `Collisions` |
| RN-8 | Teléfono normalizado E.164 | `PhoneNumber.resolve`/`normalize` | `PhoneNumberCountryCodeTest`; sandbox `resolve.*` |
| RN-9 | Perfil incompleto redirige a `/mi-organizacion` | `OrgRequestController` + `User.needsProfileCompletion` | `OrganizationControllerTest`; sandbox `orgRequestsView.redirectsWhenIncomplete` |
| RN-10 | Ciudad, dirección y materiales obligatorios | `RequestService.validateCreate`/`validateUpdate` + `Request.updateDraft` | `RequestServiceValidationTest` (13); sandbox errores de `create` |
| RN-11 | Borrado solo si `PENDING` y propio | `RequestService.deleteOwnedRequest` | `RequestServiceTest`; sandbox `delete.*` |

## 3. Cobertura de superficies (contratos automáticos)

Además de los tests por RF/RN, el proyecto tiene tests de contrato que
verifican sincronía entre capas sin ejecutar la app:

| Contrato | Qué fija |
|---|---|
| `ServerMessageContractTest` | Toda clave de error/flash del enum `ServerMessage` existe traducida en es/pt |
| `TemplateI18nContractTest` | Toda clave `data-i18n` literal de los templates existe en los JSON |
| `OrphanI18nKeysTest` | Toda clave traducida se usa (sin traducciones muertas) |
| `DocsContractTest` | Paths y clases citadas en `docs/` existen; endpoints documentados coinciden con rutas reales |
| `RoutesTest` | Rutas centralizadas en `Routes.java` |
| `PageContentImagesTest`/`PageContentRenderingTest` | Contenido estático de páginas públicas |

## 4. Verificación ejecutable del dominio

`scratch/sim/run.sh` ejecuta la especificación del dominio sin Spring ni
Mongo: 95 checks, 44/44 claves de error, 42/42 branches cubiertos, más la
matriz de colisiones entre actores (`run.sh actores`). Es la evidencia de
que las RN valen independientemente de la infraestructura.

---

## Referencias

- `docs/REQUISITOS.md` — catálogo canónico RF/RN y criterio de alcance.
- `docs/ENDPOINTS.md` — rutas HTTP.
- `docs/MEJORAS.md` — estado de cada mejora (implementado/descartado/diferido).
