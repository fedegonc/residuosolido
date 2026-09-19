# Indice de Documentacion — Eco Solicitud

> Fecha de sincronizacion: post-consolidacion de pantallas de organizacion  
> Tests: 153, 0 failures, 0 errors, 0 skipped (medium loop, sin browser)
> Build: SUCCESS

Este documento es el punto de entrada para toda la documentacion del proyecto.  
Los documentos tecnicos estan concentrados en `docs/` y estan actualizados al estado actual del codigo.

---

## Documentos principales

| Archivo | Contenido | Estado |
|---|---|---|
| `README.md` | Presentacion general, instalacion, stack tecnologico y enlaces a documentacion. | Actualizado |
| `docs/INDICE.md` | Este archivo. Mapa de toda la documentacion y anexos. | Nuevo |
| `docs/DEFENSA.md` | Guia para la defensa de tesis: argumento, preguntas esperadas, limitaciones y tradeoffs. | Actualizado |
| `docs/DIAGRAMAS.md` | Diagramas UML y de arquitectura, flujos principales y requisitos en anexo. | Actualizado |
| `docs/ENDPOINTS.md` | Listado de rutas HTTP, acceso por rol, anotaciones OpenAPI y resumen de testing. | Actualizado |
| `docs/MEJORAS.md` | Tabla de mejoras implementadas, descartadas, diferidas y latentes; correcciones aplicadas. | Actualizado |
| `docs/METODOLOGIA.md` | Modelo iterativo-incremental de 4 fases, copys de UI en anexo. | Actualizado |
| `docs/BOILERPLATE_VS_CORE.md` | Clasificación clase por clase: infraestructura genérica vs núcleo de negocio real. Documento vivo. | Nuevo |
| `docs/SEGURIDAD.md` | Checklist mínimo de secretos y seguridad: credenciales, `.env`/`.env.example`, índices de Mongo. | Nuevo |

---

## Anexos consolidados

Los anexos que antes se referenciaban como archivos separados ahora viven como secciones dentro de los documentos principales. Este indice indica donde encontrarlos:

| Anexo | Archivo y seccion |
|---|---|
| Arquitectura / Nucleo del sistema | `docs/DIAGRAMAS.md` §Nucleo del Sistema |
| Casos de uso y ER | `docs/DIAGRAMAS.md` §1, §3 |
| Requisitos y reglas de negocio (RF-RN) | `docs/DIAGRAMAS.md` §Requisitos y Reglas de Negocio |
| Testing y estrategia | `docs/ENDPOINTS.md` §Testing (anexo) |
| Correcciones y hardening | `docs/MEJORAS.md` §Hardening — Correcciones aplicadas |
| Tradeoffs de diseno | `docs/DEFENSA.md` §Tradeoffs — Decisiones de Diseno |
| Copys de UI / i18n | `docs/METODOLOGIA.md` §Copys — Single Source of Truth |

---

## Mejoras recientes que ya estan documentadas

- **Rutas centralizadas en `Routes.java`**: una sola fuente de verdad para URL de controllers, seguridad y tests.
- **Validación inline en `User`**: email, nombre y teléfono se validan y canonicalizan en los setters, sin value objects separados.
- **`PhoneNumber` como utility class**: normalización E.164 para Uruguay (+598) y Brasil (+55) con métodos estáticos.
- **Transiciones de estado en `RequestStatus`**: las transiciones `accept/reject/complete` están encapsuladas en el enum de estado.
- **OpenAPI/Swagger UI**: documentacion automatica de endpoints en `/swagger-ui.html`.
- **i18n unificado en JSON**: catalogos `es.json` y `pt.json` en `static/i18n/`.

---

## Como mantener sincronizada la documentacion

1. Cuando se agregue un endpoint nuevo, actualizar `docs/ENDPOINTS.md` y `Routes.java`.
2. Cuando se agregue o modifique un mecanismo de seguridad, actualizar `docs/DEFENSA.md` y `docs/MEJORAS.md`.
3. Cuando cambie el conteo de tests, actualizar el encabezado de `docs/INDICE.md` y los documentos afectados.
4. Antes de la defensa, regenerar `docs/INDICE.md` si se renombran archivos o se agregan anexos.
