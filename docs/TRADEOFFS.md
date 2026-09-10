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
