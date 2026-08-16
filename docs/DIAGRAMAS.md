# Diagramas — EcoSolicitud

Basado directamente en el modelo de datos real (`src/main/java/com/residuosolido/app/model`, `enums`) y en los flujos implementados en los controllers. Complementa a `RF-RN.md` (requisitos y reglas de negocio).

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
│ estimatedWeight / estimatedVolume: String (opcional)   │
│ imageUrl: String (opcional)                            │
│ confirmedSlot: TimeSlot                                │
│ status: RequestStatus = PENDING (indexado)             │
│ createdAt: LocalDateTime                               │
├──────────────────────────────────────────────────────┤
│ accept(TimeSlot) / reject() / complete()               │
│ canBeEdited() / isGuest() / hasMaterials()             │
│ assignOrganization(User)                                │
└──────────────────────────────────────────────────────┘

┌─────────────────────────────┐
│      InformalCollector      │
├─────────────────────────────┤
│ id: String                  │
│ organizationId: String       │──── N:1 → User (organización dueña, por id, no @DocumentReference)
│ name / phone: String        │
│ city: City                  │
│ materials: List<MaterialCategory> │
│ notes: String                │
│ active: boolean = true       │
│ createdAt: LocalDateTime     │
├─────────────────────────────┤
│ create(...) [factory]        │
│ updateDetails(...)           │
│ belongsTo(User)              │
└─────────────────────────────┘
```

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

---

## 3. Diagrama Entidad-Relación (colecciones MongoDB)

```
┌────────────┐          ┌──────────────┐          ┌──────────────────────┐
│   users    │ 1     0..N│   requests   │  0..N  1 │        users         │
│ (Usuario / │──────────▶│ (creador)    │◀─────────│    (organización)    │
│Organización)│          │              │          │                       │
└─────┬──────┘          └──────────────┘          └───────────────────────┘
      │ 1
      │
      │ 0..N (por organizationId, referencia manual — no @DocumentReference)
      ▼
┌─────────────────────┐
│ informal_collectors  │
└─────────────────────┘
```

- 3 colecciones Mongo: `users`, `requests`, `informal_collectors`.
- `requests.user` → referencia a `users` (opcional, null si es invitado).
- `requests.organization` → referencia a `users` con `role=ORGANIZATION` (obligatoria tras crear/editar).
- `informal_collectors.organizationId` → referencia por `String id` (no `@DocumentReference`), validada en código (`InformalCollectorService`).

---

## 4. Diagrama de estados — ciclo de vida de `Request`

```
                    ┌─────────┐
                    │ PENDING │  (estado inicial, al crear)
                    └────┬────┘
                         │
            ┌────────────┼────────────┐
            │ accept(slot)             │ reject()
            ▼                          ▼
     ┌──────────────┐            ┌──────────┐
     │ IN_PROGRESS  │            │ REJECTED │  (estado final)
     └──────┬───────┘            └──────────┘
            │ complete()
            ▼
     ┌──────────────┐
     │  COMPLETED   │  (estado final)
     └──────────────┘
```

**Reglas (ver RN-07 en `RF-RN.md`):**
- Solo se puede `accept()`/`reject()` desde `PENDING`.
- Solo se puede `complete()` desde `IN_PROGRESS`.
- `reject()` también es válido desde `IN_PROGRESS` (arrepentimiento de la organización).
- Solo en `PENDING` la solicitud puede editarse o eliminarse (`canBeEdited()`).
- Transiciones protegidas con `@Version` (optimistic locking) contra condiciones de carrera si dos operadores actúan simultáneamente.

---

## 5. Diagrama de secuencia — Crear solicitud (RF-3)

```
Invitado/Usuario      RequestCreateController   RequestValidator   CityOrgService   RequestService   GuestRateLimiter
      │                        │                       │                │                │                  │
      │  GET /solicitudes/nueva│                       │                │                │                  │
      │───────────────────────▶│                       │                │                │                  │
      │  (form: ciudad, org,   │                       │                │                │                  │
      │   materiales, etc.)    │                       │                │                │                  │
      │◀───────────────────────│                       │                │                │                  │
      │                        │                       │                │                │                  │
      │  POST /solicitudes/nueva                       │                │                │                  │
      │───────────────────────▶│                       │                │                │                  │
      │                        │──isAllowed(ip)?───────┼────────────────┼────────────────┼─────────────────▶│
      │                        │◀───────true/false──────┼────────────────┼────────────────┼──────────────────│
      │                        │──validateCreate()─────▶│                │                │                  │
      │                        │◀──OK / IllegalArgument─│                │                │                  │
      │                        │──createRequestWithImage()──────────────┼───────────────▶│                  │
      │                        │                       │                │──findOrganizationByIdAndCity()────▶│(dentro de RequestService)
      │                        │                       │                │◀───User (org)───│                  │
      │                        │                       │                │                │  save(Request)   │
      │◀───redirect /solicitudes/exito──────────────────────────────────┼────────────────│                  │
```

---

## 6. Diagrama de flujo — Aceptar/Rechazar/Completar solicitud (RF-6)

```
[Organización ve /acopio/requests]
            │
            ▼
[Selecciona acción: accept | reject | complete]
            │
            ▼
   POST /acopio/requests/{id}/transition
            │
            ▼
  ┌─────────────────────────┐
  │ RequestTransitionService │
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
   NotificationService.sendWhatsApp()
   (si hay teléfono de contacto)
               ▼
   redirect a /acopio/requests
```

---

## 7. Diagramas de casos de uso por actor

### Invitado
```
Invitado
  ├─ CU: Solicitar recolección sin cuenta (RF-3)
  └─ CU: Rastrear solicitud por teléfono (RF-4)
```

### Usuario (registrado)
```
Usuario
  ├─ CU: Registrarse (RF-1)
  ├─ CU: Iniciar sesión (RF-2)
  ├─ CU: Solicitar recolección (RF-3)
  ├─ CU: Rastrear solicitud por teléfono (RF-4)
  ├─ CU: Ver dashboard e historial (RF-5)
  ├─ CU: Editar solicitud propia pendiente (RF-5)
  ├─ CU: Eliminar solicitud propia pendiente (RF-5)
  └─ CU: Editar perfil (RF-7 — vía UserProfileController)
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
  ├─ CU: Completar solicitud (RF-6)
  └─ CU: Gestionar recolectores informales — CRUD (RF-8)
```

Para el detalle de precondiciones/postcondiciones de cada RF, ver `RF-RN.md`.
