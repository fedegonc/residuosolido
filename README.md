# Eco Solicitud — Plataforma de Gestión de Reciclaje

Sistema de **recolección de residuos reciclables** para la Frontera de la Paz (Rivera, Uruguay — Sant'Ana do Livramento, Brasil). MVP implementado bajo metodología iterativa-incremental con arquitectura en capas, 380 tests no-browser, auditoría de diagramas UML y documentación completa para defensa de tesis de **Tecnólogo en Análisis y Desarrollo de Sistemas** (UTEC/UDELAR).

## Características Principales

**Flujo de Usuario (Solicitud → Aceptación → Completado)**
- ✓ Ciudadano crea solicitud de recolección (registrado o invitado)
- ✓ Rastreo anónimo por teléfono + código privado (sin login)
- ✓ Organización recibe, aceptada/rechaza con franja horaria
- ✓ Notificación in-app cuando la organización responde
- ✓ Ciudadano ve detalle en cualquier estado

**Características de Calidad**
- ✓ 380 tests no-browser, 0 failures (27 fuzz tests PhoneNumber)
- ✓ Cobertura JaCoCo 94% instrucciones en servicios core
- ✓ P0 Defensa: @Transactional, structured logging, exponential backoff, error handling tipado
- ✓ Auditoría de diagramas UML (85–90% fidelidad, sincronizados post-refactor)
- ✓ 191 mejoras documentadas (implementadas/descartadas/diferidas)

**Autenticación y Seguridad**
- ✓ Roles: `USER` (ciudadano) + `ORGANIZATION` (acopio)
- ✓ Onboarding forzado de organización (ciudad + materiales + teléfono)
- ✓ Rate limiting invitados (ventana deslizante por IP)
- ✓ Bloqueo de cuenta 15min tras 3 intentos fallidos
- ✓ CSRF token en todos los formularios, CSP estricta

**Internacionalización y Accesibilidad**
- ✓ Bilingüe: español (Rivera) + portugués (Sant'Ana do Livramento)
- ✓ Locale automático por ciudad del usuario
- ✓ Selector manual (dropdown en navbar)
- ✓ `data-i18n` + client-side JS lazy-loaded, sin FOUC
- ✓ Semántica HTML5 (`<fieldset>`, `<legend>`, `role` explícitos)

**Infraestructura**
- ✓ Selector de código de país (UY +598 / BR +55) con normalización E.164
- ✓ Validación de teléfono exhaustiva (27 tests parametrizados)
- ✓ Almacenamiento de imágenes local con validación (tipo/extensión/tamaño)
- ✓ Optimistic locking en transiciones con retry automático
- ✓ Diseño canónico (variables CSS, BEM, 0 estilos inline)

## Stack Tecnológico

| Capa | Tecnologías |
|---|---|
| **Backend** | Java 21, Spring Boot 3.2, Spring Security 6, Spring Data MongoDB |
| **Frontend** | Thymeleaf 3 (SSR), thymeleaf-layout-dialect, Vanilla JS (sin frameworks) |
| **Estilos** | CSS puro (variables, BEM, sin Tailwind/Bootstrap), Font Awesome 6 (sprite SVG local) |
| **Base de Datos** | MongoDB (Atlas en prod, spring-data-mongodb, índices explícitos) |
| **Testing** | JUnit 5, Mockito, JaCoCo (cobertura), Playwright (E2E Chrome) |
| **Análisis Estático** | PMD, compiler warnings como errores |
| **Build** | Maven 3.8+, CI/CD en Render (auto-deploy a main) |

## Requisitos

- Java 21 o superior
- Maven 3.8+
- MongoDB (local o Atlas)
- Opcional: Docker (para ambiente de prod simulado)

## Quick Start

```bash
# Clonar repositorio
git clone https://github.com/fedegonc/residuosolido.git
cd residuosolido

# Configurar entorno
cp .env.example .env
# Editar .env con MongoDB URI, puerto, etc.

# Build sin tests
mvn clean install -DskipTests

# Ejecutar en desarrollo
mvn spring-boot:run
# Acceder: http://localhost:8080
```

## Comandos Frecuentes

```bash
# Tests completos (380 unit tests)
mvn clean test

# Cobertura JaCoCo (reporte en target/site/jacoco/index.html)
mvn clean test jacoco:report

# Análisis estático PMD
mvn pmd:pmd pmd:check

# Build de producción
mvn clean package -DskipTests
java -jar target/app-0.0.1-SNAPSHOT.jar --server.port=8080

# Sandbox ejecutable (especificación del dominio)
# Requiere Maven compilado previamente
java scratch/App.java              # Modo por defecto (scenarios)
java scratch/App.java actores      # Matriz de colisiones (concurrencia)
java scratch/App.java landing-cards # Carga de landing cards
```

## Configuración

Copiar `.config/.env.example` a `.env` (gitignored):
```bash
cp .config/.env.example .env
```

Completar con valores reales:
```bash
SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/residuosolido
UPLOAD_DIR=/tmp/uploads
SERVER_PORT=8080
```

En **producción** (Render/otros), las variables se configuran directamente en el panel del servicio — nunca versionadas. Ver `docs/TRADEOFFS.md` §26.

## Documentación para Defensa

**Lectura Principal (Tribunal):**
- [`docs/DEFENSA.md`](docs/DEFENSA.md) — Guía de exposición de tesis (argumento, limitaciones, SPOFs, roadmap)
- [`docs/REQUISITOS.md`](docs/REQUISITOS.md) — Catálogo RF/RN y criterio de alcance
- [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) — Núcleo: componentes, flujos, patrones
- [`docs/DIAGRAMAS.md`](docs/DIAGRAMAS.md) — 7 diagramas UML (casos de uso, ER, clases, estados, secuencias)
- [`docs/TRADEOFFS.md`](docs/TRADEOFFS.md) — 26 decisiones de diseño justificadas
- [`docs/METODOLOGIA.md`](docs/METODOLOGIA.md) — Modelo iterativo-incremental + DSRM
- [`docs/REQUERIMIENTOS.md`](docs/REQUERIMIENTOS.md) — Trazabilidad RF/RN → código

**Referencia Técnica:**
- [`docs/ENDPOINTS.md`](docs/ENDPOINTS.md) — 43 rutas HTTP, esquemas OpenAPI, ejemplos
- [`docs/MEJORAS.md`](docs/MEJORAS.md) — 191 mejoras documentadas (estado + justificación)
- [`docs/BOILERPLATE_VS_CORE.md`](docs/BOILERPLATE_VS_CORE.md) — Clasificación: infraestructura vs negocio (156 LOC core)
- [`docs/USABILIDAD.md`](docs/USABILIDAD.md) — Instrumento Likert para evaluación con usuarios
- [`docs/COPIES.md`](docs/COPIES.md) — Single source of truth de textos i18n

**Acceso en vivo:**
- `/docs/DEFENSA` — Vista renderizada (Markdown → HTML)
- `/docs/diagramas` — Visor interactivo draw.io (7 figuras UML)
- `/scratch/App.java` — Especificación ejecutable del dominio (86 scenarios)

Ver [`docs/INDICE.md`](docs/INDICE.md) para mapa temático completo (12 canónicos + 8 referencia).

## Testing y Calidad

```
✓ 380 Unit Tests (0 failures)
  - 27 Fuzz Tests (PhoneNumber validación exhaustiva)
  - 251 Test Methods (JUnit 5 + Mockito)
  - 1 Contract Test suite (i18n, diagramas, docs)

✓ Cobertura JaCoCo
  - Servicios core: 94% instrucciones, 73% ramas
  - Controllers: 85%+ (test suite completa)
  - Model/Exceptions: 98%+ (tipos críticos)

✓ Análisis Estático
  - PMD: 0 violaciones críticas
  - Compiler: warnings → errors
  - Desactivado: inspecciones no productivas

✓ Testing End-to-End (Playwright/Chrome)
  - 6 clases BrowserTest (no disponible en CI, requiere Docker)
  - Pruebas reales: ciudadano, invitado, organización, flujos
```

## Sandbox Ejecutable

La especificación del **núcleo de dominio** se mantiene viva en `scratch/sim/`:

```bash
# 95 scenarios, 42/42 branches, 0 fallos
java scratch/App.java

# Matriz de colisiones (concurrencia, optimistic locking)
java scratch/App.java actores

# Validación de landing cards (contenido estático)
java scratch/App.java landing-cards
```

**Propósito:** port sin-framework del dominio que compila solo contra `Request`, `User`, `PhoneNumber`, excepciones `Keyed` y `ServerMessage`. Si una regla del dominio agarra una dependencia de Spring, el sandbox deja de compilar — test de pureza de la capa de negocio.

## Arquitectura

```
Domain (enums, PhoneNumber, Request, User, Notification)
    ↓
Ports (UserRepository, RequestRepository, CityOrgPort, RateLimiterPort)
    ↓
Services (RequestService, UserService, NotificationService, auth, images)
    ↓
Controllers (Web adapters, validación de entrada, routing)
    ↓
Persistence (MongoDB via spring-data, indices explícitos)
```

**Patrón:** arquitectura hexagonal con front-adapter (Thymeleaf) y back-adapters (Mongo, imágenes en disco). Capa de dominio es **pura** (testeable sin Spring).

## Notas de Desarrollo

- **Console-driven:** no hay Artifacts externos; todo análisis/reportes/diagramas en la terminal/chat
- **Tests antes que código:** TDD para dominios críticos (PhoneNumber, RequestStateMachine, optimistic locking)
- **Documentación viva:** cada cambio se consolida en docs/ — no drift
- **Contrato de tests:** validación automática de i18n, diagrama↔código, referencias

## Producción

```bash
# Build y run en Docker
docker build -t residuosolido .
docker run -p 8080:8080 -e SPRING_DATA_MONGODB_URI=... residuosolido

# O JAR directo
java -jar target/app-0.0.1-SNAPSHOT.jar
```

Deploy automático a Render en cada push a `main` (ver `docs/TRADEOFFS.md` §19).

## Contribución

Abrir issue o PR. Seguir las convenciones de CLAUDE.md (Conventional Commits, ramas de tópico desde `developer`, policy de squashing).
