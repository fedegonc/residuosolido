# Diagramas — Eco Solicitud

Basado directamente en el modelo de datos real (`src/main/java/com/residuosolido/app/model`, `enums`) y en los flujos implementados en los controllers. Complementa a `docs/REQUISITOS.md` (requisitos y reglas de negocio).

---

## 1. Diagrama de clases

```
┌─────────────────────────────┐        ┌──────────────────────────────┐
│            User             │ 1 ──0..1│        Organization          │
├─────────────────────────────┤ mismo   ├──────────────────────────────┤
│ id: String                  │──_id────│ id: String (= User.id)       │
│                             │         │ tipo: OrgType                │
│ username: String            │         │ name: String                 │
│ email: String                │         │ phone: String                │
│ password: String            │         │ city: City                   │
│ firstName: String           │         │ acceptedMaterials: List<…>   │
│ phone: String                │         │ profileCompleted: Boolean    │
│ city: City                  │         ├──────────────────────────────┤
│ active: boolean = true      │         │ completeProfile()            │
│ createdAt: LocalDateTime    │         │ isProfileComplete()          │
├─────────────────────────────┤         └──────────────────────────────┘
│ getDisplayName()             │   ∃ Organization(userId) ⇒ la cuenta
│ (sin campo role — derivado)  │   ES organización; sin doc ⇒ ciudadano
└──────────────┬──────────────┘
               │ 1
               │ creador (user)         organización asignada
       ┌───────┴────────┐        ┌──────────────┐
       │ 0..N            │        │ 0..N         │
┌──────▼──────────────────▼────────────────────▼─────┐
│                      Request                        │
├──────────────────────────────────────────────────────┤
│ id: String                                            │
│ version: Long          (optimistic locking, @Version) │
│ user: User             (requerido — solicitante)      │
│ organization: Organization (indexado)                 │
│ city: City                                            │
│ address / addressReference: String                    │
│ materials: List<MaterialCategory>                     │
│ imageUrl: String (opcional)                            │
│ confirmedSlot: TimeSlot                                │
│ status: RequestStatus = PENDING (indexado)             │
│ createdAt: LocalDateTime                               │
├──────────────────────────────────────────────────────┤
│ forCitizen(User)                                       │
│ updateDraft(city, address, ref, materials)             │
│ accept(TimeSlot) / reject() / complete()               │
│ canBeEdited() / hasMaterials()                         │
│ assignOrganization(Organization)                       │
└──────────────────────────────────────────────────────┘

┌─────────────────────────────┐
│        Notification         │
├─────────────────────────────┤
│ id: String                  │
│ user: User (@DocumentReference, lazy) │
│ requestId: String  (por valor, sobrevive al borrado) │
│ type: NotificationType      │
│ confirmedSlot: TimeSlot (solo ACCEPTED) │
│ read: boolean = false       │
│ createdAt: LocalDateTime    │
├─────────────────────────────┤
│ markRead()                  │
└──────────────┬──────────────┘
               │ N            │
               ▼ 1            │
             User (destinatario — solo registrados)
```

**Relaciones:** `User` 1 ─── 0..N `Notification` (destinatario);
`Notification` 0..N ─── 0..1 `Request` por `requestId` (referencia por
valor, no `@DocumentReference`).

**Notas del modelo real (MongoDB, no relacional):**
- `Request.user` y `Request.organization` son `@DocumentReference(lazy = true)` — referencias a documentos `User`, no joins SQL.
- No existe una entidad `Material` separada: `MaterialCategory` es un **enum fijo** (`PLASTICO`, `PAPEL`, `CARTON`, `VIDRIO`, `METAL`, `MADERA`, `ESCOMBROS`), embebido como lista en `Request` y `Organization.acceptedMaterials`.
- No existen las entidades `Post`, `Category` ni `Feedback` — no hay CMS ni sistema de contenido educativo.
- `Role` tiene solo 2 valores: `USER`, `ORGANIZATION` — no existe rol `ADMIN`. Además `Role` **no se persiste en `User`**: es vocabulario de seguridad derivado — `ORGANIZATION` ⟺ existe el doc `Organization` con el mismo `_id`; `USER` es el default.

---

## 2. Enums del dominio

| Enum | Valores |
|---|---|
| `Role` | `USER`, `ORGANIZATION` |
| `RequestStatus` | `PENDING`, `IN_PROGRESS`, `REJECTED`, `COMPLETED` |
| `City` | `RIVERA`, `LIVRAMENTO` |
| `TimeSlot` | `MANANA`, `TARDE`, `NOCHE` |
| `MaterialCategory` | `PLASTICO`, `PAPEL`, `CARTON`, `VIDRIO`, `METAL`, `MADERA`, `ESCOMBROS` |
| `NotificationType` | `ACCEPTED`, `REJECTED` |
| `OrgType` | `CENTRO_ACOPIO`, `SELECCION_Y_PRENSADO`, `COOPERATIVA`, `RECOLECTOR_INFORMAL` |

---

## 3. Diagrama Entidad-Relación (colecciones MongoDB)

```
┌────────────┐          ┌──────────────┐          ┌──────────────────────┐
│   users    │ 1     0..N│   requests   │  0..N  1 │        users         │
│ (Usuario / │──────────▶│ (creador)    │◀─────────│    (organización)    │
│Organización)│          │              │          │                       │
└─────┬──────┘          └──────┬───────┘          └───────────────────────┘
      │ 1                      │ 0..1 (requestId por valor)
      │               ┌────────▼───────┐
      └──────────────▶│ notifications  │
        0..N          │ (destinatario) │
                      └────────────────┘
```

- 3 colecciones Mongo: `users`, `requests` y `notifications`.
- `requests.user` → referencia a `users` (obligatoria: el solicitante).
- `requests.organization` → referencia a `organizations` (obligatoria tras crear/editar).
- `notifications.user` → referencia a `users` (toda solicitud tiene usuario registrado).
- `notifications.requestId` → id de la solicitud **por valor** (String, no `@DocumentReference`): la notificación histórica sobrevive si la solicitud se borra.

---

## 4. Diagrama de secuencia — Crear solicitud (RF-3)

**Figura 4a** (`docs/diagrams/figura4-secuencia.drawio`): diagrama de secuencia UML 2.5 del flujo real de creación de una solicitud.

**Participantes:**

- Solicitante (`Usuario` autenticado).
- Formulario Thymeleaf `request-form.html`.
- `RequestCreateController`.
- `RateLimiter`.
- `RequestValidator` (validación de datos y materiales).
- `RequestService`.
- `LocalImageService`.
- `CityOrgService`.
- `RequestRepository`.

**Secuencia representada:**

1. El solicitante abre `/solicitar` y completa el formulario.
2. El formulario envía `POST /solicitudes`.
3. `RequestValidator` valida los datos y materiales de la solicitud.
4. `LocalImageService` valida y guarda la imagen (si existe).
5. `CityOrgService` valida la organización seleccionada y su ciudad.
6. `RequestRepository` persiste la solicitud.
7. El controller redirige a `/mis-solicitudes`.

---

## 5. Ciclo de estados de `Request` — complemento textual

```text
PENDING ──accept(slot)──> IN_PROGRESS ──complete()──> COMPLETED
   │                            │
   └────────reject()────────> REJECTED <──reject()───┘
```

**Transiciones y guardas:**
- Solo `PENDING` puede editarse o eliminarse (el detalle se puede VER en cualquier estado).
- `accept` requiere una franja horaria (`TimeSlot`).
- `REJECTED` y `COMPLETED` son estados finales (sin transiciones salientes).
- Todas las transiciones son validadas por `RequestStatus.transitionAccept/Complete/Reject()` (vía `Request.accept/complete/reject()`) antes de ejecutarse — el wrapper `RequestStateMachine` que existía acá se eliminó (duplicaba el mismo chequeo sin aportar nada, ver `docs/MEJORAS.md` #221).

**Solicitante:** siempre un usuario registrado — no existen solicitudes
anónimas ni código de seguimiento (el flujo de invitado fue eliminado).

**Concurrencia:**
- Las transiciones se protegen con `@Version` y optimistic locking (`OptimisticLockingFailureException`).
- Si la persistencia falla, se captura la excepción y se reintenta con backoff exponencial (RequestServiceRetryHelper).
- `accept`/`reject` notifican al solicitante registrado **DESPUÉS del save (RN-12)**: si la persistencia falla, no se notifica.
- `complete` no notifica (es operación interna de la organización).

---

## 6. Diagrama de flujo — Aceptar/Rechazar/Completar solicitud (RF-6)

**Figura 5** (secuencia de transición de estado con notificaciones):

```
[Organización ve /acopio/solicitudes]
            │
            ▼
[Selecciona acción: accept | reject | complete]
            │
            ▼
   POST /acopio/solicitudes/{id}/aceptar
            │
            ▼
  ┌──────────────────────────────┐
  │  Request.accept/reject/      │ (valida transición vía
  │  complete()                  │  RequestStatus.transitionX())
  └────────────┬─────────────────┘
               │
     ┌─────────┼─────────────────┐
     ▼         ▼                 ▼
 accept()   reject()         complete()
     │         │                 │
     ▼         ▼                 ▼
RequestService (structured logging — sin @Transactional, ver TRADEOFFS §36)
     │         │                 │
     ▼         ▼                 ▼
[status=IN_PROGRESS] [status=REJECTED] [status=COMPLETED]
     │         │                 │
     └─────────┴─────────────────┘
               ▼
   IF status changed AND user es registrado:
         NotificationService.notify(...)
               │
               ▼
         [Bandeja + badge actualizado]
               │
               ▼
   redirect a /acopio/solicitudes
```

**Notas:**
- `RequestStatus.transitionX()` (vía `Request.accept/reject/complete()`) valida que la transición sea permitida (ver guardas en §5) — reemplaza a `RequestStateMachine`, eliminado por duplicar el mismo chequeo (`docs/MEJORAS.md` #221).
- `RequestService.acceptRequest()`/`rejectRequest()`/`completeRequest()` NO usan `@Transactional` (no hay `PlatformTransactionManager` configurado — era decorativo, removido, ver `docs/TRADEOFFS.md` §36); cada transición es una sola escritura atómica a un documento Mongo.
- Si falla el save de `Request`, la notificación no se crea (RN-12).
- Si falla el save por concurrencia (`OptimisticLockingFailureException`), se reintenta automáticamente con exponential backoff.

---

## 7. Diagramas de casos de uso por actor

### Usuario (registrado)
```
Usuario
  ├─ CU: Registrarse (RF-1)
  ├─ CU: Iniciar sesión (RF-2)
  ├─ CU: Crear solicitud de recolección (RF-3)
  ├─ CU: Ver dashboard e historial (RF-5)
  ├─ CU: Ver detalle de solicitud propia — cualquier estado (RF-5)
  ├─ CU: Editar solicitud propia pendiente (RF-5)
  ├─ CU: Eliminar solicitud propia pendiente (RF-5)
  ├─ CU: Consultar notificaciones — bandeja + badge (RF-9)
  └─ CU: Editar perfil (RF-7 — vía RequestController)
```

### Organización
```
Organización
  ├─ CU: Registrarse (RF-1)
  ├─ CU: Iniciar sesión (RF-2)
  ├─ CU: Completar perfil (onboarding forzado) (RF-7)
  ├─ CU: Editar perfil (RF-7)
  ├─ CU: Ver solicitudes asignadas, filtrar por estado (RF-6)
  ├─ CU: Aceptar solicitud (con horario) (RF-6)
  ├─ CU: Rechazar solicitud (RF-6)
  └─ CU: Completar solicitud (RF-6)
```

### Visitante (público)
```
Visitante
  ├─ CU: Ver landing page
```

Para el detalle de precondiciones/postcondiciones de cada RF, ver `docs/REQUISITOS.md`.

---

## Auditoría de Fidelidad (2026-09-24)

Los diagramas fueron auditados comparándolos con el código fuente post-refactor. **Fidelidad global: 85-90%.**

**Diagramas sincronizados correctamente:**
- Diagrama de clases (95%) — Todas las entidades, atributos y relaciones coinciden.
- Modelo ER (95%) — Colecciones y relaciones de MongoDB correctas.
- Casos de uso (90%) — Actores y flujos funcionales correctos.

**Diagramas actualizados en esta revisión:**
- **Figura 4a (Crear solicitud):** Agregado `RequestValidator` como participante explícito.
- **Figura 5 (Ciclo de estados):** Agregado `RequestStateMachine` (eliminado posteriormente, ver #221 — este punto de la auditoría ya no refleja el código actual), aclarado que tracking code solo se genera para invitados (flujo eliminado posteriormente), corregida descripción de optimistic locking.
- **Figura 6 (Aceptar/Rechazar/Completar):** Agregado `RequestStateMachine` y `@Transactional` (ambos eliminados posteriormente — #221 y TRADEOFFS §36 respectivamente), aclarado flujo de notificaciones.

**Notas:**
- Los diagramas draw.io (`docs/diagrams/`) contienen las figuras visuales; este documento es complemento textual.
- Para cambios en draw.io (SVG), ver el repositorio directo.
- Dos figuras previamente duplicadas como "Figura 4" ahora son "Figura 4a" (crear) y "Figura 5" (flujo).

---

## Documentos relacionados

- `docs/REQUISITOS.md` — catálogo RF/RN y criterio de alcance.
- `docs/ARQUITECTURA.md` — núcleo del sistema: componentes, flujos y decisiones.
- `docs/GITFLOW.md` — flujo de trabajo del repositorio.
