# Especificación de Requisitos de Software (SRS)
## Eco Solicitud — Plataforma de Gestión de Reciclaje

**Versión:** 1.0  
**Fecha:** 2026-09-24  
**Autor:** Federico Goncálvez  
**Estado:** Aprobado para Defensa de Tesis

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica los requisitos funcionales y no funcionales del sistema **Eco Solicitud**, una plataforma web para la gestión y coordinación de recolección de residuos reciclables en la Frontera de la Paz (Rivera, Uruguay — Sant'Ana do Livramento, Brasil).

El SRS establece el contrato entre:
- **Usuarios finales:** ciudadanos, organizaciones de acopio
- **Desarrollador:** Federico Goncálvez
- **Tribunal de defensa:** UTEC/UDELAR

### 1.2 Alcance del Producto

**Objetivo:** Digitalizar el flujo de solicitud de recolección de reciclables, eliminando gestión manual en ciudades fronterizas con baja conectividad digital.

**Población objetivo:**
- Ciudadanos (registrados e invitados) que solicitan recolección
- Organizaciones de acopio (catadores, cooperativas) que responden
- Oficiales municipales (consulta de estadísticas)

**Fuera de alcance:**
- Logística de ruta optimizada (diferido)
- Gamificación de incentivos (diferido)
- Sistema de créditos/moneda interna (diferido)

### 1.3 Definiciones y Acrónimos

| Término | Definición |
|---------|-----------|
| **RF** | Requisito Funcional |
| **RN** | Restricción/Requisito No Funcional |
| **MVP** | Mínimo Producto Viable |
| **E.164** | Formato internacional de números telefónicos (+país-número) |
| **Invitado** | Usuario sin registro que solicita recolección anónima |
| **Organización** | Entidad (coop, catadores) que recibe solicitudes |
| **Slot** | Franja horaria (MAÑANA/TARDE/NOCHE) para recolección |
| **Tracking Code** | Código privado de 8 caracteres para rastreo anónimo |

---

## 2. Descripción General

### 2.1 Perspectiva del Producto

```
┌─────────────────────────────────────────────────┐
│  Ciudadano                                      │
│  (registrado / invitado)                        │
│                                                 │
│  ┌──────────────────────────────────────────┐   │
│  │ Crear Solicitud de Recolección           │   │
│  │ • Seleccionar ciudad, materiales, imagen │   │
│  │ • Si invitado: rastrear por tel + código │   │
│  └──────────────────────────────────────────┘   │
│         ↓                                        │
│  ┌──────────────────────────────────────────┐   │
│  │  Organización recibe & responde          │   │
│  │  • Aceptar (con franja horaria)          │   │
│  │  • Rechazar                              │   │
│  │  • Completar (después de recoger)        │   │
│  └──────────────────────────────────────────┘   │
│         ↓                                        │
│  Ciudadano registrado: notificación in-app      │
│  Invitado: consulta vía rastreo anónimo         │
└─────────────────────────────────────────────────┘
```

### 2.2 Características Principales

1. **Autenticación con roles:** USER (ciudadano) / ORGANIZATION (acopio)
2. **Solicitudes anónimas:** Rastreo por teléfono + código privado sin login
3. **Flujo de estados:** PENDING → IN_PROGRESS/REJECTED → COMPLETED
4. **Notificaciones in-app:** Para usuarios registrados que reciben respuesta
5. **Panel de organización:** Kanban integrado con estadísticas por ciudad
6. **Bilingüismo:** Español (Rivera) + Portugués (Sant'Ana do Livramento)
7. **Validación de teléfono:** E.164 con selector de país (UY +598 / BR +55)

### 2.3 Restricciones Generales

- **Escala:** MVP para 0–1.000 solicitudes/mes, 100–500 usuarios activos
- **Idiomas:** Español e Portugués (sin cambio dinámico dentro de sesión)
- **Tecnología:** Spring Boot 3.2, MongoDB, Thymeleaf (SSR)
- **Disponibilidad:** 99.5% en horario de oficina (8am–6pm)
- **Seguridad:** Spring Security 6, CSRF, rate limiting

---

## 3. Requisitos Específicos

### 3.1 Requisitos Funcionales

#### **RF-1: Autenticación y Registro**

**Actor:** Ciudadano / Organización (anónimo)

**Precondición:** Usuario en página `/registrarse` o `/entrar`

**Flujo principal:**
1. Ingresa username (único, min 3 caracteres) + PIN (4 dígitos) + teléfono (E.164)
2. Sistema normaliza teléfono (`PhoneNumber.of("+598", "99123456", null)`)
3. Valida unicidad de username (case-insensitive)
4. Persiste User en MongoDB con rol seleccionado
5. Redirige a `/entrar` (ciudadano) o `/mi-organizacion` (onboarding forzado)

**Postcondición:** Usuario registrado, sesión creada

**Variantes:**
- Si es Organización: obliga a completar perfil (ciudad, materiales, teléfono)
- Si login falla: bloquea cuenta 15min tras 3 intentos

**Restricciones:**
- Username: 3–50 caracteres, sin espacios
- PIN: 4 dígitos exactos (no se almacena hashed por MVP, diferir a prod)
- Rate limit: 5 intentos/min por IP

---

#### **RF-3: Crear Solicitud de Recolección**

**Actor:** Ciudadano registrado / Invitado

**Precondición:** En página `/solicitar`

**Flujo principal:**

1. **Formulario inicial:**
   - Selector de país (UY +598 / BR +55)
   - Teléfono nacional (sin +, sin 0 inicial)
   - Ciudad (dropdown: RIVERA / LIVRAMENTO)
   - Dirección + referencia
   - Materiales (checkboxes: 7 categorías)
   - Imagen (opcional, validada: JPEG/PNG, <5MB)

2. **Validación:**
   - `RequestValidator.validateCreate()` → lanza `ValidationException` con clave i18n
   - `PhoneNumber.normalize(cc, nat, ddd)` → E.164 o excepción
   - Imagen: tipo MIME + extensión + tamaño

3. **Persistencia:**
   - Crea `Request` con `RequestStateMachine.create()`
   - Asigna organización por ciudad (`CityOrgService`)
   - Si invitado: genera `trackingCode` (8 caracteres random)
   - Si usuario: se marca `user_id` (no tracking code)

4. **Resultado:**
   - Invitado → redirige a `/rastrear?telefono=+59899123456&codigo=ABC12345`
   - Registrado → redirige a `/mis-solicitudes`

**Restricciones:**
- Rate limit invitados: 5 solicitudes/IP por hora
- Organización requerida en la ciudad (sino: error `NOT_IN_CITY`)
- Dirección: 10–200 caracteres

---

#### **RF-4: Rastrear Solicitud (Invitado)**

**Actor:** Invitado sin cuenta

**Precondición:** Acceso a `/rastrear?telefono=+598...&codigo=ABC123`

**Flujo:**

1. Busca solicitud por `(guestPhone normalized, trackingCode)`
2. Si no encontrada: muestra estado vacío
3. Si encontrada: muestra detail (estado, organización, dirección, franja si aceptada)
4. Si estado REJECTED/COMPLETED: muestra motivo (si existe) + fecha

**Validación:**
- Teléfono debe ser E.164 válido
- Código exacto (case-sensitive, 8 chars)
- Sin persistencia de búsqueda (privacidad)

---

#### **RF-6: Transiciones de Solicitud (Organización)**

**Actor:** Usuario con rol ORGANIZATION

**Precondición:** En `/acopio/solicitudes`, solicitud con estado PENDING

**Transiciones permitidas:**

| Estado | Acción | Resultado | Notificación |
|--------|--------|-----------|--------------|
| PENDING | accept | IN_PROGRESS + slot | Sí (usuario) |
| PENDING | reject | REJECTED | Sí (usuario) |
| IN_PROGRESS | complete | COMPLETED | No |
| IN_PROGRESS | reject | REJECTED | Sí (usuario) |

**Validación:**
- `RequestStateMachine.accept/reject/complete()` valida transición
- `accept` requiere `TimeSlot` (MANANA/TARDE/NOCHE)
- Optimistic locking: si falla por `@Version`, reintenta 3x con backoff exponencial

**Auditoría:**
- Structured logging: `REQUEST_ACCEPT_STARTED`, `REQUEST_ACCEPT_SUCCESS`, `REQUEST_ACCEPT_FAILED`
- Timestamp + usuario + solicitud_id

---

#### **RF-7: Perfil de Organización**

**Actor:** Usuario con rol ORGANIZATION

**Precondición:** En `/mi-organizacion`

**Acciones:**
1. **Ver:** Muestra nombre, ciudad, materiales aceptados, teléfono, descripción
2. **Editar:** Puede cambiar todos los campos excepto rol
3. **Validación:**
   - Ciudad + teléfono: **obligatorios siempre** (no solo en perfil incompleto)
   - Materiales: mín 1, máx 7
   - Teléfono: E.164 válido

**Restricciones:**
- `isAvailable()` = false si falta ciudad O falta teléfono
- No puede eliminarse a sí misma

---

#### **RF-9: Notificaciones In-App**

**Actor:** Usuario registrado (no invitado)

**Flujo:**

1. Cuando `acceptRequest()` o `rejectRequest()` ejecuta commit en DB:
   - Crea documento `Notification` en colección `notifications`
   - Campos: `user_id`, `requestId`, `type` (ACCEPTED/REJECTED), `confirmedSlot`, `read`, `createdAt`

2. Badge en navbar: muestra conteo de no-leídas
   - Refresh cada 30s (AJAX con `/api/notificaciones/count`)
   - Cache en client-side localStorage

3. Inbox en `/notificaciones`:
   - Listar cronológicamente (reciente primero)
   - Marcar como leído al cargar la página
   - Empty state: "No tienes notificaciones"

4. **No se notifica:**
   - Si save de Request falla (optimistic lock exhausted)
   - Si invitado (no tiene cuenta)
   - Si completada (operación interna)

---

### 3.2 Requisitos No Funcionales

#### **RN-1: Rendimiento**

| Métrica | Objetivo | Método |
|---------|----------|--------|
| Tiempo respuesta GET | <200ms p95 | Spring Boot Actuator, Prometheus |
| Tiempo respuesta POST crear solicitud | <500ms p95 | Incluye validación + persistencia |
| Búsqueda invitado (rastrear) | <100ms | Sin joins, índice en `(guestPhone, trackingCode)` |
| Listar solicitudes org | <300ms | 100 registros, índice en status |
| Concurrencia | 50 usuarios simultáneos | Load test con Apache Bench |

#### **RN-2: Seguridad**

| Aspecto | Requisito | Implementación |
|--------|-----------|-----------------|
| Autenticación | REQUIRED | Spring Security 6, sesión HTTP-only |
| Autorización | Role-based | @Secured("ROLE_USER"), ownership checks |
| CSRF | REQUIRED | Spring Security token en formularios |
| SQL Injection | PREVENTED | Spring Data (parameterized queries) |
| XSS | PREVENTED | Thymeleaf escaping automático |
| Rate Limiting | REQUIRED | 5 solicitudes/IP/hora (invitados) |
| Password storage | NO (MVP) | PIN en texto plano → usar bcrypt en prod |
| Bloqueo cuenta | 3 intentos | 15 min lockout con `LoginAttemptService` |

#### **RN-3: Disponibilidad**

- **Uptime:** 99.5% horario de oficina (8am–6pm Uruguay/Brasil)
- **RTO:** 1 hora (restore desde backup MongoDB Atlas)
- **RPO:** 24 horas (backup diario a las 3am UTC)
- **SLA público:** No (MVP, 0 usuarios reales)

#### **RN-4: Escalabilidad**

**MVP (actual):**
- Escala hasta 1.000 solicitudes/mes
- 500 usuarios activos concurrentes
- MongoDB single instance (sin réplica)
- Rate limiter en memoria (no distribuido)

**Futuro (post-defensa):**
- Replica set MongoDB (3 nodos)
- Redis para cache + rate limiter distribuido
- Estatificación de assets en CDN
- Load balancer Nginx

#### **RN-5: Usabilidad**

| Aspecto | Requisito |
|---------|-----------|
| Responsive | Funcional en móvil 390px, tablet 768px, desktop 1920px |
| i18n | Español/Portugués, sin cambio en sesión activa |
| Accesibilidad | WCAG 2.1 Level AA: `<fieldset>`, `<legend>`, `role`, semántica HTML5 |
| Empty states | Icono + texto explicativo (no solo grilla vacía) |
| Validación cliente | HTML5 `required`, `pattern`; servidor es authoritative |
| Microcopy | Concreto, sin aspiracional; 1 error message por campo |

#### **RN-6: Mantenibilidad**

| Aspecto | Requisito |
|--------|-----------|
| Cobertura tests | 90%+ (servicios core) |
| Fuzz testing | PhoneNumber: 27 tests parametrizados (UY/BR, edge cases) |
| Static analysis | PMD 0 violaciones críticas, compiler warnings = errors |
| Documentación | 12 docs canónicos: requisitos, arquitectura, diagramas, tradeoffs |
| Código | Convención de commits, policy de squashing, git history limpio |

#### **RN-7: Portabilidad**

- **Java:** 21+ (LTS)
- **Spring Boot:** 3.2.x
- **MongoDB:** 5.0+ (Atlas o local)
- **Browser:** Chrome 100+, Firefox 98+, Safari 15+ (Chromium core tests)
- **OS:** Linux/macOS/Windows (Spring Boot portable)

---

## 4. Interfaces Externas

### 4.1 Interfaz de Usuario

**Páginas principales:**

| Ruta | Rol | Propósito |
|------|-----|----------|
| `/` | Público | Landing, CTA |
| `/entrar` | Anónimo | Login |
| `/registrarse` | Anónimo | Registro |
| `/solicitar` | Ciudadano | Crear solicitud |
| `/rastrear` | Invitado | Rastrear por tel+código |
| `/mis-solicitudes` | Ciudadano | Dashboard + historial |
| `/notificaciones` | Ciudadano | Bandeja in-app |
| `/acopio/inicio` | Org | Dashboard org |
| `/acopio/solicitudes` | Org | Kanban + filtros |
| `/mi-organizacion` | Org | Perfil org |
| `/docs/{file}` | Público | Documentación Markdown |

**Componentes reutilizables (Thymeleaf fragments):**

- `ui::msg` (alert success/error/warning/info)
- `ui::state` (empty state con ícono)
- `forms::field` (input con validación visual)
- `forms::submit` (botón con loader)
- `forms::phone` (selector país + teléfono)
- `card::request` (resumen de solicitud)

### 4.2 Interfaz de Datos

**REST API (OpenAPI 3.0 en `/swagger-ui.html`):**

| Método | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/api/solicitudes/{id}` | USER | Detalle solicitud |
| POST | `/api/solicitudes` | USER | Crear solicitud |
| PUT | `/api/solicitudes/{id}` | USER | Editar draft |
| DELETE | `/api/solicitudes/{id}` | USER | Eliminar draft |
| POST | `/api/solicitudes/{id}/aceptar` | ORG | Aceptar |
| POST | `/api/solicitudes/{id}/rechazar` | ORG | Rechazar |
| POST | `/api/solicitudes/{id}/completar` | ORG | Completar |
| GET | `/api/notificaciones` | USER | Listar notificaciones |
| POST | `/api/notificaciones/{id}/leer` | USER | Marcar como leído |
| GET | `/api/notificaciones/count` | USER | Conteo sin leer |

**Formatos:**
- Input/Output: JSON
- Errores: `{error: "clave_i18n", details: "..."}` (HTTP 400/403/500)
- Validación: server-side authoritative

### 4.3 Interfaz de Hardware

- **Almacenamiento:** Disco local en `/uploads/` (configurable vía `UPLOAD_DIR`)
- **Base de datos:** MongoDB (TCP 27017 por defecto)
- **Conectividad:** TCP/IP HTTP(S) solo

### 4.4 Interfaz de Software

**Dependencias de terceros:**

| Dependencia | Versión | Propósito |
|------------|---------|-----------|
| Spring Boot | 3.2.x | Framework |
| Spring Security | 6.x | Autenticación |
| MongoDB Driver | spring-data | Persistencia |
| Thymeleaf | 3.x | Rendering |
| JUnit 5 | 5.9+ | Testing |
| Mockito | 5.x | Mocks |
| Playwright | 1.40+ | E2E tests |

---

## 5. Atributos del Producto

### 5.1 Confiabilidad

- **MTBF (Mean Time Between Failures):** N/A (0 usuarios reales)
- **MTTR (Mean Time To Repair):** Objetivo 1 hora
- **Recuperación:** Backup diario de Mongo, restore manual

### 5.2 Integridad de Datos

- **Validación entrada:** Antes de persistir (ValidationException)
- **Transactions:** `@Transactional` en mutaciones críticas (accept/reject/complete)
- **Concurrencia:** Optimistic locking con `@Version` en Request
- **Backup:** Diario a las 3am UTC (MongoDB Atlas)

### 5.3 Cumplimiento de Estándares

- **Teléfono:** E.164 (RFC 3966)
- **Fechas:** ISO 8601
- **Códigos país:** ISO 3166-1 (UY/BR)
- **HTTP:** RFC 7231, status codes estándar

---

## 6. Restricciones de Diseño

### 6.1 Restricciones de Estándares

- **Framework:** Spring Boot 3.2 (obligatorio por tesis)
- **Lenguaje:** Java 21 (LTS)
- **Frontend:** Thymeleaf SSR, NO frameworks JS (Vanilla + fetch)
- **CSS:** BEM + variables, NO Tailwind/Bootstrap
- **Persistencia:** MongoDB (NO SQL relacional)

### 6.2 Restricciones de Implementación

- **i18n:** Un JSON por idioma, server-side + client-side
- **Componentes:** Reutilizables en `templates/fragments/`
- **JavaScript:** Exclusivo de página en `static/js/{pagina}.js`
- **Imágenes:** Local en disco (NO S3/cloud)

### 6.3 Restricciones de Negocio

- **Costo:** MVP gratuito (0 presupuesto)
- **Timeline:** Completar antes de 2026-10-15 (defensa)
- **Personal:** 1 desarrollador (Federico)
- **Usuarios pilotos:** 0 (fase testing manual)

---

## 7. Otros Requisitos

### 7.1 Legales y de Cumplimiento

- **Privacidad:** Sin recopilación de datos sensibles
- **Consentimiento:** No requiere (demo, sin usuarios reales)
- **Términos de servicio:** Diferido para producción

### 7.2 Documentación

Todos los requisitos deben documentarse en:
- `docs/REQUISITOS.md` (catálogo RF/RN)
- `docs/ENDPOINTS.md` (API contracts)
- `docs/DIAGRAMAS.md` (flujos)
- Comentarios de código (para dominios complejos)

### 7.3 Testing

**Cobertura mínima:**
- RF core: 90% de instrucciones
- RN seguridad: 100% (validaciones)
- RN performance: Benchmark (no test, diferido)
- Edge cases: Fuzz tests (PhoneNumber)

---

## 8. Matriz de Trazabilidad

| RF | Componente | Tests | Estado |
|----|-----------|-------|--------|
| RF-1 | AuthController, UserService | 9 | ✅ Implementado |
| RF-3 | RequestCreateController, RequestValidator | 12 | ✅ Implementado |
| RF-4 | GuestTrackingController | 4 | ✅ Implementado |
| RF-6 | RequestService, RequestStateMachine | 15 | ✅ Implementado |
| RF-7 | OrgProfileController, UserService | 7 | ✅ Implementado |
| RF-9 | NotificationService, NotificationController | 12 | ✅ Implementado |

---

## 9. Aprobación

| Rol | Nombre | Fecha | Firma |
|-----|--------|-------|-------|
| Desarrollador | Federico Goncálvez | 2026-09-24 | ✓ |
| Tribunal | [Tribunal UTEC] | [Fecha defensa] | __ |

---

**Documento finalizado:** 2026-09-24  
**Próxima revisión:** Post-defensa de tesis  
**Control de versiones:** Git @ github.com/fedegonc/residuosolido
