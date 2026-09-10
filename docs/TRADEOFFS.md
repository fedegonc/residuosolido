# Tradeoffs — Decisiones de Diseño y sus Consecuencias

Este documento registra las decisiones de diseño más importantes del
MVP **Residuo Sólido**, con su justificación y las consecuencias
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

## 8. Notificaciones WhatsApp (mock)

**Decisión:** `NotificationService` solo loggea, no envía WhatsApp
real.

**A favor:** no requiere API key, no gasta créditos, no expone
números.

**En contra:** el ciudadano no recibe notificación real de cambio de
estado.

**Para producción:** integrar la API oficial de WhatsApp Business
(Meta) o un proveedor (Twilio, 360dialog).

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
193 tests pasando. Un refactor de controllers a una semana de la
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
- Script inline en `<head>` aplica el tema antes de pintar (sin flash).

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

## 19. CI/CD con GitHub Actions (no solo Render.com)

**Decisión:** agregar un pipeline de CI en GitHub Actions que corre
tests, PMD, JaCoCo y un smoke test de Docker en cada push/PR, además
del deploy automático que ya hace Render.com.

**A favor:**
- Validación antes del deploy: si los tests fallan, Render.com no
  recibe código roto.
- Reportes de cobertura (JaCoCo) y análisis estático (PMD) como
  artifacts descargables.
- Smoke test del Dockerfile detecta problemas de build antes de
  producción.
- MongoDB service container en CI replica el entorno real.

**En contra:**
- Duplica el tiempo de feedback (local + CI).
- MongoDB en CI suma ~30s al pipeline (startup del service container).
- PMD tiene 68 violaciones con `failOnViolation=false` — el pipeline
  no bloquea por PMD (ver §20).

**Para producción:** activar `failOnViolation=true` cuando se
reduzcan las violaciones de PMD a cero.

---

## 20. PMD con failOnViolation=false

**Decisión:** PMD corre en el build y en CI, pero no bloquea el
build si encuentra violaciones (`failOnViolation=false`).

**A favor:**
- El proyecto tiene 68 violaciones heredadas; bloquear el build
  detendría el desarrollo.
- PMD sigue generando reportes útiles para análisis.

**En contra:**
- Las violaciones pueden acumularse sin consecuencia.
- El CI no valida calidad estática, solo compilación y tests.

**Para producción:** migrar a PMD 7 (maven-pmd-plugin 3.28), revisar
las 68 violaciones, activar `failOnViolation=true` cuando lleguen a
cero.

---

## 21. Versiones del stack — staying en Spring Boot 3.2 / Java 17

**Decisión:** el MVP se entrega con Spring Boot 3.2.0 y Java 17,
aunque existen Spring Boot 4.1.1 y Java 25 LTS.

**A favor:**
- Spring Boot 3.2 es estable, documentado y compatible con todas
  las dependencias del proyecto.
- Java 17 es LTS (soporte hasta septiembre 2029).
- Migrar a Spring Boot 4.0 implica 115 breaking changes (42 rompen
  compilación, 24 fallan en runtime, 19 dan resultados incorrectos
  silenciosamente). `@MockBean` se elimina, Spring Security tiene
  un DSL rewrite, properties se renombran.

**En contra:**
- El tribunal puede cuestionar el uso de versiones no actuales.
- Se pierden mejoras de rendimiento y seguridad de Java 21/25.
- Spring Boot 3.2 llega a fin de soporte OSS en diciembre 2026.

**Para producción:** migrar primero a 3.5.x (limpiar deprecations),
después a 4.0 (migración mayor). Subir Java a 21 (safe, sin breaking
changes). Documentado en `docs/MEJORAS.md` items 63-68.
