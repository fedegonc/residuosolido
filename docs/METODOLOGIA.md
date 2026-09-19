# Metodología — Modelo Iterativo Incremental

El sistema **Eco Solicitud** se desarrolló siguiendo un **modelo
iterativo incremental** adaptado a las restricciones de un MVP de
tesis (tecnólogo en Análisis y Desarrollo de Sistemas). Este documento
describe las 4 fases del modelo aplicado, su alcance y los artefactos
producidos en cada una.

---

## Por qué iterativo incremental

El proyecto tiene un plazo acotado (una semana para la entrega) y un
alcance definido por el oficio de la carrera. Un modelo en cascada puro
exigiría especificar todo antes de programar; un modelo ágil puro
(Scrum, sprints de 2 semanas) no encaja con un solo desarrollador y un
plazo tan corto.

El **iterativo incremental** permite:

- Entregar valor usable al final de cada fase (no esperar al final).
- Reducir riesgo temprano (validar arquitectura y seguridad antes de
  agregar features).
- Documentar y probar incrementalmente (cada fase deja tests que
  protegen a la siguiente).
- Ajustar alcance sin rehacer todo (las fases son acumulativas).

---

## Fase 1 — Fundación y seguridad

**Objetivo:** establecer la arquitectura base, autenticación, roles y
las reglas de seguridad más críticas.

**Artefactos producidos:**

- Stack: Java 21 + Spring Boot 3.2 + Thymeleaf + MongoDB.
- `User` con roles `USER` / `ORGANIZATION` (sin rol Admin).
- `SecurityConfig` con CSRF, login por formulario, bloqueo por intentos
  fallidos (`LoginAttemptService`).
- `RegistrationForm` (DTO) para evitar mass-assignment.
- Onboarding forzado de organización (`profileCompleted`).
- Validaciones server-side de email, username y contraseña.
- Rate limiting de invitados (`GuestRateLimiter`).
- 11 tests de seguridad crítica (`CriticalSecurityTest`).

**Cierre de fase:** el sistema autentica, autoriza por rol y rechaza
entradas maliciosas básicas. No hay flujo de solicitudes todavía.

---

## Fase 2 — Flujo principal de solicitudes

**Objetivo:** implementar el caso de uso central: un ciudadano (o
invitado) crea una solicitud y una organización la gestiona.

**Artefactos producidos:**

- `Request` con ciclo de estados (`PENDING → IN_PROGRESS / REJECTED →
  COMPLETED`), `@Version` para optimistic locking.
- `RequestCreateController` + `RequestService` (creación con imagen
  opcional).
- `CityOrgService` — resolución de organización por ciudad y materiales.
- `OrgRequestController` + `RequestTransitionService` (aceptar /
  rechazar / completar).
- Rastreo de invitados por teléfono + código privado de 8 caracteres.
- `InformalCollector` (CRUD interno de la organización).
- Tests de servicios (validación, transiciones, queries, métricas).

**Cierre de fase:** el flujo end-to-end funciona. Un usuario puede
registrarse, crear una solicitud, y una organización puede aceptarla y
completarla. 14 tests E2E (`EndToEndFlowsTest`) validan el flujo
completo.

---

## Fase 3 — Endurecimiento, métricas y usabilidad

**Objetivo:** cerrar brechas de seguridad, agregar métricas, mejorar
la experiencia de usuario y la consistencia visual.

**Artefactos producidos:**

- `PhoneNumber` con `CountryCode` (UY +598 / BR +55) y normalización
  E.164.
- Revalidación de materiales en edición de solicitudes.
- Borrado con optimistic locking (`delete(entity)`).
- Dashboard de organización con estadísticas (faceted aggregation).
- i18n español / portugués con `data-i18n` en templates.
- Sistema de diseño canónico: variables CSS, BEM, dark mode,
  transiciones suaves, `prefers-reduced-motion`.
- 181 tests, 0 failures.

**Cierre de fase:** el sistema es seguro, consistente y bilingüe. (La PWA
de esta fase se descartó después — ver `docs/MEJORAS.md` #11/#100.) Las
métricas públicas funcionan.

---

## Fase 4 — Contenido, defensa y documentación

**Objetivo:** preparar el sistema para la defensa de tesis: contenido
público que transmita el propósito, documentación sincronizada,
diagramas, tradeoffs explícitos.

**Artefactos producidos:**

- Panel de acopio consolidado en una sola vista (`/acopio/solicitudes`):
  estadísticas + lista filtrable por estado. (Originalmente fue un
  Kanban en `/acopio/inicio`; se eliminó por duplicar la lista, ver
  `docs/MEJORAS.md` #130 y `docs/TRADEOFFS.md` §7.)
- "Mis solicitudes" — modal canónico con rastreo sin cuenta / login.
- Política de contraseña mínima de 8 caracteres (documentada en
  `docs/MEJORAS.md`).
- Documentación completa y sincronizada:
  - `README.md` — instalación, stack y mapa de documentos.
  - `docs/INDICE.md` — punto de entrada a toda la documentación.
  - `docs/ENDPOINTS.md` — rutas HTTP, OpenAPI y testing.
  - `docs/DIAGRAMAS.md` — diagramas UML y flujos.
  - `docs/REQUISITOS.md` — catálogo RF/RN y criterio de alcance.
  - `docs/ARQUITECTURA.md` — núcleo del sistema (componentes y flujos).
  - `docs/DEFENSA.md` — guía de defensa.
  - `docs/TRADEOFFS.md` — decisiones de diseño y tradeoffs.
  - `docs/MEJORAS.md` — tabla de superficies de mejora.
  - `docs/COPIES.md` — single source of truth de textos de UI.
  - `docs/METODOLOGIA.md` — este archivo (fases del desarrollo).
- Diagramas draw.io: casos de uso, modelo lógico, clases y secuencia UML.

**Cierre de fase:** el sistema está listo para la defensa. La
documentación refleja el estado real del código. Los tradeoffs están
explícitos. Las limitaciones están documentadas como decisiones
conscientes, no como omisiones.

---

## Resumen de fases

| Fase | Foco | Entregable principal | Tests |
|---|---|---|---|
| 1 | Fundación y seguridad | Auth, roles, validación | ~30 |
| 2 | Flujo de solicitudes | CRUD Request, transiciones | ~100 |
| 3 | Endurecimiento y UX | Métricas, i18n, PWA, diseño | ~60 |
| 4 | Contenido y defensa | Docs, diagramas | 181 total |

Cada fase es **incremental** (agrega funcionalidad nueva) e
**iterativa** (refina y endurece lo anterior). Los tests de cada fase
protegen a la siguiente contra regresiones.

---

## Relación con Design Science Research Methodology (DSRM)

El modelo iterativo incremental aplicado es compatible con el marco
DSRM de Peffers et al. (2007), usado aquí como marco de análisis
retrospectivo, no como metodología seguida desde el inicio.

| Etapa DSRM | Correspondencia en el proyecto |
|---|---|
| 1. Identificación del problema | Necesidad de coordinación entre ciudadanos y organizaciones |
| 2. Objetivos de la solución | MVP que gestiona solicitudes de recolección y su estado |
| 3. Diseño y desarrollo | Arquitectura, modelo de datos, reglas de negocio, interfaz |
| 4. Demostración | Demo con datos sintéticos (flujo end-to-end) |
| 5. Evaluación | 181 tests + verificación en navegador; usabilidad pendiente |
| 6. Comunicación | Documentación técnica, diagramas UML, esta defensa |

**Referencia:** Peffers, K., Tuunanen, T., Rothenberger, M. A., &
Chatterjee, S. (2007). A Design Science Research Methodology for
Information Systems Research. *MIS Quarterly*, 32(1), 77–105.
[DOI: 10.2753/MIS0742-1222240302](https://doi.org/10.2753/MIS0742-1222240302)

---

## Fuentes académicas y contexto

Ver `docs/TRADEOFFS.md` §8 para la lista completa de fuentes UTEC,
metodología, sistemas comparables y contexto territorial.

---

## Documentos relacionados

- `docs/COPIES.md` — single source of truth de textos de UI (claves i18n).
- `docs/REQUISITOS.md` — catálogo RF/RN.
- `docs/DEFENSA.md` — guía de defensa; `docs/TRADEOFFS.md` — decisiones de diseño.
