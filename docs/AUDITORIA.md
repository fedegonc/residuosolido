# Auditoría de Superficies — Eco Solicitud

Inventario auditado de copies, estilos, esquemas, endpoints y templates, con desincronizaciones detectadas y recomendaciones de limpieza. Extraído del anexo de `MEJORAS.md`.

# Superficies del sistema — auditoría (anexo)

> **⚠️ Foto histórica, congelada en el commit `1e4d575`.** Su
> recomendación de prioridad alta ("3 fuentes de copies superpuestas →
> Unificar en una") **ya se implementó**: `messages.properties`,
> `messages_es.properties` y `messages_pt.properties` **ya no existen**.
> Todo quedó unificado en `static/i18n/{lang}.json` (`JsonMessageSource` +
> `UiCopyCatalog`, ver §4 más arriba). Las secciones de abajo que hablan
> de sincronizar/limpiar esos `.properties` son historia, no un TODO
> pendiente — no las tomes como checklist para la defensa.

> **Propósito (original):** centralizar todas las superficies del sistema (copies,
> estilos, esquemas, endpoints) en un único documento para detectar
> crecimientos innecesarios y mantener consistencia.
>
> **Fecha de auditoría:** commit `1e4d575`
> **Tests:** 181, 0 failures

---

## 1. Copies (textos de interfaz)

### 1.1 Resumen de fuentes de copies

| Fuente | Rol | Claves | ¿Usada? |
|---|---|---|---|
| `static/i18n/common/es.json` | i18n cliente (JS) | 39 | Sí |
| `static/i18n/common/pt.json` | i18n cliente (JS) | 39 | Sí |
| `messages_es.properties` | i18n servidor (Thymeleaf) | 136 | Parcial |
| `messages_pt.properties` | i18n servidor (Thymeleaf) | 109 | Parcial |
| `messages.properties` | Fallback (sin locale) | 108 | No visible |
| Texto fallback en templates | HTML hardcodeado en `data-i18n` | 227 claves | Sí |

**Problema:** hay **3 fuentes de copies** que se superponen:
1. `data-i18n` en templates (texto fallback en español)
2. `messages_*.properties` (servidor)
3. `i18n/common/*.json` (cliente, JS)

Muchas claves de `messages_es.properties` no se usan (son de fases
anteriores del proyecto). Los textos reales que ve el usuario vienen
de `data-i18n` + `i18n/common/*.json`.

### 1.2 Desincronización detectada

| Problema | Estado |
|---|---|
| `messages_es` tiene 27 claves más que `messages_pt` | Pendiente |
| `messages.properties` tiene 108 claves (¿se usa?) | No visible |
| `messages_es` tiene claves `index.features.*` sin uso | Muertas |
| `messages_es` tiene claves `index.how.*` que duplican `data-i18n` | Redundantes |
| Texto fallback de `data-i18n` y `messages_es` no siempre coinciden | Pendiente |

### 1.3 Claves i18n por categoría

| Categoría | Claves | Templates |
|---|---|---|
| Navbar/nav | 15 | navbar, base |
| Auth | 18 | login, register |
| Hero/index | 8 | index |
| How (pasos) | 8 | index |
| Track/rastreo | 10 | track |
| Dashboard user | 10 | dashboard, requests |
| Dashboard org | 12 | org/dashboard |
| Request form | 30 | request-form |
| Request list | 8 | requests |
| Profile org | 15 | org/profile |
| Onboarding | 8 | complete-profile |
| Error 404 | 2 | error/404 |
| Footer | 6 | base |
| Modal track | 6 | navbar |
| Coop CTA | 3 | index |

**Total claves data-i18n únicas:** 227

### 1.4 Copies muertos o redundantes en `messages_es.properties`

| Clave | Motivo |
|---|---|
| `index.welcome` | No se usa en ningún template |
| `index.features.*` (6 claves) | Sección eliminada del index |
| `index.how.title` | El index ya no tiene título "¿Cómo funciona?" |
| `index.how.subtitle` | Idem |
| `index.how.step1` a `step4` | Duplican `data-i18n="how_step*"` |
| `index.login` | No se usa |
| `metrics.title` | Duplica `data-i18n` |
| `footer.description` | No se usa (el footer usa `data-i18n`) |
| `footer.nav.title` | No se usa |
| `footer.contact.title` | No se usa |

**Estimado:** ~20 claves muertas en `messages_es.properties`

---

## 2. Estilos (CSS)

### 2.1 Resumen

| Métrica | Valor |
|---|---|
| Líneas CSS | 582 |
| Clases únicas | 292 |
| Variables CSS definidas | 16 |
| Uso de `var()` | 262 |
| Media queries | 12 |
| Hex fuera de `:root` | 0 |
| Estilos inline | 0 |

### 2.2 Clases por categoría

| Categoría | Clases | ¿Justificadas? |
|---|---|---|
| Navbar | 18 | Sí |
| Dropdown | 7 | Sí |
| Hero | 11 | Sí |
| How (pasos) | 7 | Sí |
| Coop CTA | 3 | Sí |
| Btn | 8 | Sí |
| Form | 16 | Sí |
| Auth | 15 | Sí |
| Panel | 9 | Sí |
| Stat | 20 | **Dudoso** — 20 clases para cards de estadística |
| Kanban | 22 | **Alto** — 22 clases para el tablero |
| Track | 10 | Sí |
| Modal | 14 | Sí |
| Track-empty | 4 | Sí |
| Error-page | 3 | Sí |
| Breadcrumb | 2 | Sí |
| Info-card | 5 | Sí |
| Page | 3 | Sí |
| Otras (utility, misc) | ~100 | Revisar |

**Categorías con posible crecimiento innecesario:**
- **Stat (20 clases):** `stat-card`, `stat-card--center`, `stat-card--xl`,
  `stat-card__icon`, `stat-card__icon--pending`, `--in_progress`,
  `--completed`, `--rejected`, `stat-card__value`, `stat-card__label`,
  etc. Se podrían consolidar con utility classes.
- **Kanban (22 clases):** `kanban-card`, `kanban-card__city`,
  `kanban-card__materials`, `kanban-card__detail`, `kanban-column`,
  `kanban-column__title`, `kanban-column__empty`, `kanban-column__count`,
  etc. Es el componente más complejo del sistema.

### 2.3 Variables CSS

| Variable | Valor | Uso |
|---|---|---|
| `--primary` | #2d6a4f | 262 usos |
| `--primary-hover` | #40916c | — |
| `--primary-light` | #d8f3dc | — |
| `--bg` | #f8fafc | — |
| `--surface` | #ffffff | — |
| `--border` | #e2e8f0 | — |
| `--dark` | #1e293b | — |
| `--gray-text` | #64748b | — |
| `--white` | #ffffff | — |
| `--danger` | #dc2626 | — |
| `--danger-bg` | #fee2e2 | — |
| `--danger-border` | #fecaca | — |
| `--warning` | #d97706 | — |
| `--warning-bg` | #fef3c7 | — |
| `--success-border` | #bbf7d0 | — |
| `--radius` | 0.5rem | — |
| `--shadow` | 0 1px 3px... | — |
| `--shadow-lg` | 0 4px 12px... | — |
| `--maxw` | 72rem | — |
| `--uruguay` | #1d4ed8 | Acento |
| `--uruguay-light` | #dbeafe | Acento |
| `--brasil` | #16a34a | Acento |

---

## 3. Esquemas (modelos de datos)

### 3.1 Modelos (3)

| Modelo | Campos | ¿Usado? |
|---|---|---|
| `User` | id, username, password, email, role, firstName, phone, city, acceptedMaterials, version | Sí |
| `Request` | id, city, address, materials, status, organization, citizen, guestName, guestPhone, trackingCode, imageId, timeSlot, weight, volume, version, createdAt, updatedAt | Sí |
| `PhoneNumber` | utility class (static methods) — normalización E.164 | Sí |

### 3.2 Enums (5)

| Enum | Valores | ¿Usado? |
|---|---|---|
| `Role` | USER, ORGANIZATION | Sí |
| `City` | RIVERA, LIVRAMENTO | Sí |
| `RequestStatus` | PENDING, IN_PROGRESS, COMPLETED, REJECTED | Sí |
| `MaterialCategory` | PLASTICO, PAPEL, CARTON, VIDRIO, METAL, MADERA, ESCOMBROS | Sí |
| `TimeSlot` | MANANA, TARDE, NOCHE | Sí |

### 3.3 Repositorios (3)

| Repo | ¿Usado? |
|---|---|
| `UserRepository` | Sí |
| `RequestRepository` | Sí |
| `InformalCollectorRepository` | **Latente** |

---

## 4. Endpoints

### 4.1 Resumen

| Método | Cantidad |
|---|---|
| GET | 22 |
| POST | 10 |
| Total | 32 |

### 4.2 Por controller

| Controller | Endpoints | ¿Justificado? |
|---|---|---|
| `AuthController` | 4 (login, register GET/POST) | Sí |
| `RequestCreateController` | 2 (nueva, crear) | Sí |
| `RequestController` | 5 (lista, detalle, editar, actualizar, eliminar) | Sí |
| `GuestTrackingController` | 2 (rastrear GET/POST) | Sí |
| `OrgDashboardController` | 1 (inicio) | Sí |
| `OrgProfileController` | 4 (completar-perfil GET/POST, perfil GET/POST) | Sí |
| `OrgRequestController` | 4 (lista, detalle, transiciones) | Sí |
| `OrgApiController` | 1 (by-city JSON) | Sí |
| `DocsController` | 2 (documentos, diagramas) | Sí |
| `BaseController` | (abstracto, sin endpoints) | Sí |


## 5. Templates

### 5.1 Resumen

| Tipo | Cantidad |
|---|---|
| Templates totales | 20 |
| Fragments | 8 |
| Páginas (heredan base) | 11 |

### 5.2 Templates por categoría

| Categoría | Templates |
|---|---|
| Público | index, error/404 |
| Auth | login, register |
| User | requests, request-form, track |
| Org | dashboard, requests, profile, complete-profile |
| Layout | base |
| Fragments | navbar, feedback, request-list, toggle-view-edit, password-field, org-sidebar, phone-country-selector, request-form-js |

### 5.3 Templates con más copies (data-i18n)

| Template | Claves data-i18n |
|---|---|
| request-form | 40 |
| org/requests | 34 |
| org/profile | 27 |
| org/dashboard | 24 |
| org/catadores | 22 (latente) |
| users/request-detail | 21 |
| public/index | 16 |
| users/track | 14 |
| users/request-success | 14 |
| auth/register | 14 |

---

## 6. Crecimientos innecesarios detectados

### 6.1 Copies

| Problema | Impacto | Solución |
|---|---|---|
| ~20 claves muertas en `messages_es.properties` | Bajo | Eliminar |
| `messages_pt` tiene 27 claves menos que `messages_es` | Medio | Sincronizar |
| `messages.properties` (108 claves) no se usa visiblemente | Bajo | Verificar o eliminar |
| 3 fuentes de copies superpuestas | Alto | Unificar en una |

### 6.2 Estilos

| Problema | Impacto | Solución |
|---|---|---|
| Stat tiene 20 clases (posible sobre-diseño) | Medio | Consolidar con utilities |
| Kanban tiene 22 clases (complejo) | Medio | Aceptable (es el componente más complejo) |
| ~100 clases "otras" sin categorizar | Bajo | Auditar |

### 6.3 Esquemas

| Problema | Impacto | Solución |
|---|---|---|
| `InformalCollector` + repo + controller + 4 endpoints + template latentes | Medio | Documentar o eliminar |

### 6.4 Endpoints

| Problema | Impacto | Solución |
|---|---|---|

---

## 7. Recomendaciones de limpieza

### Prioridad alta (antes de la defensa)

1. **Sincronizar `messages_pt` con `messages_es`** — faltan 27 claves
2. **Eliminar claves muertas de `messages_es`** — ~20 claves sin uso
3. **Verificar si `messages.properties` se usa** — si no, eliminar

### Prioridad media (después de la defensa)

4. **Consolidar stat-card** — 20 clases → ~8 con utilities
5. **Decidir qué hacer con `InformalCollector`** — eliminar o activar
6. **Unificar fuentes de copies** — una sola fuente de verdad

### Prioridad baja
