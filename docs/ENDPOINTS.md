# Endpoints — Eco Solicitud

Rutas HTTP disponibles en la aplicación. Para esquemas completos y ejemplos, ver `/swagger-ui.html`.

---

## Público (sin autenticación)

| Ruta | Método | Descripción |
|---|---|---|
| `/`, `/index` | GET | Landing page pública |
| `/registrarse` | GET | Formulario de registro |
| `/registrarse` | POST | Procesa registro (usuario u organización) |
| `/entrar` | GET | Formulario de login |
| `/salir` | POST | Cierra sesión |
| `/solicitar` | GET | Formulario de nueva solicitud |
| `/solicitar` | POST | Crea solicitud (con imagen opcional) |
| `/docs/diagramas` | GET | Visor de diagramas UML |
| `/docs/{file}` | GET | Documento renderizado HTML |
| `/docs/{file}.md` | GET | Documento sin renderizar (Markdown) |
| `/docs/diagrams/{file}.drawio` | GET | Diagrama sin renderizar (XML) |
| `/scratch/{file}` | GET | Especificación ejecutable (App.java) |
| `/pagina/{slug}` | GET | Página de contenido genérico |

## Usuario autenticado (rol USER)

| Ruta | Método | Descripción |
|---|---|---|
| `/mis-solicitudes` | GET | Lista de solicitudes propias |
| `/solicitudes/{id}` | GET | Detalle de solicitud |
| `/solicitudes/{id}/editar` | GET | Formulario de edición |
| `/solicitudes/{id}` | PUT | Actualizar solicitud |
| `/solicitudes/{id}` | DELETE | Eliminar solicitud |
| `/notificaciones` | GET | Bandeja de notificaciones |

## Organización autenticada (rol ORGANIZATION)

| Ruta | Método | Descripción |
|---|---|---|
| `/acopio/solicitudes` | GET | Dashboard de acopio (con filtro `?estado=`) |
| `/acopio/solicitudes/{id}` | GET | Detalle de solicitud asignada |
| `/acopio/solicitudes/{id}/aceptar` | POST | Aceptar solicitud |
| `/acopio/solicitudes/{id}/rechazar` | POST | Rechazar solicitud |
| `/acopio/solicitudes/{id}/completar` | POST | Marcar como completada |
| `/mi-organizacion` | GET | Perfil de la organización |
| `/mi-organizacion` | PUT | Actualizar perfil |

## Internos (fetch de formularios)

| Ruta | Método | Descripción |
|---|---|---|
| `/solicitudes/org-options` | GET | Opciones de organizaciones por ciudad |

---

**Notas:**
- **Rutas centralizadas** en `com.residuosolido.app.config.Routes` (fuente única de verdad).
- **Spring MVC + Thymeleaf (SSR):** no hay API REST pública. `/solicitudes/org-options` es infraestructura interna del formulario (fetch vanilla JS).
- **Para esquemas y ejemplos completos,** consultar `/swagger-ui.html` en desarrollo o el endpoint OpenAPI.
