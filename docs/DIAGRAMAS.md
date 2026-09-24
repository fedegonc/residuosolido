# Diagramas — Eco Solicitud

Basado directamente en el modelo de datos real (`src/main/java/com/residuosolido/app/model`, `enums`) y en los flujos implementados en los controllers. Complementa a `docs/REQUISITOS.md` (requisitos y reglas de negocio).

---

## 1. Diagrama de clases

```
┌─────────────────────────────┐
│            User             │
├─────────────────────────────┤
│ id: String                  │
│ username: String            │
│ email: String                │
│ password: String            │
│ role: Role                  │
│ firstName: String           │
│ phone: String                │
│ city: City                  │
│ active: boolean = true      │
│ profileCompleted: Boolean   │
│ acceptedMaterials: List<MaterialCategory> │
│ createdAt: LocalDateTime    │
├─────────────────────────────┤
│ isOrganization()             │
│ isProfileComplete()          │
│ needsProfileCompletion()     │
│ completeProfile()            │
│ getDisplayName()             │
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
│ user: User             (null si es invitado)          │
│ organization: User     (indexado)                     │
│ guestName / guestPhone: String                        │
│ city: City                                            │
│ address / addressReference: String                    │
│ materials: List<MaterialCategory>                     │
│ imageUrl: String (opcional)                            │
│ confirmedSlot: TimeSlot                                │
│ status: RequestStatus = PENDING (indexado)             │
│ createdAt: LocalDateTime                               │
├──────────────────────────────────────────────────────┤
│ forCitizen(User) / forGuest(name, phone, code)         │
│ updateDraft(city, address, ref, materials)             │
│ accept(TimeSlot) / reject() / complete()               │
│ canBeEdited() / isGuest() / hasMaterials()             │
│ assignOrganization(User)                                │
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
- No existe una entidad `Material` separada: `MaterialCategory` es un **enum fijo** (`PLASTICO`, `PAPEL`, `CARTON`, `VIDRIO`, `METAL`, `MADERA`, `ESCOMBROS`), embebido como lista en `Request` y `User.acceptedMaterials`.
- No existen las entidades `Post`, `Category` ni `Feedback` — no hay CMS ni sistema de contenido educativo.
- `Role` tiene solo 2 valores: `USER`, `ORGANIZATION` — no existe rol `ADMIN`.

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
- `requests.user` → referencia a `users` (opcional, null si es invitado).
- `requests.organization` → referencia a `users` con `role=ORGANIZATION` (obligatoria tras crear/editar).
- `notifications.user` → referencia a `users` (solo registrados; el invitado no tiene bandeja).
- `notifications.requestId` → id de la solicitud **por valor** (String, no `@DocumentReference`): la notificación histórica sobrevive si la solicitud se borra.

---

## 4. Diagrama de secuencia — Crear solicitud (RF-3)

**Figura 4** (`docs/diagrams/figura4-secuencia.drawio`): diagrama de secuencia UML 2.5 del flujo real de creación de una solicitud.

**Participantes:**

- Solicitante (`Invitado` o `Usuario`).
- Formulario Thymeleaf `request-form.html`.
- `RequestCreateController`.
- `RateLimiter`.
- `RequestService`.
- `LocalImageService`.
- `CityOrgService`.
- `RequestRepository`.

**Secuencia representada:**

1. El solicitante abre `/solicitar` y completa el formulario.
2. El formulario envía `POST /solicitudes`.
3. Si es invitado, el controller verifica el rate limit.
4. `RequestService` valida la imagen y los datos de la solicitud.
5. `CityOrgService` valida la organización seleccionada y su ciudad.
6. `RequestRepository` persiste la solicitud.
7. Si existe imagen, `LocalImageService` la guarda y actualiza `imageUrl`.
8. El controller redirige: invitado → `/rastrear?telefono&codigo`; registrado → `/mis-solicitudes`.

---

## 5. Ciclo de estados de `Request` — complemento textual

```text
PENDING ──accept(slot)──> IN_PROGRESS ──complete()──> COMPLETED
   │                            │
   └────────reject()────────> REJECTED <──reject()───┘
```

- Solo `PENDING` puede editarse o eliminarse (el detalle se puede VER en cualquier estado).
- `accept` requiere una franja horaria.
- `REJECTED` y `COMPLETED` son estados finales.
- Las transiciones se protegen con `@Version` y optimistic locking.
- `accept`/`reject` notifican al solicitante registrado DESPUÉS del save (RN-12): si la persistencia falla por concurrencia, no se notifica. `complete` no notifica.

---

## 6. Diagrama de flujo — Aceptar/Rechazar/Completar solicitud (RF-6)

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
  ┌─────────────────────────┐
  │     RequestService       │
  └────────────┬─────────────┘
               │
     ┌─────────┼─────────────┐
     ▼         ▼             ▼
 accept()   reject()     complete()
     │         │             │
     ▼         ▼             ▼
[status=IN_PROGRESS] [status=REJECTED] [status=COMPLETED]
     │         │             │
     └─────────┴─────────────┘
               ▼
   redirect a /acopio/solicitudes
```

---

## 7. Diagramas de casos de uso por actor

### Invitado
```
Invitado
  ├─ CU: Crear solicitud de recolección sin cuenta (RF-3)
  └─ CU: Consultar solicitud por teléfono + código privado (RF-4)
```

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

## Documentos relacionados

- `docs/REQUISITOS.md` — catálogo RF/RN y criterio de alcance.
- `docs/ARQUITECTURA.md` — núcleo del sistema: componentes, flujos y decisiones.
- `docs/referencia/GITFLOW.md` — flujo de trabajo del repositorio (figura5-gitflow).
