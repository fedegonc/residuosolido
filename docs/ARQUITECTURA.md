# Arquitectura — Núcleo del Sistema

Inventario canónico de componentes, flujos principales y decisiones de arquitectura. Extraído del anexo de `DIAGRAMAS.md` (ahora solo figuras UML).


## Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                        PRESENTACIÓN                           │
│  Controllers  → Templates  → Fragments JS                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          NEGOCIO                             │
│  Services (8) → Modelos → Lógica de dominio                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          DATOS                               │
│  Repositories  → MongoDB                                      │
└─────────────────────────────────────────────────────────────┘
```

### Inventario (auto-generado)

<!-- INVENTORY_START -->
| Capa | Cantidad | Detalle |
|---|---|---|
| Controllers | 18 | .java en app/controller |
| Services | 12 | .java en app/service |
| Models | 8 | .java en app/model |
| Repositories | 4 | .java en app/repository |
| Templates (total) | 23 | .html en templates/ |
| Fragments | 5 | .html en templates/fragments/ |
| Test classes | 65 | *Test.java en src/test/java |
| Test methods (@Test) | 458 | anotaciones @Test |

> Generado por .config/inventory-check.sh

Última actualización: 2026-09-27T20:29:55-03:00
<!-- INVENTORY_END -->

## Mapeo por Capa

### 1. Controllers

**Auth:**
- `AuthController` — Login, registro, logout

**Usuario (Ciudadano):**
- `RequestController` — Listar, ver detalle, editar, eliminar solicitudes
- `RequestCreateController` — Crear solicitudes (invitado + usuario)

**Organización (Acopio):**
- `OrgRequestController` — Panel de acopio: estadísticas + lista, detalle y acciones (aceptar/rechazar/completar)
- `OrgProfileController` — Perfil de organización (edición y onboarding en una sola página)

**Público:**
- `DocsController` — Páginas públicas de documentación (`/documentos`, `/diagramas`)

**Soporte:**
- `Messages` — Mensajes i18n/flash compartidos, inyectado por constructor
- `@CurrentUser` + `CurrentUserArgumentResolver` — Inyección del usuario
  autenticado en parámetros de handler (reemplazaron a `BaseController`,
  eliminado: los controllers ya no hereden utilidades)

### 2. Services

- `UserService` — Gestión de usuarios y perfiles
- `UserRegistrationService` — Registro de nuevos usuarios/organizaciones
- `RequestService` — Creación, consultas, edición, eliminación y transiciones de estado de solicitudes (incluye validación de propiedad)
- `RequestMetricsService` — Métricas de solicitudes (user + org dashboards)
- `CityOrgService` — Búsqueda de organizaciones por ciudad
- `LocalImageService` — Subida de imágenes locales
- `RequestValidator` — Validaciones centralizadas de solicitudes
- `RequestServiceRetryHelper` — Retries optimistas
- `NotificationService` — Envío de notificaciones
- `NotificationEventListener` — Listener de eventos de cambio de estado
- `MongoAggregationUtils` — Utilidades estáticas de agregación MongoDB (facets)

### 3. Modelos

- `User` — Usuarios y organizaciones (mismo modelo, diferente rol)
- `Request` — Solicitudes de recolección con ciclo de estados
- `PhoneNumber` — Utility class de normalización E.164 (Uruguay +598 y Brasil +55)
- `OrganizationProfile` — Perfil de organización embebido en `User`
- `Notification` — Notificaciones generadas por transiciones de estado

### 4. Repositories

- `UserRepository` — Persistencia de usuarios
- `RequestRepository` — Persistencia de solicitudes
- `NotificationRepository` — Persistencia de notificaciones

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
  ↓ RequestService.acceptRequest(...)
Request.accept(TimeSlot)  (ciclo de estados)
  ↓ RequestRepository.save
```

### 3. Onboarding de organización (RF-7)

```
Organización registrada
  ↓ Login
AuthenticationEventHandler → Routes.resolveHomeForRole → redirige /acopio/solicitudes
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

## i18n: fuente única de verdad de copies

*(Rescatado de `docs/CORRECCIONES.md` §4 al archivarlo — ver
`docs/MEJORAS.md` #166 sobre la duplicación de carga entre las 2 clases.)*

Dos fuentes de texto, deben coincidir:

1. **`static/i18n/{lang}.json`** (uno por idioma, no por página) — fuente
   única real. `JsonMessageSource` (server-side, claves `_server_*`, usado
   por `Messages.msg()` y Thymeleaf `#{...}`) y `UiCopyCatalog`
   (`@ControllerAdvice`, expone el catálogo completo como `window.uiCopies`
   para JS y como modelo `uiCopies` para templates) parsean cada uno su
   propia copia del JSON al arrancar. Duplicación conocida y dejada a
   propósito sin resolver — ver `docs/MEJORAS.md` #166.
2. **Fallback en templates** — texto visible si el JS falla (`th:text`
   junto al `data-i18n`). Debe coincidir con el JSON; lo audita
   `TemplateI18nContractTest`/`OrphanI18nKeysTest` (recorren los 15 `.html`
   sin excepción).

**Regla:** cuando se cambia un copy, se actualizan ambas fuentes — el
contrato de tests lo hace build-breaking si se olvida.

## Contrato de Fragments

Los fragments son la raíz única de la UI. Cada uno tiene una firma fija;
cambiar un parámetro es un contrato que rompe los templates que lo llaman.

### `fragments/forms.html`

| Fragment | Parámetros | Usado en |
|---|---|---|
| `text(id, name, type, labelKey, labelText, phKey, phText, required, value)` | 9 | index, register, request-form |
| `phone(prefix, ccName, natName, dddName, labelKey, labelText, required, nationalValue, selectedCode, dddValue, hintKey, hintText)` | 12 | index, register, request-form, org/profile, track |
| `checkGrid(items, name, selected, i18nPrefix)` | 4 | request-form, org/profile |
| `check(id, name, labelKey, labelText, hintKey, hintText)` | 6 | register |
| `pin(id, name, hintKey, hintText)` | 4 | login, register |
| `submit(icon, labelKey, labelText, variant)` | 4 | 6 páginas |
| `altCta(titleKey, descKey, href, icon, buttonKey)` | 5 | register, login, request-form |

### `fragments/ui.html`

| Fragment | Parámetros | Usado en |
|---|---|---|
| `state(icon, messageKey, message)` | 3 | org/requests, users/requests, track |
| `status(status)` | 1 | request-list, track |
| `row(icon, label, value)` | 3 | org/profile |
| `tile(icon, value, labelKey, labelText)` | 4 | org/requests, users/requests |
| `options` | 0 | request-form |
| `msg(type, text)` | 2 | flash messages |
| `trail(crumbs)` | 1 | org/requests, users/requests |
| `greeting(greetingKey, greetingText, name, exclamation, subtitleKey, subtitleText)` | 6 | users/requests |
| `educationalLinks(cards)` | 1 | org/requests, users/requests |

### `fragments/landing-cards.html`

| Fragment | Parámetros | Usado en |
|---|---|---|
| `card(icon, titleKey, descKey, buttonKey, href)` | 5 | index |
| `section` | 0 | index |

## Decisiones de Arquitectura

- **Sin panel Admin**: Gestión distribuida por roles (USER, ORGANIZATION).
- **Separación User / Organization (en progreso)**: el sandbox (`scratch/sim/`) ya modela `User` (auth) y `Organization` (perfil de negocio) como entidades separadas. El Spring real aún usa el mono-modelo `User` + `OrganizationProfile` embebido; el port planificado seguirá este blueprint. Ver `docs/MEJORAS.md` #221.
- **Cobertura binacional**: Enum `City` limitado a RIVERA y LIVRAMENTO.
- **Breadcrumbs inline**: Construidos con `List.of(Map.of(...))` en cada controller.
- **JavaScript scoped por página**: lo global/reusado por 2+ páginas vive en `app.js`; lo exclusivo de una página (ej. `filterMaterialsByOrg`, el toggle view/edit de perfil) va en su propio archivo (`request-form.js`, `org-profile.js`) cargado vía `layout:fragment="pageScripts"`. Reemplaza al enfoque anterior de scripts embebidos en fragments HTML (`fragments/toggle-view-edit.html`/`request-form-js.html`, eliminados).
- **Imágenes locales**: `LocalImageService` guarda archivos en disco, no en Cloudinary.
- **Sin API REST pública**: Spring MVC + Thymeleaf SSR de punta a punta. `/solicitudes/org-options` es el único endpoint JSON/HTML-fragment, y es infraestructura interna del formulario (fetch de `request-form.js`), no una API de consumo externo — sin Swagger/OpenAPI. Ver `docs/TRADEOFFS.md` §35.

## Limitaciones de escalabilidad (estado actual)

Estas son limitaciones conscientes del MVP, documentadas como tradeoffs en
`docs/TRADEOFFS.md` y verificables por código:

| Componente | Asunción | Implicación si se escala |
|---|---|---|
| `RateLimiter` | **Nodo único**: contadores en `ConcurrentHashMap` locales. | Con réplicas, cada nodo tiene su propio rate limit / lockout. Un atacante distribuido puede sortear el lockout por usuario rotando entre instancias. Migración: Redis + Bucket4j o un gateway con rate limiting centralizado. |
| `LocalImageService` | **Disco local**: guarda en `uploads/` del filesystem del contenedor. | En PaaS con filesystem efímero (Render) o réplicas, las imágenes se pierden o no son accesibles desde otro nodo. Migración: Cloudinary, S3 o volumen compartido. |
| `NotificationService` | **Best-effort in-process**: `notifyRequester` se llama después del save de la request, en el mismo hilo y sin transacción. | Si el proceso falla entre el save de la request y el save de la notification, el ciudadano no recibe la notificación in-app aunque el estado sí quedó persistido. Migración: outbox pattern / evento transaccional. |
| Caches de servicio (ej. `CityOrgService`, `LandingCardLoader`) | Memoria local por nodo (`@Cacheable` → ConcurrentMapCacheManager de Spring). | Con réplicas, cada nodo puede servir contenido cacheado distinto. Migración: Redis / Caffeine compartido o invalidación centralizada. |

## Pruebas

Ver el inventario auto-generado arriba para el conteo actual de clases y métodos de test. Ver también `docs/ENDPOINTS.md` y `docs/INDICE.md`.
