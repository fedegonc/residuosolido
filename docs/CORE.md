# Núcleo del Sistema — Residuo Sólido

## Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                        PRESENTACIÓN                           │
│  Controllers (16) → Templates (27) → Fragments JS           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          NEGOCIO                             │
│  Services (11) → Validadores → Lógica de dominio            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          DATOS                               │
│  Repositories (3) → MongoDB (Request, User, InformalCollector)│
└─────────────────────────────────────────────────────────────┘
```

## Mapeo por Capa

### 1. Controllers (16 archivos)

**Auth:**
- `AuthController` — Login, registro, logout

**Usuario (Ciudadano):**
- `RequestController` — Listar, ver detalle, eliminar solicitudes
- `RequestCreateController` — Crear solicitudes (invitado + usuario)
- `RequestEditController` — Editar solicitudes pendientes
- `UserProfileController` — Dashboard y perfil del ciudadano

**Organización (Acopio):**
- `OrgDashboardController` — Dashboard de organización con Kanban integrado
- `OrgRequestController` — Lista de solicitudes recibidas
- `OrgRequestDetailController` — Detalle y acciones (aceptar/rechazar/completar)
- `OrgProfileController` — Perfil de organización
- `OrgOnboardingController` — Completar perfil post-registro
- `InformalCollectorController` — CRUD de catadores (sin link en sidebar, ver TRADEOFFS.md)

**Público:**
- `PublicMetricsController` — Métricas abiertas de reciclaje por ciudad
- `GuestTrackingController` — Rastreo de solicitudes por teléfono + código
- `BlogController` — Blog estático en una sola página (`/blog`)

**API:**
- `OrgApiController` — Listado de organizaciones por ciudad (JSON)

**Base:**
- `BaseController` — Utilidades comunes (usuario actual, mensajes flash)

### 2. Services (11 archivos)

- `UserService` — Gestión de usuarios y perfiles
- `UserRegistrationService` — Registro de nuevos usuarios/organizaciones
- `RequestService` — Creación de solicitudes
- `RequestQueryService` — Consultas de solicitudes
- `RequestUpdateService` — Edición y eliminación de solicitudes
- `RequestOrgService` — Consultas de solicitudes para organizaciones
- `RequestTransitionService` — Transiciones de estado (aceptar/rechazar/completar)
- `RequestMetricsService` — Métricas de solicitudes (user + org dashboards)
- `PublicMetricsService` — Métricas públicas por ciudad
- `CityOrgService` — Búsqueda de organizaciones por ciudad
- `LocalImageService` — Subida de imágenes locales
- `InformalCollectorService` — Gestión de recolectores informales
- `NotificationService` — Envío de notificaciones WhatsApp (mock)

### 3. Modelos (5 clases)

- `User` — Usuarios y organizaciones (mismo modelo, diferente rol)
- `Request` — Solicitudes de recolección con ciclo de estados
- `InformalCollector` — Recolectores informales vinculados a una org
- `OrganizationDto` — DTO para API de organizaciones

### 4. Repositories (3 interfaces)

- `UserRepository` — Persistencia de usuarios
- `RequestRepository` — Persistencia de solicitudes
- `InformalCollectorRepository` — Persistencia de recolectores

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
  ↓ NotificationService.sendWhatsApp
  ↓ RequestRepository.save
```

### 3. Onboarding de organización (RF-7)

```
Organización registrada
  ↓ Login
LoginSuccessHandler → redirige /acopio/completar-perfil
OrgOnboardingController.completeProfile
  ↓ UserService.completeOrgProfile
  ↓ profileCompleted = true
  ↓ Redirect /acopio/inicio
```

## Decisiones de Arquitectura

- **Sin panel Admin**: Gestión distribuida por roles (USER, ORGANIZATION).
- **Mono-modelo User**: Usuarios y organizaciones comparten la misma entidad, diferenciados por `Role`.
- **Cobertura binacional**: Enum `City` limitado a RIVERA y LIVRAMENTO.
- **Breadcrumbs inline**: Construidos con `List.of(Map.of(...))` en cada controller.
- **JavaScript por fragmento**: Reutilización de scripts en `fragments/toggle-view-edit.html` y `fragments/request-form-js.html`.
- **Notificaciones mock**: `NotificationService` solo loggea (WhatsApp no integrado).
- **Imágenes locales**: `LocalImageService` guarda archivos en disco, no en Cloudinary.

## Pruebas

193 tests unitarios e integrales en 22 suites. Ver `docs/TESTING.md`.
