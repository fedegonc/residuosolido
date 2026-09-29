# Superficies de Mejora — Estado

Tabla centralizada de todas las mejoras posibles del sistema. Para justificación y tradeoffs, ver `docs/TRADEOFFS.md`.

**Estados:**
- **Implementado:** hecho y funcionando
- **Descartado:** evaluado y rechazado (justificación en TRADEOFFS si aplica)
- **Diferido:** pendiente, postergado a después de la defensa
- **Latente:** código existe pero sin UI activa
- **Retirado:** fue implementado, después se decidió sacarlo

---

| # | Mejora | Estado | Tradeoff |
|---|---|---|---|
| 1 | Auth con roles + onboarding forzado | Implementado | §1 |
| 2 | Solicitudes de recolección (CRUD) | Implementado | — |
| 3 | Rastreo por teléfono + código privado | Implementado | — |
| 4 | Selector de código de país (UY/BR) | Implementado | — |
| 5 | Flujo de estados (PENDING→IN_PROGRESS→COMPLETED/REJECTED) | Implementado | §2 |
| 6 | Kanban integrado al dashboard de org | Implementado | — |
| 7 | Blog estático (3 artículos) | Descartado | §5 |
| 8 | Métricas públicas por ciudad | Implementado | — |
| 9 | Rate limiting de invitados | Implementado | — |
| 10 | Bloqueo por intentos de login | Implementado | — |
| 11 | PWA instalable | Descartado → Retirado | §6 |
| 12 | i18n español/portugués | Implementado | §7 |
| 13 | Dark mode toggle | Implementado | — |
| 14 | FOUC fix (flash de texto sin traducir) | Implementado | — |
| 15 | Diseño canónico (variables CSS, BEM) | Implementado | — |
| 16 | Empty states con iconos + microcopy | Implementado | — |
| 17 | Microcopy anti-slop | Implementado | — |
| 18 | CSRF en todos los formularios | Implementado | — |
| 19 | DTO RegistrationForm (sin mass-assignment) | Implementado | — |
| 20 | Optimistic locking (@Version) | Implementado | §3 |
| 21 | Validación de imagen antes de persistir | Implementado | — |
| 22 | PIN mínima 3 caracteres | Implementado | §4 |
| 23 | Service Worker cache (CSS/JS only) | Descartado | §6 |
| 24 | Limpieza de claves i18n muertas | Implementado | — |
| 25 | Documentación centralizada con INDICE.md | Implementado | — |
| 26 | Diagramas UML (4 figuras) | Implementado | — |
| 27 | Tests (315 unit + integration) | Implementado | — |
| 28 | Metodología iterativo-incremental (4 fases) | Implementado | — |
| 29 | Deploy en Render.com (PaaS) | Implementado | §15 |
| 30 | Páginas públicas /docs y /diagramas | Implementado | — |
| 31 | Diagrama de secuencia UML 2.5 | Implementado | — |
| 32 | CI/CD pipeline (GitHub Actions) | Descartado | — |
| 33 | AWS (IaaS) | Descartado | §15 |
| 34 | VPN para acceso | Descartado | — |
| 35 | Panel Admin / moderación | Descartado | §2 |
| 36 | CMS para blog | Descartado | §5 |
| 37 | Mapas / geolocalización | Descartado | §10 |
| 38 | MongoDB réplica set / transacciones | Descartado | §3 |
| 39 | Cloud storage (S3/Cloudinary) | Descartado | §9 |
| 40 | Fusionar request-form + request-edit | Implementado | — |
| 41 | Fusionar perfiles (user + org + onboarding) | Implementado | — |
| 42 | Fusionar dashboards (user + org) | Diferido | §14 |
| 43 | Reducir clases CSS con utilities | Diferido | — |
| 44 | Contraseña mínima 8+ (producción) | Diferido | §4 |
| 45 | Asignación de recolector a solicitud (RF-8) | Diferido | — |
| 46 | SEO / meta tags dinámicos | Diferido | — |
| 47 | Tests E2E con Playwright completo | Diferido | — |
| 48 | Monitoreo (Prometheus/Grafana) | Diferido | — |
| 49 | Logs estructurados (JSON) | Diferido | — |
| 50 | Rate limiting con Redis | Diferido | §11 |
| 51 | Multi-tenancy (multi-frontera) | Diferido | — |
| 52 | Java 21 LTS | Implementado | — |
| 53 | Spring Boot 3.5 | Diferido | — |
| 54 | Spring Boot 4.0 | Diferido | — |
| 55 | Validación inline (email, name) | Implementado | — |
| 56 | Notificaciones WhatsApp | Descartado | §8 |
| 57 | OpenAPI / Swagger UI | Implementado | — |
| 58 | Centralización de rutas (Routes.java) | Implementado | — |
| 59 | Layouts anidados (base-sidebar) | Implementado | — |
| 60 | Componentes UI reutilizables | Implementado | — |

**Notas:**
- Línea 11: PWA fue descartado inicialmente, retomado brevemente (#143), luego eliminado completamente (#182).
- Línea 23: Service Worker eliminado junto con el resto de PWA en #11.
- Para decisiones sobre qué NO entró al scope, ver `docs/TRADEOFFS.md` (§1-§15+).

---

**Cambios recientes:** Consolidadas entradas redundantes; removed verbosity en justificaciones (ahora en TRADEOFFS.md).
