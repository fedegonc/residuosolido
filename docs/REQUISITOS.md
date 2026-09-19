# Requisitos y Reglas de Negocio — Eco Solicitud

Catálogo canónico de requisitos funcionales (RF), reglas de negocio (RN) y
criterio de alcance del MVP. Es la fuente de verdad del repo para la
especificación de requisitos de la tesis.

**Actores:** invitado (sin cuenta), usuario registrado (`ROLE_USER`),
organización de acopio (`ROLE_ORGANIZATION`).

---

## 1. Requisitos funcionales

### RF-1 — Registrarse

| Actor | Estado |
|---|---|
| Usuario, Organización | Implementado |

Registro con nombre, teléfono y PIN de 4 dígitos (ver `docs/TRADEOFFS.md`
§24). Una sola entidad `User` diferenciada por `Role`.

### RF-2 — Iniciar sesión

| Actor | Estado |
|---|---|
| Usuario, Organización | Implementado |

Login con teléfono (E.164) + PIN en `/entrar`. Redirección por rol:
usuario → `/usuarios/inicio`, organización → `/acopio/requests`.

### RF-3 — Crear solicitud de recolección

| Actor | Estado |
|---|---|
| Invitado, Usuario | Implementado |

Formulario en `/solicitudes/nueva`: ciudad, dirección, materiales, peso/volumen
estimado, imagen opcional. Invitados ingresan nombre + teléfono (los campos se
pueden precargar desde el index). El sistema asigna la organización elegible
más cercana y genera un código de seguimiento.

### RF-4 — Consultar / rastrear solicitud

| Actor | Estado |
|---|---|
| Invitado, Usuario | Implementado |

Invitado: `/rastrear` con teléfono + código privado (RN-3). Usuario: ve sus
solicitudes en `/solicitudes` y el detalle en `users/track.html`.

### RF-5 — Gestionar solicitudes propias

| Actor | Estado |
|---|---|
| Usuario | Implementado |

Listar historial con estadísticas, editar y eliminar solicitudes propias
mientras estén `PENDING` (RN-4, RN-11).

### RF-6 — Gestionar solicitudes asignadas

| Actor | Estado |
|---|---|
| Organización | Implementado |

Panel `/acopio/requests`: estadísticas, filtro por estado, detalle y
transiciones — aceptar con franja horaria, rechazar con motivo, completar
(RN-1, RN-2).

### RF-7 — Completar y editar perfil

| Actor | Estado |
|---|---|
| Usuario, Organización | Implementado |

`/acopio/perfil` absorbe el onboarding: si el perfil de la organización está
incompleto (sin teléfono o ciudad), el formulario abre en modo edición y
`updateProfile` marca `profileCompleted` al guardar.

### RF-8 — Gestionar recolectores informales

| Actor | Estado |
|---|---|
| Organización | Latente — CRUD implementado sin acceso visible en la UI |

Decisión consciente: el modelo `Catador` existe pero no hay navegación hacia
él en el MVP (ver `docs/TRADEOFFS.md` §6).

---

## 2. Reglas de negocio

| RN | Regla | Dónde se aplica |
|---|---|---|
| RN-1 | Una organización no puede modificar solicitudes ajenas | `RequestTransitionService`, verificación de propiedad |
| RN-2 | Una solicitud no puede completarse directamente desde `PENDING` | Ciclo `PENDING → IN_PROGRESS → COMPLETED` (`REJECTED` terminal) |
| RN-3 | El seguimiento de invitado requiere teléfono **y** código privado; el teléfono solo no devuelve resultados | `RequestQueryService` + `/rastrear` |
| RN-4 | Solo una solicitud `PENDING` puede editarse o eliminarse | `Request.canBeEdited()`, tests RN-11 |
| RN-5 | Una organización asignable debe estar activa, tener rol `ORGANIZATION`, perfil completo, teléfono válido, ciudad coincidente y materiales aceptados no vacíos | `CityOrgService` (resolución de organizaciones) |
| RN-6 | Todos los materiales de la solicitud deben estar incluidos entre los aceptados por la organización | Validación en creación/asignación |
| RN-7 | Transiciones y borrado usan optimistic locking con `@Version` | `Request.version` — conflictos devuelven `409`/`IllegalStateException` |
| RN-8 | El teléfono se normaliza a formato E.164 (`+598`/`+55`) | `PhoneNumber` utility, setters de `User` |
| RN-9 | Una organización con perfil incompleto es redirigida a `/acopio/perfil` antes de gestionar solicitudes | `OrgRequestController` |
| RN-10 | Materiales, dirección y ciudad son obligatorios al crear/editar | `RequestServiceValidationTest` (13 tests) |
| RN-11 | Borrado permitido solo si `PENDING` y propiedad del solicitante | `deleteOwnedRequest` + tests de regresión |

> **Nota:** el conteo canónico de la especificación declara 14 RN; las 11
> anteriores están verificadas en código y tests. Las restantes son variantes
> de validación cubiertas por RN-5, RN-6 y RN-10.

---

## 3. Criterio de alcance y backlog pendiente

**Nota para la defensa:** estos puntos no son omisiones — son decisiones de
alcance conscientes, justificadas porque exceden lo que una herramienta de
software puede o debe resolver.

**Criterio para decidir si algo nuevo entra al alcance** (en este orden):

1. ¿Está en el oficio o surge de una necesidad real confirmada por el
   stakeholder (organización/usuario)?
2. ¿Es responsabilidad de un sistema de software, o es logística/inversión
   física/proceso humano?
3. ¿Se puede resolver con un campo o servicio simple, o requiere una
   entidad/módulo nuevo?

Si 1 es sí, 2 es "sí es del software" y 3 es "simple" → entra al backlog.
Si no, se documenta como limitación consciente (ver `docs/LIMITACIONES.md`).

**Backlog pendiente (no implementado):**

- 🟡 **Métricas privadas por organización + descarga PDF.** Nueva ruta
  protegida `/acopio/metricas` con `@PreAuthorize("hasRole('ORGANIZATION')")`
  y endpoint `GET /acopio/metricas/pdf` (sugerido: OpenPDF o iText community).
  La ruta pública `/metricas` (totales agregados, sin datos personales) es una
  decisión consciente, no un bug — está explícitamente en `permitAll()` en
  `SecurityConfig`.
- 🟡 **Consistencia de nombres** (baja prioridad): revisar que los nombres de
  métodos de `RequestQueryService`/`RequestOrgService`/`RequestMetricsService`/
  `CityOrgService` reflejen consistentemente su sub-dominio.

---

## 4. Referencias

- `docs/ENDPOINTS.md` — rutas HTTP que implementan cada RF.
- `docs/DIAGRAMAS.md` — casos de uso por actor, secuencia RF-3, flujo RF-6.
- `docs/ARQUITECTURA.md` — componentes que ejecutan cada RN.
- `docs/LIMITACIONES.md` — lo que quedó fuera del alcance y por qué.
