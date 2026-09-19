# Tradeoffs — Decisiones de Diseño

Registro de decisiones de diseño con alternativas evaluadas, tradeoffs y justificación. Extraído del anexo de `DEFENSA.md` (que queda como guía de defensa).



Este documento registra las decisiones de diseño más importantes del
MVP **Eco Solicitud**, con su justificación y las consecuencias
asumidas. Forma parte del análisis crítico para la defensa de tesis.

---

## 1. Mono-modelo User (Usuario + Organización)

**Decisión:** usuarios y organizaciones comparten la misma entidad
`User`, diferenciados por `role`.

**A favor:** simplicidad de modelo, un solo repositorio, un solo flujo
de auth, menos código.

**En contra:** una cuenta `ORGANIZATION` representa simultáneamente
identidad de acceso y participante de negocio. No soporta múltiples
operadores por cooperativa, ni una misma persona como ciudadano y
operador.

**Para producción:** separar `Cuenta` (auth) de `Organización`
(entidad de negocio) con una relación de pertenencia.

---

## 2. Sin panel Admin

**Decisión:** no existe rol `ADMIN` ni panel de administración
general.

**A favor:** menos superficie de ataque, menos código, menos
responsabilidad de moderación.

**En contra:** no hay forma de revocar organizaciones fraudulentas,
editar usuarios, ni gestionar contenido desde el sistema.

**Para producción:** agregar rol `ADMIN` con scope limitado (gestión
de organizaciones, moderación de contenido, métricas globales).

---

## 3. MongoDB standalone (sin réplica ni sharding)

**Decisión:** MongoDB standalone, sin transacciones multi-documento.

**A favor:** setup simple, suficiente para MVP local, sin ops.

**En contra:** las operaciones que involucran varios documentos (crear
solicitud + subir imagen) no son atómicas. `@Version` protege
transiciones y borrado, pero no hay rollback automático si una
operación parcial falla después de persistir.

**Para producción:** migrar a réplica set (mínimo) para habilitar
transacciones multi-documento.

**Tripwire:** ante el primer incidente real de dato parcial (ej. una
solicitud creada sin su imagen porque la operación falló a mitad de
camino) — no "cuando escale", una condición verificable y puntual.

---

## 4. Contraseña mínima de 3 caracteres (fase MVP)

**Decisión:** el mínimo de contraseña es 3 caracteres, no 8.

**A favor:** facilita demostración y pruebas manuales en la defensa.

**En contra:** trivialmente vulnerable a fuerza bruta. **No es
aceptable para producción.**

**Para producción:** restaurar a 8 mínimo (o 12 con políticas OWASP:
complejidad, breach-list check). Una línea en `UserService.validatePassword()`.

**Tripwire:** el mismo que el PIN de registro (§24) — **~50 usuarios
reales**. Son la misma decisión (fricción de auth vs. facilidad de
prueba), no dos criterios distintos.

---

## 5. Blog estático — descartado del MVP

**Decisión:** el blog estático fue planificado pero no se implementó.
No existe `BlogController`, ni templates, ni rutas `/blog`.

**A favor de descartarlo:** el blog no aporta al flujo core (solicitud →
aceptación → completado) y suma mantenimiento sin valor para el MVP.

**En contra:** la landing page tiene menos contenido editorial.

**Para producción:** si se reactiva, colección `posts` en MongoDB,
editor en panel, slug único, fecha de publicación, estado
borrador/publicado.

---

## 6. Catadores — CRUD descartado

**Decisión:** el CRUD de `InformalCollector` fue planificado pero no
se implementó. No existe controller ni servicio.

**A favor de descartarlo:** no mezcla gestión interna con comunicación
pública.

**En contra:** `Request` no tiene relación con el recolector
responsable (no se puede responder "quién atendió esta solicitud").

**Para producción:** decidir si el CRUD vuelve como herramienta
interna (asignación de recolector a solicitud, RF-8 completo) o si se
elimina.

---

## 7. Panel de acopio: lista filtrada, no Kanban (revisado)

**Decisión original:** el tablero Kanban vivía en `/acopio/inicio`, no
en una ruta separada — menos navegación, todo en un solo lugar.

**Decisión actual:** el Kanban fue **eliminado**. El panel de acopio es
`/acopio/solicitudes`: una lista filtrable por estado con estadísticas en
el encabezado. Las acciones (aceptar/rechazar/completar) se hacen desde
el detalle de cada solicitud.

**Por qué cambió:** el Kanban duplicaba funcionalidad — mostraba las
mismas solicitudes agrupadas por estado que la lista ya podía filtrar,
y ofrecía las mismas transiciones que el detalle. Dos vistas del mismo
dato era código doble para mantener y explicar. Además cargaba una
lista de pendientes que nunca llegaba a renderizarse. Eliminarlo
suma en credibilidad (menos superficie duplicada) y en
mantenibilidad (una sola vista de la verdad).

**Lo que se pierde:** la vista de "tablero" con columnas por estado es
más visual que una lista. Con el volumen esperado del MVP (pocas
solicitudes simultáneas por organización), el filtro por estado cubre
la misma necesidad.

**Para producción:** si el volumen crece, paginación de la lista o una
vista dedicada con drag-and-drop. El tripwire original sigue vigente:
~20 solicitudes pendientes simultáneas por organización.

---

## 8. Notificaciones WhatsApp

**Decisión:** el envío de notificaciones por WhatsApp fue eliminado del
MVP.

**A favor:** menos código, sin dependencia de proveedores externos, sin
acoplamiento entre transiciones y notificaciones.

**En contra:** el ciudadano no recibe aviso automático de cambio de
estado.

**Para producción:** si se valida con usuarios, integrar la API oficial
de WhatsApp Business (Meta) o un proveedor (Twilio, 360dialog) usando
eventos de dominio para no acoplar el flujo de estados.

---

## 9. Imágenes locales en disco

**Decisión:** `LocalImageService` guarda archivos en disco, no en
cloud storage.

**A favor:** sin dependencia externa, sin costo, sin configuración.

**En contra:** no escalable horizontalmente (si hay múltiples
instancias, cada una tiene su disco), sin backup automático, sin CDN.

**Para producción:** migrar a S3 / Cloudinary / similar.

**Tripwire:** antes del primer deploy a un entorno con filesystem
efímero real (ej. Render sin disco persistente) — riesgo ya identificado
en el FODA del proyecto: un redeploy puede borrar `/uploads` entero.

---

## 10. Sin mapas ni geolocalización

**Decisión:** no hay mapas ni GPS, fundamentado en el Oficio 044/2023
de la carrera.

**A favor:** reduce alcance, no requiere API de mapas, respeta el
oficio.

**En contra:** la asignación es por ciudad (no por proximidad real).

**Para producción:** ver sección "Fuera de alcance — GPS" en
`HARDENING.md` para el plan de implementación.

---

## 11. Diseño canónico (un solo CSS, sin CSS Modules)

**Decisión:** un solo `app.css` con variables CSS y BEM, sin CSS
Modules ni build step de frontend.

**A favor:** cero tooling de frontend, coherencia visual garantizada,
fácil de mantener para un MVP.

**En contra:** no hay scoping automático de estilos por componente
(como sí lo tendría CSS Modules o styled-components).

**Para producción:** si el frontend crece, considerar CSS Modules
(vía PostCSS) o un framework de componentes. Hoy no es necesario.

---

## 12. Métricas públicas sin protección

**Decisión:** `/metricas` expone totales agregados (sin datos
personales) sin autenticación.

**A favor:** transparencia, cualquier ciudadano ve el impacto
agregado.

**En contra:** expone datos sin control de acceso (aunque sean
agregados).

**Para producción:** mantener público si es decisión de diseño, o
proteger si se agregan métricas con más detalle. Documentado en el
backlog (`docs/REQUISITOS.md` §3).

---

## 13. Compactación del sistema — aplicada

**Decisión:** compactar el sistema para reducir área y carga
cognitiva, priorizando las mejoras de bajo costo y alto impacto.

**Aplicado:**

- **Index fusionado (4 secciones → 2):** hero + cómo funciona en una
  sola sección; CTA cooperativas en otra. El banner de cooperativas
  se integró como `.coop-cta` inline dentro de la sección de contenido,
  no como sección separada.
- **Blog descartado:** el blog estático fue planificado pero no se
  implementó. No hay `BlogController`, ni templates, ni rutas `/blog`.
- **Padding reducido:** el hero pasó de `2.5rem` a `1.5rem` de
  padding, uniformando con el resto de las secciones.

**A favor:** menos scroll, menos secciones, menos templates, menos
rutas. El visitante ve el contenido completo en menos espacio.

**En contra:** el index tiene menos contenido editorial sin el blog.

---

## 14. Compactación del sistema — diferida

**Decisión:** las siguientes fusiones fueron evaluadas pero
**diferidas** por riesgo de regresión a una semana de la defensa.

**Implementado (post-podado):**

- **Fusionar request-form + request-edit:** `RequestEditController`
  fusionado en `RequestController`. Ahora un solo controller maneja
  listar, ver, editar y eliminar solicitudes.
- **Fusionar perfiles (user + org + onboarding):** `OrgOnboardingController`
  fusionado en `OrgProfileController`. Ahora un solo controller maneja
  onboarding y edición de perfil.
- **Fusionar org-request + org-request-detail:** `OrgRequestDetailController`
  fusionado en `OrgRequestController`. Ahora un solo controller maneja
  lista, detalle y transiciones.
- **Simplificar RequestStatus:** patrón State abstracto reemplazado por
  enum simple con métodos basados en `if`. Mismo comportamiento, mismo
  mensaje de error, 73 → 46 líneas.

**Diferido:**

- **Reducir clases CSS con utilities:** muchas clases únicas son
  reemplazables por utilities (`text-sm`, `mt-1`). Pero implica
  cambiar múltiples templates. Riesgo: bajo pero tedioso. Ahorro:
  ~40 clases.

**Resuelto desde entonces:** la fusión de dashboards quedó obsoleta —
el dashboard de usuario se absorbió en su lista de solicitudes, y el
dashboard Kanban de organización se eliminó directamente (ver §7).
Los controllers web bajaron a 11 con 153 tests pasando sin regresiones.

**Para después de la defensa:** la compactación de CSS con utilities
es el próximo paso natural. Se puede hacer de forma aislada con su
propio set de tests.

---

## 15. Deploy: Render.com (PaaS) en vez de AWS (IaaS)

**Decisión:** el MVP se despliega en Render.com (PaaS). AWS se
**descarta** para esta fase.

**A favor de Render.com:**
- Deploy automático desde GitHub (push → deploy).
- MongoDB Atlas integrado como add-on.
- HTTPS y dominio gratuito incluidos.
- Cero configuración de infraestructura.
- Plan gratuito suficiente para MVP.
- Ideal para tesis: el foco es el sistema, no DevOps.

**En contra (de no usar AWS):**
- Menos control sobre infraestructura.
- No hay VPC, IAM, ni seguridad a nivel red.
- Escalado limitado en plan gratuito.
- Sin acceso a servicios AWS (S3, SES, Lambda).

**Por qué AWS se descarta para la tesis:**
- Complejidad de configuración no aporta al análisis del sistema.
- Costo: capa gratuita de 12 meses, pero MongoDB no incluido.
- El tiempo de configuración (EC2, security groups, load balancer,
  Route 53, RDS/DocumentDB) no se justifica para un MVP.
- Render.com demuestra despliegue PaaS moderno, que es lo que un
  tecnólogo en sistemas debe conocer.

**Para producción (post-tesis):** migrar a AWS o equivalente IaaS
cuando se necesite: escalado horizontal, VPC, IAM granular,
multi-región, backups automatizados, compliance.

---

## 16. Dark mode con CSS variables (sin framework)

**Decisión:** dark mode implementado con CSS variables + atributo
`data-theme` en `<html>`. Sin framework, sin librería, sin
JavaScript pesado.

**A favor:**
- Cero dependencias.
- Toggle instantáneo (sin recarga).
- `localStorage` persiste la preferencia.
- `prefers-color-scheme` respeta la preferencia del SO.
- Script externo `theme.js` en `<head>` aplica el tema antes de pintar (sin flash).

**En contra:**
- Duplicación de variables en `:root` y `[data-theme="dark"]`.
- Algunos componentes necesitan overrides manuales (navbar, hero,
  forms, cards).
- No hay transición suave entre temas (intencional: evitar parpadeo).

---

## 17. FOUC fix — ocultar contenido hasta que i18n esté listo

**Decisión:** el `<html>` se oculta (`visibility: hidden`) hasta
que las traducciones client-side se aplican. Fallback de 800ms.

**A favor:**
- Elimina el flash de texto sin traducir (español → portugués).
- El usuario ve directamente el idioma correcto.

**En contra:**
- Pequeña demora inicial (generalmente <200ms).
- Si el JS falla, el fallback de 800ms muestra el contenido.
- No es ideal para SEO (pero el sistema es una app, no un blog).

---

## 18. Páginas públicas de documentación (/documentos, /diagramas)

**Decisión:** agregar dos páginas públicas que sirven como índice de
la documentación técnica (`docs/*.md`) y los diagramas UML
(`docs/diagrams/*.drawio`), con un resource handler que sirve los
archivos estáticos desde el directorio `docs/`.

**A favor:**
- Acceso directo a la documentación desde la app, sin repositorio.
- Visor interactivo de diagramas (diagrams.net embebido).
- Descarga directa de archivos `.drawio` para edición offline.
- Sin auth requerida: la documentación es pública.

**En contra:**
- La documentación se sirve desde el filesystem del servidor, no
  desde `src/main/resources/static` (necesita resource handler
  custom en `WebConfig`).
- Si los archivos `.md` se renombran o eliminan, las páginas quedan
  con links rotos (no hay validación dinámica).
- El visor de diagramas depende de CDN (diagrams.net) para funcionar.

**Para producción:** generar los diagramas como SVG embebido en
lugar de depender del visor JS de diagrams.net.

---

## 19. Verificación manual antes del deploy automático

**Decisión:** eliminar el workflow de GitHub Actions. Los tests, PMD y
el build Docker se ejecutan manualmente antes del push; Render.com
construye el artefacto sin repetir tests dependientes de MongoDB o del
navegador.

**A favor:**
- El build de Render no depende de una instancia de MongoDB de tests.
- Se evita duplicar una suite extensa en GitHub Actions y Render.
- El deploy conserva el flujo simple: verificación local, push y build
  Docker en Render.

**En contra:**
- La calidad depende de ejecutar la verificación manual antes del push.
- GitHub no bloquea código sin tests ni genera artifacts de JaCoCo/PMD.
- No existe una barrera automática antes del deploy.

**Para producción:** reintroducir una verificación automatizada cuando
el equipo o la frecuencia de cambios justifiquen el costo operativo.

---

## 20. PMD con failOnViolation=false

**Decisión:** PMD está configurado para ejecución manual, pero no bloquea el
build si encuentra violaciones (`failOnViolation=false`).

**A favor:**
- El proyecto tiene 68 violaciones heredadas; bloquear el build
  detendría el desarrollo.
- PMD sigue generando reportes útiles para análisis.
- Migrado a PMD 7 (maven-pmd-plugin 3.28.0) con rulesets category-based
  (`bestpractices.xml`, `design.xml`).

**En contra:**
- Las violaciones pueden acumularse sin consecuencia.
- No existe una validación automática de calidad estática antes del deploy.

**Para producción:** revisar las 68 violaciones de PMD 7, activar
`failOnViolation=true` cuando lleguen a cero.

---

## 21. Versiones del stack — Spring Boot 3.2 / Java 21

**Decisión:** el MVP se entrega con Spring Boot 3.2.0 y Java 21,
aunque existe Spring Boot 4.1.1.

**A favor:**
- Spring Boot 3.2 es estable, documentado y compatible con todas
  las dependencias del proyecto.
- Java 21 es LTS (soporte hasta septiembre 2028).
- Migrar a Spring Boot 4.0 implica 115 breaking changes (42 rompen
  compilación, 24 fallan en runtime, 19 dan resultados incorrectos
  silenciosamente). `@MockBean` se elimina, Spring Security tiene
  un DSL rewrite, properties se renombran.

**En contra:**
- El tribunal puede cuestionar el uso de Spring Boot 3.2 (no 4.x).
- Spring Boot 3.2 llega a fin de soporte OSS en diciembre 2026.

**Para producción:** migrar primero a 3.5.x (limpiar deprecations),
después a 4.0 (migración mayor). Documentado en `docs/MEJORAS.md`.

---

## 22. Brief de personalidad visual (anti-slop de diseño)

**Decisión:** definir por escrito la personalidad visual del producto
antes de seguir ajustando CSS, porque el frontend actual — aunque
minimalista y bien tokenizado — usa casi exclusivamente los defaults
genéricos de cualquier template generado por IA (gradiente diagonal en
botones, círculos numerados en "cómo funciona", una sola familia
tipográfica "segura", cero color secundario con propósito). Consistencia
sin intención sigue siendo genérico.

**Los 3 adjetivos que debe transmitir:**
1. **Territorial** — es un sistema de un lugar concreto (Rivera —
   Sant'Ana do Livramento), no una plataforma global genérica.
2. **Institucional / confiable** — se parece más a un servicio público
   bien hecho que a una landing de SaaS buscando ronda de inversión.
3. **Directo al grano** — sin lenguaje aspiracional ni decoración que no
   cumple una función (coherente con la regla ya existente de
   "microcopy anti-slop", `docs/MEJORAS.md` #17 y #21).

**Qué NO debe sentirse:**
- No debe parecer una landing de growth/SaaS (gradientes, iconos en
  círculos de colores, copy tipo "revolucionamos el reciclaje").
- No debe depender de decoración para verse terminado — si algo se
  saca y la interfaz sigue funcionando igual de bien, esa decoración no
  cumplía un propósito.

**Referencia de sensación (no de diseño literal):** un servicio
municipal o cooperativo bien ejecutado — sobrio, con jerarquía clara,
sin necesidad de "vender" nada porque ya resuelve un problema real.

**Consecuencia práctica:** cualquier elección visual nueva se evalúa
contra estos 3 adjetivos antes de agregarse. Los cambios de la Fase 1
(sin gradientes, con color secundario con propósito real, "cómo
funciona" sin círculos numerados) están documentados en `docs/MEJORAS.md`.

---

## 23. Métricas del proyecto y diagnóstico de escala (¿un estudiante o un equipo?)

**Pregunta que anticipa esta sección:** "¿esto lo hizo un solo estudiante
o hacía falta un equipo?" Se responde con métricas, no con opinión.

### Líneas de código por capa

| Capa | Archivos | LOC |
|---|---|---|
| Backend Java (producción) | 47 clases | 3.457 |
| Backend Java (tests) | 26 clases | 3.245 |
| Frontend — templates Thymeleaf | 25 | 1.127 |
| Frontend — CSS (`app.css`+`fonts.css`+`htmx-states.css`) | 3 | 287 |
| Frontend — JS vanilla | 1 | 180 |
| i18n (es/pt, JSON) | 2 | 556 |
| Documentación (`docs/*.md`) | 7 | 3.258 |
| **Total** | **111** | **~12.110** |

### Estructura del backend (47 clases)

| Paquete | Archivos | Rol |
|---|---|---|
| `config` | 13 | Seguridad, i18n, rutas centralizadas (incluye `resolveHomeForRole`), carga de datos |
| `controller` | 12 | HTTP, 27 endpoints (`@*Mapping`), 32 constantes de ruta en `Routes.java` |
| `service` | 7 | Lógica de negocio (el más grande: `RequestService`, 329 líneas) |
| `enums` | 5 | Estados, materiales, ciudades, franjas |
| `model` | 3 | Entidades de dominio |
| `repository` / `dto` / `exception` | 2 c/u | Persistencia, transferencia, manejo de errores |

**Ningún archivo supera las 330 líneas** (el más grande, `RequestService.java`,
sigue siendo una sola responsabilidad legible de punta a punta). No hay
"clases dios" ni lógica dispersa sin dueño claro.

### Testing y desarrollo

- **173 métodos `@Test`** + **1 `@ParameterizedTest`** con 8 casos
  (`I18nMessageResolutionTest`) → **181 ejecuciones reales** (26 archivos
  de test, 24 con métodos `@Test`; 2 son clases base/seed sin tests
  propios: `PlaywrightBaseTest`, `BrowserTestSeed`). `docs/INDICE.md`/
  `MEJORAS.md` ya reflejan este número (actualizado en esta pasada de
  sincronización); igual **correr `mvn clean test` antes de la defensa**
  para confirmar que sigue siendo exacto — la suite `MongoAggregationUtilsIntegrationTest`,
  `DocsControllerTest` y otras requieren un MongoDB real en
  `localhost:27017`, así que este conteo es estático (por código), no
  el resultado de una corrida verificada en este entorno.
- **428 commits**, un solo autor (3 alias de email de la misma persona:
  `fedegonc`/`FedericoGoncalvez`/`goncalvezfede@gmail.com`), a lo largo de
  **~14 meses** (jul. 2025 → sep. 2026).
- Metodología iterativo-incremental documentada en `docs/METODOLOGIA.md`
  (4 fases), no un sprint de último momento.

### Diagnóstico

**La escala y la estructura son consistentes con un estudiante trabajando
solo de forma sostenida, no con una necesidad real de equipo.** Argumentos:

1. **Tamaño total** (~12k líneas contando tests y docs, ~7k solo código):
   está en el rango típico de un proyecto de tesis/capstone individual
   (3k-15k LOC), muy por debajo del umbral donde el paralelismo de un
   equipo se vuelve necesario (proyectos de 50k+ LOC, múltiples
   servicios desplegables, o necesidad de especialización simultánea
   24/7 en áreas separadas).
2. **Sin necesidad de especialización paralela**: una sola base de datos
   (MongoDB, no distribuida), un solo desplegable (no microservicios),
   frontend deliberadamente minimalista sin framework (`docs/MEJORAS.md`
   — decisión explícita, no limitación de tiempo).
3. **Cero señales de "demasiadas manos sin coordinar"**: un solo autor
   en el historial de git, convenciones de nombres y capas consistentes
   en las 47 clases, sin duplicación de responsabilidades entre
   controllers/services.
4. **Lo que sí haría falta un equipo**: escalar esto a producción real
   multi-frontera (`docs/MEJORAS.md` #62, diferido), Redis para rate
   limiting distribuido (#61, diferido), o un piloto de campo con
   cooperativas — trabajo de *operación*, no de *desarrollo inicial*.

**Lo que no se puede afirmar:** que ninguna herramienta de asistencia se
usó durante el desarrollo — esta sección mide escala y estructura del
resultado, no el proceso de escritura línea por línea.

---

## 24. Registro simplificado: nombre + teléfono + PIN de 4 dígitos

**Decisión:** el registro/login dejaron de pedir usuario técnico + email +
contraseña de 8+ caracteres. Ahora piden: **nombre** (acepta espacios,
sigue siendo la clave de login internamente), **número de celular** (en
vez de email) y un **PIN de 4 dígitos** (en vez de contraseña).

**Motivo — fricción mínima para pruebas, no una decisión de producción:**
inventar un usuario+email+contraseña de 8 caracteres es fricción real al
probar el sistema repetidamente (demos, pruebas manuales, defensa). El
teléfono además es un dato que el sistema ya necesita del ciudadano para
coordinar la recolección — pedirlo en el registro no agrega un campo
nuevo, solo lo adelanta.

**A favor:**
- Menos campos, menos fricción para probar el flujo completo repetidas veces.
- El teléfono es dato real del dominio (ya se usa para rastreo de invitados).
- Consistente con la contraseña mínima de 3-8 caracteres ya aceptada como
  tradeoff de MVP (§4) — este es el mismo tipo de decisión, más explícita.

**En contra — límites que no se pueden ignorar:**
- **Un PIN de 4 dígitos (10.000 combinaciones) no es apto para producción
  real.** Sin límite de intentos más agresivo que el actual (`LoginAttemptService`,
  3 intentos/15 min ya existente) sería fuerza-bruteable en un sistema real
  con más usuarios.
- El nombre como clave de login puede colisionar (dos "Juan Pérez") —
  aceptable para pruebas, no para escala real.
- El email dejó de pedirse; el índice único de `email` en Mongo se volvió
  `sparse` para permitir múltiples usuarios sin email (ver
  `MongoIndexMigration.java`, `docs/MEJORAS.md` #123).

**Para producción:** volver a exigir contraseña real (8+ caracteres,
`UserService.validatePassword` ya lo hace para el flujo de edición de
perfil, sin usar todavía desde ninguna pantalla) y considerar 2FA por SMS
ya que el teléfono ya se captura.

**Umbral concreto para revisar esto — decisión explícita, no "algún día":**
no vale la pena invertir en registro robusto (contraseñas fuertes, límites
de intentos más estrictos, verificación de teléfono) hasta tener **~50
usuarios reales** (no de prueba/demo). Antes de eso, la fricción de un
registro robusto cuesta más de lo que previene. Las organizaciones sí
mantienen email (siguen siendo pocas y verificadas manualmente por ahora).

## 25. Visor embebido de diagramas UML en vez de links a XML crudo

**Decisión:** los links "UML" del footer temporal (§22/#127 en
`docs/MEJORAS.md`) no abren el `.drawio` crudo — abren `/docs/diagramas`,
una página que renderiza los 5 diagramas con el script oficial
`viewer-static.min.js` de draw.io.

**Por qué no la alternativa obvia (`https://www.draw.io/?lightbox=1#U<url>`):**
esa URL depende de que el servidor de draw.io haga *fetch* cross-origin al
`.drawio` en `localhost:8080`, algo que puede fallar en silencio por CORS y
que no se puede verificar de forma confiable sin un navegador real. La
solución elegida evita el problema por completo: `DocsController` lee el
XML del disco del lado del servidor y lo pasa embebido (como JSON en un
atributo `data-mxgraph`) directamente en el HTML; el script de draw.io solo
lo renderiza, no necesita pedirle nada a este servidor. CSP `script-src`
se amplió con `https://viewer.diagrams.net` para permitir ese script.

**Mismo tripwire que #127:** es parte del mismo footer temporal, se saca
junto con el resto antes de que un usuario real vea el sitio.

## 26. Centralización de secretos vía `.env` + `.env.example`

**Decisión:** una única fuente de verdad para credenciales (`SPRING_DATA_MONGODB_URI`,
`MONGODB_DATABASE`, `UPLOAD_DIR`): un archivo `.env` en la raíz (gitignored),
cargado automáticamente por Spring Boot vía
`spring.config.import=optional:file:.env[.properties]` en `application.properties`.
`.env.example` (sí versionado) documenta las claves esperadas con valores de
ejemplo, sin secretos reales. En producción (Render) las mismas claves se
configuran como variables de entorno del servicio, no como archivo — Render
las inyecta al proceso y Spring las toma igual vía `${...}`.

**Por qué:** `application-dev.properties` tenía la connection string de
MongoDB Atlas hardcodeada y **committeada en git** (usuario + password reales
en texto plano, filtrados en el historial). Al rotar esa credencial en Atlas
tras detectar el leak, se corrigió la causa raíz en vez de solo cambiar el
valor: ahora ningún archivo versionado puede contener una credencial real,
porque no hay dónde escribirla salvo `.env` (ignorado) o el dashboard del
host. `application-dev.properties` ya no tiene `spring.data.mongodb.uri`
propio — hereda el de `application.properties`, que a su vez viene de `.env`.

**Consecuencia asumida:** cada desarrollador nuevo necesita copiar
`.env.example` → `.env` y pedir la credencial real por un canal aparte (no
git) antes del primer `mvn spring-boot:run`. Es fricción de onboarding a
cambio de no volver a filtrar credenciales por accidente.
