# Diagramas — Eco Solicitud

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
└────────────┘          └──────────────┘          └───────────────────────┘
```

- 2 colecciones Mongo: `users` y `requests`.
- `requests.user` → referencia a `users` (opcional, null si es invitado).
- `requests.organization` → referencia a `users` con `role=ORGANIZATION` (obligatoria tras crear/editar).

---

## 4. Diagrama de secuencia — Crear solicitud (RF-3)

**Figura 4** (`docs/diagrams/figura4-secuencia.drawio`): diagrama de secuencia UML 2.5 del flujo real de creación de una solicitud.

**Participantes:**

- Solicitante (`Invitado` o `Usuario`).
- Formulario Thymeleaf `request-form.html`.
- `RequestCreateController`.
- `GuestRateLimiter`.
- `RequestService`.
- `LocalImageService`.
- `CityOrgService`.
- `RequestRepository`.

**Secuencia representada:**

1. El solicitante abre `/solicitudes/nueva` y completa el formulario.
2. El formulario envía `POST /solicitudes`.
3. Si es invitado, el controller verifica el rate limit.
4. `RequestService` valida la imagen y los datos de la solicitud.
5. `CityOrgService` valida la organización seleccionada y su ciudad.
6. `RequestRepository` persiste la solicitud.
7. Si existe imagen, `LocalImageService` la guarda y actualiza `imageUrl`.
8. El controller redirige a `/solicitudes/exito`.

---

## 5. Ciclo de estados de `Request` — complemento textual

```text
PENDING ──accept(slot)──> IN_PROGRESS ──complete()──> COMPLETED
   │                            │
   └────────reject()────────> REJECTED <──reject()───┘
```

- Solo `PENDING` puede editarse o eliminarse.
- `accept` requiere una franja horaria.
- `REJECTED` y `COMPLETED` son estados finales.
- Las transiciones se protegen con `@Version` y optimistic locking.

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
   redirect a /acopio/requests
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
  ├─ CU: Editar solicitud propia pendiente (RF-5)
  ├─ CU: Eliminar solicitud propia pendiente (RF-5)
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

Para el detalle de precondiciones/postcondiciones de cada RF, ver `RF-RN.md`.

---

# Requisitos y Reglas de Negocio (anexo)

**Nota para la defensa de tesis:** estos puntos no son omisiones — son decisiones de alcance conscientes, justificadas porque exceden lo que una herramienta de software puede o debe resolver.

---

## 6. Criterio de alcance y backlog pendiente

**Criterio para decidir si algo nuevo entra al alcance** (chequear en este orden):
1. ¿Está en el oficio o surge de una necesidad real confirmada por el stakeholder (organización/usuario)?
2. ¿Es responsabilidad de un sistema de software, o es logística/inversión física/proceso humano?
3. ¿Se puede resolver con un campo o servicio simple, o requiere una entidad/módulo nuevo?

Si 1 es sí, 2 es "sí es del software" y 3 es "simple" → entra al backlog. Si no, se documenta como limitación consciente (sección 5).

**Backlog pendiente (no implementado):**
- 🟡 **Métricas privadas por organización + descarga PDF.** Nueva ruta protegida `/acopio/metricas` con `@PreAuthorize("hasRole('ORGANIZATION')")` y endpoint `GET /acopio/metricas/pdf` (sugerido: OpenPDF o iText community). La ruta pública `/metricas` (totales agregados, sin datos personales) es una decisión consciente de diseño, no un bug — está explícitamente en `permitAll()` en `SecurityConfig`.
- 🟡 **Consistencia de nombres** (baja prioridad): revisar que los nombres de métodos de `RequestQueryService`/`RequestOrgService`/`RequestMetricsService`/`CityOrgService` reflejen consistentemente su sub-dominio.

---

# Núcleo del Sistema (anexo)


## Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                        PRESENTACIÓN                           │
│  Controllers (16) → Templates (28) → Fragments JS           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          NEGOCIO                             │
│  Services (8) → Modelos → Lógica de dominio                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          DATOS                               │
│  Repositories (2) → MongoDB (Request, User)                  │
└─────────────────────────────────────────────────────────────┘
```

## Mapeo por Capa

### 1. Controllers (16 archivos)

**Auth:**
- `AuthController` — Login, registro, logout

**Usuario (Ciudadano):**
- `RequestController` — Listar, ver detalle, editar, eliminar solicitudes
- `RequestCreateController` — Crear solicitudes (invitado + usuario)

**Organización (Acopio):**
- `OrgDashboardController` — Dashboard de organización con Kanban integrado
- `OrgRequestController` — Lista, detalle y acciones (aceptar/rechazar/completar)
- `OrgProfileController` — Perfil de organización + onboarding post-registro

**Público:**
- `GuestTrackingController` — Rastreo de solicitudes por teléfono + código
- `DocsController` — Páginas públicas de documentación (`/documentos`, `/diagramas`)

**API:**
- `OrgApiController` — Listado de organizaciones por ciudad (JSON)

**Base:**
- `BaseController` — Utilidades comunes (usuario actual, mensajes flash)

### 2. Services (9 archivos)

- `UserService` — Gestión de usuarios y perfiles
- `UserRegistrationService` — Registro de nuevos usuarios/organizaciones
- `RequestService` — Creación, edición y eliminación de solicitudes
- `RequestQueryService` — Consultas de solicitudes y validación de propiedad
- `RequestTransitionService` — Transiciones de estado (aceptar/rechazar/completar)
- `RequestMetricsService` — Métricas de solicitudes (user + org dashboards)
- `PublicMetricsService` — Métricas públicas por ciudad
- `CityOrgService` — Búsqueda de organizaciones por ciudad
- `LocalImageService` — Subida de imágenes locales

### 3. Modelos (3 clases + DTO)

- `User` — Usuarios y organizaciones (mismo modelo, diferente rol)
- `Request` — Solicitudes de recolección con ciclo de estados
- `PhoneNumber` — Utility class de normalización E.164 (Uruguay +558 y Brasil +55)
- `OrganizationDto` — DTO para API de organizaciones

### 4. Repositories (2 interfaces)

- `UserRepository` — Persistencia de usuarios
- `RequestRepository` — Persistencia de solicitudes

## Flujos Principales

### 1. Solicitud de recolección (RF-3)

```
Usuario/Invitado
  ↓ POST /solicitudes
RequestCreateController
  ↓ RequestService.createRequest(...)
CityOrgService.findOrganizationByIdAndCity  (valida org en ciudad)
  ↓ RequestRepository.save
  ↓ Redirect /solicitudes/exito
```

### 2. Aceptar solicitud (RF-6)

```
Organización
  ↓ POST /acopio/requests/{id}/transition?action=accept
OrgRequestController
  ↓ RequestTransitionService.acceptRequest(...)
Request.accept(TimeSlot)  (ciclo de estados)
  ↓ RequestRepository.save
```

### 3. Onboarding de organización (RF-7)

```
Organización registrada
  ↓ Login
LoginSuccessHandler → redirige /acopio/completar-perfil
OrgProfileController.completeProfile
  ↓ UserService.completeOrgProfile
  ↓ profileCompleted = true
  ↓ Redirect /acopio/inicio
```

## Sistema de Layouts (Thymeleaf Layout Dialect)

Layouts anidados con `layout:decorate` para separar responsabilidades sin duplicar navbar/footer/alerts en cada página:

```
layout/base.html (raíz)
  ├─ slots: content, extraHead, pageScripts
  ├─ maneja: navbar, alerts globales, footer, scripts globales (app.js)
  │
  └─ layout/base-sidebar.html (decora base.html)
       ├─ slots: sidebar, pageContent
       ├─ maneja: grid de 2 columnas (.org-layout)
       │
       └─ org/dashboard.html, org/requests.html, org/profile.html
            (llenan solo 'sidebar' y 'pageContent', heredan todo lo demás)
```

**Por qué dos niveles en vez de fragmentos nativos:** Layout Dialect es 100% server-side
(no pesa en el bundle del navegador) y da slots múltiples con herencia real. Los 3
templates de `/org` antes duplicaban `<div class="org-layout"><sidebar/><main>...`
manualmente; ahora solo declaran su `sidebar` y `pageContent`, y cualquier página nueva
con sidebar puede decorar `layout/base-sidebar` sin repetir el grid.

## Decisiones de Arquitectura

- **Sin panel Admin**: Gestión distribuida por roles (USER, ORGANIZATION).
- **Mono-modelo User**: Usuarios y organizaciones comparten la misma entidad, diferenciados por `Role`.
- **Cobertura binacional**: Enum `City` limitado a RIVERA y LIVRAMENTO.
- **Breadcrumbs inline**: Construidos con `List.of(Map.of(...))` en cada controller.
- **JavaScript scoped por página**: lo global/reusado por 2+ páginas vive en `app.js`; lo exclusivo de una página (ej. `filterMaterialsByOrg`, el toggle view/edit de perfil) va en su propio archivo (`request-form.js`, `org-profile.js`) cargado vía `layout:fragment="pageScripts"`. Reemplaza al enfoque anterior de scripts embebidos en fragments HTML (`fragments/toggle-view-edit.html`/`request-form-js.html`, eliminados).
- **Imágenes locales**: `LocalImageService` guarda archivos en disco, no en Cloudinary.

## Pruebas

176 tests unitarios e integrales en 22 suites. Ver `docs/ENDPOINTS.md`.
