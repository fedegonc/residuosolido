# Indice de Documentacion — Eco Solicitud

> Fecha de sincronizacion: 2026-09-23 — borrado de peso/volumen estimados y RequestValidator muerto (#182)  
> Tests: 423 no-browser, 7 failures conocidos (`PageContentImagesTest`/`PageContentRenderingTest` sobre `catadores`, pre-existentes #177; browser tests con fallos por selectores desincronizados — ver MEJORAS.md #180)
> Build: SUCCESS (con `-DexcludedGroups=browser`; ver MEJORAS.md #180 para el estado de `*BrowserTest`)

Este documento es el punto de entrada para toda la documentacion del proyecto.  
Los documentos tecnicos estan concentrados en `docs/` y estan actualizados al estado actual del codigo.

**Regla de organizacion:** un documento = un tema, y cada doc entra en una
de dos categorías (ver `docs/MEJORAS.md` #173 para el criterio completo):

- **Canónico** — lo que un miembro del tribunal leería: especificación,
  arquitectura, decisiones de diseño, estado de mejoras, defensa. Un solo
  archivo por tema, sin redundancia entre ellos.
- **Referencia** — material de respaldo (auditorías, historial de
  hardening, análisis de trabajo) que no compite por espacio en la lectura
  principal pero queda versionado y disponible si alguien pide el detalle.

---

## Documentos canónicos

| Archivo | Contenido | Estado |
|---|---|---|
| `README.md` | Presentacion general, instalacion, stack tecnologico y enlaces a documentacion. | Actualizado |
| `docs/INDICE.md` | Este archivo. Mapa de toda la documentacion. | Actualizado |
| `docs/REQUISITOS.md` | Catalogo canonico de RF, RN y criterio de alcance. Fuente de verdad para la especificacion. | Actualizado |
| `docs/REQUERIMIENTOS.md` | Trazabilidad RF/RN → implementación (clases/rutas/templates) → verificación (tests/escenarios del sandbox). | Actualizado |
| `docs/DEFENSA.md` | Guia para la defensa de tesis: argumento, exposicion, preguntas esperadas, limitaciones (§7, fusionado desde `LIMITACIONES.md`). | Actualizado |
| `docs/TRADEOFFS.md` | Las 26 decisiones de diseno con alternativas y justificacion. | Actualizado |
| `docs/DIAGRAMAS.md` | Diagramas UML (clases, ER, secuencia, estados, casos de uso) y flujos. | Actualizado |
| `docs/ARQUITECTURA.md` | Nucleo del sistema: componentes, flujos, layouts, arquitectura de i18n. | Actualizado |
| `docs/ENDPOINTS.md` | Listado de rutas HTTP, acceso por rol, OpenAPI y resumen de testing. | Actualizado |
| `docs/METODOLOGIA.md` | Modelo iterativo-incremental de 4 fases y relacion con DSRM. | Actualizado |
| `docs/MEJORAS.md` | Tabla de superficies de mejora: implementadas, descartadas, diferidas. | Actualizado |
| `docs/BOILERPLATE_VS_CORE.md` | Clasificación clase por clase: infraestructura genérica vs núcleo de negocio real. | Actualizado |
| `docs/USABILIDAD.md` | Instrumento de evaluación de usabilidad: Likert por funcionalidad (preparado, no aplicado). | Nuevo |

## Documentos de referencia (fuera de la lectura principal, versionados igual)

| Archivo | Contenido | Por qué no es canónico |
|---|---|---|
| `docs/referencia/CORRECCIONES.md` | Historial de hardening y correcciones (2026-09-11 y anteriores). | Superseded — su contenido ya está en `MEJORAS.md` (fixes con entradas propias) o en `DEFENSA.md` §7 (limitaciones); la arquitectura de i18n que aportaba pasó a `ARQUITECTURA.md`. Ver #173 |
| `docs/referencia/GITFLOW.md` | Flujo de ramas, commits y deploy. | Evidencia de proceso, no argumento de defensa — `METODOLOGIA.md` ya cubre el encuadre académico (DSRM) |
| `docs/AUDITORIA.md` | Auditoria de superficies: copies, estilos, esquemas, endpoints, templates. | Análisis de trabajo, no narrativa de defensa |
| `docs/COPIES.md` | Índice editorial de textos de UI (la fuente real son `static/i18n/*.json`). | Ya degradado a índice desde `MEJORAS.md` #162 |
| `docs/SEGURIDAD.md` | Checklist operativo de secretos y `git config`. | Operativo, no defensa |
| `docs/sincronizacion-codigo-texto.md` | Auditoría código↔tesis (SYNC-XX). | Gitignored, insumo de trabajo |
| `docs/gaps-detalle.md` | Lógica de negocio extraída para `scratch/pseudoapp.java`. | Gitignored, insumo de trabajo |
| `docs/tesis/CONTEXTO_LLM.md` | Contexto de continuidad entre sesiones de agente. | Es para el agente, no para lectura humana |
| `docs/REFACTOR_PLAN.md` | Plan de refactorización por fases (dominio, máquina de estados, web). | Plan de trabajo, no narrativa de defensa — lo ejecutado queda asentado en `MEJORAS.md` |

CLAUDE.md (raíz del repo) queda fuera de esta clasificación — son instrucciones para agentes, no documentación de la tesis.

---

## Mapa tematico

| Tema | Documento |
|---|---|
| Requisitos funcionales y reglas de negocio | `docs/REQUISITOS.md` |
| Casos de uso, ER, secuencia, estados | `docs/DIAGRAMAS.md` §1-§7 |
| Gitflow / forma de trabajo con ramas | `docs/referencia/GITFLOW.md` + `docs/diagrams/figura5-gitflow.drawio` |
| Componentes y flujos internos, arquitectura de i18n | `docs/ARQUITECTURA.md` |
| Rutas HTTP | `docs/ENDPOINTS.md` |
| Decisiones de diseno (por que X y no Y) | `docs/TRADEOFFS.md` §1-§26 |
| Argumento y preguntas de defensa | `docs/DEFENSA.md` |
| Metodologia de desarrollo | `docs/METODOLOGIA.md` |
| Estado de mejoras propuestas/aplicadas | `docs/MEJORAS.md` |
| Limitaciones y fuera de alcance | `docs/DEFENSA.md` §7 |
| Evaluación de usabilidad (Likert por funcionalidad) | `docs/USABILIDAD.md` |
| Historial de hardening (referencia) | `docs/referencia/CORRECCIONES.md` |
| Textos de UI / copys (referencia) | `docs/COPIES.md` |
| Inventario auditado de superficies (referencia) | `docs/AUDITORIA.md` |
| Secretos y reglas de git config (referencia) | `docs/SEGURIDAD.md` |

---

## Mejoras recientes que ya estan documentadas

- **Rutas centralizadas en `Routes.java`**: una sola fuente de verdad para URL de controllers, seguridad y tests.
- **Validación inline en `User`**: email, nombre y teléfono se validan y canonicalizan en los setters, sin value objects separados.
- **`PhoneNumber` como utility class**: normalización E.164 para Uruguay (+598) y Brasil (+55) con métodos estáticos.
- **Transiciones de estado en `RequestStatus`**: las transiciones `accept/reject/complete` están encapsuladas en el enum de estado.
- **OpenAPI/Swagger UI**: documentacion automatica de endpoints en `/swagger-ui.html`.
- **i18n unificado en JSON**: catalogos `es.json` y `pt.json` en `static/i18n/`, arquitectura en `docs/ARQUITECTURA.md`.
- **Documentos triage C/R**: 12 canónicos, 8 de referencia — ver `docs/MEJORAS.md` #173.

---

## Como mantener sincronizada la documentacion

1. Cuando se agregue un endpoint nuevo, actualizar `docs/ENDPOINTS.md` y `Routes.java`.
2. Cuando se agregue o modifique un mecanismo de seguridad, actualizar `docs/SEGURIDAD.md` y `docs/MEJORAS.md`.
3. Cuando se tome una decision de diseno, agregarla a `docs/TRADEOFFS.md`.
4. Cuando cambie el conteo de tests, actualizar el encabezado de `docs/INDICE.md` y los documentos afectados.
5. Cuando cambie el flujo de ramas o deploy, actualizar `docs/referencia/GITFLOW.md` y `docs/diagrams/figura5-gitflow.drawio`.
6. Antes de la defensa, regenerar `docs/INDICE.md` si se renombran archivos o se agregan documentos.
7. Un doc nuevo entra como Canónico solo si un miembro del tribunal lo leería; si es análisis de respaldo, va directo a "Documentos de referencia".
