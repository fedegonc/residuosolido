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

## 7. Panel de acopio: tablero Kanban por estado

**Decisión actual:** `/acopio/solicitudes` muestra un tablero Kanban con
cuatro columnas (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `REJECTED`). Cada
solicitud se representa como una tarjeta dentro de su columna. Las
estadísticas de estado del encabezado actúan como anclas a cada columna.
Las acciones (aceptar/rechazar/completar) se hacen desde el detalle de
la solicitud.

**Por qué se eligió el Kanban:** en las pruebas de concepto la lista
filtrable resultaba menos comprensible para usuarios no técnicos: el
estado quedaba disperso y había que cambiar filtros para ver el flujo
completo. El Kanban agrupa por estado de un vistazo, reutiliza los
cards del listado de ciudadano y mantiene un único modelo mental para
ambos perfiles.

**Diseño técnico:**
- Fragmento reusable `fragments/kanban.html` con `kanban-column` y
  `kanban-card`, usado tanto en `/acopio/solicitudes` como en
  `/mis-solicitudes`.
- Controladores agrupan las solicitudes en `requestsByStatus` (mapa
  `Map<RequestStatus, List<Request>>`) en vez de pasar una sola lista
  filtrada.
- CSS propio en `app.css` con flexbox horizontal en desktop y apilado
  vertical en mobile.

**Lo que se pierde:**
- Cada columna solo muestra el subconjunto cargado por paginación; si
  hay más de 20 solicitudes por estado se requiere paginación propia o
  "cargar más".
- El listado compacto en tabla era más denso para organizaciones con
  muchas solicitudes; el Kanban consume más altura por elemento.

**Alternativas consideradas:**
| Opción | Por qué sí/no |
|---|---|
| **Kanban con cards (elegida)** | Mejor comprensión del flujo de estado; reutiliza componentes; unifica UX de ciudadano y organización |
| Lista filtrable compacta | Más densa y escalable en volumen, pero obliga a cambiar filtros para ver el estado global |
| Drag-and-drop para mover tarjetas entre columnas | Alto valor visual, pero requiere endpoints REST y más interacción JS; no aporta al MVP porque las transiciones siguen hechas desde el detalle |

**Para producción:** si una organización supera ~20 solicitudes activas
simultáneas, agregar paginación por columna o un toggle lista/Kanban.

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
| Frontend — CSS (`app.css`+`fonts.css`) | 2 | 287 |
| Frontend — JS vanilla | 3 | 180 |
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
  real.** Sin límite de intentos más agresivo que el actual (`RateLimiter`,
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

## 27. Íconos: sprite SVG local en vez de icon font (Font Awesome)

Los 39 íconos de la app se sirven desde `static/images/icons.svg` — un sprite
de `<symbol>`s extraídos del propio jar de Font Awesome — referenciados con
`<svg class="icon"><use href="/images/icons.svg#{id}"/></svg>`.

**Por qué sprite y no otra alternativa:**

| Alternativa | Por qué no |
|---|---|
| Font Awesome webjar (status quo) | ~100KB de CSS + ~1MB de webfonts por 39 íconos de ~2.000; un request extra por página |
| Otra icon font (Bootstrap Icons, Material Symbols) | Mismo modelo, mismo peso — cambia el logo, no la arquitectura |
| Iconify / runtime JS | Una dependencia JS para inyectar markup estático — contradice el objetivo de mínimo JS |
| SVG inline en fragment `ui::icon(name)` | Funciona, pero los `<path>` ilegibles quedan en cada página; con `use` el path vive en un archivo cacheable |
| Emoji | Inconsistente entre OS y sin equivalentes serios para `clipboard-list`/`route` |

**Ventajas concretas:** cero JS, cero fuentes, CSP intacto (asset same-origin),
cacheable, hereda `currentColor`, y el grep `icons.svg#` da el inventario de
íconos usados. El costo: cada `<use>` externo hace un fetch la primera vez
(cacheado después) y los ids se escriben a mano — mitigado extrayendo los
símbolos de FA para que el nombre del id coincida con el `fa-*` original.

El path `/images/**` se eligió porque ya está en `permitAll` de
`SecurityConfig` — `/img/` habría requerido tocar el filter chain.

## 28. `App.java` de un archivo: el núcleo del sistema como herramienta de análisis

**Contexto:** después de varios bugs de "capa conectiva" (ternary en
`th:attr`, selectores CSS huérfanos, claves i18n stale) surgió la pregunta
de cuánto del sistema es dominio real y cuánto es plomería de framework.

**Qué se hizo:** `App.java` en la raíz del repo — una UI de consola que
**importa las clases reales del sistema** (`javac -cp "target/classes:$(cat
cp.txt)" App.java`). No es un espejo ni una copia: llama a
`PhoneNumber.normalize`, `Request.accept/reject/complete`,
`Request.assignOrganization`, `ServerMessage`, y a las validaciones puras
de `RequestService` (instanciado con repositorios `null` — solo se usan los
métodos que no tocan persistencia). Lo único propio es lo que el contexto
legítimamente reemplaza: persistencia en `Map`/`List` y UI con `Scanner`.

**Para qué sirve:** validar casos del dominio en segundos (¿puedo completar
una PENDING? ¿qué pasa si la org no acepta ese material?) sin levantar la
app ni Mongo, y demostrar académicamente que el núcleo no depende del
framework — si una regla del dominio agarrara dependencia de Spring, esta
clase dejaría de compilar: es el test de pureza del núcleo. Un espejo
habría introducido el mismo drift que §29 eliminó; reusar las clases hace
la divergencia imposible por construcción.

**Conclusión extraída:** todos los bugs recientes vivieron en el tejido
(contratos implícitos entre capas), ninguno en el núcleo. La unidad del
sistema no se mejora fusionando archivos sino haciendo los contratos
explícitos y verificables (grep-able, testeados en sus paths de error).

## 29. Claves i18n server-side tipadas: `ServerMessage` enum + excepciones `Keyed`

**Contexto:** siguiendo §28, el núcleo ya era puro pero las ~60 claves i18n
que emite el servidor viajaban como strings crudos (`throw new
IllegalArgumentException("error.request.city_required")`). El contrato
Java→JSON era convención, no verificable — el drift ya había picado dos
veces (claves `login.*` stale, `mat_*` inexistentes).

**Alternativas:**

| Opción | Por qué sí/no |
|---|---|
| Strings + convención (status quo) | Cero costo de escribir, costo infinito de verificar — el typo compila |
| Un enum por tipo (`ErrorCode` + `FlashKey`) — opción descartada | Más "correcto" semánticamente, pero dos listas para un solo contrato |
| **`ServerMessage` único (elegida)** | Una lista = un contrato; `code()`/`serverKey()` encapsulan la convención `_server_`; el nombre no miente: son *mensajes* del servidor, no solo errores |

**Excepciones:** en vez de un `DomainException` único, tres clases que
**extienden los tipos JDK existentes** (`ValidationException`→
`IllegalArgumentException`, `StateException`→`IllegalStateException`,
`OwnershipException`→`SecurityException`) e implementan `Keyed`. Así los
`catch` y el `@ExceptionHandler` existentes no cambian — el tipo sigue
despachando, el enum solo garantiza la clave.

**Ganancia medida:** el refactor encontró 7 bugs latentes (5 throws que el
patrón no cubría + 5 claves sin traducción JSON que se habrían mostrado
crudas). `ServerMessageContractTest` hace el drift imposible: agregar una
constante sin traducción rompe el build.

## 30. Landing cards: server-side rendering (Thymeleaf) en vez de REST API + fetch

**Contexto:** la primera implementación de las landing cards (contenedor
escalable en el home, ver `docs/MEJORAS.md` #178) fue un endpoint
`GET /api/landing-cards?lang=` que devolvía JSON, más `landing-cards.js`
haciendo `fetch` + render en el DOM al cargar la página.

**Alternativas:**

| Opción | Por qué sí/no |
|---|---|
| REST API + fetch client-side (descartada, se implementó y se sacó) | Requiere reglas de seguridad propias (`permitAll` sobre `/api/**`, que además colisiona con el matcher genérico de la API autenticada); si JS falla o no corre, el contenedor queda vacío sin ningún fallback |
| **Server-side rendering con Thymeleaf (elegida)** | El HTML llega completo en la respuesta inicial — funciona sin JavaScript, sin request adicional, sin reglas de seguridad nuevas (la ruta ya es pública). El modelo (`AuthController.rootOrIndex()`) hace `model.addAttribute("cards", LandingCardLoader.loadCards(lang))`, el fragment itera con `th:each` |

**Costo real encontrado:** `LandingCardLoader.loadCards()` atrapa cualquier
`Exception` del parseo JSON y devuelve `List.of()` en silencio — un JSON
malformado (una coma de más) no tira ningún error, el contenedor
simplemente queda vacío. El bug se vivió en esta misma feature: JSON con
coma final antes del `]`, cards invisibles en el navegador, cero mensaje de
error en ningún log. La capa de tests (`LandingCardsTest.JsonDataLayer`)
existe específicamente para atrapar esto en CI en vez de en el navegador.

## 31. Página de contenido por slug (`/pagina/{slug}`) en vez de un endpoint fijo por página

**Contexto:** la página "Sobre los catadores" empezó como
`@GetMapping("/sobre-catadores")` fijo en `AuthController`. Con el sistema
de landing cards ya pensado para crecer (#178: 9 cards), cada card que
ganara una página propia habría significado un `@GetMapping` nuevo, una
entrada nueva en `SecurityConfig` y un `Routes.XXX` nuevo — repetido por
cada página. Terminaron siendo 9 páginas reales de una (ver #179), lo que
confirmó rápido que el registro fijo por página no iba a escalar.

**Alternativas:**

| Opción | Por qué sí/no |
|---|---|
| Un `@GetMapping` fijo por página (status quo) | Cero indirección, pero no escala: cada página nueva = 1 método + 1 regla de seguridad + 1 constante en `Routes`. Con 9 páginas ya hubieran sido 9 de cada cosa |
| **`PageController.showPage(@PathVariable slug)` + `PageContentLoader` (elegida)** | Una sola ruta (`Routes.PAGE_BY_SLUG = "/pagina/{slug}"`), un solo permiso en `SecurityConfig`, un solo template genérico (`public/page-content.html`). Agregar una página = 1 entrada en `pages-{es,pt}.json`, sin tocar rutas, seguridad ni Java. El `slug` es el mismo `id` que ya traen las cards del JSON — no es un concepto nuevo, es el mismo identificador reutilizado |
| Contenido 100% en Mongo, sin JSON en disco | Sobre-ingeniería para contenido que cambia poco y no lo edita un usuario final — el JSON versionado en git ya da historial de cambios gratis |

**Consecuencia de diseño:** un slug sin entrada en `SLUG_TEMPLATES` devuelve
404 real (`ResponseStatusException`), no una redirección disfrazada. Esto
expuso un bug en `GlobalExceptionHandler`: no existía un
`@ExceptionHandler(ResponseStatusException.class)`, así que el 404 caía en
el catch-all de `Exception` y redirigía a `/entrar` — comportamiento
incorrecto detectado por `LandingCardsTest$RenderingLayer#unknownSlug_returns404`
antes de llegar a producción, no después.

## 32. Sandbox multi-archivo con puertos (`scratch/sim/`) vs. un solo `App.java`

**Contexto:** `scratch/App.java` había crecido a ~1600 líneas y, aunque ya
fluía de general a específico, reproducía la estructura antigua
models→services→tests: las entidades `User`/`Request` estaban declaradas
después de los servicios que las usaban, no existían puertos (los servicios
recibían `Map` crudos) y había un modo consola interactivo que nadie usaba.

**Alternativas:**

| Opción | Por qué sí/no |
|---|---|
| Un solo archivo reorganizado por niveles | Conserva `java scratch/App.java` en un paso, pero el límite hexagonal queda solo en comentarios — nada impide que un servicio vuelva a tocar un `Map` |
| **Multi-archivo por nivel + interfaces de puerto (elegida)** | `scratch/sim/`: `Domain.java` (enums, `PhoneNumber`, `User`, `Request`), `Ports.java` (`UserRepository`/`RequestRepository`/`CityOrgPort` + impls `InMemory*`), `Services.java` (`Auth`, `Registration`, `Profiles`, `Images`, `RequestService`), `Harness.java` (`Check`+`Fixtures`), `Scenarios.java`, `Collisions.java`, `Landing.java`, `App.java`. Los servicios compilan contra las interfaces — el límite es estructural, no declarativo. Pedagógicamente muestra la dirección de dependencias completa (dominio→puertos←adapters) |
| Interfaces también en la app real | YAGNI: los repositorios Spring Data ya son interfaces; agregar otra capa para una sola implementación no paga. Las interfaces acá existen porque el sandbox *demuestra* la arquitectura, no porque el diseño las exija en producción |

**Costo aceptado:** `java App.java` (single-file launcher) NO compila archivos
hermanos — el run pasa a `javac -d bin *.java && java -cp bin App`, empaquetado
en `scratch/sim/run.sh` para que siga siendo un solo comando.

**Decisión sobre el modo consola:** eliminado (~150 líneas de menús/`Scanner`).
No aportaba sobre la especificación ejecutable: los escenarios ya ejercitan
cada regla del dominio en segundos y la vista pública la sirve Spring. Cada
línea extra en el sandbox es otra cosa que mantener sincronizada a mano con el
dominio real — mismo criterio que #182 (borrar lo que no se usa).

**Resultado:** 86 PASS / 0 FAIL, 44/44 error-keys, 33/33 branches (misma
checklist que el archivo único) + matriz de colisiones sin cambios de
comportamiento. El hexágono queda demostrable archivo por archivo en la
defensa.

## 33. Notificación in-app (bandeja Mongo) vs. email/SMS al aceptar o rechazar

Contexto: el usuario pidió "que le llegue un mensaje" al ciudadano cuando la
organización acepta o rechaza su solicitud. El canal define la arquitectura:

| Opción | Por qué sí/no |
|---|---|
| **Bandeja in-app persistida en Mongo (elegida)** | Cero infraestructura nueva: reusa SSR + Spring Data. La notificación es un dato del dominio (colección `notifications`: user, requestId, `NotificationType`, `confirmedSlot`, `read`, `createdAt`) — queda historial auditable, badge de no-leídas en navbar vía `@ModelAttribute` global, y la página `/notificaciones` marca todo como leído al listar (las recién vistas se sellan "nueva" en esa carga) |
| Email | `User.email` es opcional — la mayoría no lo tiene. SMTP agrega credenciales, deliverability y un canal asíncrono sin valor si el usuario ya entra a la app. DIFERIDO |
| SMS/WhatsApp sobre `guestPhone` | Único canal que alcanzaría al **invitado** (sin cuenta, sin bandeja). Requiere gateway pago (Twilio u otro). El contrato ya quedó modelado en el sandbox (`NotificationPort`, recipient = `user.id` o `guestPhone`) — el adapter externo queda preparado pero DIFERIDO; el invitado sigue consultando estado por teléfono + código de rastreo |

**Invariante de orden:** `RequestService` notifica DESPUÉS del save de la
request — si el save falla por locking optimista (dos actores sobre la misma
solicitud), no se notifica un estado que nunca se persistió. El test
`acceptRequest_concurrentConflict_doesNotNotify` lo fija.

**Decisión sobre el alcance:** solo `ACCEPTED`/`REJECTED` notifican. La org no
recibe notificación por solicitud nueva (ya tiene su panel `/acopio/solicitudes`
con filtro PENDING — duplicaría señal) y `COMPLETED` tampoco notifica (la
franja confirmada ya se comunicó al aceptar; completar es el cierre esperado).

**Resultado:** feature validada primero en `scratch/sim` (`NotificationPort` +
3 branches nuevos: `notify.accepted.citizen`, `notify.accepted.guest`,
`notify.rejected.guest` → 89 checks, 36 branches) y portada a Spring: `Notification`,
`NotificationRepository`, `NotificationService`, `NotificationController`,
`GlobalModelAttributes.unreadNotifications` (tolerante a sesión stale → null),
link + badge en navbar (desktop, menú usuario, mobile), `users/notifications.html`,
i18n es/pt. Tests: 252 no-browser.

## 34. Fail-fast + mensaje claro vs. circuit breaker para fallos de Mongo

Contexto: sin timeouts explícitos el driver de Mongo usa sus defaults
(`serverSelectionTimeout` 30s) — si Atlas no responde, cada request que toca
la base queda colgado 30s. La pregunta es cuánta infraestructura de
resiliencia vale la pena para este backend.

| Opción | Por qué sí/no |
|---|---|
| **Timeouts cortos + retry a nivel driver + mensaje de servicio no disponible (elegida)** | Con una sola instancia (Render) y un solo backend (Atlas), no hay a dónde hacer failover ni qué aislar con un breaker — el único valor real es fallar rápido (5s en vez de 30s) y no confundir al usuario ("algo está roto" vs. "el servicio no está disponible, reintentá"). `retryWrites`/`retryReads` ya cubren el caso real y frecuente: un failover de primary en Atlas durante mantenimiento programado |
| Circuit breaker (Resilience4j) | Tiene sentido cuando hay múltiples instancias/réplicas y se quiere evitar que todas golpeen un backend caído a la vez, o cuando hay un fallback real (caché, degradar funcionalidad). Acá no hay ninguna de las dos cosas — agregar el patrón sería complejidad sin beneficio, el tipo de sobre-ingeniería que no compra menos riesgo real |
| Reintentos con backoff a nivel aplicación (Spring Retry) | Redundante con `retryWrites`/`retryReads` del driver para el caso común (failover transitorio); para una caída real y sostenida de Atlas, reintentar más solo alarga el tiempo hasta el error sin cambiar el resultado |

**Resultado:** `MongoResilienceConfig` (timeouts) + `GlobalExceptionHandler
.handleMongoUnavailable` (mensaje + log distinguible de un bug de código). El
health check de Mongo en `/actuator/health` ya lo daba gratis Spring Boot
Actuator — no hizo falta agregar nada ahí. Ver `docs/MEJORAS.md` #201.

## 35. Sin API REST pública: Spring MVC + Thymeleaf SSR de punta a punta

Contexto: `OrgApiController` (eliminado, ver más abajo) exponía `GET
/organizaciones?ciudad=&material=` como JSON, con anotaciones OpenAPI
completas (`@Operation`, `@ApiResponse`, `@Parameter`, `@Schema`) — presentaba
al sistema como si tuviera una API REST de verdad. Verificado: **cero
consumidores reales** (ni JS ni templates lo llamaban, solo su propio test
unitario, también eliminado). El endpoint que el formulario usa de
verdad ya existía y hacía lo mismo de forma nativa a SSR: `RequestCreateController
.orgOptionsForCity` (`GET /solicitudes/org-options`) devuelve un fragmento
Thymeleaf (`fragments/ui :: options`), no JSON — Vanilla JS reemplaza el
`<select>` con el HTML que llega, sin parsear nada.

| Opción | Por qué sí/no |
|---|---|
| **Borrar el endpoint JSON, dejar solo el fragment SSR (elegida)** | Es simplemente el mismo dato servido dos veces con dos paradigmas distintos, y el que sobra (JSON) no tiene un solo consumidor. Mantenerlo era deuda de percepción: alguien leyendo el código asumiría que hay una API REST pensada para terceros, cuando nunca la hubo |
| Renombrar y reusar como endpoint "interno" | Se evaluó (`OrganizationOptionsController` en `/solicitudes/org-options`) pero esa ruta ya estaba tomada por el endpoint SSR real — hubiera chocado el mapping de Spring. Renombrar algo que no se usa no lo vuelve útil, solo lo esconde mejor |
| Mantener ambos (JSON + HTML) | Dos formas de pedir lo mismo, sin ningún consumidor real para una de ellas. Superficie de mantenimiento (tests, docs, seguridad) por cero beneficio |

**Efecto en cascada:** el ahora eliminado `OrgApiController` era el único
controller con anotaciones `@Operation`/`@ApiResponse`/`@Schema` de springdoc
— al borrarlo, Swagger UI (`/swagger-ui.html`) quedaba documentando una API
vacía. Se removió la dependencia `springdoc-openapi-starter-webmvc-ui`
completa, el `@OpenAPIDefinition` de `ResiduoSolidoApplication`, las rutas
`SWAGGER_V3`/`SWAGGER_UI`/`SWAGGER_HTML` de `Routes`/`SecurityConfig`, y el
link a Swagger del hub (`src/main/resources/templates/docs/hub.html`). También
se eliminó la ahora innecesaria `OrganizationDto` (solo la usaba el
controller eliminado) y su test.

**Resultado:** Spring MVC + Thymeleaf SSR sin excepciones — ni un endpoint
JSON de cara a un consumidor externo, ni una dependencia que sugiera lo
contrario. `docs/ARQUITECTURA.md` y `docs/ENDPOINTS.md` actualizados. Ver
`docs/MEJORAS.md` #205.

## 36. Notificación desacoplada del camino crítico vía evento + `@Async` (sin `@TransactionalEventListener`)

Contexto: `RequestService.acceptRequest`/`rejectRequest` tenían `@Transactional`
y llamaban directo a `NotificationService.notifyRequester(...)` en el mismo
hilo HTTP, después del `save()`. La intención original era correcta (no
notificar un estado que no se persistió), pero la implementación tenía dos
problemas verificados, no supuestos:

1. **`@Transactional` no hacía nada.** Verificado empíricamente (test de
   sondeo, ver `docs/MEJORAS.md` #208): 0 beans `PlatformTransactionManager`
   en todo el contexto, `RequestService` no es un proxy AOP transaccional.
   La anotación era decorativa desde que se agregó.
2. **La notificación bloqueaba el hilo HTTP.** Si la escritura de la
   notificación tardaba (o la base estaba lenta), el usuario que acepta/
   rechaza una solicitud esperaba por una escritura que no debería afectar
   la respuesta de su acción principal.

| Opción | Por qué sí/no |
|---|---|
| `@TransactionalEventListener(phase = AFTER_COMMIT)` | Era el plan original — **descartado al verificar que no hay transacción real de la cual colgarse.** Sin un `PlatformTransactionManager`, el evento nunca se publicaría (no hay commit que dispare la sincronización). Habría roto las notificaciones en silencio |
| Configurar un `MongoTransactionManager` real primero | Correcto a mediano plazo (le daría sentido real a `@Transactional` en todo el proyecto, no solo acá), pero es un cambio de mayor alcance — afecta semántica de escritura en todo el codebase, necesita revisión propia. Fuera de alcance de este ítem puntual |

**Efecto colateral honesto:** `@Transactional` se **removió** de `acceptRequest`/
`rejectRequest`/`completeRequest` en vez de dejarlo como decoración. Cada
método sigue siendo seguro porque hace una sola escritura a un único
documento Mongo (atómica por diseño del motor) — la garantía que necesitan
hoy no requiere una transacción distribuida.

**Resultado:** `RequestStatusChangedEvent` (record) + `NotificationEventListener`
hilos). `RequestService` ya no depende de `NotificationService` — el punto de
extensión para email/SMS declarado en §33 ahora es "agregar otro
`@EventListener`", no "tocar `RequestService`". Ver `docs/MEJORAS.md` #208.

## 37. Deuda técnica diferida: de "documentada" a "con trigger medible"

Contexto: `TRADEOFFS.md` documenta bien el *por qué* de cada decisión
diferida, pero ninguna tenía una condición objetiva de cuándo deja de ser
aceptable. "Cache diferido" o "PIN provisorio" sin fecha ni umbral tienden a
volverse permanentes por inercia, no por decisión.

**Triggers agregados** (a cada ítem correspondiente, no solo acá):

| Decisión diferida | Trigger objetivo | Cómo se mide |
|---|---|---|
| ~~Cache en `CityOrgService.getOrganizationsByCity`~~ | **Implementado** (ver §41) — se adelantó sin esperar el trigger, costo casi nulo (`@Cacheable` + `@EnableCaching`, sin dependencia nueva) | — |
| Autenticación PIN "provisoria" (`fragments/forms::pin`, comentario explícito en el HTML) | Antes de que el primer usuario real complete un registro en producción — no "cuando haya tiempo" | Gate manual: revisar antes de anunciar el sistema a usuarios reales, no una métrica automática |
| Escalado horizontal / imágenes en disco local (§ imágenes locales, `ARQUITECTURA.md`) | Segunda instancia de Render, o `LocalImageService` supera el disco disponible del tier actual | Alerta de disco vía `/actuator/health` → `diskSpace.status` (ya expuesto) |
| Redundancia de la instancia (single point of failure, ver §34) | Primer incidente real de downtime reportado por un usuario, o SLA formal comprometido | No medible preventivamente — es un gate de "primera vez que duele de verdad" |

**Resultado:** la deuda técnica documentada pasa de ser una lista de "sabemos
que esto es una limitación" a una lista con una condición verificable de
cuándo se vuelve prioridad — usando la instrumentación que ya existe
(`/actuator/metrics`, `/actuator/health`) en vez de agregar herramienta nueva.
Ver `docs/MEJORAS.md` #209.

## 38. Extraer `OrganizationProfile` embebido en `User` (con migración de datos reales)

Contexto: `User` modela ciudadano y organización en la misma colección
(§ mono-modelo, `ARQUITECTURA.md`). De los campos que solo tienen sentido
para organización, solo 2 son genuinamente exclusivos: `acceptedMaterials`
y `profileCompleted` — `city` resultó ser compartido (lo usa también
`CityAwareLocaleResolver` para ciudadanos), corrigiendo una imprecisión del
análisis de arquitectura original.

El riesgo real no era el código, era el **dato ya persistido en Atlas**:
mover estos campos a un subdocumento `organizationProfile` en el modelo Java
sin migrar los documentos existentes los habría dejado leyendo `[]`/`false`
silenciosamente — cualquier organización real que ya tuviera materiales
aceptados los habría "perdido" al primer deploy.

| Opción | Por qué sí/no |
|---|---|
| **Extraer + migrar con `CommandLineRunner` idempotente (elegida)** | Mismo patrón ya probado en `MongoIndexMigration` (que resolvió un problema real de índice roto). Opera con BSON crudo, no con el mapper de `User` — evita el problema de "leer con el modelo nuevo antes de migrar". Corre una vez, después es no-op |
| Convertirlo en trigger diferido (documentar, no migrar todavía) | Válido si el dolor fuera bajo, pero acá había un ciudadano-cero: no había ningún caso donde "no migrar" fuera más seguro que "migrar" — los datos existentes de organización son pocos (seed de desarrollo) y el patrón de migración ya estaba probado en el proyecto |
| Partir `User` en 2 colecciones (una nueva para organización) | Cambio de mucho mayor alcance — reescribe queries, relaciones (`Request.organization` referencia un `User`), y tests. Desproporcionado para 2 campos; el embebido resuelve la asimetría real sin ese costo |

**API pública de `User` sin cambios:** `getAcceptedMaterials()`/
`setAcceptedMaterials()`/`getProfileCompleted()`/`setProfileCompleted()`
siguen existiendo con la misma firma, ahora delegando a
`organizationProfile` (lazy-init en el setter). Ningún caller — servicios,
templates Thymeleaf, 8 archivos de test — necesitó cambiar.

**Verificación real, no solo mocks:** `OrganizationProfileMigrationTest`
inserta un documento con la forma vieja directo en Mongo real (Atlas),
corre la migración, confirma el subdocumento nuevo y que los campos viejos
desaparecieron (`$unset`), y prueba idempotencia (correrla 2 veces no rompe
nada). Limpia el documento de prueba después — no ensucia la base
compartida.

**Resultado:** `OrganizationProfile` (embebido, no `@Document` propio) +
`OrganizationProfileMigration`. Ver `docs/MEJORAS.md` #210.

### Trabajo futuro: split completo a 2 colecciones (no implementado, solo planeado)

Si el dominio de organización crece lo suficiente (múltiples sedes, horarios
de recolección, empleados con acceso propio), el embebido de §38 deja de
alcanzar y el paso siguiente es partir en dos colecciones reales, no solo
en un subdocumento:

```
User (identidad/auth — colección "users")
  ├── id, username, email, password, phone, firstName
  ├── role: USER | ORGANIZATION
  ├── active, createdAt
  └── organizationProfileId (nullable, referencia)

OrganizationProfile (operación — colección nueva "organization_profiles")
  ├── id, userId
  ├── city, acceptedMaterials, profileCompleted
  └── (futuro: sedes, horarios, empleados)
```

**Por qué NO es simplemente herencia con `@Document`:** en Mongo/Spring Data,
una jerarquía con discriminador sobre `@Document(collection = "users")`
sigue guardando todo en la misma colección — no es un split real, es lo
mismo con otro nombre. La diferencia real es dos colecciones + una
referencia (`organizationProfileId` o `@DocumentReference`).

**Alcance si se hace:** repositorio nuevo para el perfil de organización;
`CityOrgService` consulta esa colección en vez de `UserRepository`
filtrando por rol/ciudad; `UserRegistrationService` crea ambos documentos
al registrar una organización; `RequestService` referencia el perfil (o su
`userId`) en vez del `User` completo; templates cambian `organization.city`/
`organization.acceptedMaterials` por el equivalente del perfil separado;
Spring Security no cambia nada (sigue cargando `UserDetails` desde
`UserRepository`, el rol sigue en `User.role`). Migración: mismo patrón que
`OrganizationProfileMigration` de §38, pero moviendo el subdocumento
embebido a un documento independiente con referencia, no al revés.

**Por qué no se hace ahora:** es un cambio de alcance mediano-alto (modelo +
repositorios + servicios + controllers + templates + migración) para un
dolor que hoy no existe — la organización sigue siendo "una cuenta con
ciudad y materiales aceptados". El embebido de §38 ya resuelve la asimetría
real sin ese costo. **Trigger objetivo para revisitar** (mismo criterio que
§37): el día que una organización necesite más de un operador, más de una
sede, o el perfil embebido crezca a 4+ campos exclusivos.

## 39. Tests de integración aislados de la base real (bug encontrado sembrando datos)

Contexto: al sembrar datos de prueba manualmente en Atlas para pruebas de
carga (`scratch/mongo/SeedTestData.java`, ver `docs/MEJORAS.md` #211),
los datos **desaparecieron** después de correr `mvn clean test`. Investigado:
`MongoAggregationUtilsIntegrationTest` y `PlaywrightBaseTest` (vía
`BrowserTestSeed`) usaban `@SpringBootTest(properties = {"spring.data
.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb..."`
— la intención era "si no hay env var, usar un Mongo local descartable". El
problema real: `SPRING_DATA_MONGODB_URI` **siempre** está seteada en este
proyecto (vía `.env`), así que el fallback local nunca se usa — el test
corría contra la Atlas real compartida (`fedelabs`) y hacía `deleteAll()`
de `users`/`requests` en cada `@BeforeEach`, borrando en silencio cualquier
dato real que hubiera.

**Esto probablemente explica** por qué la base estaba casi vacía (1 usuario,
0 solicitudes) las primeras veces que se inspeccionó con `scratch/mongo
/MongoDump.java` — no es que nunca hubiera datos reales, es que cada
corrida de tests los borraba.

| Opción | Por qué sí/no |
|---|---|
| **`spring.data.mongodb.database` explícito, distinto al real (elegida)** | Cada test de integración que necesita una colección "limpia" para empezar (agregaciones, seed de browser tests) declara su propia base dedicada (`residuosolido_test_aggregation`, `residuosolido_test_browser`) en el mismo cluster Atlas. Sigue siendo Mongo real (no mock, no embebido) — solo aislado del dato compartido |
| Mongo embebido (Flapdoodle) o Testcontainers | Más correcto en abstracto (aislamiento total, sin depender de red/Atlas), pero es una dependencia nueva y un cambio de infraestructura de testing más grande — desproporcionado para arreglar 2 archivos con un problema puntual y bien entendido |
| Dejar el fallback como estaba, documentar el riesgo | Ya estaba "documentado" implícitamente en el propio código (`${VAR:default}` sugiere que el default se usa alguna vez) — pero en la práctica nunca se cumplía, así que era una falsa sensación de seguridad. No corregirlo dejaba el bug activo |

**Resultado:** 2 archivos corregidos (`MongoAggregationUtilsIntegrationTest`,
`PlaywrightBaseTest`) con `spring.data.mongodb.database` explícito. Barrido
completo de los 18 archivos de test que referencian `SPRING_DATA_MONGODB_URI`
confirmó que ningún otro hace escrituras destructivas contra el repositorio
real sin acotar por `_id` (`OrganizationProfileMigrationTest`, escrito ayer,
ya lo hacía bien desde el principio). **Verificado con datos reales, no
teoría:** se sembraron 25 usuarios + 23 solicitudes, se corrió la suite
completa (458 tests), y los datos sobrevivieron intactos — antes del fix,
la misma corrida los borraba a 1 usuario + 0 solicitudes. Ver
`docs/MEJORAS.md` #212.

## 40. Rediseño del panel de organización: panel de control + informe PDF

Contexto: inspeccionando el panel de organización recién sembrado con datos
reales (#211), el feedback fue directo — la UX estaba "malísima", sin
estadísticas reales (sentía "estar en cero"), estructurado como "una fila de
cosas" en vez de un panel de control, con demasiado scroll e "islas"
visuales sin relación entre sí. Se pidió además una forma de descargar un
informe en PDF, calificada como "fácil".

**(a) Stats-como-filtro en vez de stats-y-filtros separados.** El layout
anterior tenía 3 tiles de conteo (decorativos, no clicables) MÁS una fila
de 5 botones de filtro debajo (mismo estado, representado dos veces, sin
vínculo visual). Se unificaron en 5 tarjetas (`org-panel__stats`): cada
una ES el conteo Y el link de filtro, con color de borde/texto igual al de
`.badge--*` para que el ojo asocie panel↔badge sin aprender una paleta
nueva, y estado activo marcado con `border-color`.

| Opción | Por qué sí/no |
|---|---|
| **5 tarjetas clicables unificadas (elegida)** | Elimina la duplicación conteo/filtro, reduce la altura total de la página (menos elementos, más densos), reutiliza colores ya existentes — cero tokens de diseño nuevos |
| Mantener tiles + filtros separados, solo agregar el conteo de rechazadas | Resuelve el gap de datos pero no el problema de UX reportado ("fila de cosas", "islas") — hubiera sido un parche sobre el síntoma equivocado |
| Gráfico (barras/dona) en vez de números | Más "dashboard" visualmente, pero es una dependencia nueva (librería de charts) o SVG hecho a mano para un dato de 4-5 categorías que un número grande ya comunica sin ambigüedad — desproporcionado para el problema real |

**(b) Gap de datos real encontrado en el camino:** el filtro "Rechazadas"
existía en la UI pero `MongoAggregationUtils.countByStatusFaceted` nunca
calculaba ese facet — solo se sumaba (sin desglosar) dentro de `total`.
Se agregó el facet `rejected` al único `$facet` (agregar un segundo stage
`$facet` separado pisa el resultado del primero — ver comentario en el
propio método) y se expuso `rejectedCount`/`allCount` en el controller.
Esto cambió el contrato de `RequestMetricsService` compartido por
`getUserRequestStats` (usado en `users/requests.html`, no tocado en esta
tarea) — de 4 a 5 keys ahí, de 3 a 4 en `getOrgRequestStats`. Corregir esto
"a ojo" (sin correr la suite) hubiera dejado pasar 4 tests rotos: 2 por
conteo exacto de keys en `RequestMetricsServiceTest`, y 2 en
`EndToEndFlowsTest` que mockeaban el mapa viejo sin `rejected` → NPE al
unboxear un `Long` `null` → 404 real en vez del 200 esperado. Se encontró
corriendo `mvn clean test` después del cambio, no asumiendo que agregar una
key a un `Map` es siempre inofensivo.

**(c) Tabla compacta en vez de cards apiladas.** Cada solicitud pasó de una
card de 6-7 líneas (fecha, estado, contacto, teléfono, dirección, botón,
padding generoso) a una fila de grid de 5 columnas (`org-panel__row`,
`6.5rem 6rem 1fr 1fr 8rem`) con header. Mismo dato, una fracción de la
altura — es la medida principal contra "sin tanto scroll". En mobile
(`max-width:768px`) colapsa a 2 columnas con `grid-template-areas`, sin
header (no aporta apilado).

**(d) Informe PDF vía impresión del navegador, no generación server-side.**

| Opción | Por qué sí/no |
|---|---|
| **`window.print()` + `@media print` (elegida)** | Cero dependencias nuevas (ni iText, ni OpenPDF, ni wkhtmltopdf), cero endpoint nuevo, cero mantenimiento de un segundo template renderer. "Guardar como PDF" es un destino nativo de todo diálogo de impresión moderno. `@media print` oculta nav/footer/stats/botones y deja título+tabla — el mismo dato ya filtrado en pantalla |
| Generación server-side (iText/OpenPDF) | Da un archivo `.pdf` real descargable sin pasos manuales del usuario y control total del layout impreso, pero agrega una dependencia nueva, un servicio nuevo, y duplica en Java el layout que ya existe en Thymeleaf+CSS — desproporcionado para "un informe con la tabla que ya se ve en pantalla" |
| Librería JS de export a PDF en cliente (ej. jsPDF) | Evita el backend pero agrega una dependencia JS nueva (el proyecto es vanilla JS a propósito) solo para re-implementar en el cliente lo que el navegador ya ofrece gratis vía `Ctrl+P` → Guardar como PDF |

Se eligió la opción sin dependencias porque el pedido explícito fue "eso es
fácil" — la lectura correcta de esa frase es "no hace falta construir un
generador de PDF", no "hacer un generador de PDF simple". Si en el futuro
se necesita branding/paginación fija no controlable por CSS de impresión
(ej. reporte con encabezado corporativo en cada página, exportable sin
intervención del usuario), ese es el trigger para revisitar con generación
server-side.

**(e) `org-panel.css` como archivo separado** de `app.css`, por ser la
primera página del proyecto con densidad visual/CSS propio de esa
magnitud — mismo criterio ya usado para JS por página (`static/js/{página}.js`
vs `app.js`, ver manifiesto en `app.js`). Cargado solo en `org/requests.html`.

**(f) Dos bugs de framework reales encontrados verificando contra el
sistema real (no asumidos):**
- **Thymeleaf:** `th:each` + `th:replace` parametrizado en el mismo
  `<th:block>` producía `SpelEvaluationException` y HTML malformado (un
  `DOCTYPE` de error anidado). Fix: separar en `<div th:each>` contenedor +
  `<th:block th:replace>` hijo — mismo patrón que ya usaba `request-item-card`
  sin que nadie lo hubiera documentado como obligatorio.
- **thymeleaf-layout-dialect:** el CSS se duplicaba en el `<head>` renderizado
  porque el dialect auto-copia al decorador cualquier elemento del `<head>`
  del contenido que no esté en un `layout:fragment`, pero si además se
  envuelve explícitamente en uno, se inserta una segunda vez. Fix: el
  `<link>` va como hijo directo de `<head>`, sin wrapper — el auto-merge lo
  inserta una sola vez. Verificado con curl contra el proceso real
  (`grep -c "org-panel.css"` 2→1), no con lectura de código nada más.

Ver `docs/MEJORAS.md` #213. Suite completa verificada dos veces (una por
bug) hasta 458/458, 0 failures.

## 41. Limpieza post-Kanban + `@Cacheable` + Bean Validation (alcance acotado)

Contexto: tras el tablero Kanban (#213/kanban), auditoría de "qué Spring
se está desaprovechando" (rutas: no aplica, ya son constantes simples;
integraciones: no aplica, es un monolito sin sistemas externos) señaló dos
huecos reales: caché declarativo y Bean Validation, cero uso de ambos en
todo el proyecto.

**(a) Limpieza de dead code post-Kanban.** El tablero reemplazó tanto la
tabla (`org-panel__table`/`__row`/`__cell--*`) como el filtro por query param
(`?estado=`, `currentStatus`) del rediseño anterior (#213), dejando huérfanos:
`fragments/request-list.html` completo (los dos fragments que tenía,
`request-item-card` y `request-item-row`, sin una sola referencia en ningún
template — se borró el archivo entero), el atributo de modelo `allCount` en
`OrgRequestController`, y ~40 líneas de CSS (`.org-panel__stat--active` y
toda la sección de tabla) en `org-panel.css`. **Bug real encontrado
limpiando:** el `@media print` (la función de informe PDF) seguía
apuntando a `.org-panel__row`, que ya no existe — el botón "Descargar
informe PDF" no iba a ocultar nada del Kanban ni a adaptar su layout
horizontal con scroll (pensado para pantalla) a una página impresa. Se
reescribió el bloque para convertir `.kanban-board` de flex-con-scroll a
apilado vertical solo en `@media print`.

**(b) `@Cacheable` en `CityOrgService.getOrganizationsByCity`.** Trigger de
§37 no esperado a propósito — el costo de implementarlo ahora es casi cero
(`@EnableCaching` + una anotación, sin `spring-boot-starter-cache`: Spring
Boot cae al `ConcurrentMapCacheManager` por defecto si no hay otro provider
en el classpath). Invalidación: `@CacheEvict(allEntries=true)` en
`UserService.updateUser` — único punto donde cambian ciudad/materiales/
teléfono de una organización ya persistida; `allEntries=true` en vez de
evictar solo la ciudad nueva porque el método no conoce la ciudad ANTERIOR
si el update la cambia, y el cache son 2 entradas (RIVERA/LIVRAMENTO) —
evictarlo entero no tiene costo real. **Verificado con contexto real de
para `@Async`/`@Transactional` en #208): un test con `new CityOrgService(mock)`
plano NUNCA hubiera detectado si `@Cacheable` fuera decorativo, porque el
cacheo lo aplica el proxy que arma `@EnableCaching`, no el objeto en sí.

**(c) Bean Validation — alcance deliberadamente acotado, no una migración
completa.** Se evaluó migrar `RequestValidator.validateCoreFields`
(complejidad ciclomática 12, el caso que motivó la pregunta) pero se
descartó por ahora:

| Opción | Por qué sí/no |
|---|---|
| **Migrar solo `RegistrationForm` (elegida)** | Es el único form del proyecto que YA tiene un DTO de binding (`@ModelAttribute RegistrationForm`, con Lombok) — agregar `@Valid` + anotaciones es un cambio de 3 archivos (DTO, controller, 1 dependencia nueva), cero DTOs nuevos que crear |
| Migrar `RequestValidator`/`RequestCreateController` completo | Requiere crear un DTO de formulario que hoy no existe (recibe `@RequestParam` sueltos), más el teléfono ahí es compuesto (país+nacional+DDD resuelto recién en el controller) y la validación de "materiales aceptados por la organización" es una regla cruzada que Bean Validation no resuelve con una anotación simple — es un refactor de 9-10 pasos tocando 3+ controllers y toda su cobertura de tests, no una tarde |
| Migrar `OrgProfileController` | Mismo problema del teléfono compuesto — el campo que hoy se valida como "requerido" (`resolvedPhone`) es un valor derivado, no un `@RequestParam` crudo anotable |

Con `RegistrationForm` se migró lo genuinamente simple: forma de `username`
(`@NotBlank`, `@Size(max=64)`) y `password`/PIN (`@Pattern(regexp="\\d{4}")`).
Teléfono (compuesto) y unicidad de `username` (necesita el repositorio)
siguen validándose en `UserRegistrationService.validateUserRegistration`
exactamente igual que antes — Bean Validation es una capa adicional que
falla más rápido para los casos simples, no un reemplazo.

**Mensajes de error sin depender del interpolador de Bean Validation:**
en vez de confiar en que Spring Boot conecte el `MessageInterpolator` de
`LocalValidatorFactoryBean` con `JsonMessageSource` (afirmación que no se
verificó y que este proyecto ya sabe que no debe asumirse, ver
`CLAUDE.md`), cada anotación usa el código `ServerMessage` tal cual como
`message` (ej. `message = "error.register.username_required"`), y el
controller resuelve ese string con `Messages.msg(String)` (helper nuevo,
misma resolución que ya usa `Messages.msg(ServerMessage)` para todo lo
demás). Cero mecanismo nuevo que aprender o que pueda romperse en
silencio — reutiliza el único camino de i18n que ya existe en el proyecto.
**Verificado con el texto real**, no solo "existe el atributo": los tests
de `AuthControllerTest` comprueban `errorMessage` == "Necesitamos tu
nombre." / "El PIN debe tener 4 dígitos." (la traducción real de
`es.json`), no solo que el campo no sea null — si la resolución del código
estuviera rota, esos tests hubieran mostrado el código crudo y lo habrían
detectado.

Suite completa: 463/463, 0 failures. Ver `docs/MEJORAS.md` #216.

## 42. Registro: errores por campo y autenticación inmediata

**Decisión:** un registro válido inicia sesión automáticamente y navega al
área correspondiente al rol. Un registro inválido vuelve al mismo formulario,
conserva nombre, teléfono, país y tipo de cuenta, limpia solamente el PIN y
muestra el mensaje junto al campo que debe corregirse.

**Por qué:** obligar a repetir nombre/teléfono o pasar inmediatamente por un
segundo formulario de login no agrega seguridad: el usuario acaba de demostrar
que conoce el PIN al crear la cuenta. Sí agrega fricción y hace que un error
común —por ejemplo, un dígito extra en el celular— parezca un reinicio de la
aplicación.

| Opción | Decisión |
|---|---|
| **Autenticar después de persistir (elegida)** | Reutiliza `AuthenticationManager`, rota el ID de sesión y guarda el `SecurityContext`; mantiene exactamente las reglas del login normal |
| Redirigir a `/entrar` | Más simple, pero obliga a repetir credenciales sin aportar una verificación nueva |
| Conservar también el PIN cuando hay error | Reduce una repetición, pero vuelve a renderizar o retener un secreto; se descartó y solo se conservan campos no sensibles |
| Solo alerta global | Comunica que algo falló, pero no dónde ni cómo corregirlo; se mantiene como resumen y se agrega error contextual por campo |

El teléfono sigue validándose server-side con `PhoneNumber`; el controller
traduce sus errores tipados a `BindingResult.rejectValue("phoneNational", ...)`.
La sesión no se crea manualmente como una identidad arbitraria: las mismas
credenciales pasan por `AuthenticationManager`, y recién el resultado
autenticado se almacena en sesión. Ver `docs/MEJORAS.md` #220.

---

## 43. Separación User / Organization: auth y perfil de negocio en colecciones distintas

**Decisión:** el modelo mono-`User` (campos de ciudadano + perfil de organización en un solo documento) se separó en `User` (autenticación y contacto básico) y `Organization` (perfil de negocio: ciudad, teléfono, materiales aceptados, onboarding).

**Por qué:** mezclar en una sola colección obligaba a que la mitad de los campos de cada documento fueran semánticamente vacíos, dificultaba reportes limpios de organizaciones y complicaba la integridad de datos (por ejemplo, `acceptedMaterials` solo tiene sentido para ORGANIZATION). La separación resuelve directamente el AMBIGUO del Bloque 3 (`USER.completeProfile()`): el campo `profileCompleted` deja de existir para ciudadanos porque solo es una precondición de negocio para organizaciones.

| Opción | Decisión |
|---|---|
| **A — separar solo el perfil de negocio, mantener `User` para auth (elegida)** | `Organization` vive en su propia colección; `User` conserva rol ORGANIZATION y el enlace es por `userId`. Costo acotado y no se duplica autenticación. |
| B — dos identidades independientes (`User` y `Organization` con login propio) | Modelo conceptualmente más puro, pero duplicaría contacto, lógica de login y roles; costo mucho mayor y sin beneficio real para el MVP. |
| C — mantener mono-`User` | Menor refactor inmediato, pero perpetúa la ambigüedad y hace más caro cualquier futuro panel de verificación/admin de organizaciones. |

**Costo real:** migración de datos existentes. Para no invalidar referencias, `Organization._id` coincide con `User._id`. La migración (`OrganizationProfileMigration`) es idempotente y opera con BSON crudo, por lo que no depende del mapper actual durante la transición. MongoDB standalone no soporta transacciones multi-documento: crear `Organization` + limpiar `User` se hace en dos `bulkWrite` separadas; si falla entre ambas, una segunda ejecución al arranque se recupera (la org ya existe → solo se limpia el documento residual).

**Implicancias de seguridad:** `SecurityConfig` sigue usando `hasRole("ORGANIZATION")` sobre `User.role`; la existencia de un `Organization` no cambia la autorización. `OrgProfileController` y `OrgRequestController` resuelven primero el `User` autenticado y luego su `Organization` por `userId`; si un usuario ORGANIZATION no tiene registro asociado, se maneja como perfil incompleto.

**Datos cruzados JavaScript:** el selector de organizaciones en `/solicitar` sigue recibiendo materiales aceptados a través de `data-materials` generado por `Organization.getAcceptedMaterialsCsv()` — se mantiene el contrato explícito, sin depender de `toString()`.

Suite completa: 475/475, 0 failures. Ver `docs/MEJORAS.md` #226.

---

## 44. Normalización case-insensitive del username

**Decisión:** los usernames se almacenan y comparan en forma canónica (`trim + lowercase`). El login se resuelve con `Username.canonical(raw)`, por lo que "Juan", "juan" y "  juan  " apuntan a la misma cuenta. El registro rechaza duplicados canónicos.

**Por qué:** evita cuentas duplicadas por diferencia de mayúsculas y hace consistentes el registro y el bloqueo por intentos fallidos (`RateLimiter`), que ya usaba `toLowerCase()` manualmente. Sustituye ese `toLowerCase()` local por una única función de dominio.

| Opción | Decisión |
|---|---|
| **Canonicizar al guardar y al buscar (elegida)** | Simple, consistente, no requiere índices con collation. |
| Mantener display original + índice case-insensitive | Más fiel al nombre que escribe el usuario, pero requiere crear un campo `usernameCanonical` o un índice con collation y complica la unicidad. |
| Solo canonicizar en login | No resuelve duplicados en registro. |

**Costo real:** la normalización es una sola línea (`trim().toLowerCase(Locale.ROOT)`), pero cambia el contrato de usernames existentes. Se agregó `UsernameNormalizationMigration` para convertir usernames viejos al arranque; si hay duplicados silenciosos preexistentes, la migración loggea el error y los deja intactos en vez de romper el arranque. En un entorno con datos reales, esos conflictos deben resolverse manualmente una sola vez.

Suite completa: 476/476, 0 failures. Ver `docs/MEJORAS.md` #228.

---

## 45. Asunción de nodo único para rate limiting, uploads y notificaciones

**Decisión:** el MVP asume una única instancia corriendo en Render. `RateLimiter` usa `ConcurrentHashMap` en memoria, `LocalImageService` escribe en `uploads/` del filesystem local, y `NotificationService` emite la notificación en el mismo hilo después del save de la request.

**Por qué:** elimina infraestructura externa (Redis, S3, cola de mensajería) para un flujo de pruebas y demo. Los costos de integración y operación superan el valor para una app académica.

| Opción | Decisión |
|---|---|
| **In-memory / local (elegida)** | Cero infra extra; sirve para desarrollo y un solo nodo en producción. |
| Redis + Bucket4j + S3/Cloudinary + outbox | Correcto para multi-node, pero agrega dependencias, configuración y costo que el MVP no necesita. |

**Costo real:** con más de una réplica o filesystem efímero, el rate limit/lockout por usuario se duplica por nodo, las imágenes pueden perderse y las notificaciones pueden no persistirse si el proceso muere entre el save de la request y el save de la notification. Esto está documentado como limitación en `docs/ARQUITECTURA.md` y aceptado para el MVP.

Suite completa: 476/476, 0 failures. Ver `docs/MEJORAS.md` #228.

---

## 46. Eliminar `RequestStateMachine`: verificar la máquina de estados real, no la envoltura

Contexto: auditoría de "máquinas de estados y mecanismos de invariantes ya existentes" (grep de patrones `enum.*Status`/`StateMachine` como texto de búsqueda, no como cita de clase) encontró que la máquina de estados REAL de `Request` vive en `enums/RequestStatus.java` (`transitionAccept/Complete/Reject()`, cada uno lanza `StateException` si la transición es inválida) — no en el `@Component` que se eliminó, agregado originalmente en #194/#195.

**Lo que se verificó, no se asumió:** `RequestStateMachine.accept(request, slot)` chequeaba `request.getStatus() != PENDING` y lanzaba `StateException` ANTES de llamar a `request.accept(slot)` — que internamente vuelve a chequear exactamente lo mismo vía `status.transitionAccept()`. Mismo patrón en `reject`/`complete`. Además, `grep -rn "stateMachine\." src/main/java/` confirmó que los 5 métodos `canEdit/canDelete/canAccept/canComplete/canReject` no tenían NINGÚN caller — todo el código (templates, `RequestService`) siempre usó `request.canBeEdited()`/`canBeDeleted()` directo.

| Opción | Por qué sí/no |
|---|---|
| **Eliminar `RequestStateMachine`, llamar `request.accept/reject/complete()` directo desde `RequestService` (elegida)** | El invariante ya está enforced en `RequestStatus` — la envoltura no agregaba una segunda línea de defensa real, agregaba una duplicación textual del mismo chequeo y 5 métodos que nadie llamaba. Menos código, mismo comportamiento observable (verificado: 501/501 tests, incluida una matriz exhaustiva nueva) |
| Mantener el wrapper eliminado como "capa de dominio explícita" | Argumento válido en abstracto (separar la máquina de estados de la entidad), pero en este código concreto la entidad (`Request`) YA delega a `RequestStatus` — el wrapper no era esa capa, era redundante sobre una capa que ya existía |
| Conectar los 5 métodos muertos a algo real (usarlos desde los templates en vez de `request.canBeEdited()`) | Hubiera sido "arreglar" código muerto dándole un propósito artificial — cambiar los templates para usar un servicio inyectado en vez de un método directo del modelo no aporta nada, es la abstracción prematura que el resto de esta sesión evitó |

**Lo que se agregó para no perder cobertura real:**
- `RequestStatusTransitionMatrixTest` — matriz exhaustiva 4 estados × 3 transiciones (12 casos), contra `RequestStatus` directo (la fuente de verdad), no contra el wrapper eliminado.
- `RequestStatusSandboxSyncTest` — hallazgo colateral de la misma auditoría: `scratch/sim/Domain.java` declara en un comentario "portado tal cual de RequestStatus.java" pero nada lo verificaba automáticamente (scratch/ no es source root de Maven, no se puede importar desde `src/test`). El test lee ambos archivos como texto, extrae la tabla de transiciones semántica de cada uno (qué estados de origen habilita cada `transitionX()`, a qué destino) y las compara — tolerante si `scratch/` no existe (gitignoreado). Verificado que detecta divergencia real: se rompió a propósito una condición del sandbox, el test falló mostrando el diff exacto, se restauró la condición, volvió a pasar.

Suite completa: 501/501, 0 failures (más 1 falla preexistente de un doc de Devin sin relación, no tocada). Ver `docs/MEJORAS.md` #221.

## 47. `TrackingCode.canonical`: búsqueda de invitados case-insensitive

Bug real encontrado al auditar "cada concepto con formato propio tiene su clase canonical": los códigos de seguimiento de invitado se generan en mayúsculas, pero `RequestService.getGuestRequests` solo hacía `trackingCode.trim()`. Un invitado que copiaba su código a mano en minúsculas (`abc123` vs `ABC123` almacenado) obtenía lista vacía en silencio en `/rastrear` — mismo patrón que el bug de username resuelto en §44, pero en el canal de lectura anónimo.

| Opción | Por qué sí/no |
|---|---|
| **Value object `TrackingCode.canonical` (trim + uppercase `Locale.ROOT`) en el punto de lookup (elegida)** | El bug ya se manifestó — criterio de la regla corregida: value object cuando el costo de no tenerlo se materializó, no como política general. Simétrico con `Username.canonical`/`PhoneNumber` |
| `.trim().toUpperCase()` inline sin clase | Mismo comportamiento, pero pierde el punto único de la regla y la simetría con los otros identificadores canonicalizados |
| Query Mongo case-insensitive (regex/collation) | Más cara y frágil: el código almacenado ya es uppercase, el problema es solo la entrada del usuario — canonicalizar la entrada es más simple que relajar el índice |

Sin migración: los códigos guardados ya son uppercase; solo la entrada se canonicaliza. Sandbox (`scratch/sim`) sincronizado con el mismo contrato y un check `getGuestRequests.lowercaseCode`. Ver `docs/MEJORAS.md` #229.
