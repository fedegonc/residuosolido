# Indice de Documentacion — Eco Solicitud

> Fecha de sincronizacion: reorganizacion de docs en archivos focales + gitflow  
> Tests: 153, 0 failures, 0 errors, 0 skipped (medium loop, sin browser)
> Build: SUCCESS

Este documento es el punto de entrada para toda la documentacion del proyecto.  
Los documentos tecnicos estan concentrados en `docs/` y estan actualizados al estado actual del codigo.

**Regla de organizacion:** un documento = un tema. Los anexos que antes
convivian pegados dentro de los archivos grandes ahora son documentos
propios. Si un tema no tiene doc dedicado, vive en el documento de su
dominio segun esta tabla.

---

## Documentos principales

| Archivo | Contenido | Estado |
|---|---|---|
| `README.md` | Presentacion general, instalacion, stack tecnologico y enlaces a documentacion. | Actualizado |
| `docs/INDICE.md` | Este archivo. Mapa de toda la documentacion. | Actualizado |
| `docs/REQUISITOS.md` | Catalogo canonico de RF, RN y criterio de alcance. Fuente de verdad para la especificacion. | Nuevo |
| `docs/DEFENSA.md` | Guia para la defensa de tesis: argumento, estructura de exposicion, preguntas esperadas, fuentes. | Actualizado |
| `docs/TRADEOFFS.md` | Las 26 decisiones de diseno con alternativas y justificacion. | Nuevo |
| `docs/DIAGRAMAS.md` | Diagramas UML (clases, ER, secuencia, estados, casos de uso) y flujos. | Actualizado |
| `docs/ARQUITECTURA.md` | Nucleo del sistema: inventario de componentes, flujos principales, layouts, decisiones. | Nuevo |
| `docs/ENDPOINTS.md` | Listado de rutas HTTP, acceso por rol, OpenAPI y resumen de testing. | Actualizado |
| `docs/METODOLOGIA.md` | Modelo iterativo-incremental de 4 fases y relacion con DSRM. | Actualizado |
| `docs/MEJORAS.md` | Tabla de superficies de mejora: implementadas, descartadas, diferidas. | Actualizado |
| `docs/CORRECCIONES.md` | Hardening aplicado, correcciones y limpieza del codigo. | Nuevo |
| `docs/LIMITACIONES.md` | Limitaciones reconocidas y lo que quedo fuera de alcance. | Nuevo |
| `docs/COPIES.md` | Single source of truth de textos de UI (claves i18n es/pt). | Nuevo |
| `docs/AUDITORIA.md` | Auditoria de superficies: copies, estilos, esquemas, endpoints, templates. | Nuevo |
| `docs/GITFLOW.md` | Flujo de trabajo del repo: ramas, merges, convencion de commits, deploy. | Nuevo |
| `docs/BOILERPLATE_VS_CORE.md` | Clasificación clase por clase: infraestructura genérica vs núcleo de negocio real. | Actualizado |
| `docs/SEGURIDAD.md` | Checklist mínimo de secretos y seguridad: credenciales, `.env`/`.env.example`, índices de Mongo. | Actualizado |
| `docs/tesis/CONTEXTO_LLM.md` | Contexto de la tesis para continuidad entre sesiones de agente. | Actualizado |

---

## Mapa tematico

| Tema | Documento |
|---|---|
| Requisitos funcionales y reglas de negocio | `docs/REQUISITOS.md` |
| Casos de uso, ER, secuencia, estados | `docs/DIAGRAMAS.md` §1-§7 |
| Gitflow / forma de trabajo con ramas | `docs/GITFLOW.md` + `docs/diagrams/figura5-gitflow.drawio` |
| Componentes y flujos internos | `docs/ARQUITECTURA.md` |
| Rutas HTTP | `docs/ENDPOINTS.md` |
| Decisiones de diseno (por que X y no Y) | `docs/TRADEOFFS.md` §1-§26 |
| Argumento y preguntas de defensa | `docs/DEFENSA.md` |
| Metodologia de desarrollo | `docs/METODOLOGIA.md` |
| Estado de mejoras propuestas/aplicadas | `docs/MEJORAS.md` |
| Correcciones aplicadas y hardening | `docs/CORRECCIONES.md` |
| Limitaciones y fuera de alcance | `docs/LIMITACIONES.md` |
| Textos de UI (copys) | `docs/COPIES.md` |
| Inventario auditado de superficies | `docs/AUDITORIA.md` |
| Secretos y reglas de git config | `docs/SEGURIDAD.md` |

---

## Mejoras recientes que ya estan documentadas

- **Rutas centralizadas en `Routes.java`**: una sola fuente de verdad para URL de controllers, seguridad y tests.
- **Validación inline en `User`**: email, nombre y teléfono se validan y canonicalizan en los setters, sin value objects separados.
- **`PhoneNumber` como utility class**: normalización E.164 para Uruguay (+598) y Brasil (+55) con métodos estáticos.
- **Transiciones de estado en `RequestStatus`**: las transiciones `accept/reject/complete` están encapsuladas en el enum de estado.
- **OpenAPI/Swagger UI**: documentacion automatica de endpoints en `/swagger-ui.html`.
- **i18n unificado en JSON**: catalogos `es.json` y `pt.json` en `static/i18n/`.
- **Documentos reorganizados**: un doc por tema; los anexos de MEJORAS/DEFENSA/METODOLOGIA/DIAGRAMAS pasaron a ser archivos propios.

---

## Como mantener sincronizada la documentacion

1. Cuando se agregue un endpoint nuevo, actualizar `docs/ENDPOINTS.md` y `Routes.java`.
2. Cuando se agregue o modifique un mecanismo de seguridad, actualizar `docs/SEGURIDAD.md`, `docs/CORRECCIONES.md` y `docs/MEJORAS.md`.
3. Cuando se tome una decision de diseno, agregarla a `docs/TRADEOFFS.md`.
4. Cuando cambie el conteo de tests, actualizar el encabezado de `docs/INDICE.md` y los documentos afectados.
5. Cuando cambie el flujo de ramas o deploy, actualizar `docs/GITFLOW.md` y `docs/diagrams/figura5-gitflow.drawio`.
6. Antes de la defensa, regenerar `docs/INDICE.md` si se renombran archivos o se agregan documentos.
