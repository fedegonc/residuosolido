# Requisitos y Reglas de Negocio — Eco Solicitud

Catálogo canónico de requisitos funcionales (RF), reglas de negocio (RN) y
criterio de alcance del MVP. Es la fuente de verdad del repo para la
especificación de requisitos de la tesis.

**Actores:** usuario registrado (`ROLE_USER`) y organización de acopio
(`ROLE_ORGANIZATION`). El visitante no autenticado solo accede a páginas
públicas, registro y login.

---

## 1. Requisitos funcionales

### RF-1 — Registrarse

| Actor | Estado |
|---|---|
| Usuario, Organización | Implementado |

Registro con nombre, teléfono y PIN de 4 dígitos (ver `docs/TRADEOFFS.md`
§24). Una entidad `User` para auth; el rol se deriva del doc `Organization` asociado (`USER`/`ORGANIZATION`).

### RF-2 — Iniciar sesión

| Actor | Estado |
|---|---|
| Usuario, Organización | Implementado |

Login con teléfono (E.164) + PIN en `/entrar`. Redirección por rol:
usuario → `/mis-solicitudes`, organización → `/acopio/solicitudes`.

### RF-3 — Crear solicitud de recolección

| Actor | Estado |
|---|---|
| Usuario | Implementado |

Formulario en `/solicitar`: ciudad, dirección, materiales (imagen opcional al
editar). Requiere sesión iniciada — el ciudadano usa los datos de su perfil;
si no tiene teléfono guardado lo completa en el mismo formulario (CU-U9).
El sistema asigna la organización elegible elegida por el usuario.

### RF-4 — Consultar solicitud

| Actor | Estado |
|---|---|
| Usuario | Implementado |

Lista en `/mis-solicitudes` y detalle propio en `GET /solicitudes/{id}` —
lectura en cualquier estado (RN-4 solo restringe editar/eliminar).

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

Panel `/acopio/solicitudes`: estadísticas, filtro por estado, detalle y
transiciones — aceptar con franja horaria, rechazar, completar (RN-1, RN-2).
El rechazo no registra motivo escrito: el modelo no tiene ese campo (gap
declarado en `docs/DEFENSA.md`, dataset de defensa).

### RF-7 — Completar y editar perfil

| Actor | Estado |
|---|---|
| Usuario, Organización | Implementado |

`/mi-organizacion` absorbe el onboarding: si el perfil de la organización está
incompleto (sin teléfono o ciudad), el formulario abre en modo edición y
`updateProfile` marca `profileCompleted` al guardar.

> **Nota sobre `ROLE_USER`:** no existe una pantalla de perfil dedicada para el
ciudadano en el MVP. Sin embargo, si un usuario autenticado crea una solicitud y
su `User.phone` está vacío, `RequestCreateController` le completa el teléfono
automáticamente desde el formulario (`userService.updateProfile(..., phone, ...)`).
Es un flujo oculto dentro de RF-3, no un endpoint RF-7 separado.

### RF-9 — Notificar al solicitante

| Actor | Estado |
|---|---|
| Usuario | Implementado |

Cuando la organización acepta o rechaza una solicitud, se persiste una
`Notification` in-app (toda solicitud tiene usuario registrado). Bandeja en
`/notificaciones` + badge de no-leídas en el navbar; abrir la bandeja marca
todo como leído. `COMPLETED` no notifica — la franja ya se comunicó al aceptar
(ver `docs/TRADEOFFS.md` §33).

---

## 2. Reglas de negocio

| RN | Regla | Dónde se aplica |
|---|---|---|
| RN-1 | Una organización no puede modificar solicitudes ajenas | `RequestService`, verificación de propiedad |
| RN-2 | Una solicitud no puede completarse directamente desde `PENDING` | Ciclo `PENDING → IN_PROGRESS → COMPLETED` (`REJECTED` terminal) |
| RN-3 | Toda solicitud pertenece a un usuario registrado — no existen solicitudes anónimas ni canal de seguimiento por código (el flujo de invitado se eliminó; ver MEJORAS.md) | `Request`/`RequestService` |
| RN-4 | Solo una solicitud `PENDING` puede editarse o eliminarse | `Request.canBeEdited()`, tests RN-11 |
| RN-5 | Una organización asignable debe estar activa, tener rol `ORGANIZATION`, perfil completo, teléfono válido, ciudad coincidente y materiales aceptados no vacíos | `CityOrgService` (resolución de organizaciones) |
| RN-6 | Todos los materiales de la solicitud deben estar incluidos entre los aceptados por la organización | Validación en creación/asignación |
| RN-7 | Transiciones y borrado usan optimistic locking con `@Version` | `Request.version` — conflictos devuelven `409`/`IllegalStateException` |
| RN-8 | El teléfono se normaliza a formato E.164 (`+598`/`+55`) | `PhoneNumber` utility, setters de `User` |
| RN-9 | Una organización con perfil incompleto es redirigida a `/mi-organizacion` antes de gestionar solicitudes | `OrgRequestController` |
| RN-10 | Materiales, dirección y ciudad son obligatorios al crear/editar | `RequestServiceValidationTest` (13 tests) |
| RN-11 | Borrado permitido solo si `PENDING` y propiedad del **usuario registrado** dueño de la solicitud | `deleteOwnedRequest` + tests de regresión |
| RN-12 | La notificación se emite solo DESPUÉS de persistir la transición | `RequestService.acceptRequest`/`rejectRequest` → `NotificationService.notifyRequester` |

> **Nota:** el conteo canónico de la especificación declara 14 RN; las 12
> anteriores están verificadas en código y tests. Las restantes son variantes
> de validación cubiertas por RN-5, RN-6 y RN-10.

---

## 3. Criterio de alcance

**Nota para la defensa:** los puntos excluidos no son omisiones — son decisiones de
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
Si no, se documenta como limitación consciente (ver `docs/DEFENSA.md` §7).

---

## 4. Referencias

- `docs/ENDPOINTS.md` — rutas HTTP que implementan cada RF.
- `docs/DIAGRAMAS.md` — casos de uso por actor, secuencia RF-3, flujo RF-6.
- `docs/ARQUITECTURA.md` — componentes que ejecutan cada RN.
- `docs/DEFENSA.md` §7 — lo que quedó fuera del alcance y por qué.
