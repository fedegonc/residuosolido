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
| Testing | 181 tests: unitarios, integración, seguridad, autorización |
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
seguimiento.

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
3. Entrar como organización (`coopverde` / `1234`).
4. Aceptar la solicitud → estado cambia a IN_PROGRESS.
5. Completar la solicitud → estado cambia a COMPLETED.
6. Mostrar una operación rechazada por una regla de negocio.
7. Mostrar que una organización no puede ver solicitudes ajenas.

Preparar capturas o un video de respaldo por si la demo falla.

### 4.6 Evaluación (3 min)

| Dimensión | Evidencia presentada |
|---|---|
| Correctitud funcional | 181 tests ligados a requisitos |
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
| "¿Por qué hay un `sw.js`?" | "Heredado del PWA descartado (#11/#100) y retomado mínimo en #143: en `activate` limpia los caches heredados del SW viejo (que interceptaba *todos* los fetches) y luego es un fetch handler pass-through — lo mínimo que Chrome exige para instalabilidad. No cachea nada: offline la app falla igual que sin SW" |
| "¿Las notificaciones de WhatsApp son reales?" | "No. No hay notificaciones en el MVP; el teléfono queda registrado en la solicitud" |
| "¿La contraseña de 8 caracteres es suficiente?" | "Es defendible para un MVP. Una política de producción exigiría complejidad (mayúsculas, números, símbolos) y rotación" |
| **Familia conectividad — "¿Funciona con poca conectividad / offline?"** | "Con conectividad lenta sí: SSR entrega HTML completo en 1 request y los assets son mínimos (~640 líneas de CSS+JS, sin frameworks) — tolera redes lentas mejor que una SPA que debe descargar el bundle antes de renderizar. Con cero conectividad no funciona: es decisión deliberada, no descuido — la PWA completa se implementó y se descartó tras un incidente real (#11/#23: SW pre-cacheando HTML dinámico → usuarios con contenido stale por horas; SW huérfanos interceptando todos los fetches, `/sw.js` detrás de auth → 302 a login; kill-switch #100; instalabilidad mínima reincorporada en #143). Offline-first real pondría `IndexedDB` como fuente de verdad — pero el código de rastreo, la validación de organizaciones y el rate limiting nacen server-side: es una reescritura del modelo, no una feature" |
| **Familia conectividad — "¿Qué pasa si se corta la conexión a mitad de uso?"** | "Falla limpio: la escritura de solicitud es atómica en Mongo (existe completa o no existe — un corte no deja estado corrupto), la sesión sobrevive (cookie local), y no hay estado atado a conexión viva (sin websockets). Huecos conocidos y declarados: (a) el form se pierde si el submit falla — la cola en `IndexedDB` con retry en evento `online` lo cubre; (b) si el POST llegó pero la respuesta se perdió, un reintento duplica la solicitud — se cubre con `clientRequestId` + índice único. Ambos son trabajo aditivo sobre el caso feliz, no correctivo" |

---

## 6. Lo que NO afirmaría

- Que aumenta el reciclaje o los ingresos de las organizaciones.
- Que es la primera plataforma de reciclaje.
- Que tiene seguridad de producción (CSP usa `unsafe-inline`).
- Que funciona offline. La app es instalable (#143: manifest + SW
  pass-through), pero el `sw.js` no cachea nada — sin conexión falla
  igual que sin SW. La PWA completa con cache se descartó por stale-cache
  (`docs/MEJORAS.md` #11/#23/#100).
- Que **todos** los tests E2E son de navegador real: `EndToEndFlowsTest`
  sigue siendo MockMvc (stack simulado, sin navegador). Sí hay 6 clases
  con Playwright/Chromium real (`browser/*BrowserTest`) que cubren los
  flujos principales de ciudadano, organización e invitado — sería
  incorrecto decir que no hay tests de navegador real, pero también
  incorrecto decir que toda la suite E2E lo es.
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
| PWA completa descartada; instalable mínimo sin offline | Resuelto/histórico | `docs/MEJORAS.md` #11, #100, #143 |
| Mayoría de tests E2E son MockMvc | Parcialmente resuelto | 6 clases Playwright reales, `docs/MEJORAS.md` §2.3 |
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
## Documentos relacionados

- `docs/TRADEOFFS.md` — las 26 decisiones de diseño citadas en esta guía (§1-§26).
- `docs/LIMITACIONES.md` — limitaciones reconocidas y fuera de alcance.
- `docs/REQUISITOS.md` — catálogo RF/RN.
