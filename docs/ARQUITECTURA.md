# Arquitectura — Núcleo del Sistema

Inventario canónico de componentes, flujos principales y decisiones de arquitectura. Extraído del anexo de `DIAGRAMAS.md` (ahora solo figuras UML).


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
- `OrgRequestController` — Panel de acopio: estadísticas + lista, detalle y acciones (aceptar/rechazar/completar)
- `OrgProfileController` — Perfil de organización (edición y onboarding en una sola página)

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
  ↓ POST /acopio/solicitudes/{id}/aceptar
OrgRequestController
  ↓ RequestTransitionService.acceptRequest(...)
Request.accept(TimeSlot)  (ciclo de estados)
  ↓ RequestRepository.save
```

### 3. Onboarding de organización (RF-7)

```
Organización registrada
  ↓ Login
LoginSuccessHandler → redirige /acopio/solicitudes
OrgRequestController.orgRequests
  ↓ needsProfileCompletion() → Redirect /mi-organizacion
OrgProfileController.profile (abre en modo edición)
  ↓ PUT /mi-organizacion
UserService.updateProfile → auto-completa si tiene teléfono + ciudad
  ↓ Redirect /acopio/solicitudes
```

## Sistema de Layouts (Thymeleaf Layout Dialect)

Todas las páginas decoran un único layout raíz con `layout:decorate`, sin duplicar navbar/footer/alerts en cada página:

```
layout/base.html (raíz)
  ├─ slots: content, extraHead, pageScripts
  ├─ maneja: navbar, alerts globales, footer, scripts globales (app.js)
  │
  └─ todas las páginas: org/requests.html, org/profile.html,
     users/requests.html, users/track.html, auth/*, public/*
     (llenan solo 'content', heredan todo lo demás)
```

**Por qué un solo nivel:** antes existía `layout/base-sidebar.html` como segundo nivel
para las páginas de `/org` (sidebar con 2 links), pero el navbar global ya muestra los
mismos links para el rol ORGANIZATION — era navegación duplicada. Se eliminó y las
páginas de org decoran `base.html` directamente (ver `docs/MEJORAS.md` #130).

## Decisiones de Arquitectura

- **Sin panel Admin**: Gestión distribuida por roles (USER, ORGANIZATION).
- **Mono-modelo User**: Usuarios y organizaciones comparten la misma entidad, diferenciados por `Role`.
- **Cobertura binacional**: Enum `City` limitado a RIVERA y LIVRAMENTO.
- **Breadcrumbs inline**: Construidos con `List.of(Map.of(...))` en cada controller.
- **JavaScript scoped por página**: lo global/reusado por 2+ páginas vive en `app.js`; lo exclusivo de una página (ej. `filterMaterialsByOrg`, el toggle view/edit de perfil) va en su propio archivo (`request-form.js`, `org-profile.js`) cargado vía `layout:fragment="pageScripts"`. Reemplaza al enfoque anterior de scripts embebidos en fragments HTML (`fragments/toggle-view-edit.html`/`request-form-js.html`, eliminados).
- **Imágenes locales**: `LocalImageService` guarda archivos en disco, no en Cloudinary.

## Pruebas

181 tests unitarios e integrales en 24 clases. Ver `docs/ENDPOINTS.md`.
