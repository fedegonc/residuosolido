# Instrucciones para Claude Code — Eco Solicitud

## Console-Driven Development (OBLIGATORIO)

**NUNCA crear Artifacts.** Todo el trabajo de análisis, reportes, diagramas y documentación debe hacerse:

- Directamente en la respuesta de texto (consola/chat), usando Markdown, tablas y ASCII cuando aplique.
- Si se necesita persistencia, guardar como archivo real dentro del repo (`docs/`, `.md`) — nunca como Artifact publicado.
- Diagramas: usar ASCII art o Mermaid en bloques de código, no SVG en un Artifact.
- Métricas y análisis: mostrarlos como tablas Markdown en la respuesta, no como HTML publicado.

**Razón:** el flujo de trabajo de este proyecto es console-driven. El usuario prefiere ver todo directo en la terminal, sin depender de links externos ni páginas publicadas.

## Documentar y Consolidar (OBLIGATORIO)

**Toda cosa nueva que se implemente debe documentarse y consolidarse**, no quedar solo en la respuesta de chat:

- Feature/mejora nueva → agregar fila en `docs/MEJORAS.md` (tabla de superficies) con su estado (Implementado/Descartado/Diferido).
- Endpoint nuevo → actualizar `docs/ENDPOINTS.md` y `Routes.java`.
- Mecanismo de seguridad nuevo o cambiado → actualizar `docs/DEFENSA.md` y `docs/MEJORAS.md`.
- Decisión de diseño/tradeoff → agregar sección en `docs/DEFENSA.md` (tradeoffs).
- Cambio en el conteo de tests → actualizar encabezado de `docs/INDICE.md`.
- Diagrama nuevo o modificado → `docs/diagrams/` + referenciar en `docs/DIAGRAMAS.md`.

**No dejar trabajo "flotando" solo en el historial de chat.** Si se crea, cambia o descarta algo, se refleja en los docs existentes del proyecto — nunca se crean documentos nuevos sueltos si ya hay un lugar canónico donde consolidar esa información (ver `docs/INDICE.md` para el mapa completo).

## Stack del Proyecto

- Java 21, Spring Boot 3.2, Spring Security 6
- Thymeleaf 3 (SSR) + thymeleaf-layout-dialect
- MongoDB (Atlas en prod, spring-data-mongodb)
- HTMX 2.0.4 + Vanilla JS (sin frameworks frontend)
- CSS puro con variables y BEM (sin Tailwind/Bootstrap)

## Comandos Frecuentes

```bash
mvn clean install -DskipTests   # build
mvn spring-boot:run             # run en modo dev
java -jar target/app-0.0.1-SNAPSHOT.jar --server.port=8080   # run desde jar
mvn clean test                  # tests completos
mvn clean test jacoco:report    # cobertura
mvn pmd:pmd pmd:check           # análisis estático
```

## Documentación del Proyecto

- `docs/INDICE.md` — mapa de toda la documentación
- `docs/DEFENSA.md` — guía de defensa de tesis, tradeoffs
- `docs/DIAGRAMAS.md` — diagramas UML y arquitectura
- `docs/ENDPOINTS.md` — rutas HTTP y OpenAPI
- `docs/MEJORAS.md` — estado de mejoras (implementado/descartado/diferido)

## Convenciones de Frontend

- Componentes reutilizables en `templates/fragments/` (alert, badge, card, info-row, etc.)
- Estados HTMX manejados vía CSS en `static/css/htmx-states.css` (sin JS extra)
- i18n: un solo JSON por idioma (`static/i18n/{lang}.json`), servido también server-side vía `JsonMessageSource` — no hay `messages_*.properties`
- JS exclusivo de una página va en su propio archivo (`static/js/{página}.js`), cargado vía `layout:fragment="pageScripts"` en esa página — no en `app.js`. `app.js` es solo lo global o componentes reusados por 2+ páginas (ver comentario-manifiesto al inicio del archivo)

## Convenciones de Backend

- **Antes de crear un `@Component`/`@Service` nuevo, preguntar: ¿tiene estado
  mutable real, o implementa una interfaz de Spring que exige ser bean
  (`HandlerInterceptor`, `AuthenticationSuccessHandler`, `LocaleResolver`,
  etc.)?** Si la respuesta a ambas es "no" — es una función pura — va como
  método estático en una clase existente afín (ej. `Routes.java` para lo
  relacionado a rutas/navegación), no como un bean inyectado nuevo.
  Ejemplo real: `RoleBasedLoginTargetUrlResolver` (un bean que solo hacía
  `Authentication → String`, sin estado) se fusionó en
  `Routes.resolveHomeForRole()` — ver `docs/MEJORAS.md` #108.
- Rutas HTTP centralizadas en `Routes.java` — nunca hardcodear un string de
  URL en un controller/config nuevo.
- Contratos de datos que cruzan Java→JS (ej. atributos `data-*` en HTML)
  necesitan un método explícito en el modelo (ej.
  `User.getAcceptedMaterialsCsv()`), nunca depender del `toString()` default
  de una colección — ver `docs/MEJORAS.md` #105.
- **Thymeleaf: `T(...)` (acceso a clase estática) NO se permite dentro de los
  argumentos de un fragment call** (`th:replace="~{frag :: nombre(${T(...)...})}"`)
  — Thymeleaf lo bloquea por seguridad ("Instantiation of new objects...
  forbidden in this context"). Tampoco se permite invocar un **método**
  estático (`T(Clase).metodo()`) dentro de un `th:with` en la raíz de un
  fragment insertado — sí se permite acceder a un **campo** estático (enum)
  como `T(Clase).VALOR`. Si hace falta resolver un `T(...)` antes de pasarlo
  como argumento, usar `th:with` en el elemento que hace el `th:replace`
  (fuera del fragment) para bindearlo a una variable simple primero. Un
  string literal con `--` dentro de una expresión `th:*` también puede
  romper el parser de attoparser — evitar computar clases BEM dinámicamente,
  dejarlas como atributo `class` estático. Ver `docs/MEJORAS.md` #111,
  #114, #115 (3 bugs reales encontrados por esto, uno enmascarando al otro).
- **Índices de Mongo: no confiar en `auto-index-creation` para cambios de
  `@Indexed`** — si un índice ya existe con opciones distintas (`unique`,
  `sparse`, `collation`), el driver tira `IndexKeySpecsConflict` y **no deja
  arrancar la app** (no es un warning). `spring.data.mongodb.auto-index-creation`
  está en `false` a propósito; los índices se gestionan explícitamente en
  `MongoIndexMigration.java` (un `CommandLineRunner` idempotente). Cualquier
  cambio a un `@Indexed` existente va ahí, no solo en la anotación.
