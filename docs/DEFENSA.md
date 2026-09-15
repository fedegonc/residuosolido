# Defensa de Tesis — Eco Solicitud

Guía para la defensa del proyecto final del Tecnólogo en Análisis y
Desarrollo de Sistemas (UTEC / IFSUL). Este documento estructura el
argumento, la demostración, las preguntas esperadas y los límites de lo
que se puede afirmar.

---

## 1. Título sugerido

> **Eco Solicitud: análisis, diseño e implementación de un sistema web
> para gestionar solicitudes de recolección de reciclables en Rivera y
> Sant'Ana do Livramento.**

---

## 2. Argumento central

> "Convertí una necesidad de coordinación en un proceso de software
> explícito, implementado y verificable. Conozco qué resuelve, qué
> evidencia lo respalda y qué todavía no puedo afirmar."

El aporte no es un algoritmo novedoso ni una plataforma terminada. Es
un proceso de coordinación modelado, implementado y verificado, con sus
reglas, permisos y limitaciones explícitas.

---

## 3. Competencias de TADS demostradas

El [perfil de egreso de TADS](https://utec.edu.uy/es/educacion/carrera/tecnologo-en-analisis-y-desarrollo-de-sistemas/)
incluye especificar requisitos, modelar software, desarrollar y testear
aplicaciones, garantizar calidad y elaborar documentación técnica.

| Competencia TADS | Evidencia en el proyecto |
|---|---|
| Análisis de requisitos | 8 RF, 14 RN, 3 actores, casos de uso (`docs/DIAGRAMAS.md`) |
| Modelado | Entidades, relaciones, estados, multiplicidades (`docs/diagrams/`) |
| Desarrollo | Flujo completo ciudadano → organización → seguimiento |
| Testing | 218 tests: unitarios, integración, seguridad, autorización |
| Calidad | JaCoCo, validación server-side, optimistic locking |
| Documentación | 11 docs técnicos, 4 diagramas UML, endpoints catalogados |
| Gestión del proyecto | Iterativo-incremental en 4 fases, tradeoffs documentados |
| Seguridad y auditoría | CSRF, roles, session fixation, CSP, rate limiting |
| Comunicación técnica | Diagramas y documentación coherentes con el código |

---

## 4. Estructura de la exposición (20-30 min)

### 4.1 Problema y relevamiento (3 min)

> "Se propone centralizar la información necesaria para solicitar una
> recolección y conocer su estado."

Distinguir:
- **Necesidad constatada:** respaldada por observación del contexto
  territorial (Rivera · Sant'Ana do Livramento).
- **Hipótesis de diseño:** una necesidad que se asumió y todavía falta
  validar con un piloto.

**No afirmar** que se entrevistaron cooperativas o que hoy pierden
solicitudes si no hay esa evidencia.

### 4.2 Alcance (2 min)

**Dentro:** solicitudes, selección de organización, estados, permisos,
seguimiento, métricas públicas, blog.

**Fuera:** optimización de rutas, GPS, pagos, notificaciones reales,
medición de impacto ambiental.

### 4.3 Análisis y diseño (5 min)

Mostrar el flujo:

```
Ciudadano o invitado
  → crea solicitud
  → selecciona organización
  → organización acepta o rechaza
  → si acepta, puede completar
  → ciudadano consulta el estado
```

Explicar tres reglas que demuestran análisis del dominio:

1. Una organización no puede modificar solicitudes ajenas.
2. Una solicitud no puede completarse directamente desde pendiente.
3. El seguimiento de invitados requiere teléfono y código, no solo
   teléfono.

### 4.4 Decisiones técnicas (3 min)

| Decisión | Argumento defendible |
|---|---|
| Spring Boot + Thymeleaf | Renderizado server-side, menos piezas de despliegue |
| Capas y servicios | Separación entre presentación, reglas y persistencia |
| MongoDB | Persistencia documental; PostgreSQL también válido |
| Bloqueo optimista (`@Version`) | Detecta conflictos concurrentes sin bloquear |
| Render.com (PaaS) | Depliegue simple; AWS es el siguiente paso si escala |
| Sin GPS/mapas | MVP enfocado en el flujo de solicitudes |

No justificar MongoDB con geolocalización o grandes volúmenes: el MVP
no demuestra esas necesidades.

### 4.5 Demostración (5 min)

Demo corta con datos sintéticos claramente identificados:

1. Crear una solicitud como invitado (sin cuenta).
2. Guardar su código de seguimiento.
3. Entrar como organización (`coopverde` / `12345678`).
4. Aceptar la solicitud → estado cambia a IN_PROGRESS.
5. Completar la solicitud → estado cambia a COMPLETED.
6. Mostrar una operación rechazada por una regla de negocio.
7. Mostrar que una organización no puede ver solicitudes ajenas.

Preparar capturas o un video de respaldo por si la demo falla.

### 4.6 Evaluación (3 min)

| Dimensión | Evidencia presentada |
|---|---|
| Correctitud funcional | 218 tests ligados a requisitos |
| Autorización | Tests de acceso con roles incorrectos |
| Integración | Flujo con aplicación y base de datos reales |
| Interfaz | Verificación en navegador, móvil, idiomas |
| Usabilidad | No realizada — reconocer como trabajo futuro |
| Impacto ambiental | No medido — requiere piloto posterior |

**Tests pasando no equivalen a usabilidad validada ni a impacto
ambiental.** Decirlo explícitamente.

### 4.7 Limitaciones y trabajo futuro (2 min)

Ver sección 7 de este documento.

---

## 5. Preguntas del tribunal que prepararía

| Pregunta | Respuesta que sostendría |
|---|---|
| "¿No es simplemente un CRUD?" | "Incluye CRUD, pero el trabajo está en las reglas, permisos, estados, selección de organizaciones y verificación del proceso completo" |
| "¿Qué aporta frente a WhatsApp?" | "Una estructura compartida de solicitudes y estados. No afirmo superioridad en adopción o facilidad sin una comparación con usuarios" |
| "¿Por qué no AWS?" | "La elección responde a necesidades operativas y recursos. Un PaaS puede ser adecuado también en producción; migraría ante requisitos concretos" |
| "¿Dónde está la innovación?" | "En la adaptación e integración para este contexto. No reclamo novedad algorítmica ni ser la primera plataforma de reciclaje" |
| "¿Demostraste que mejora el reciclaje?" | "No. Evalué el software. El impacto requiere un piloto posterior" |
| "¿Usaste IA?" | "Sí, como asistencia de desarrollo y revisión. Declaro su alcance y asumo la responsabilidad de explicar, verificar y corregir el resultado" |
| "¿Por qué MongoDB y no PostgreSQL?" | "Ambos son válidos. MongoDB simplifica el modelado de materiales como array embebido. Documenté el tradeoff" |
| "¿La PWA funciona offline?" | "Se puede instalar, pero HTML no se cachea intencionalmente para evitar contenido stale. Solo CSS/JS/imágenes están disponibles offline" |
| "¿Las notificaciones de WhatsApp son reales?" | "No. No hay notificaciones en el MVP; el teléfono queda registrado en la solicitud" |
| "¿La contraseña de 8 caracteres es suficiente?" | "Es defendible para un MVP. Una política de producción exigiría complejidad (mayúsculas, números, símbolos) y rotación" |

---

## 6. Lo que NO afirmaría

- Que aumenta el reciclaje o los ingresos de las organizaciones.
- Que es la primera plataforma de reciclaje.
- Que tiene seguridad de producción (CSP usa `unsafe-inline`).
- Que funciona offline completo (HTML no se cachea intencionalmente).
- Que los tests E2E son de navegador (son MockMvc con servicios
  simulados).
- Que envía notificaciones reales por WhatsApp (no hay notificaciones en el MVP).
- Que se validó la usabilidad con usuarios reales (no se hizo).
- Que se siguió DSRM desde el inicio (la metodología fue
  iterativo-incremental; DSRM se usa como marco de análisis
  retrospectivo).

---

## 7. Limitaciones y trabajo futuro

### 7.1 Limitaciones del MVP

| Limitación | Estado | Documentación |
|---|---|---|
| CSP con `unsafe-inline` | Aceptada | `docs/MEJORAS.md` §2.1 |
| PWA sin offline completo | Tradeoff intencional | `docs/MEJORAS.md` §2.2 |
| Tests E2E son MockMvc | Aceptada | `docs/MEJORAS.md` §2.3 |
| Notificaciones WhatsApp | Descartada | Eliminadas del MVP; sin proveedor configurado |
| Sin validación de usabilidad | Trabajo futuro | `docs/MEJORAS.md` §2.4 |
| Sin medición de impacto | Trabajo futuro | `docs/MEJORAS.md` §2.5 |

### 7.2 Trabajo futuro priorizado

1. **Piloto con usuarios reales** — ciudadanos y organizaciones de
   Rivera y Sant'Ana. Medir tareas concretas: crear solicitud,
   encontrarla, interpretar su estado, gestionarla.
2. **Notificaciones reales** — integrar WhatsApp Business API o SMS.
3. **Tests de navegador** — Selenium o Playwright para reemplazar
   MockMvc.
4. **CSP con nonces** — eliminar `unsafe-inline` de script-src.
5. **Política de contraseña más fuerte** — complejidad, no solo
   longitud.

---

## 8. Fuentes académicas

### 8.1 UTEC

- [Perfil de egreso TADS](https://utec.edu.uy/es/educacion/carrera/tecnologo-en-analisis-y-desarrollo-de-sistemas/)
- [Plan de carrera TADS](https://utec.edu.uy/es/educacion/carrera/plan-de-carrera-tads/)
- [Primeros egresados TADS (2023)](https://utec.edu.uy/es/noticia/egresan-los-primeros-tecnologos-en-analisis-y-desarrollo-de-sistemas/)

### 8.2 Metodología

- Peffers, K., Tuunanen, T., Rothenberger, M. A., & Chatterjee, S.
  (2007). A Design Science Research Methodology for Information Systems
  Research. *MIS Quarterly*, 32(1), 77–105.
  [DOI: 10.2753/MIS0742-1222240302](https://doi.org/10.2753/MIS0742-1222240302)

### 8.3 Sistemas de reciclaje comparables

- Coelho, Hino & Vahldick (2019). The use of ICT in the informal
  recycling sector: The Brazilian case of Relix.
  [DOI: 10.1002/isd2.12078](https://doi.org/10.1002/isd2.12078)
- Flores et al. (2025). Selective Collection: App Prototype for
  Circular Solid Waste Management in the Amazon.
  [DOI: 10.5753/latinoware.2025.16613](https://doi.org/10.5753/latinoware.2025.16613)
- Cruz, García & Ferreira (2025). Information-management platform for
  autonomous recyclers.
  [DOI: 10.35699/2237-549x.2024.52970](https://doi.org/10.35699/2237-549x.2024.52970)
- Kintschner et al. (2022). Multi-cooperative information system.
  [DOI: 10.29327/sustentare_wipis_2022.525806](https://doi.org/10.29327/sustentare_wipis_2022.525806)
- Espinosa-Aquino et al. (2023). Informal waste management in Latin
  America.
  [DOI: 10.3390/su15031826](https://doi.org/10.3390/su15031826)
- Rossi & Angelo (2024). A reciclagem e seus atores: aproximações entre
  Brasil e Uruguai.
  [Cadernos NAUI, 13(24), 198–222](https://ojs.sites.ufsc.br/index.php/naui/article/view/7252)

### 8.4 Contexto territorial

- [Ministerio de Ambiente de Uruguay — clasificadores](https://www.gub.uy/ministerio-ambiente/inclusionsocialclasificadores)
- [Plan Nacional de Gestión de Residuos](https://otu.opp.gub.uy/gestor/imagesbiblioteca/6beb184b51ad6081e603cfe7e206a66f00f9349f.pdf)
- [Ley 18331 — Protección de datos personales](https://www.impo.com.uy/bases/leyes/18331-2008)

### 8.5 Nota sobre las fuentes

Parte de la comparación se basa en resúmenes y fichas editoriales, no
en todos los artículos completos. Que un resumen no mencione una
función no demuestra que el sistema no la tenga. Tampoco permite afirmar
que este proyecto sea "el primero".

---

## 9. Cierre

> "El aporte no es un algoritmo novedoso ni una plataforma terminada.
> Es un proceso de coordinación modelado, implementado y verificado,
> con sus reglas, permisos y limitaciones explícitas. Lo que sigue es
> un piloto con usuarios reales para medir si esa estructura traduce
> en mejoras concretas."

---

# Tradeoffs — Decisiones de Diseño (anexo)


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

---

## 4. Contraseña mínima de 3 caracteres (fase MVP)

**Decisión:** el mínimo de contraseña es 3 caracteres, no 8.

**A favor:** facilita demostración y pruebas manuales en la defensa.

**En contra:** trivialmente vulnerable a fuerza bruta. **No es
aceptable para producción.**

**Para producción:** restaurar a 8 mínimo (o 12 con políticas OWASP:
complejidad, breach-list check). Una línea en `AccountInput.password()`.

---

## 5. Blog estático (sin CMS)

**Decisión:** el blog es contenido estático en templates Thymeleaf,
no en base de datos.

**A favor:** cero complejidad de backend, cero riesgo de inyección,
rápido de implementar.

**En contra:** no hay editor, no hay panel de administración de
artículos, cambiar contenido requiere editar código y redeployar.

**Para producción:** colección `posts` en MongoDB, editor en panel,
slug único, fecha de publicación, estado borrador/publicado. Las rutas
`/blog` y `/blog/{slug}` ya son compatibles.

---

## 6. Catadores — CRUD latente

**Decisión:** el CRUD de `InformalCollector` existe en backend pero
no se expone en el sidebar de organización.

**A favor:** no mezcla gestión interna con comunicación pública; el
blog reemplaza la exposición con contenido editorial.

**En contra:** código sin uso visible; `Request` no tiene relación
con el recolector responsable (no se puede responder "quién atendió
esta solicitud").

**Para producción:** decidir si el CRUD vuelve como herramienta
interna (asignación de recolector a solicitud, RF-8 completo) o si se
elimina.

---

## 7. Kanban integrado al dashboard (no página aparte)

**Decisión:** el tablero Kanban vive en `/acopio/inicio`, no en una
ruta separada.

**A favor:** menos navegación, la organización ve todo en un solo
lugar, menos código.

**En contra:** el dashboard puede sentirse cargado si crece el número
de solicitudes.

**Para producción:** paginación del Kanban o vista dedicada con
drag-and-drop si el volumen lo justifica.

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
backlog (`RF-RN.md` sección 6).

---

## 13. Compactación del sistema — aplicada

**Decisión:** compactar el sistema para reducir área y carga
cognitiva, priorizando las mejoras de bajo costo y alto impacto.

**Aplicado:**

- **Index fusionado (4 secciones → 2):** hero + cómo funciona en una
  sola sección; blog + CTA cooperativas en otra. El banner de
  cooperativas se integró como `.coop-cta` inline dentro de la sección
  de blog, no como sección separada.
- **Blog en una sola página (3 templates → 1):** `/blog` muestra los
  3 artículos expandidos uno debajo del otro. La ruta `/blog/{slug}`
  se eliminó. Las cards del index linkean a `/blog#slug` (anchor).
- **Padding reducido:** el hero pasó de `2.5rem` a `1.5rem` de
  padding, uniformando con el resto de las secciones.

**A favor:** menos scroll, menos secciones, menos templates, menos
rutas. El visitante ve el contenido completo en menos espacio.

**En contra:** el index tiene menos respiración visual; el blog no
tiene páginas individuales (no se puede compartir un link a un solo
artículo).

---

## 14. Compactación del sistema — diferida

**Decisión:** las siguientes fusiones fueron evaluadas pero
**diferidas** por riesgo de regresión a una semana de la defensa.

**Diferido:**

- **Fusionar request-form + request-edit:** dos controllers
  (`RequestCreateController`, `RequestEditController`) con rutas y
  model attributes distintos. Merging requiere cambiar controllers,
  rutas y tests. Riesgo: alto. Ahorro: ~100 líneas.
- **Fusionar perfiles (user + org + onboarding):** tres controllers
  (`UserProfileController`, `OrgProfileController`,
  `OrgOnboardingController`) con lógica de validación distinta.
  Riesgo: alto. Ahorro: ~150 líneas.
- **Fusionar dashboards (user + org):** el dashboard de organización
  ya tiene Kanban integrado; el de usuario es un subset. Merging
  requiere condicionales por rol y cambios en ambos controllers.
  Riesgo: medio. Ahorro: ~80 líneas.
- **Reducir clases CSS con utilities:** 292 clases únicas, muchas
  reemplazables por utilities (`text-sm`, `mt-1`). Pero implica
  cambiar múltiples templates. Riesgo: bajo pero tedioso. Ahorro:
  ~40 clases.

**Justificación de la postergación:** el sistema está estable con
218 tests pasando. Un refactor de controllers a una semana de la
defensa puede introducir regresiones difíciles de detectar. Las
compactaciones aplicadas (sección 13) ya redujeron el área visible
del sistema sin tocar controllers ni rutas.

**Para después de la defensa:** estas fusiones son el próximo paso
natural de compactación. Cada una se puede hacer de forma aislada
con su propio set de tests.

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
