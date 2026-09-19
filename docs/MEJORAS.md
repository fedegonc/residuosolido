# Superficies de Mejora — Estado

> **Propósito:** tabla centralizada de todas las mejoras posibles del
> sistema, con su estado actual: implementado, descartado o diferido.
>
> **Fecha:** post-commit (`15b7db5`)
> **Tests:** 181, 0 failures

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
| 7 | Blog estático (3 artículos) | **Descartado** | Eliminado del MVP; sin BlogController ni templates |
| 8 | Métricas públicas por ciudad | Implementado | Sin auth requerido |
| 9 | Rate limiting de invitados | Implementado | Ventana deslizante por IP |
| 10 | Bloqueo por intentos de login | Implementado | 5 intentos, 15 min bloqueo |
| 11 | PWA instalable | **Descartado** (ver #100) | manifest.json, SW, install prompt — luego removidos; solo queda un Service Worker "kill-switch" para desinstalar el viejo en navegadores que lo tenían |
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
| 23 | Service Worker cache (CSS/JS only) | **Descartado** (ver #100) | HTML network-first, sin pre-cache — el SW completo se descartó después junto con el resto de la PWA (#11) |
| 24 | Limpieza de claves muertas (messages_*) | Implementado | 136→91 claves, ES/PT sincronizados |
| 25 | Documentación centralizada con índice docs/INDICE.md | Implementado | Single source of truth |
| 26 | Diagramas UML (casos de uso, ER, clases, estados) | Implementado | draw.io, 4 figuras |
| 27 | 181 tests (unit + integration + e2e) | Implementado | 0 failures |
| 28 | Metodología iterativo-incremental (4 fases) | Implementado | docs/METODOLOGIA.md |
| 29 | 21 tradeoffs documentados | Implementado | docs/DEFENSA.md |
| 30 | Deploy en Render.com (PaaS) | Implementado | GitHub→deploy automático |
| 31 | Páginas públicas /documentos y /diagramas | Implementado | DocsController, índice de docs y figuras |
| 32 | Diagrama de secuencia UML 2.5 (Figura 4) | Implementado | figura4-secuencia.drawio, flujo de creación de solicitud |
| 33 | CI/CD pipeline (GitHub Actions) | Descartado | Workflow eliminado; la verificación es manual y Render construye el artefacto sin ejecutar tests dependientes del entorno |
| 34 | **AWS (IaaS)** | **Descartado** | Complejidad no aporta a la tesis. Ver docs/TRADEOFFS.md §15 |
| 35 | **VPN para acceso** | **Descartado** | Render.com da dominio público + HTTPS |
| 36 | **MCP de AWS** | **Descartado** | Sin credenciales AWS, sin valor para MVP |
| 37 | **Panel Admin / moderación** | **Descartado** | Fuera de scope MVP. Ver docs/TRADEOFFS.md §2 |
| 38 | **CMS para blog** | **Descartado** | Blog estático suficiente. Ver docs/TRADEOFFS.md §5 |
| 39 | **Mapas / geolocalización** | **Descartado** | Fuera de scope. Ver docs/TRADEOFFS.md §10 |
| 40 | **Mascota virtual** | **Descartado** | Microcopy + iconos en su lugar |
| 41 | **Emojis en UI** | **Descartado** | Inapropiado para tesis académica |
| 42 | **Separar Cuenta de Organización** | **Descartado** | Mono-modelo User. Ver docs/TRADEOFFS.md §1 |
| 43 | **MongoDB réplica set / transacciones** | **Descartado** | Standalone suficiente para MVP. Ver docs/TRADEOFFS.md §3 |
| 44 | **WhatsApp Business API real** | **Descartado** | Eliminadas del MVP. Ver docs/TRADEOFFS.md §8 |
| 45 | **Cloud storage (S3/Cloudinary)** | **Descartado** | Local en disco. Ver docs/TRADEOFFS.md §9 |
| 46 | Fusionar request-form + request-edit | Implementado | `RequestEditController` fusionado en `RequestController` |
| 47 | Fusionar perfiles (user + org + onboarding) | Implementado | `OrgOnboardingController` fusionado en `OrgProfileController` |
| 48 | Fusionar dashboards (user + org) | Diferido | Riesgo medio. Ver docs/TRADEOFFS.md §14 |
| 49 | Reducir clases CSS con utilities | Diferido | 292 clases, cambio masivo. Ver docs/TRADEOFFS.md §14 |
| 50 | Consolidar stat-card (20→8 clases) | Diferido | Posible sobre-diseño |
| 51 | Simplificar kanban (22→15 clases) | Diferido | Componente más complejo |
| 52 | Unificar fuentes de copies (3→1) | Diferido | data-i18n + messages_* + JSON |
| 53 | Catadores CRUD (InformalCollector) | Latente | Modelo + repo + controller + 4 endpoints sin UI |
| 54 | Contraseña mínima 8+ (producción) | Diferido | 3 chars para MVP. Ver docs/TRADEOFFS.md §4 |
| 55 | Asignación de recolector a solicitud (RF-8) | Diferido | Catadores latentes |
| 56 | Transiciones suaves entre temas | Diferido | Intencional: evitar parpadeo |
| 57 | SEO / meta tags dinámicos | Diferido | El sistema es una app, no un blog |
| 58 | Tests E2E con Selenium/Playwright | Diferido | E2E con MockMvc suficiente |
| 59 | Monitoreo (Prometheus/Grafana) | Diferido | Fuera de scope MVP |
| 60 | Logs estructurados (JSON) | Diferido | Logs Spring Boot suficientes |
| 61 | Rate limiting con Redis | Diferido | In-memory suficiente para MVP. **Tripwire:** antes de escalar a más de 1 instancia del servidor — el estado in-memory no se comparte entre instancias, el rate limit dejaría de funcionar en silencio, no con un error visible |
| 62 | Multi-tenancy (multi-frontera) | Diferido | Una sola frontera (Rivera-Livramento) |
| 63 | Upgrade Java 17→21 (pom + Dockerfile) | Implementado | Java 21 LTS en pom, Dockerfile y CI |
| 64 | Upgrade JaCoCo 0.8.11→0.8.15 | Implementado | Patch, sin breaking changes |
| 65 | Upgrade maven-pmd-plugin 3.21→3.28 | Implementado | PMD 7, rulesets category-based |
| 66 | Upgrade FontAwesome 6.5→7.2 | Implementado | CDN y webjar actualizados |
| 67 | Migración Spring Boot 3.2→3.5 | Diferido | Paso intermedio seguro antes de 4.0 |
| 68 | Migración Spring Boot 3.5→4.0 | Diferido | 115 breaking changes, @MockBean eliminado |
|| 69 | Validación inline de `email` y `name` en `User` | Implementado | Validación server-side sin value objects separados |
|| 70 | Notificaciones WhatsApp / asincrónicas | Descartado | Eliminadas del MVP; no proveedor configurado |
|| 71 | OpenAPI / Swagger UI | Implementado | `/swagger-ui.html` generado desde controllers |
|| 72 | Centralización de rutas en `Routes.java` | Implementado | Única fuente de verdad para endpoints y seguridad |
|| 73 | Layouts anidados (`layout/base-sidebar`) | Implementado | Elimina duplicación de `.org-layout` en dashboard/requests/profile. Ver `docs/DIAGRAMAS.md` §Sistema de Layouts |
|| 74 | Componentes UI reutilizables (`fragments/ui/`) | Implementado | button, alert, card — DRY para markup repetido |
|| 75 | HTMX states CSS (`htmx-states.css`) | Implementado | Retroalimentación visual (spinner, opacity) sin JS extra |
|| 76 | Fix `.how__grid` 3→4 columnas en landing | Implementado | Bug: 4 pasos ("Solicitás/Asignamos/Retiran/Impactamos") forzados en grilla de 3, el 4to quedaba descolgado en desktop |
|| 77 | Spinner en botón "Mis solicitudes" (navbar, invitado) | Implementado | `.htmx-indicator` en vez de solo opacity al abrir el modal de rastreo |
|| 78 | Toast de error genérico en fallos HTMX | Implementado | `htmx:responseError`/`htmx:sendError` → alerta reutilizando `_server_error_generic` (i18n) |
|| 79 | Eliminar `fragments/ui/` (librería de componentes duplicada, sin uso) | Implementado | `button.html`/`card.html`/`alert.html` no eran referenciados por ningún template real |
|| 80 | Unificar naming BEM del footer (`.site-footer`+`.footer-inner`+`.footer-min__*` → `.footer`/`.footer__*`) | Implementado | 3 prefijos de bloque distintos para un solo componente |
|| 81 | Eliminar los 2 últimos estilos inline (`error/404.html`, `users/requests.html`) | Implementado | `MEJORAS.md` #15 afirmaba "0 estilos inline"; ahora es cierto |
|| 82 | Tokenizar escala tipográfica (`--text-2xs`…`--text-3xl`) | Implementado | 9 valores de `font-size` estaban hardcodeados y repetidos en todo `app.css`; ahora las 57 declaraciones referencian un solo token cada una |
|| 83 | Consolidar breakpoints a 3 valores fijos (640/768/1024) | Implementado | Había 4 valores ad-hoc (640/700/768/900) sin escala; `var()` no funciona dentro de `@media`, así que se documentó como comentario fijo en vez de token CSS |
|| 84 | Tokenizar colores de badges de estado (`--badge-*-bg/-text`) | Implementado | Antes hex crudo repetido; se mantuvieron tonos propios (más saturados que los `alert--*`) por decisión de diseño — no se fusionaron con los tokens de alert |
|| 85 | Tokenizar transiciones (`--transition-fast/-base`, `--ease-base/-in/-out`) | Implementado | 9 transiciones sin ningún token; 5 usaban `transition:all` (recalcula todas las props animables). Ahora cada regla anima solo la(s) propiedad(es) que realmente cambia(n) — `background-color`/`border-color`/`color`/`opacity` — con duración y easing tokenizados |
|| 86 | Instalar skill `emil-design-eng` (design-engineering, animación/craft) en `.agents/skills/` | Implementado | Skill externa (github.com/emilkowalski/skills) para auditar futuras decisiones de animación con el framework de Emil Kowalski (Sonner/Vaul) |
|| 87 | Fix `ease-in` → `ease-out` en `.htmx-settling` + eliminar token `--ease-in` | Implementado | La skill: "Never use ease-in for UI animations" — se detectó que el contenido entrante usaba la curva que la skill prohíbe |
|| 88 | Feedback de presión `:active{scale(.97)}` en `.btn`, `.check-card`, `.radio-card` | Implementado | Principio de la skill: "Buttons must feel responsive to press" — no existía ningún estado de presión en el sistema |
|| 89 | Brief de personalidad visual (anti-slop) | Implementado | `docs/TRADEOFFS.md` §22 — 3 adjetivos (territorial/institucional/directo al grano) para evaluar decisiones visuales futuras |
|| 90 | Eliminar gradientes diagonales (`.btn--primary`, `.navbar__logo`, `.navbar .btn--primary`, `.hero`) | Implementado | Tell visual más repetido de templates genéricos de IA; color plano + hover real en su lugar (antes `.btn--primary` no tenía ningún estado hover) |
|| 91 | Formalizar `--accent`/`--accent-hover` como tokens reales en `:root` | Implementado | Se usaba en `modal__option-icon--accent` pero nunca estaba definido (caía a un hex huérfano `#e76f51`); ahora es un terracota sobrio (`#c1552c`) coherente con el brief |
|| 92 | Rediseñar "Cómo funciona" (landing): círculos numerados → numeración tipográfica editorial | Implementado | Los círculos con número + ícono son el segundo cliché más repetido de landings genéricas; ahora es número grande + borde superior, alineado a la izquierda |
|| 93 | Documentar y ajustar la escala tipográfica a un ratio real (1.125, Segunda Mayor) | Implementado | 6 de 9 pasos ya coincidían con el ratio dentro de 1px; se ajustó `--text-2xl` (24→23px) y se documentó `--text-3xl` como excepción deliberada ("display size") en vez de dejarlo como número suelto |
|| 94 | `addResources=true` en `spring-boot-maven-plugin` | Implementado | Los cambios en `static/`/`templates/` no se reflejaban en `mvn spring-boot:run` sin recompilar manualmente (`target/classes` quedaba desactualizado); ahora el classpath usa `src/main/resources` directo en modo dev |
|| 95 | Fusionar 5 cards en 1 con `.divider` — detalle de solicitud (`org/requests.html`) | Implementado | Contacto/Dirección/Materiales/Foto/Franja eran 5-6 boxes con borde+sombra propios para una sola unidad de información; ahora es 1 card con secciones internas separadas por `.divider`. La sección de acción (Aceptar/Finalizar) queda como card separada a propósito — es funcionalmente distinta |
|| 96 | Quitar cards anidadas en `request-form.html` (`.card`→`.form-section`) | Implementado | Las secciones "contacto"/"detalles" eran cards blancas con borde+sombra propios *dentro* de otra card blanca — blanco sobre blanco sin aportar jerarquía. Ahora son secciones planas; la única superficie elevada es la card exterior del formulario |
|| 97 | `--ease-out` custom (`cubic-bezier(.23,1,.32,1)`) en vez de la curva nativa | Implementado | La skill `emil-design-eng`: "built-in CSS easings... lack the punch". Afecta `:active` de botones/cards y el fade de HTMX — ahora se siente crisp, coherente con el brief institucional en vez de la curva nativa "floja" |
|| 98 | Entrada animada del modal de rastreo (`@starting-style`, fade + `scale(.96)`) | Implementado | El modal aparecía sin ninguna transición (pop instantáneo); ahora fade-in + scale sutil con `--transition-base`/`--ease-out`. Sin JS, CSS nativo moderno; degrada a aparición instantánea en navegadores sin soporte |
|| 99 | Fix asset corrupto: `plus-jakarta-sans-400.woff2` era HTML, no la fuente | Implementado | El archivo en disco (269KB) era una página de GitHub guardada por error en vez del binario. Reemplazado por el woff2 real (27KB, subset latin, verificado con magic bytes `wOF2`) — rompía la carga de la tipografía en todo el sitio |
|| 100 | Kill-switch para Service Worker huérfano (`/sw.js` + `SecurityConfig` permitAll) | Implementado | El PWA se descartó (#11) pero navegadores que lo visitaron antes del 15/9 seguían con el SW viejo activo, interceptando *todos* los fetches (rompía fuentes, HTML, todo) porque `/sw.js` devolvía 302→login. Nuevo `/sw.js` se auto-desregistra y borra caché en la próxima visita de cualquier navegador afectado |
|| 101 | Fondo de página blanco en vez de gris (`body{background:var(--surface)}`) | Implementado | `--bg` (gris) hacía doble función: fondo de página y relleno de hover/disabled/kanban. Se dejó `--bg` intacto para esos 6 usos funcionales y solo se cambió el body a `--surface` (blanco) — las cards siguen distinguiéndose por borde+sombra |
|| 102 | Métricas de LOC + diagnóstico de escala (¿estudiante o equipo?) | Implementado | `docs/TRADEOFFS.md` §23 — LOC por capa, estructura de las 48 clases backend, 428 commits/1 autor/14 meses, y argumento de por qué la escala es consistente con trabajo individual sostenido |
|| 103 | Permitir foto/peso/volumen/franja horaria en la **creación** de la solicitud (no solo al editar) | Diferido | Hoy esos 4 campos son `th:if="${isEdit}"` — el ciudadano debe crear y después editar para adjuntar una foto. No son campos sensibles, es fricción evitable |
|| 104 | Paginación en `org/requests.html` (lista) | Diferido | Sin `Pageable`/límite — renderiza todas las solicitudes que matchean el filtro de estado en una sola página. No escala con volumen alto |
|| 105 | Contrato explícito `User.getAcceptedMaterialsCsv()` en vez de `List.toString()` | Implementado | El filtro de materiales dependía del formato default de `toString()` de `List` (`"[A, B]"`) parseado con un regex que pelaba corchetes. Ahora hay un método propio, documentado, que es la única fuente de verdad del formato — el JS quedó más simple (ya no necesita el regex) |
|| 106 | Separar `app.js` — lógica exclusiva de página fuera del archivo global | Implementado | `filterMaterialsByOrg` (solo `request-form.html`) y el toggle view/edit (solo `org/profile.html`) pasaron a `request-form.js`/`org-profile.js`, cargados vía `layout:fragment="pageScripts"` (existía sin uso). `app.js` quedó con un comentario-manifiesto de qué componente usa qué página, para lo que sigue siendo compartido |
|| 107 | Fix: error de validación (`IllegalArgumentException`) redirigía a `/auth/login` en vez de al formulario de origen | Implementado | `GlobalExceptionHandler.handleIllegalArgument` usaba un target "genérico" por rol que, para un usuario no autenticado, siempre caía en `/auth/login` — un error de teléfono mal formado en `request-form.html` terminaba mostrándose en la pantalla de login (sin campo de teléfono). Ahora usa el header `Referer` para volver a la página de origen; verificado con `curl` simulando el POST |
|| 108 | Eliminar `RoleBasedLoginTargetUrlResolver` (`@Component` + DI) → `Routes.resolveHomeForRole()` (static) | Implementado | Era una función pura (`Authentication → String`) envuelta en un bean inyectado en 4 archivos de 2 paquetes distintos, duplicando además los strings `"/acopio/inicio"`/`"/usuarios/inicio"` que ya existían como `Routes.ORG_HOME`/`Routes.USER_HOME`. Ahora es un método estático en `Routes.java` (la fuente única de verdad para rutas) — 1 archivo menos, sin duplicar constantes. Tests migrados a `RoutesTest.java` (6/6 verde) |
|| 109 | Documentar la truth table de `request-form.html` | Implementado | Comentario HTML al inicio del archivo con las 3 flags (`isEdit`×`isGuest`×`needsPhone`) y sus combinaciones reales — antes había que simular 8 casos mentalmente leyendo los `th:if` sueltos |
|| 110 | `viewType` de `String` crudo a enum `RequestViewType` | Implementado | `"list"`/`"detail"` eran strings sin ninguna garantía de compilación — un typo no se detecta hasta runtime. Ahora `RequestViewType.LIST/DETAIL`, comparado en el template con `T(...)` (mismo patrón que `RequestStatus` en otros templates). Tests de `EndToEndFlowsTest` actualizados |
|| 111 | Extraer botón "Mis solicitudes" duplicado del navbar a fragments | Implementado | Estaba escrito 2 veces, idéntico, para desktop/mobile. Se probó parametrizarlo por clase vía `th:replace` pero el `--` de BEM dentro de un string literal rompe el parser de expresiones de Thymeleaf/attoparser (`TemplateProcessingException`) — se resolvió con 2 fragments sin parámetro (`trackButtonDesktop`/`trackButtonMobile`), adyacentes en el archivo en vez de duplicados a 40 líneas de distancia |
|| 112 | Corregir `docs/DIAGRAMAS.md` — citaba archivos JS ya eliminados | Implementado | "Decisiones de Arquitectura" mencionaba `fragments/toggle-view-edit.html`/`request-form-js.html`, borrados al principio de esta sesión (ver #106) |
|| 113 | **Fix crítico**: `org/profile.html` tiraba 500 siempre — `organization.hasCity()` no existía | Implementado | Bug preexistente desde el commit `6aba626` ("podado de backend"), descubierto al correr `EndToEndFlowsTest` completo. Agregado `User.hasCity()` (mismo patrón que `hasPhone()`) |
|| 114 | **Fix crítico**: `org/dashboard.html` (Kanban) tiraba 500 siempre — `T(...)` no se puede usar dentro de argumentos de un fragment call | Implementado | Thymeleaf lo prohíbe por seguridad ("Instantiation of new objects... forbidden"). Se resolvió con `th:with` para resolver los 4 `RequestStatus` antes de pasarlos como variables simples a `kanban-column :: column(...)` |
|| 115 | **Fix crítico**: `fragments/kanban-column.html` tenía una llave `}` sobrante en un ternario anidado + NPE si `requests` llegaba null | Implementado | Bug de sintaxis preexistente (3 aperturas `\${` vs 4 cierres `}`, verificado con conteo exacto) que quedaba enmascarado por el bug #114 — al arreglar uno, apareció el otro. `th:class` del ícono de estado corregido; `requests`/`isEmpty()` protegidos con `safeRequests=\${requests != null ? requests : {}}` (nota: `T(...).metodo()` tampoco se permite en este contexto, solo acceso a campos estáticos — se usó el literal SpEL `{}` en vez de `Collections.emptyList()`) |
|| 116 | Fix título duplicado en la pestaña de la home ("Eco Solicitud — Eco Solicitud") | Implementado | `index.html` tenía `<title>Eco Solicitud</title>` — igual al `$LAYOUT_TITLE`, duplicando el patrón `$LAYOUT_TITLE — $CONTENT_TITLE`. Cambiado a `<title>Inicio</title>` |
|| 117 | Fix `favicon.svg` — todavía tenía el gradiente de la Fase 1 | Implementado | El resto del sitio ya es plano (sin gradientes) desde la Fase 1, pero el ícono de la pestaña se escapó del barrido — tenía el mismo verde degradado `#52b788→#1b4332`. Ahora plano (`--primary` #2d6a4f) |
|| 118 | Extraer pieza `StatTile` (`fragments/stat-tile.html`) | Implementado | El bloque ícono+valor+label estaba escrito 7 veces idéntico (3 en `org/dashboard.html`, 4 en `users/requests.html`). Ahora es 1 fragment parametrizado (`icon,value,labelKey,labelText`), verificado en vivo con login real en ambos roles |
|| 119 | Extraer pieza `PageHeader.greeting` (`fragments/page-header.html`) | Implementado | El bloque "Hola, {nombre}" + subtítulo se repetía en `org/dashboard.html` y `users/requests.html` (con una diferencia real: uno lleva "¡...!" y el otro no — parametrizado con `exclamation:boolean`). Las acciones de cada header quedaron fuera del fragment porque sí difieren genuinamente por página |
|| 120 | Fusionar 8 fragments chicos en `fragments/ui.html` | Implementado | `empty-state`, `badge`, `info-row`, `stat-tile`, `org-options`, `alert`, `breadcrumb`, `page-header` eran 8 archivos de 8-11 líneas cada uno, cada uno repitiendo el mismo boilerplate `<html>`/`<body>`. Fusionados en 1 archivo con 8 `th:fragment` — bajó de 14 a 7 archivos en `fragments/` y de 330 a 309 líneas totales. Actualizadas las 7 plantillas + 1 controller (`RequestCreateController`) que los referenciaban; verificado en vivo (home, dashboard, perfil, lista/detalle, track, org-options) |
|| 121 | Ritmo de espaciado interno/externo (`--space-lg`, `.mb-lg`/`.mt-lg`) | Implementado | Todo el espaciado entre bloques usaba `--gap` (1rem) parejo, sin distinguir "dentro de una sección" de "entre secciones". Nuevo token `--space-lg` (2.5rem) para separación entre secciones distintas — aplicado en `index.html` (hero→pasos→CTA), `org/dashboard.html` y `users/requests.html` (header→stats→contenido). El espaciado interno (`--gap`/`.mb`) se mantiene igual dentro de cada sección |
|| 122 | Agrupar CSS compartido `.navbar`/`.footer` | Implementado | `.navbar__inner`/`.footer__inner` eran casi idénticos (mismas 6 propiedades, navbar solo agrega 2), y `.navbar`/`.footer`, `.navbar__brand`/`.footer__brand`, `.navbar__links`/`.footer__links` repetían el mismo patrón `display:flex;align-items:center...`. Agrupados en selectores compartidos (`.navbar,.footer{...}`) con las diferencias reales (tamaño, gap, bordes) en reglas separadas — mismo resultado visual, sin propiedades duplicadas. Cero cambio de HTML, solo CSS |
|| 123 | Registro simplificado: nombre + teléfono + PIN de 4 dígitos | Implementado | Fricción mínima para pruebas (ver `docs/TRADEOFFS.md` §24 para el tradeoff completo). `RegistrationForm`/`UserRegistrationService` cambiaron email→teléfono y contraseña 8+→PIN 4 dígitos; `username` ya acepta espacios (es "nombre" ahora). Tests migrados (`UserServiceTest` 17/17, `MvpRegressionTest` 9/9, `TransversalBrowserTest`). Verificado en vivo: 2 registros + login reales de punta a punta |
|| 124 | `MongoIndexMigration` — reemplaza `auto-index-creation` por migración explícita | Implementado | El índice único de `email` (sin `sparse`) chocó al dejar de pedir email en el registro (2+ usuarios con `email=null`) y tiró abajo el arranque completo (`IndexKeySpecsConflict`). Se desactivó `spring.data.mongodb.auto-index-creation` y se agregó un `CommandLineRunner` idempotente que arregla el índice viejo y asegura los otros 3 `@Indexed` del proyecto explícitamente — más robusto que la creación automática, y portable a una base nueva |
|| 125 | Fix inconsistencia: teléfono de registro usaba un input suelto en vez del selector binacional | Implementado | El campo de teléfono del registro (#123) se implementó con un `<input>` simple, distinto del selector país+nacional+DDD ya usado en `request-form.html`/`org/profile.html`. Ahora `register.html` usa exactamente el mismo componente (prefijo `phone`, ya soportado por `app.js` sin cambios), y `RegistrationForm` construye el teléfono vía `PhoneNumber.normalize(countryCode, phoneNational, ddd)` en vez de un string crudo. Verificado en vivo con ambas ramas (UY y BR+DDD) |
|| 126 | Crear `docs/BOILERPLATE_VS_CORE.md` | Implementado | Documento vivo (v1) que clasifica cada clase backend/frontend/infra en Boilerplate, Core o Zona gris — separa qué es infraestructura genérica de Spring Boot de qué es el aporte real del dominio. Referenciado desde `docs/INDICE.md` |
|| 127 | **TEMPORAL** — links a `MEJORAS.md`/`DEFENSA.md`/`BOILERPLATE_VS_CORE.md` + visor de diagramas UML en el footer | Implementado (sacar antes de producción) | Agregados para revisar avance fácil esta semana de desarrollo, vía `DocsController` (`/docs/{file}.md`, ambos ya públicos). **Tripwire: sacar todo esto antes de que un usuario real (no de prueba) vea el sitio** — no tiene sentido exponer docs internos de tesis en el footer de producción |
|| 128 | Visor embebido de diagramas UML (`/docs/diagramas`) | Implementado | El link de diagramas del footer (#127) originalmente apuntaba a los 5 `.drawio` crudos (XML sin renderizar, requería copiar/pegar a app.diagrams.net a mano). Ahora `DocsController.viewDiagrams()` lee cada `.drawio` de `docs/diagrams/` y lo embebe inline en un `data-mxgraph` (JSON con la clave `xml`, escapado automáticamente por Thymeleaf al atributo HTML), renderizado client-side por el script oficial `viewer-static.min.js` de draw.io. Evita el problema de CORS de la alternativa (`?lightbox=1#U<url>`, que depende de que draw.io pueda hacer fetch cross-origin a `localhost`) porque el XML ya viaja embebido en el HTML, sin fetch externo. CSP `script-src` ampliado con `https://viewer.diagrams.net`. Mismo tripwire que #127 |
|| 129 | Centralizar secretos: `.env` + `.env.example`, sacar credencial hardcodeada de `application-dev.properties` | Implementado | `application-dev.properties` tenía la connection string de MongoDB Atlas (usuario+password reales) hardcodeada y committeada en git — credencial rotada en Atlas tras detectar el leak. Ahora `application-dev.properties` no tiene `spring.data.mongodb.uri` propio, hereda el de `application.properties` (`${SPRING_DATA_MONGODB_URI}`), que Spring Boot carga desde `.env` en la raíz (gitignored) vía `spring.config.import=optional:file:.env[.properties]`. `.env.example` (versionado, sin secretos reales) documenta las 3 claves (`SPRING_DATA_MONGODB_URI`, `MONGODB_DATABASE`, `UPLOAD_DIR`). En Render, las mismas claves van como variables de entorno del servicio. Se borró `.env.local` suelto (no lo leía nadie, tenía la credencial vieja ya revocada). Ver `docs/TRADEOFFS.md` §26 |
|| 130 | Consolidar pantallas de organización: 4 → 2 (dashboard Kanban + onboarding absorbidos) | Implementado | `org/dashboard.html` + `fragments/kanban-column.html` + `OrgDashboardController` eliminados — el Kanban duplicaba `org/requests.html` (misma data, mismas transiciones accept/reject/complete vía detalle; el filtro por estado hace el mismo trabajo) y cargaba `pendingRequestsList` que nunca se renderizaba. Las stat tiles pasaron al header de `org/requests.html`. `org/complete-profile.html` + sus endpoints eliminados — era un subconjunto de `org/profile.html` (solo phone+city): ahora el perfil abre en modo edición cuando `!isProfileComplete()` y `UserService.updateProfile` auto-completa el flag si la org tiene phone+city (método `completeOrgProfile` eliminado). El check `needsProfileCompletion()` se movió a `OrgRequestController`. `/acopio/inicio` y `/acopio/completar-perfil` dejaron de existir; `resolveHomeForRole` ahora devuelve `/acopio/requests` para orgs. Sidebar org quedó con 2 links. También eliminados de `RequestService`: `getRecentPendingRequestsByOrganization`, `getRequestsByOrganizationGroupedByStatus` (huérfanos). Tests migrados: `EndToEndFlowsTest`, `RoutesTest`, `CriticalSecurityTest`, `OrganizationControllerTest`, `UserServiceTest`, `OrganizationBrowserTest`, `TransversalBrowserTest` |
| 134 | Renombrar rutas al vocabulario de la defensa: `/solicitar`, `/mis-solicitudes`, `/rastrear`, `/acopio/solicitudes/{id}/{aceptar,rechazar,completar}`, `/mi-organizacion`, `/organizaciones`, `/salir`, `/registrarse` | Implementado | El modelo anterior mezclaba verbos y sustantivos sin criterio (`/solicitudes/nueva`, `/solicitud/{id}`, `/acopio/requests/{id}/transition?action=`, `/api/organizations/by-city`, `/logout`, `/registrar`). Ahora: las acciones principales son verbos de una palabra (`/solicitar`, `/rastrear` — las únicas URLs que un ciudadano tipearía a mano), CRUD puro queda sustantivo plural (`PUT`/`DELETE /solicitudes/{id}` vía `_method` + `HiddenHttpMethodFilter`), `/acopio/**` es el prefijo de zona org (matcher de seguridad de una línea) y las transiciones son 3 endpoints separados en vez de `?action=`. Params de query en español (`telefono`, `codigo`, `estado`, `ciudad`, `material`, `nombre`). Bonus: `/solicitud/{id}` nunca existió como GET — el redirect post-edición era un 404 latente, ahora redirige a `/mis-solicitudes`. ORG→rutas de usuario ahora responde 403 (negado por `AuthorizationFilter`, antes pasaba por `GlobalExceptionHandler`→redirect). `POST /rastrear` eliminado (el form es GET). 14 acciones / 10 recursos — ver `docs/ENDPOINTS.md` |
| 133 | Simplificar switch de idioma: links comunes en vez de máquina de estado JS | Implementado | El cambio de idioma hacía un viaje innecesario: click → JS → localStorage → `?lang=` → reload → `serverLang` → re-sync. Ahora los botones ES/PT son `<a href="?lang=es|pt">` comunes — el `LocaleChangeInterceptor` + sesión hacen todo, funciona **sin JavaScript**. `is-active` se resuelve server-side con `th:classappend`. Eliminados de `app.js`: `serverLang`, `localStorage` de idioma, click handler y `markActiveLang` (-25 líneas). El cliente ya no tiene estado de idioma: solo aplica `window.uiCopies` que el server ya sirvió en el idioma correcto |
| 132 | Fix cambio de idioma: `i18n.js` ignoraba la elección `?lang=` | Implementado | `I18nScriptController` usaba `request.getLocale()` — API servlet que lee `Accept-Language` y **ignora el LocaleResolver de Spring**: el catálogo siempre reflejaba el idioma del navegador, no el elegido. Ahora usa `LocaleContextHolder.getLocale()` (resultado de `CityAwareLocaleResolver` = sesión/ciudad). Segundo bug latente: `Cache-Control: max-age=1h` serviría el catálogo ES cacheado al cambiar a PT — resuelto haciendo la URL específica por idioma (`/js/i18n.js?l=es|pt` desde `base.html` vía `#locale.language`). Verificado en vivo: `?lang=pt` → catálogo pt, `?lang=es` → catálogo es |
| 131 | Reorganizar docs: un archivo por tema + gitflow documentado | Implementado | Los 4 docs grandes eran varios documentos pegados (patrón "doc + anexos"). Extraídos a archivos propios: `REQUISITOS.md` (catálogo RF/RN nuevo — cubría un hueco: el canonical doc `RF-RN.md` se referenciaba pero no existía), `ARQUITECTURA.md` (núcleo del sistema), `COPIES.md` (copys i18n), `TRADEOFFS.md` (26 decisiones), `CORRECCIONES.md` (hardening + limpieza), `LIMITACIONES.md`, `AUDITORIA.md` (inventario de superficies). MEJORAS 966→267, DEFENSA 922→271, METODOLOGIA 613→180, DIAGRAMAS 374→215 líneas. Nuevo `GITFLOW.md` + `docs/diagrams/figura5-gitflow.drawio` (registrada en `DocsController.DIAGRAMS`, visible en `/docs/diagramas` y linkeada en el footer). Cross-refs actualizados: `DEFENSA.md §N` de tradeoffs → `TRADEOFFS.md §N` (14 refs), `RF-RN.md` → `REQUISITOS.md`, convenciones en `CLAUDE.md`, 2 [FALTA] de `CONTEXTO_LLM.md` resueltos |

---

## Resumen por estado

| Estado | Cantidad |
|---|---|
| Implementado | 96 |
| Descartado | 14 |
| Diferido | 20 |
| Latente | 1 |
| **Total** | **131** |

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

---

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
  `UserService.validatePassword()`.
- **Corrección:** el mínimo original era 3 caracteres. Se subió a 8
  en la fase de corrección (ver `docs/MEJORAS.md` §1.4).
- **Documentado como decisión consciente de fase**, no como omisión.

### 12. Blog estático — descartado del MVP

- El blog estático (`/blog`, `/blog/{slug}`) fue planificado pero
  **no se implementó**: no existe `BlogController`, ni templates, ni
  rutas. Las clases CSS de blog se eliminaron en la consolidación.
- **Razón:** el blog no aporta al flujo core (solicitud → aceptación →
  completado) y suma mantenimiento sin valor para el MVP.
- **Para producción:** si se reactiva, migrar a contenido dinámico
  (colección `posts` en MongoDB, editor en panel, slug único, fecha
  de publicación, borrador/publicado).

### 13. Catadores — CRUD descartado

- El CRUD de `InformalCollector` (`/acopio/catadores/**`) fue
  planificado pero **no se implementó**: no existe `InformalCollectorController`
  ni servicio. Quedó documentado como decisión de diseño.
- **Razón:** exponer una tabla de recolectores informales en el panel
  mezcla responsabilidades (gestión interna vs. comunicación pública) y
  no aporta al flujo principal del MVP.
- **Para producción:** decidir si el CRUD se implementa como herramienta
  interna (asignación de recolector a solicitud, RF-8 completo) o si
  se elimina definitivamente.


---

## Documentos relacionados

- `docs/CORRECCIONES.md` — hardening aplicado y correcciones/limpieza.
- `docs/LIMITACIONES.md` — limitaciones reconocidas y fuera de alcance.
- `docs/AUDITORIA.md` — auditoría de superficies (copies, CSS, esquemas, endpoints, templates).
- `docs/COPIES.md` — single source of truth de textos de UI.
- `docs/SEGURIDAD.md` — checklist de secretos y reglas de `git config`.
