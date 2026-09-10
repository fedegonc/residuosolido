# Superficies de Mejora — Estado

> **Propósito:** tabla centralizada de todas las mejoras posibles del
> sistema, con su estado actual: implementado, descartado o diferido.
>
> **Fecha:** post-commit (`15b7db5`)
> **Tests:** 193, 0 failures

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
| 25 | Documentación centralizada (COPIES.md, SUPERFICIES.md) | Implementado | Single source of truth |
| 26 | Diagramas UML (casos de uso, ER, clases, estados) | Implementado | draw.io, 4 figuras |
| 27 | 193 tests (unit + integration + e2e) | Implementado | 0 failures |
| 28 | Metodología iterativo-incremental (4 fases) | Implementado | docs/METODOLOGIA.md |
| 29 | 21 tradeoffs documentados | Implementado | docs/TRADEOFFS.md |
| 30 | Deploy en Render.com (PaaS) | Implementado | GitHub→deploy automático |
| 31 | Páginas públicas /documentos y /diagramas | Implementado | DocsController, índice de docs y figuras |
| 32 | Diagrama de estados UML 2.5 (Figura 4) | Implementado | figura4-estados.drawio, guardas + acciones |
| 33 | CI/CD pipeline (GitHub Actions) | Implementado | Tests + PMD + JaCoCo + Docker smoke test |
| 34 | **AWS (IaaS)** | **Descartado** | Complejidad no aporta a la tesis. Ver TRADEOFFS §15 |
| 35 | **VPN para acceso** | **Descartado** | Render.com da dominio público + HTTPS |
| 36 | **MCP de AWS** | **Descartado** | Sin credenciales AWS, sin valor para MVP |
| 37 | **Panel Admin / moderación** | **Descartado** | Fuera de scope MVP. Ver TRADEOFFS §2 |
| 38 | **CMS para blog** | **Descartado** | Blog estático suficiente. Ver TRADEOFFS §5 |
| 39 | **Mapas / geolocalización** | **Descartado** | Fuera de scope. Ver TRADEOFFS §10 |
| 40 | **Mascota virtual** | **Descartado** | Microcopy + iconos en su lugar |
| 41 | **Emojis en UI** | **Descartado** | Inapropiado para tesis académica |
| 42 | **Separar Cuenta de Organización** | **Descartado** | Mono-modelo User. Ver TRADEOFFS §1 |
| 43 | **MongoDB réplica set / transacciones** | **Descartado** | Standalone suficiente para MVP. Ver TRADEOFFS §3 |
| 44 | **WhatsApp Business API real** | **Descartado** | Mock suficiente. Ver TRADEOFFS §8 |
| 45 | **Cloud storage (S3/Cloudinary)** | **Descartado** | Local en disco. Ver TRADEOFFS §9 |
| 46 | Fusionar request-form + request-edit | Diferido | 2 controllers, riesgo alto. Ver TRADEOFFS §14 |
| 47 | Fusionar perfiles (user + org + onboarding) | Diferido | 3 controllers, riesgo alto. Ver TRADEOFFS §14 |
| 48 | Fusionar dashboards (user + org) | Diferido | Riesgo medio. Ver TRADEOFFS §14 |
| 49 | Reducir clases CSS con utilities | Diferido | 292 clases, cambio masivo. Ver TRADEOFFS §14 |
| 50 | Consolidar stat-card (20→8 clases) | Diferido | Posible sobre-diseño |
| 51 | Simplificar kanban (22→15 clases) | Diferido | Componente más complejo |
| 52 | Unificar fuentes de copies (3→1) | Diferido | data-i18n + messages_* + JSON |
| 53 | Catadores CRUD (InformalCollector) | Latente | Modelo + repo + controller + 4 endpoints sin UI |
| 54 | Contraseña mínima 8+ (producción) | Diferido | 3 chars para MVP. Ver TRADEOFFS §4 |
| 55 | Asignación de recolector a solicitud (RF-8) | Diferido | Catadores latentes |
| 56 | Transiciones suaves entre temas | Diferido | Intencional: evitar parpadeo |
| 57 | SEO / meta tags dinámicos | Diferido | El sistema es una app, no un blog |
| 58 | Tests E2E con Selenium/Playwright | Diferido | E2E con MockMvc suficiente |
| 59 | Monitoreo (Prometheus/Grafana) | Diferido | Fuera de scope MVP |
| 60 | Logs estructurados (JSON) | Diferido | Logs Spring Boot suficientes |
| 61 | Rate limiting con Redis | Diferido | In-memory suficiente para MVP |
| 62 | Multi-tenancy (multi-frontera) | Diferido | Una sola frontera (Rivera-Livramento) |
| 63 | Upgrade Java 17→21 (pom + Dockerfile) | Diferido | Safe, bajo riesgo |
| 64 | Upgrade JaCoCo 0.8.11→0.8.15 | Diferido | Patch, sin breaking changes |
| 65 | Upgrade maven-pmd-plugin 3.21→3.28 | Diferido | PMD 6→7, migrar rulesets |
| 66 | Upgrade FontAwesome 6.5→7.2 | Diferido | Menor, verificar cambios de iconos |
| 67 | Migración Spring Boot 3.2→3.5 | Diferido | Paso intermedio seguro antes de 4.0 |
| 68 | Migración Spring Boot 3.5→4.0 | Diferido | 115 breaking changes, @MockBean eliminado |

---

## Resumen por estado

| Estado | Cantidad |
|---|---|
| Implementado | 33 |
| Descartado | 12 |
| Diferido | 22 |
| Latente | 1 |
| **Total** | **68** |

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
| Diagramas docs/diagrams/ | 3 figuras | 4 figuras | +figura4-estados.drawio |
| Controllers | 16 | 17 | +DocsController |
| Templates | 27 | 29 | +docs.html, +diagrams.html |
| Endpoints públicos | 8 | 10 | +/documentos, +/diagramas |
| CI/CD | — | 1 workflow | +.github/workflows/ci.yml |
