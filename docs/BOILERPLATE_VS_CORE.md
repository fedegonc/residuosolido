# Boilerplate vs Core — Eco Solicitud

**Estado:** v1, documento vivo — se actualiza cada vez que se agrega/mueve
una clase, no es una foto única.
**Propósito:** separar qué parte del sistema es infraestructura genérica
(cualquier web app Spring Boot la tiene) de qué parte es el aporte real
del dominio (lo que hay que defender/entender a fondo, lo que no se puede
copiar de otro proyecto). Es la fuente de verdad para esa distinción —
otros docs (`DEFENSA.md`, `DIAGRAMAS.md`) describen QUÉ hace el sistema,
este describe QUÉ PARTE de eso es realmente suya.

**Por qué importa la distinción:** si alguien pregunta "¿qué de esto
programaste vs qué te dio el framework", la respuesta no es "todo" ni
"nada" — es esta tabla. También sirve como mapa de qué se puede
refactorizar/reemplazar sin riesgo (boilerplate) vs qué requiere entender
las reglas de negocio antes de tocar (core).

---

## Backend — por clase

### Controllers

| Clase | Clasificación | Por qué |
|---|---|---|
| `AuthController` | Boilerplate | Login/registro — mecanismo genérico de cualquier app con auth |
| `BaseController` | Boilerplate | Helpers compartidos (flash messages), sin conocimiento de negocio |
| `DocsController` | Boilerplate | Sirve archivos estáticos de `docs/`, no toca el dominio |
| `I18nScriptController` | Boilerplate | Sirve el catálogo JSON de traducciones |
| `SeedController` | Boilerplate | Carga de datos demo, solo dev |
| `GuestTrackingController` | **Core** | Rastreo por teléfono+código privado — específico de "invitado sin cuenta" |
| `RequestCreateController` | **Core** | Creación de solicitud — el punto de entrada al dominio |
| `RequestController` | **Core** | Vista/edición de solicitud del ciudadano |
| `OrgRequestController` | **Core** | Panel de acopio + transiciones de estado (aceptar/rechazar/completar) — las reglas de negocio viven acá |
| `OrgApiController` | **Core** | Organizaciones por ciudad — parte de la lógica de matching |
| `OrgProfileController` | Zona gris | El CRUD de perfil es genérico, pero el **onboarding forzado** (no podés operar sin perfil completo) es una regla de negocio |

### Services

| Clase | Clasificación | Por qué |
|---|---|---|
| `MongoAggregationUtils` | Boilerplate | Utilidad técnica de queries, sin semántica de dominio |
| `LocalImageService` | Boilerplate | Subida de archivos genérica (el *qué* se sube es negocio, el *cómo* no) |
| `UserRegistrationService` | Boilerplate | Alta de cuenta — mecanismo genérico (el PIN de 4 dígitos es una decisión de fricción, no de dominio) |
| `RequestService` | **Core** | El servicio más grande del proyecto (329 líneas) — crear/validar/transicionar solicitudes, es el corazón |
| `CityOrgService` | **Core** | Selección de organización por ciudad + materiales aceptados — el matching es el aporte real |
| `RequestMetricsService` | **Core** | Stats del panel de solicitudes agrupadas por estado — conoce el ciclo de vida del dominio |
| `UserService` | Zona gris | Update de perfil genérico, pero `updateProfile` auto-completa el perfil de org validando reglas de negocio (teléfono + ciudad) |

### Config

| Clase | Clasificación | Por qué |
|---|---|---|
| `SecurityConfig`, `SecurityBeansConfig` | Boilerplate | Spring Security estándar |
| `Routes` | Boilerplate | Constantes de URL — aunque `resolveHomeForRole()` codifica "USER→/usuarios, ORGANIZATION→/acopio", que es negocio-lite |
| `WebConfig`, `AuthNavigationInterceptor` | Boilerplate | Plomería de Spring MVC |
| `RateLimiter` (consolida lo que antes era `GuestRateLimiter` + `LoginAttemptService`) | Boilerplate | Seguridad genérica, no específica del dominio de reciclaje |
| `AuthenticationEventHandler` (fusiona los viejos `LoginSuccessHandler`/`LoginFailureHandler`) | Boilerplate | Redirige por rol vía `Routes.resolveHomeForRole` — mecanismo genérico aunque el destino (`/acopio/solicitudes` vs `/mis-solicitudes`) es negocio |
| `JsonMessageSource`, `UiCopyCatalog` | Boilerplate | Mecanismo de i18n, agnóstico del contenido |
| `DataLoader` | Boilerplate | Seed de datos demo |
| `MongoIndexMigration` | Boilerplate | Infraestructura de datos, agnóstica del dominio |
| `CityAwareLocaleResolver` | Zona gris | El *mecanismo* (LocaleResolver de Spring) es genérico, pero la *regla* "RIVERA→es, LIVRAMENTO→pt" es una decisión territorial específica de este proyecto — es la frontera binacional codificada en una clase |

### Modelo y enums

| Clase | Clasificación | Por qué |
|---|---|---|
| `Request` | **Core** | La entidad central del dominio |
| `RequestStatus` | **Core** | El ciclo de vida (PENDING→IN_PROGRESS→COMPLETED/REJECTED) — es literalmente el proceso que se está modelando |
| `City` | **Core** | RIVERA/LIVRAMENTO — el contexto territorial no es incidental, es el problema |
| `MaterialCategory`, `TimeSlot` | **Core** | Vocabulario específico del dominio de reciclaje/logística |
| `RequestViewType` | Boilerplate | Solo controla qué vista de UI renderizar, sin semántica de negocio |
| `PhoneNumber` | Zona gris | La utilidad de normalización es técnica/genérica, pero las reglas por país (UY: 8 dígitos, BR: DDD+9) codifican el contexto binacional |
| `User` | Zona gris | `username`/`password`/`email` es boilerplate de auth; el **mono-modelo** (USER y ORGANIZATION en la misma entidad, ver `DEFENSA.md` §1) es una decisión de negocio deliberada |
| `Role` | Zona gris | El mecanismo (enum + Spring Security) es genérico; qué implica cada rol (`isProfileComplete`) es negocio |

---

## Frontend

| Capa | Clasificación | Ejemplos |
|---|---|---|
| Shell/layout | Boilerplate | `layout/base.html`, `navbar.html`, footer |
| Piezas genéricas | Boilerplate | `fragments/ui.html` (alert, badge, info-row, empty-state, breadcrumb, page-header, stat-tile), `password-field.html` |
| Auth | Boilerplate | `auth/login.html`, `auth/register.html` |
| `request-form.html` | **Core** | El formulario central — conoce ciudad, materiales, organización, franjas |
| `request-list.html` | **Core** | Encapsula el ciclo de estados y el vocabulario de materiales en la UI |
| `fragments/ui.html :: options` | **Core** | El filtro ciudad→organización vive acá también, no solo en el backend. Nota: técnicamente vive dentro de `ui.html` (fusionado ahí junto con las piezas boilerplate por reducción de archivos, ver `MEJORAS.md` #120), pero esta pieza específica sigue siendo Core, no Boilerplate |
| `track.html`, `track-modal.html` | **Core** | Rastreo de invitado — específico del dominio |
| `org/profile.html` | Zona gris | El onboarding forzado (absorbido acá, abre en modo edición si está incompleto) es negocio, la forma (un CRUD simple) es genérica |

---

## Infraestructura de deploy

**100% boilerplate** — nada acá es específico de "reciclaje" ni de
"Rivera/Sant'Ana": `Dockerfile`, `application*.properties`, Render.com
(PaaS), GitHub Actions (descartado). Cualquier app Spring Boot con
MongoDB se despliega igual. Ver tradeoffs en `DEFENSA.md` §15.

---

## Cómo usar este documento

- **Para la defensa:** cuando pregunten "¿qué es realmente tuyo?", la
  respuesta concreta son las filas **Core** — 47 clases backend, de las
  cuales ~15 son core, ~7 zona gris, el resto boilerplate.
- **Para refactors:** las filas **Boilerplate** se pueden tocar sin
  releer reglas de negocio. Las **Core** y **Zona gris** no — ahí vive
  la lógica que hay que entender antes de cambiar.
- **Al agregar algo nuevo:** clasificarlo acá también. Si todo lo nuevo
  cae en "Boilerplate", probablemente el proyecto está creciendo en
  plomería, no en dominio — señal de alerta.
