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
| GET | `/rastrear` | `GuestTrackingController` | Formulario de rastreo por teléfono |
| POST | `/rastrear` | `GuestTrackingController` | Busca solicitudes por teléfono |
| GET | `/metricas` | `PublicMetricsController` | Métricas públicas (total completadas, por ciudad) — **sin protección**, ver backlog en `RF-RN.md` |
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
| GET | `/acopio/inicio` | `OrgDashboardController` | Dashboard con estadísticas y solicitudes pendientes recientes |
| GET | `/acopio/completar-perfil` | `OrgOnboardingController` | Formulario de onboarding forzado (teléfono + ciudad) |
| POST | `/acopio/completar-perfil` | `OrgOnboardingController` | Guarda el perfil inicial |
| GET | `/acopio/perfil` | `OrgProfileController` | Formulario de edición de perfil |
| POST | `/acopio/perfil` | `OrgProfileController` | Actualiza datos de la organización |
| GET | `/acopio/requests` | `OrgRequestController` | Lista de solicitudes asignadas, con filtro por estado y paginado |
| GET | `/acopio/requests/{id}` | `OrgRequestDetailController` | Detalle de una solicitud asignada |
| POST | `/acopio/requests/{id}/transition` | `OrgRequestController` | Cambia estado: `action=accept\|reject\|complete` |
| GET | `/acopio/catadores` | `InformalCollectorController` | Lista de recolectores informales de la organización |
| GET | `/acopio/catadores/edit/{id}` | `InformalCollectorController` | Carga un recolector para editar |
| POST | `/acopio/catadores` | `InformalCollectorController` | Crea o actualiza un recolector (según si llega `id`) |
| POST | `/acopio/catadores/{id}/delete` | `InformalCollectorController` | Elimina un recolector |

---

## Notas

- No existen rutas `/admin/**` — no hay rol Admin ni panel de administración general.
- No existen rutas `/posts`, `/categories`, `/feedback` — no hay CMS ni sistema de contenido educativo.
- Las rutas están en español (`/usuarios`, `/acopio`, `/solicitudes`) por decisión de diseño, sin alias en inglés.
- `PublicMetricsController` (`/metricas`) es la única ruta pública que expone datos agregados sin protección — pendiente en el backlog (`RF-RN.md`, sección 6) renombrarlo/protegerlo si se agregan métricas privadas por organización.
