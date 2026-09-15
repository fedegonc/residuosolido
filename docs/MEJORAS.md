# Superficies de Mejora — Estado

> **Propósito:** tabla centralizada de todas las mejoras posibles del
> sistema, con su estado actual: implementado, descartado o diferido.
>
> **Fecha:** post-commit (`15b7db5`)
> **Tests:** 218, 0 failures

---

## Leyenda

- **Implementado:** hecho y funcionando
- **Descartado:** evaluado y rechazado (con justificación)
- **Diferido:** pendiente, postergado a después de la defensa
- **Latente:** código existe pero sin UI activa

---

## Tabla de superficies

| # | Mejora | Estado | Justificación |
|---|---|---|---|
| 1 | Auth con roles + onboarding forzado | Implementado | Spring Security 6, roles USER/ORGANIZATION |
| 2 | Solicitudes de recolección (CRUD) | Implementado | Crear, editar, eliminar, ver detalle |
| 3 | Rastreo por teléfono + código privado | Implementado | 8 caracteres, sin login requerido |
| 4 | Selector de código de país (UY/BR) | Implementado | Normalización E.164 |
| 5 | Flujo de estados (PENDING→IN_PROGRESS→COMPLETED/REJECTED) | Implementado | Con optimistic locking |
| 6 | Kanban integrado al dashboard de org | Implementado | 4 columnas, sin página aparte |
| 7 | Blog estático (3 artículos) | Implementado | Una sola página con anchors |
| 8 | Métricas públicas por ciudad | Implementado | Sin auth requerido |
| 9 | Rate limiting de invitados | Implementado | Ventana deslizante por IP |
| 10 | Bloqueo por intentos de login | Implementado | 5 intentos, 15 min bloqueo |
| 11 | PWA instalable | Implementado | manifest.json, SW, install prompt |
| 12 | i18n español/portugués | Implementado | Client-side + server-side |
| 13 | Dark mode toggle | Implementado | CSS variables, localStorage, prefers-color-scheme |
| 14 | FOUC fix (flash de texto sin traducir) | Implementado | visibility:hidden hasta i18n ready |
| 15 | Diseño canónico (variables CSS, BEM) | Implementado | 0 estilos inline, 0 hex fuera de :root |
| 16 | Empty states con iconos + microcopy | Implementado | Font Awesome, sin emojis |
| 17 | Microcopy anti-slop | Implementado | Descripciones concretas, no aspiracional |
| 18 | CSRF en todos los formularios | Implementado | Spring Security |
| 19 | DTO RegistrationForm (sin mass-assignment) | Implementado | Validación server-side |
| 20 | Optimistic locking (@Version) | Implementado | En transiciones y borrado |
| 21 | Validación de imagen antes de persistir | Implementado | Tipo, extensión, tamaño |
| 22 | Contraseña mínima 3 caracteres | Implementado | Decisión de fase MVP |
| 23 | Service Worker cache (CSS/JS only) | Implementado | HTML network-first, sin pre-cache |
| 24 | Limpieza de claves muertas (messages_*) | Implementado | 136→91 claves, ES/PT sincronizados |
| 25 | Documentación centralizada con índice docs/INDICE.md | Implementado | Single source of truth |
| 26 | Diagramas UML (casos de uso, ER, clases, estados) | Implementado | draw.io, 4 figuras |
| 27 | 218 tests (unit + integration + e2e) | Implementado | 0 failures |
| 28 | Metodología iterativo-incremental (4 fases) | Implementado | docs/METODOLOGIA.md |
| 29 | 21 tradeoffs documentados | Implementado | docs/DEFENSA.md |
| 30 | Deploy en Render.com (PaaS) | Implementado | GitHub→deploy automático |
| 31 | Páginas públicas /documentos y /diagramas | Implementado | DocsController, índice de docs y figuras |
| 32 | Diagrama de secuencia UML 2.5 (Figura 4) | Implementado | figura4-secuencia.drawio, flujo de creación de solicitud |
| 33 | CI/CD pipeline (GitHub Actions) | Descartado | Workflow eliminado; la verificación es manual y Render construye el artefacto sin ejecutar tests dependientes del entorno |
| 34 | **AWS (IaaS)** | **Descartado** | Complejidad no aporta a la tesis. Ver docs/DEFENSA.md §15 |
| 35 | **VPN para acceso** | **Descartado** | Render.com da dominio público + HTTPS |
| 36 | **MCP de AWS** | **Descartado** | Sin credenciales AWS, sin valor para MVP |
| 37 | **Panel Admin / moderación** | **Descartado** | Fuera de scope MVP. Ver docs/DEFENSA.md §2 |
| 38 | **CMS para blog** | **Descartado** | Blog estático suficiente. Ver docs/DEFENSA.md §5 |
| 39 | **Mapas / geolocalización** | **Descartado** | Fuera de scope. Ver docs/DEFENSA.md §10 |
| 40 | **Mascota virtual** | **Descartado** | Microcopy + iconos en su lugar |
| 41 | **Emojis en UI** | **Descartado** | Inapropiado para tesis académica |
| 42 | **Separar Cuenta de Organización** | **Descartado** | Mono-modelo User. Ver docs/DEFENSA.md §1 |
| 43 | **MongoDB réplica set / transacciones** | **Descartado** | Standalone suficiente para MVP. Ver docs/DEFENSA.md §3 |
| 44 | **WhatsApp Business API real** | **Descartado** | Eliminadas del MVP. Ver docs/DEFENSA.md §8 |
| 45 | **Cloud storage (S3/Cloudinary)** | **Descartado** | Local en disco. Ver docs/DEFENSA.md §9 |
| 46 | Fusionar request-form + request-edit | Diferido | 2 controllers, riesgo alto. Ver docs/DEFENSA.md §14 |
| 47 | Fusionar perfiles (user + org + onboarding) | Diferido | 3 controllers, riesgo alto. Ver docs/DEFENSA.md §14 |
| 48 | Fusionar dashboards (user + org) | Diferido | Riesgo medio. Ver docs/DEFENSA.md §14 |
| 49 | Reducir clases CSS con utilities | Diferido | 292 clases, cambio masivo. Ver docs/DEFENSA.md §14 |
| 50 | Consolidar stat-card (20→8 clases) | Diferido | Posible sobre-diseño |
| 51 | Simplificar kanban (22→15 clases) | Diferido | Componente más complejo |
| 52 | Unificar fuentes de copies (3→1) | Diferido | data-i18n + messages_* + JSON |
| 53 | Catadores CRUD (InformalCollector) | Latente | Modelo + repo + controller + 4 endpoints sin UI |
| 54 | Contraseña mínima 8+ (producción) | Diferido | 3 chars para MVP. Ver docs/DEFENSA.md §4 |
| 55 | Asignación de recolector a solicitud (RF-8) | Diferido | Catadores latentes |
| 56 | Transiciones suaves entre temas | Diferido | Intencional: evitar parpadeo |
| 57 | SEO / meta tags dinámicos | Diferido | El sistema es una app, no un blog |
| 58 | Tests E2E con Selenium/Playwright | Diferido | E2E con MockMvc suficiente |
| 59 | Monitoreo (Prometheus/Grafana) | Diferido | Fuera de scope MVP |
| 60 | Logs estructurados (JSON) | Diferido | Logs Spring Boot suficientes |
| 61 | Rate limiting con Redis | Diferido | In-memory suficiente para MVP |
| 62 | Multi-tenancy (multi-frontera) | Diferido | Una sola frontera (Rivera-Livramento) |
| 63 | Upgrade Java 17→21 (pom + Dockerfile) | Implementado | Java 21 LTS en pom, Dockerfile y CI |
| 64 | Upgrade JaCoCo 0.8.11→0.8.15 | Implementado | Patch, sin breaking changes |
| 65 | Upgrade maven-pmd-plugin 3.21→3.28 | Implementado | PMD 7, rulesets category-based |
| 66 | Upgrade FontAwesome 6.5→7.2 | Implementado | CDN y webjar actualizados |
| 67 | Migración Spring Boot 3.2→3.5 | Diferido | Paso intermedio seguro antes de 4.0 |
| 68 | Migración Spring Boot 3.5→4.0 | Diferido | 115 breaking changes, @MockBean eliminado |
|| 69 | Value Objects `Email` y `Name` en `User` | Implementado | Validación y canonicalización server-side |
|| 70 | Notificaciones WhatsApp / asincrónicas | Descartado | Eliminadas del MVP; no proveedor configurado |
|| 71 | OpenAPI / Swagger UI | Implementado | `/swagger-ui.html` generado desde controllers |
|| 72 | Centralización de rutas en `Routes.java` | Implementado | Única fuente de verdad para endpoints y seguridad |

---

## Resumen por estado

| Estado | Cantidad |
|---|---|
| Implementado | 39 |
| Descartado | 14 |
| Diferido | 18 |
| Latente | 1 |
| **Total** | **72** |

---

## Crecimientos innecesarios detectados

| Superficie | Antes | Después | Acción |
|---|---|---|---|
| Claves messages_es | 136 | 91 | Limpiado (45 muertas) |
| Claves messages_pt | 109 | 91 | Sincronizado con ES |
| Claves messages.properties | 108 | 108 | Sin cambio (fallback) |
| Claves data-i18n en templates | 227 | 227 | Sin cambio (en uso) |
| CSS clases | 292 | ~300 | +8 (dark mode + theme btn) |
| CSS líneas | 582 | ~670 | +88 (dark theme) |
| SW cache version | v14 | v17 | Bumpeado |
| Documentos docs/ | 9 | 11 | +COPIES.md, +SUPERFICIES.md |
| Diagramas activos docs/diagrams/ | 3 figuras | 4 figuras | +figura4-secuencia.drawio |
| Controllers | 16 | 17 | +DocsController |
| Templates | 27 | 29 | +docs.html, +diagrams.html |
| Endpoints públicos | 8 | 10 | +/documentos, +/diagramas |
| CI/CD | — | 1 workflow | +.github/workflows/ci.yml |

---

# Hardening — Correcciones aplicadas (anexo)


Este documento describe las correcciones aplicadas al MVP y las limitaciones
que quedan fuera del alcance actual. Forma parte del análisis crítico del
sistema y debe consultarse junto con los diagramas de `docs/diagrams/`.

## Correcciones aplicadas

### 1. Registro de cuentas

- **Antes:** el controlador recibía directamente la entidad `User` mediante
  `@ModelAttribute` y la persistía. Un cliente podía enviar campos internos
  (`id`, `profileCompleted`, `acceptedMaterials`, `role`).
- **Ahora:** el controlador recibe un `RegistrationForm` (DTO) con solo
  `username`, `email` y `password`. El servicio construye una entidad nueva
  bajo control del servidor y asigna el rol según el checkbox de
  organización.
- **Validaciones:** username obligatorio y sin espacios; contraseña mínima
  de 8 caracteres (ver sección 11); email válido y normalizado a
  minúsculas; unicidad de username y email con índices únicos en MongoDB
  y manejo de `DuplicateKeyException`.

### 2. Rastreo de solicitudes de invitado

- **Antes:** el rastreo público consultaba por teléfono solo. Cualquier
  persona que conociera el teléfono podía ver dirección, materiales y
  organización de solicitudes ajenas.
- **Ahora:** cada solicitud de invitado recibe un código privado de 8
  caracteres al crearse. El rastreo requiere teléfono + código. El teléfono
  solo no devuelve resultados.
- **UI:** la pantalla de éxito muestra el código al invitado; el formulario
  de rastreo pide ambos campos; los textos (es/pt) fueron actualizados.
- **Solicitudes antiguas sin código:** no son rastreables por el flujo
  público. La organización puede consultarlas desde su panel. No se habilitó
  acceso inseguro por compatibilidad.

### 3. Disponibilidad de organizaciones

- **Antes:** `getOrganizationsByCity` caía a organizaciones inactivas si no
  había activas. La asignación de una solicitud no verificaba `active`.
- **Ahora:** una organización recibe solicitudes solo si está activa, tiene
  rol `ORGANIZATION`, perfil completo, teléfono válido, ciudad coincidente
  y materiales aceptados no vacíos. El listado por ciudad nunca cae a
  organizaciones inactivas o incompletas.

### 4. Validación de materiales

- **Antes:** el navegador ocultaba los materiales no aceptados, pero el
  servidor no verificaba la compatibilidad.
- **Ahora:** el servidor valida que todos los materiales solicitados estén
  incluidos en `acceptedMaterials` de la organización, tanto en la creación
  como en la edición.

### 5. Persistencia de imagen

- **Antes:** `createRequestWithImage` persistía la solicitud y después
  subía la imagen. Si la imagen fallaba, la solicitud quedaba huérfana.
- **Ahora:** la imagen se valida (tipo, extensión, tamaño) antes de
  persistir la solicitud. Una imagen inválida no deja solicitud en la base.

### 6. Borrado concurrente

- **Antes:** el borrado hacía `deleteById` sin verificar versión. Entre la
  verificación de propiedad/estado y el borrado, una organización podía
  aceptar la solicitud.
- **Ahora:** el borrado usa `delete(entity)` sobre la entidad cargada con
  su `@Version`. Si una transición concurrente la modificó, se lanza
  `OptimisticLockingFailureException` con un mensaje controlado.

### 7. Edición de solicitudes

- **Antes:** la edición no revalidaba la compatibilidad de materiales con la
  organización.
- **Ahora:** la edición revalida ciudad, dirección, materiales,
  organización y compatibilidad de materiales. Solo las solicitudes
  `PENDING` pueden editarse.

### 8. Teléfono canónico

- **Antes:** los formatos `+598 99 123 456` y `+59899123456` se trataban
  como distintos.
- **Ahora:** `PhoneNumber` normaliza espacios y valida formato
  internacional. La misma canonicidad se aplica a usuarios, invitados,
  recolectores informales y consultas de rastreo.

### 9. Actualización de perfil

- **Antes:** `updateUser` confiaba en los campos de identidad del objeto
  enviado.
- **Ahora:** carga el usuario persistido, valida email duplicado, normaliza
  teléfono y actualiza solo los campos permitidos.

### 10. Promesa de "más cercana"

- **Antes:** la portada decía que la solicitud se enviaba a la cooperativa
  "más cercana", pero el sistema implementa elección explícita del usuario.
- **Ahora:** los textos (es/pt) describen elección explícita por ciudad y
  materiales.

## Limitaciones reconocidas (fuera del alcance del MVP)

### Arquitectura de cuenta/organización

- Una cuenta `User` con rol `ORGANIZATION` representa simultáneamente
  identidad de acceso y participante de negocio.
- **No soporta:** múltiples operadores por cooperativa, cambio de
  responsable sin cambiar la cuenta, ni una misma persona como ciudadano y
  operador de cooperativa.
- **Para soportarlo** haría falta separar `Cuenta`, `Organización` y su
  relación de pertenencia. No se hace en este MVP.

### Verificación de legitimidad

- El registro público permite autodeclararse organización. No verifica que
  la cooperativa exista ni que quien se registra la represente.
- **Para producción** se necesitaría validación humana o un entorno
  controlado.

### Trazabilidad del residuo

- `COMPLETED` significa "la organización declaró completada la solicitud".
  No verifica retiro físico, cantidad recibida, conformidad del ciudadano
  ni destino final.
- Las métricas cuentan solicitudes completadas, no kilos reciclados ni
  impacto ambiental medido.

### Asignación de recolector

- `InformalCollector` es una agenda interna de la organización. `Request`
  no tiene relación con el recolector responsable.
- No se puede responder "qué recolector atendió esta solicitud".

### Concurrencia

- MongoDB standalone no soporta transacciones multi-documento. Las
  operaciones que involucran varios documentos (crear solicitud + subir
  imagen) no son atómicas a nivel de base de datos.
- `@Version` protege las transiciones de estado y el borrado, pero no hay
  rollback automático si una operación parcial falla después de persistir.

### Historial de transiciones

- Se conserva el estado actual, no una historia de transiciones con fecha,
  responsable y motivo.

## Mejoras incrementales implementadas

### Selector de código de país en teléfono

- Se agregó un selector de país (`CountryCode`) con dos opciones: Uruguay
  (+598, 8 dígitos nacionales, primer dígito 9) y Brasil (+55, DDD de 2
  dígitos + 9 dígitos nacionales, primer dígito 9).
- `PhoneNumber.of(CountryCode, national, ddd)` normaliza el número al
  formato E.164, quitando el 0 inicial doméstico uruguayo y anteponiendo
  el DDD para Brasil.
- Los formularios de solicitud de invitado, perfil de usuario y perfil de
  organización usan el selector. La validación HTML5 (pattern) es solo
  visual; la validación real es del servidor.
- `PhoneNumber.of(String raw)` se mantiene para compatibilidad con datos
  existentes y tests previos.

### Tablero Kanban de solicitudes (CU-06)

- El tablero Kanban se integró al dashboard de organización (`/acopio/inicio`),
  no es una página aparte. La organización ve sus solicitudes agrupadas en 4
  columnas: Pendientes, En curso, Completadas, Rechazadas, directamente en
  el panel principal.
- Cada card muestra ciudad, materiales, solicitante y fecha. Los botones
  de acción llaman a los mismos endpoints de `OrgRequestController`
  (`/acopio/requests/{id}/transition`) — no duplica lógica de negocio.
- No implementa drag-and-drop; usa botones como acción intencional.
- La ruta `/acopio/kanban` fue eliminada; el template `requests-kanban.html`
  fue removido.

### 11. Política de contraseñas — mínimo 8 caracteres

- **Decisión:** la validación mínima de contraseña exige 8 caracteres.
- **Justificación:** 8 caracteres es defendible para un MVP. El demo
  password `12345678` cumple el mínimo.
- **Riesgo:** 8 caracteres sin exigir complejidad (mayúsculas, números,
  símbolos) sigue siendo vulnerable a ataques dirigidos. Esta política
  **no es aceptable para producción sin reforzarse**.
- **Para producción:** aplicar políticas OWASP: mínimo 12, complejidad,
  breach-list check. El cambio es una sola línea en
  `AccountInput.password()`.
- **Corrección:** el mínimo original era 3 caracteres. Se subió a 8
  en la fase de corrección (ver `docs/MEJORAS.md` §1.4).
- **Documentado como decisión consciente de fase**, no como omisión.

### 12. Blog estático — contenido editorial

- Se agregó un blog estático (`/blog`, `/blog/{slug}`) con 3 artículos:
  recolectores informales, galpones de acopio, Frontera de la Paz.
- El contenido vive en templates Thymeleaf y en el controlador
  `BlogController`, no en base de datos.
- **No es un CMS:** no hay panel de administración de artículos, ni
  editor, ni base de datos de posts.
- **Para producción:** migrar a contenido dinámico (colección `posts` en
  MongoDB, editor en panel de organización o admin, slug único, fecha
  de publicación, borrador/publicado). La estructura de rutas `/blog`
  y `/blog/{slug}` ya es compatible.
- **Fuentes citadas** en cada artículo (WIEGO, MNCR, Intendencia de
  Rivera, Eixo Atlântico). Las afirmaciones locales se enmarcan con
  cuidado; la investigación general brasileña no se presenta como
  evidencia directa sobre una organización específica.

### 13. Catadores — CRUD sin exposición en sidebar

- El CRUD de `InformalCollector` (`/acopio/catadores/**`) sigue
  funcionando a nivel de controlador y servicio, pero el link se sacó
  del sidebar de organización.
- **Razón:** exponer una tabla de recolectores informales en el panel
  mezcla responsabilidades (gestión interna vs. comunicación pública) y
  no aporta al flujo principal del MVP.
- El blog estático (sección 12) reemplaza esa exposición con contenido
  editorial sobre recolectores, más apropiado para visitantes.
- **Para producción:** decidir si el CRUD vuelve como herramienta
  interna (asignación de recolector a solicitud, RF-8 completo) o si
  se elimina. Hoy queda como funcionalidad latente, documentada.

## Fuera de alcance — GPS y geolocalización interactiva

La sección 1.4 de la tesis establece explícitamente que "No hay mapas ni
geolocalización interactiva", fundamentado en el Oficio 044/2023. Esta
decisión se mantiene para el MVP.

Si después de la defensa se decide ampliar el alcance, la implementación
sería:

1. **Modelo:** agregar campo opcional `location: {lat: Double, lng: Double}`
   a `Request`. No obligatorio, no rompe RF-3.
2. **Frontend:** botón "Usar mi ubicación GPS" (Geolocation API del
   navegador) que muestra un mapa Leaflet con marcador arrastrable dentro
   del área de cobertura Rivera–Sant'Ana do Livramento.
3. **Validación server-side:** el punto lat/lng, si viene, debe caer dentro
   de un radio o polígono que cubra la zona fronteriza. Rechazar o ignorar
   si cae fuera.
4. **Documentación:** actualizar la sección 1.4 de la tesis y este archivo
   explicando el cambio de alcance y su justificación técnica.

Esta mejora queda registrada como posible evolución, no como deuda técnica.

---

# Correcciones y limpieza (anexo)


Fecha: 2026-09-11
Versión: post-cleanup (sin tag aún)
Tests: 218, 0 failures, 0 errors, 0 skipped
Build: SUCCESS

---

## 1. Defectos corregidos

### 1.1 Seed destructivo (CRÍTICO)

**Defecto:** `SeedDataFactory.seedAll()` ejecutaba `deleteAll()` sobre usuarios, solicitudes y catadores antes de cargar datos de prueba. Si `app.seed=true` estaba activo, borraba datos reales sin confirmación.

**Corrección:**
- Se eliminaron los tres `deleteAll()`.
- Se agregó un guard: si cualquier colección tiene datos, el seed se omite.
- `application-dev.properties` cambió a `app.seed=false`.
- `DataLoader` se restringió a `@Profile("dev & !prod & !test")`.
- Se agregó `SeedSafetyTest` con 3 tests que verifican que el guard funciona.

**Verificación:** `SeedSafetyTest` pasa. El seed no se ejecuta si hay datos existentes.

### 1.2 FOUC (flash de texto sin traducir)

**Defecto:** `app.js` hacía `fetch('/i18n/{page}/{lang}.json')` asíncrono y reemplazaba los `[data-i18n]` después de que la página se renderizaba. El usuario veía el texto fallback primero y luego el traducido.

**Corrección:**
- Se creó `UiCopyCatalog.java` (`@ControllerAdvice`) que carga los JSON i18n server-side y los inyecta en `window.uiCopies` en el `base.html`.
- `app.js` ahora usa `window.uiCopies` directamente, sin fetch asíncrono.
- Se eliminó el `visibility:hidden` del `<html>` que era un parche anterior.

**Verificación:** El HTML servido incluye `window.uiCopies` con todas las traducciones. El JS las aplica sincrónicamente.

### 1.3 Copies stale en JSON i18n

**Defecto:** Los templates tenían copies actualizados, pero los JSON i18n todavía contenían texto viejo ("Juntos por un futuro más limpio", "Rastrear solicitud", "Cuidemos nuestra ciudad"). El JS los cargaba y sobrescribía el texto correcto.

**Corrección:**
- Se actualizaron todos los JSON i18n: home, auth, track, common, requests (es y pt).
- Se unificó el verbo "Rastrear" → "Buscar" en toda la interfaz de usuario.
- Se unificó "Enviar solicitud" → "Pedir recolección" en el botón del formulario.
- Se actualizaron los fallbacks de los templates para que coincidan.
- Se corrigieron `messages.properties`, `messages_es.properties`, `messages_pt.properties`.

**Verificación:** `grep -rn "Rastrear\|Juntos\|Cuidemos" src/main/resources/` no encuentra copies activos (solo URLs `/rastrear` que son rutas del servidor).

### 1.4 Política de contraseña débil

**Defecto:** `AccountInput.password()` aceptaba contraseñas de 3 caracteres. Insuficiente para un servicio público.

**Corrección:**
- Se subió el mínimo a 8 caracteres.
- Se actualizaron los mensajes de error en ES y PT.
- Se actualizó el test `I18nMessageResolutionTest`.
- Se actualizó el hint del template de registro.
- El demo password `12345678` (8 caracteres) sigue funcionando.

**Verificación:** Los tests existentes usan "12" (2 caracteres) y null, que siguen fallando la validación. Los tests pasan.

### 1.5 Recursos PWA no autorizados

**Defecto:** `SecurityConfig` no permitía acceso público a `/manifest.json`, `/sw.js`, `/icon-*.png`, `/icon-*.svg`. Un usuario no autenticado no podía instalar la PWA.

**Corrección:**
- Se agregaron `/manifest.json`, `/sw.js`, `/icon-*.png`, `/icon-*.svg` a los `permitAll()`.

**Verificación:** Los recursos se sirven sin redirección a login.

### 1.6 Service worker pre-cacheaba HTML stale

**Defecto:** El SW pre-cacheaba `'/'` en `STATIC_ASSETS`. Cuando se actualizaba el SW, cacheaba la página vieja. El usuario veía contenido stale por horas.

**Corrección:**
- `'/'` removido de `STATIC_ASSETS`.
- HTML/API: network-first puro, sin guardar en cache.
- Registro con `updateViaCache: 'none'`.
- `reg.update()` en cada carga.
- Cache version bumped a `v19`.

**Verificación:** El SW no pre-cachea HTML. Las páginas siempre van al server.

### 1.7 Navbar móvil sin botones de auth

**Defecto:** En móvil, `.navbar__actions` se ocultaba y los botones Entrar/Registrarse estaban dentro. Desaparecían.

**Corrección:**
- Se movieron los botones a `.dropdown__auth` separado.
- Registrarse usa `btn--primary`, Entrar usa `btn--outline`.
- Se agregó CSS para `.dropdown__auth`.

**Verificación:** Los botones son visibles en el dropdown móvil.

### 1.8 Manifest con nombre incorrecto

**Defecto:** `manifest.json` no coincidía con el branding vigente del sistema.

**Corrección:** Se actualizó el nombre y short_name.

---

## 2. Limitaciones pendientes (no corregidas)

### 2.1 CSP con `unsafe-inline`

El CSP usa `script-src 'unsafe-inline'` y `style-src 'unsafe-inline'`. Es necesario para:
- `window.uiCopies = [[${uiCopies}]]` (script inline en `base.html`).
- Estilos inline residuales.

**Para corregir:** Usar nonces o hashes CSP. Requiere cambio arquitectónico.

### 2.2 PWA sin offline completo

La PWA se puede instalar, pero HTML no está cacheado intencionalmente. Navegación offline no funciona. Solo CSS/JS/imágenes se cachean.

**No se corrige:** Es un tradeoff intencional para evitar contenido stale.

### 2.3 Tests E2E son MockMvc

`EndToEndFlowsTest` usa MockMvc con servicios simulados. No es un test de navegador real.

**Para corregir:** Agregar tests con Selenium o Playwright.

### 2.4 Sin validación de usabilidad

No hay evaluación con usuarios reales. Los tests verifican correctitud funcional, no usabilidad.

**Para corregir:** Realizar una evaluación formativa con ciudadanos y organizaciones.

### 2.5 Sin medición de impacto

No se puede afirmar que el sistema aumenta el reciclaje o los ingresos. Eso requiere un piloto de campo.

---

## 3. Tradeoffs intencionales

| Decisión | Beneficio | Costo |
|---|---|---|
| HTML no cacheado en SW | Sin contenido stale | Sin offline |
| `window.uiCopies` inline | Sin FOUC, sin fetch asíncrono | CSP requiere `unsafe-inline` |
| Password mínimo 8 | Defendible para MVP | No incluye complejidad (mayús/números) |
| Render.com (PaaS) | Depliegue simple | Menos control que IaaS |
| MongoDB | Persistencia documental | PostgreSQL también válido |
| Sin GPS/mapas | MVP enfocado | Sin geolocalización |

---

## 4. Política de fuente de verdad de copies

El sistema tiene tres fuentes de copies:

1. **`static/i18n/{page}/{lang}.json`** — fuente primaria. `UiCopyCatalog` los carga server-side y los inyecta en `window.uiCopies`.
2. **`messages_{es,pt}.properties`** — mensajes de validación del servidor (errores de formularios, flash messages).
3. **Fallback en templates** — texto visible si el JS falla. Debe coincidir con el JSON.

**Regla:** Cuando se cambia un copy, se actualizan las tres fuentes. `docs/METODOLOGIA.md` es el inventario, no la fuente de runtime.

---

## 5. Comando de tests

```bash
mvn test
```

Resultado actual:

```
Tests run: 218, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Requiere MongoDB en `localhost:27017`.

---

## 6. Comportamiento PWA/cache actual

- **Cache version:** `ecosolicitud-v19`
- **Pre-cacheados:** CSS, JS, manifest, icons, favicon
- **No pre-cacheados:** HTML, API
- **HTML/API:** network-first, sin guardar en cache
- **Actualización:** `updateViaCache: 'none'` + `reg.update()` en cada carga
- **Offline:** CSS/JS/imágenes disponibles; HTML no

---

## 7. Datos de demo (solo desarrollo)

El seed crea 10 usuarios y 12 solicitudes. Password: `12345678`.

- `juan` / `12345678` — usuario
- `coopverde` / `12345678` — organización

**Importante:** El seed solo se ejecuta si `app.seed=true` Y la base está vacía. No borra datos existentes.

---

## 8. Archivos nuevos

- `src/main/java/com/residuosolido/app/config/UiCopyCatalog.java` — carga JSON i18n server-side.
- `src/main/resources/static/js/theme.js` — aplica tema antes de pintar (sin inline).
- `src/test/java/com/residuosolido/app/config/SeedSafetyTest.java` — 3 tests de seguridad del seed.

## 9. Archivos eliminados

- `src/main/java/com/residuosolido/app/config/UiCopyDialect.java` — no necesario, eliminado.

---

# Superficies del sistema — auditoría (anexo)


> **Propósito:** centralizar todas las superficies del sistema (copies,
> estilos, esquemas, endpoints) en un único documento para detectar
> crecimientos innecesarios y mantener consistencia.
>
> **Fecha de auditoría:** commit `1e4d575`
> **Tests:** 218, 0 failures

---

## 1. Copies (textos de interfaz)

### 1.1 Resumen de fuentes de copies

| Fuente | Rol | Claves | ¿Usada? |
|---|---|---|---|
| `static/i18n/common/es.json` | i18n cliente (JS) | 39 | Sí |
| `static/i18n/common/pt.json` | i18n cliente (JS) | 39 | Sí |
| `messages_es.properties` | i18n servidor (Thymeleaf) | 136 | Parcial |
| `messages_pt.properties` | i18n servidor (Thymeleaf) | 109 | Parcial |
| `messages.properties` | Fallback (sin locale) | 108 | No visible |
| Texto fallback en templates | HTML hardcodeado en `data-i18n` | 227 claves | Sí |

**Problema:** hay **3 fuentes de copies** que se superponen:
1. `data-i18n` en templates (texto fallback en español)
2. `messages_*.properties` (servidor)
3. `i18n/common/*.json` (cliente, JS)

Muchas claves de `messages_es.properties` no se usan (son de fases
anteriores del proyecto). Los textos reales que ve el usuario vienen
de `data-i18n` + `i18n/common/*.json`.

### 1.2 Desincronización detectada

| Problema | Estado |
|---|---|
| `messages_es` tiene 27 claves más que `messages_pt` | Pendiente |
| `messages.properties` tiene 108 claves (¿se usa?) | No visible |
| `messages_es` tiene claves `index.features.*` sin uso | Muertas |
| `messages_es` tiene claves `index.how.*` que duplican `data-i18n` | Redundantes |
| Texto fallback de `data-i18n` y `messages_es` no siempre coinciden | Pendiente |

### 1.3 Claves i18n por categoría

| Categoría | Claves | Templates |
|---|---|---|
| Navbar/nav | 15 | navbar, base |
| Auth | 18 | login, register |
| Hero/index | 8 | index |
| How (pasos) | 8 | index |
| Blog | 0 (estático) | blog, index |
| Track/rastreo | 10 | track |
| Dashboard user | 10 | dashboard, requests |
| Dashboard org | 12 | org/dashboard |
| Request form | 30 | request-form |
| Request detail | 15 | request-detail |
| Request list | 8 | requests |
| Request success | 12 | request-success |
| Profile user | 12 | users/profile |
| Profile org | 15 | org/profile |
| Onboarding | 8 | complete-profile |
| Catadores | 8 | catadores (latente) |
| Métricas | 3 | metrics |
| Error 404 | 2 | error/404 |
| Footer | 6 | base |
| Modal track | 6 | navbar |
| Coop CTA | 3 | index |

**Total claves data-i18n únicas:** 227

### 1.4 Copies muertos o redundantes en `messages_es.properties`

| Clave | Motivo |
|---|---|
| `index.welcome` | No se usa en ningún template |
| `index.features.*` (6 claves) | Sección eliminada del index |
| `index.how.title` | El index ya no tiene título "¿Cómo funciona?" |
| `index.how.subtitle` | Idem |
| `index.how.step1` a `step4` | Duplican `data-i18n="how_step*"` |
| `index.login` | No se usa |
| `metrics.title` | Duplica `data-i18n` |
| `footer.description` | No se usa (el footer usa `data-i18n`) |
| `footer.nav.title` | No se usa |
| `footer.contact.title` | No se usa |

**Estimado:** ~20 claves muertas en `messages_es.properties`

---

## 2. Estilos (CSS)

### 2.1 Resumen

| Métrica | Valor |
|---|---|
| Líneas CSS | 582 |
| Clases únicas | 292 |
| Variables CSS definidas | 16 |
| Uso de `var()` | 262 |
| Media queries | 12 |
| Hex fuera de `:root` | 0 |
| Estilos inline | 0 |

### 2.2 Clases por categoría

| Categoría | Clases | ¿Justificadas? |
|---|---|---|
| Navbar | 18 | Sí |
| Dropdown | 7 | Sí |
| Hero | 11 | Sí |
| How (pasos) | 7 | Sí |
| Blog | 18 | Sí |
| Coop CTA | 3 | Sí |
| Btn | 8 | Sí |
| Form | 16 | Sí |
| Auth | 15 | Sí |
| Panel | 9 | Sí |
| Stat | 20 | **Dudoso** — 20 clases para cards de estadística |
| Kanban | 22 | **Alto** — 22 clases para el tablero |
| Track | 10 | Sí |
| Modal | 14 | Sí |
| Track-empty | 4 | Sí |
| Error-page | 3 | Sí |
| Breadcrumb | 2 | Sí |
| Info-card | 5 | Sí |
| Success-card | 1 | Sí |
| Page | 3 | Sí |
| Otras (utility, misc) | ~100 | Revisar |

**Categorías con posible crecimiento innecesario:**
- **Stat (20 clases):** `stat-card`, `stat-card--center`, `stat-card--xl`,
  `stat-card__icon`, `stat-card__icon--pending`, `--in_progress`,
  `--completed`, `--rejected`, `stat-card__value`, `stat-card__label`,
  etc. Se podrían consolidar con utility classes.
- **Kanban (22 clases):** `kanban-card`, `kanban-card__city`,
  `kanban-card__materials`, `kanban-card__detail`, `kanban-column`,
  `kanban-column__title`, `kanban-column__empty`, `kanban-column__count`,
  etc. Es el componente más complejo del sistema.

### 2.3 Variables CSS

| Variable | Valor | Uso |
|---|---|---|
| `--primary` | #2d6a4f | 262 usos |
| `--primary-hover` | #40916c | — |
| `--primary-light` | #d8f3dc | — |
| `--bg` | #f8fafc | — |
| `--surface` | #ffffff | — |
| `--border` | #e2e8f0 | — |
| `--dark` | #1e293b | — |
| `--gray-text` | #64748b | — |
| `--white` | #ffffff | — |
| `--danger` | #dc2626 | — |
| `--danger-bg` | #fee2e2 | — |
| `--danger-border` | #fecaca | — |
| `--warning` | #d97706 | — |
| `--warning-bg` | #fef3c7 | — |
| `--success-border` | #bbf7d0 | — |
| `--radius` | 0.5rem | — |
| `--shadow` | 0 1px 3px... | — |
| `--shadow-lg` | 0 4px 12px... | — |
| `--maxw` | 72rem | — |
| `--uruguay` | #1d4ed8 | Acento |
| `--uruguay-light` | #dbeafe | Acento |
| `--brasil` | #16a34a | Acento |

---

## 3. Esquemas (modelos de datos)

### 3.1 Modelos (6)

| Modelo | Campos | ¿Usado? |
|---|---|---|
| `User` | id, username, password, email, role, firstName, phone, city, acceptedMaterials, version | Sí |
| `Request` | id, city, address, materials, status, organization, citizen, guestName, guestPhone, trackingCode, imageId, timeSlot, weight, volume, version, createdAt, updatedAt | Sí |
| `InformalCollector` | id, name, active, notes, organizationId | **Latente** (sin UI) |
| `PhoneNumber` | countryCode, nationalNumber | Sí |
| `Name` | firstName, lastName | Sí |
| `CountryCode` | enum (UY, BR) | Sí |

### 3.2 Enums (5)

| Enum | Valores | ¿Usado? |
|---|---|---|
| `Role` | USER, ORGANIZATION | Sí |
| `City` | RIVERA, LIVRAMENTO | Sí |
| `RequestStatus` | PENDING, IN_PROGRESS, COMPLETED, REJECTED | Sí |
| `MaterialCategory` | PLASTICO, PAPEL, CARTON, VIDRIO, METAL, MADERA, ESCOMBROS | Sí |
| `TimeSlot` | MANANA, TARDE, NOCHE | Sí |

### 3.3 Repositorios (3)

| Repo | ¿Usado? |
|---|---|
| `UserRepository` | Sí |
| `RequestRepository` | Sí |
| `InformalCollectorRepository` | **Latente** |

---

## 4. Endpoints

### 4.1 Resumen

| Método | Cantidad |
|---|---|
| GET | 22 |
| POST | 10 |
| Total | 32 |

### 4.2 Por controller

| Controller | Endpoints | ¿Justificado? |
|---|---|---|
| `AuthController` | 4 (login, register GET/POST) | Sí |
| `RequestCreateController` | 2 (nueva, crear) | Sí |
| `RequestEditController` | 2 (editar, actualizar) | Sí |
| `RequestController` | 3 (lista, detalle, eliminar) | Sí |
| `GuestTrackingController` | 2 (rastrear GET/POST) | Sí |
| `UserProfileController` | 2 (perfil, actualizar) | Sí |
| `OrgDashboardController` | 1 (inicio) | Sí |
| `OrgProfileController` | 2 (perfil, actualizar) | Sí |
| `OrgOnboardingController` | 2 (completar, guardar) | Sí |
| `OrgRequestController` | 3 (aceptar, rechazar, completar) | Sí |
| `OrgRequestDetailController` | 1 (detalle) | Sí |
| `PublicMetricsController` | 1 (métricas) | Sí |
| `BlogController` | 1 (/blog) | Sí |
| `InformalCollectorController` | 4 (CRUD) | **Latente** — sin UI |
| `OrgApiController` | 1 (by-city JSON) | Sí |
| `BaseController` | 1 (change-language) | Sí |

**Endpoint latente:** `InformalCollectorController` tiene 4 endpoints
(CRUD de catadores) que no son accesibles desde la UI. Los tests
verifican que redirigen a login, pero no hay flujo visible.

---

## 5. Templates

### 5.1 Resumen

| Tipo | Cantidad |
|---|---|
| Templates totales | 28 |
| Fragments | 9 |
| Páginas (heredan base) | 19 |

### 5.2 Templates por categoría

| Categoría | Templates |
|---|---|
| Público | index, blog, metrics, error/404 |
| Auth | login, register |
| User | dashboard, requests, request-form, request-detail, request-success, track, profile |
| Org | dashboard, requests, request-detail, profile, complete-profile, catadores (latente) |
| Layout | base |
| Fragments | navbar, feedback, request-list, toggle-view-edit, password-field, org-sidebar, phone-country-selector, icons, request-form-js |

### 5.3 Templates con más copies (data-i18n)

| Template | Claves data-i18n |
|---|---|
| request-form | 40 |
| org/requests | 34 |
| org/profile | 27 |
| users/profile | 26 |
| org/dashboard | 24 |
| org/catadores | 22 (latente) |
| users/request-detail | 21 |
| public/index | 16 |
| users/track | 14 |
| users/request-success | 14 |
| auth/register | 14 |

---

## 6. Crecimientos innecesarios detectados

### 6.1 Copies

| Problema | Impacto | Solución |
|---|---|---|
| ~20 claves muertas en `messages_es.properties` | Bajo | Eliminar |
| `messages_pt` tiene 27 claves menos que `messages_es` | Medio | Sincronizar |
| `messages.properties` (108 claves) no se usa visiblemente | Bajo | Verificar o eliminar |
| 3 fuentes de copies superpuestas | Alto | Unificar en una |

### 6.2 Estilos

| Problema | Impacto | Solución |
|---|---|---|
| Stat tiene 20 clases (posible sobre-diseño) | Medio | Consolidar con utilities |
| Kanban tiene 22 clases (complejo) | Medio | Aceptable (es el componente más complejo) |
| ~100 clases "otras" sin categorizar | Bajo | Auditar |

### 6.3 Esquemas

| Problema | Impacto | Solución |
|---|---|---|
| `InformalCollector` + repo + controller + 4 endpoints + template latentes | Medio | Documentar o eliminar |

### 6.4 Endpoints

| Problema | Impacto | Solución |
|---|---|---|
| `InformalCollectorController` 4 endpoints sin UI | Medio | Documentar o eliminar |

---

## 7. Recomendaciones de limpieza

### Prioridad alta (antes de la defensa)

1. **Sincronizar `messages_pt` con `messages_es`** — faltan 27 claves
2. **Eliminar claves muertas de `messages_es`** — ~20 claves sin uso
3. **Verificar si `messages.properties` se usa** — si no, eliminar

### Prioridad media (después de la defensa)

4. **Consolidar stat-card** — 20 clases → ~8 con utilities
5. **Decidir qué hacer con `InformalCollector`** — eliminar o activar
6. **Unificar fuentes de copies** — una sola fuente de verdad

### Prioridad baja

7. **Auditar ~100 clases "otras"** — categorizar o eliminar
8. **Reducir kanban a ~15 clases** — si se simplifica el componente
