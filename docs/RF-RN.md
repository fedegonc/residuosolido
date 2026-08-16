# Reglas de Negocio y Requerimientos Funcionales — EcoSolicitud

## 1. Actores del sistema

### Invitado
- **Descripción:** Vecino o ciudadano que desea solicitar recolección de residuos reciclables sin necesidad de registrarse previamente.
- **Qué NO puede hacer:**
  - Ver un dashboard personal ni historial de solicitudes (salvo vía rastreo por teléfono)
  - Editar ni eliminar solicitudes
  - Acceder a funciones de organización
  - Recibir notificaciones por WhatsApp (no hay teléfono verificado asociado a una cuenta)

### Usuario
- **Descripción:** Ciudadano registrado que puede crear, editar y trackear sus solicitudes de recolección, y gestionar su perfil.
- **Qué NO puede hacer:**
  - Aceptar, rechazar o completar solicitudes de otros usuarios
  - Acceder al panel de organización (`/acopio/**`)
  - Gestionar recolectores informales
  - Ver métricas privadas de organizaciones

### Organización
- **Descripción:** Cooperativa o centro de acopio registrado que recibe solicitudes, gestiona el ciclo de vida de las mismas y administra recolectores informales.
- **Qué NO puede hacer:**
  - Crear solicitudes de recolección para sí misma
  - Acceder a rutas de usuario (`/usuarios/**`, `/solicitudes` como lista)
  - Eliminar solicitudes (solo puede cambiar su estado: aceptar, rechazar, completar)
  - Registrar nuevos usuarios o organizaciones

---

## 2. Requerimientos Funcionales (RF-1 a RF-8)

### RF-1: Registro de usuario
- **Actor(es):** Invitado
- **Descripción:** Permite a un invitado crear una cuenta como Usuario o como Organización, completando username, email, nombre, contraseña y (opcional) checkbox de organización.
- **Precondición:** No estar autenticado. Username y email no deben existir previamente en el sistema.
- **Postcondición / resultado esperado:** Nuevo `User` persistido con `role=USER` o `role=ORGANIZATION`, contraseña hasheada, `active=true`. Redirección a login con mensaje de éxito.
- **Reglas de negocio asociadas:** RN-01, RN-02, RN-03, RN-04
- **Pantalla(s) relacionada(s):** `auth/register.html`

### RF-2: Inicio de sesión
- **Actor(es):** Usuario, Organización
- **Descripción:** Permite a un usuario registrado autenticarse en el sistema mediante username y contraseña.
- **Precondición:** Tener una cuenta activa creada previamente.
- **Postcondición / resultado esperado:** Sesión creada, redirección a `/usuarios/inicio` (USER) o `/acopio/inicio` (ORGANIZATION). Si la org no completó perfil, redirección a `/acopio/completar-perfil`.
- **Reglas de negocio asociadas:** RN-01, RN-05
- **Pantalla(s) relacionada(s):** `auth/login.html`

### RF-3: Solicitud de recolección
- **Actor(es):** Invitado, Usuario
- **Descripción:** Permite crear una solicitud de recolección de residuos reciclables, seleccionando ciudad, organización, materiales, dirección y (opcional) foto. Los invitados deben proveer nombre y teléfono.
- **Precondición:** Que exista al menos una organización activa en la ciudad seleccionada. El invitado está sujeto a rate limiting.
- **Postcondición / resultado esperado:** `Request` persistida con `status=PENDING`, organización asignada, materiales seleccionados. En ambos casos (invitado o usuario), redirección a `/solicitudes/exito`, cuya vista muestra contenido condicional según `isGuest` (invitado ve el link de rastreo por teléfono; usuario ve el link a `/solicitudes`).
- **Reglas de negocio asociadas:** RN-06, RN-07, RN-08, RN-09, RN-10
- **Pantalla(s) relacionada(s):** `users/request-form.html`

### RF-4: Rastreo de solicitud por teléfono
- **Actor(es):** Invitado, Usuario
- **Descripción:** Permite buscar solicitudes asociadas a un número de teléfono, sin necesidad de login.
- **Precondición:** Haber enviado al menos una solicitud como invitado con ese teléfono.
- **Postcondición / resultado esperado:** Lista de solicitudes mostrada con ID, dirección, ciudad, materiales, fecha y estado localizado.
- **Reglas de negocio asociadas:** RN-08
- **Pantalla(s) relacionada(s):** `users/track.html`

### RF-5: Gestión de solicitudes por usuario
- **Actor(es):** Usuario
- **Descripción:** Permite al usuario ver la lista de sus solicitudes, ver el detalle de cada una, editar solicitudes pendientes y eliminar solicitudes.
- **Precondición:** Estar autenticado como USER.
- **Postcondición / resultado esperado:** Lista paginada de solicitudes del usuario. Solo puede editar/eliminar solicitudes propias en estado PENDING.
- **Reglas de negocio asociadas:** RN-07, RN-11
- **Pantalla(s) relacionada(s):** `users/requests.html`, `users/request-detail.html`, `users/request-form.html` (modo edición)

### RF-6: Gestión de solicitudes por organización
- **Actor(es):** Organización
- **Descripción:** Permite a la organización ver las solicitudes asignadas, filtrar por estado, ver detalle, aceptar (con horario confirmado), rechazar y completar solicitudes.
- **Precondición:** Estar autenticado como ORGANIZATION con perfil completado.
- **Postcondición / resultado esperado:** Cambio de estado de la solicitud (PENDING→IN_PROGRESS, PENDING→REJECTED, IN_PROGRESS→COMPLETED). Notificación WhatsApp enviada al contacto en cada transición.
- **Reglas de negocio asociadas:** RN-07, RN-12, RN-13
- **Pantalla(s) relacionada(s):** `org/requests.html`, `org/dashboard.html`

### RF-7: Gestión de perfil y onboarding de organización
- **Actor(es):** Organización
- **Descripción:** Permite a una organización completar su perfil (teléfono + ciudad) tras el registro, y posteriormente editar email, nombre, teléfono y ciudad.
- **Precondición:** Estar autenticado como ORGANIZATION. Para onboarding, `profileCompleted=false`.
- **Postcondición / resultado esperado:** `User` actualizado con datos de contacto y ciudad. `profileCompleted=true` tras onboarding. Acceso al dashboard desbloqueado.
- **Reglas de negocio asociadas:** RN-04, RN-14
- **Pantalla(s) relacionada(s):** `org/complete-profile.html`, `org/profile.html`

### RF-8: Registro de recolectores informales
- **Actor(es):** Organización
- **Descripción:** Permite a la organización registrar, editar y desactivar recolectores informales asociados, indicando nombre, teléfono, ciudad, materiales que maneja y notas.
- **Precondición:** Estar autenticado como ORGANIZATION con perfil completado.
- **Postcondición / resultado esperado:** `InformalCollector` persistido, vinculado a la organización. Lista actualizada en la vista.
- **Reglas de negocio asociadas:** RN-08, RN-14
- **Pantalla(s) relacionada(s):** `org/catadores.html`

---

## 3. Reglas de Negocio (RN)

### RN-01: Password mínimo 3 caracteres
- **Aplica a:** RF-1, RF-2
- **Regla:** La contraseña debe tener al menos 3 caracteres.
- **Validación:** Ambos — client-side (hint "Mínimo 3 caracteres" en `register.html`, sin `minlength` HTML) y server-side (`UserRegistrationService.validateUserRegistration()` y `UserService.updateUser()`)
- **Excepciones:** Ninguna

### RN-02: Username sin espacios y único
- **Aplica a:** RF-1
- **Regla:** El username no puede contener espacios y no debe existir previamente en la base de datos.
- **Validación:** Server-side (`UserRegistrationService.validateUserRegistration()`)
- **Excepciones:** Ninguna

### RN-03: Email válido y único
- **Aplica a:** RF-1
- **Regla:** El email debe contener `@` y no existir previamente en la base de datos.
- **Validación:** Server-side (`UserRegistrationService.validateUserRegistration()`)
- **Excepciones:** Ninguna

### RN-04: Ciudad obligatoria para organizaciones
- **Aplica a:** RF-1, RF-7
- **Regla:** Las organizaciones deben completar su ciudad (y teléfono) antes de poder acceder al dashboard. El onboarding es forzado.
- **Validación:** Server-side (`OrgDashboardController` redirige a `/acopio/completar-perfil` si `needsProfileCompletion()`)
- **Excepciones:** Ninguna

### RN-05: Redirección post-login por rol
- **Aplica a:** RF-2
- **Regla:** Tras login exitoso, USER va a `/usuarios/inicio`, ORGANIZATION va a `/acopio/inicio` (o `/acopio/completar-perfil` si falta perfil).
- **Validación:** Server-side (`LoginSuccessHandler` / `RoleBasedLoginTargetUrlResolver`)
- **Excepciones:** Ninguna

### RN-06: Organización debe pertenecer a la ciudad seleccionada
- **Aplica a:** RF-3
- **Regla:** La organización elegida en la solicitud debe estar registrada en la misma ciudad que la solicitud. No se confía en el `organizationId` del form.
- **Validación:** Server-side (`CityOrgService.findOrganizationByIdAndCity()` valida que el org pertenezca a la ciudad)
- **Excepciones:** Ninguna

### RN-07: Ciclo de estados de solicitud
- **Aplica a:** RF-3, RF-5, RF-6
- **Regla:** El ciclo de estados es: `PENDING` → `IN_PROGRESS` (aceptada) o `REJECTED` → `COMPLETED`. Solo se puede editar/eliminar una solicitud en estado `PENDING`. La organización solo puede aceptar/rechazar si está `PENDING`, y completar si está `IN_PROGRESS`.
- **Validación:** Server-side (`Request.canBeEdited()`, `Request.accept()`, `Request.complete()`, `Request.reject()`)
- **Excepciones:** Ninguna

### RN-08: Cobertura binacional
- **Aplica a:** RF-3, RF-4, RF-8
- **Regla:** El sistema opera en Rivera (Uruguay) y Sant'Ana do Livramento (Brasil). Los nombres de ciudad se muestran localizados según idioma del navegador.
- **Validación:** Server-side (enum `City` con `RIVERA` y `LIVRAMENTO`). i18n con keys `city.RIVERA` y `city.LIVRAMENTO`.
- **Excepciones:** Ninguna

### RN-09: Rate limiting para invitados
- **Aplica a:** RF-3
- **Regla:** Los invitados están sujetos a rate limiting en la creación de solicitudes (prevención de spam/abuso).
- **Validación:** Server-side (`GuestRateLimiter.isAllowed()` basado en IP)
- **Excepciones:** Usuarios autenticados no están sujetos a rate limiting.

### RN-10: Mínimo 1 material en la solicitud
- **Aplica a:** RF-3
- **Regla:** La solicitud debe incluir al menos un material reciclable seleccionado.
- **Validación:** Ambos — client-side (`validateMaterials()` en `request-form.html`) y server-side (`RequestValidator.validateCoreFields()` lanza `IllegalArgumentException` si `materials` es `null` o vacío).
- **Excepciones:** Ninguna

### RN-11: Solo solicitudes propias y pendientes pueden editarse/eliminarse
- **Aplica a:** RF-5
- **Regla:** Un usuario solo puede editar o eliminar solicitudes donde `request.user == user` y `request.status == PENDING`.
- **Validación:** Server-side (`RequestQueryService.getOwnedRequest()` verifica propiedad; `RequestQueryService.getEditableOwnedRequest()` verifica propiedad + estado. `RequestUpdateService.updateRequest()` y `RequestUpdateService.deleteOwnedRequest()` usan `getEditableOwnedRequest()`, por lo que ambas operaciones exigen PENDING)
- **Excepciones:** Ninguna

### RN-12: Notificación WhatsApp en transiciones de estado
- **Aplica a:** RF-6
- **Regla:** Se envía notificación por WhatsApp al teléfono de contacto del solicitante cuando la organización acepta, rechaza o completa una solicitud.
- **Validación:** Server-side (`RequestTransitionService` llama a `NotificationService.sendWhatsApp()` en cada transición: `acceptRequest()`, `rejectRequest()`, `completeRequest()`)
- **Excepciones:** Si no hay teléfono de contacto (`getContactPhone()` retorna null), no se envía notificación.

### RN-13: Horario confirmado al aceptar
- **Aplica a:** RF-6
- **Regla:** Al aceptar una solicitud, la organización debe seleccionar un horario confirmado (`TimeSlot`: MAÑANA, TARDE, NOCHE).
- **Validación:** Server-side (`OrgRequestController` recibe `@RequestParam("confirmedSlot") TimeSlot confirmedSlot` como obligatorio)
- **Excepciones:** Ninguna

### RN-14: Perfil de usuario y organización son editables
- **Aplica a:** RF-7
- **Regla:** Tanto el Usuario (rol USER) como la Organización pueden editar sus datos de contacto (email, nombre, teléfono, ciudad).
- **Validación:** Server-side — `UserProfileController.updateProfile()` maneja `POST /usuarios/perfil` (rol USER), `OrgProfileController` maneja la edición de organización. Ambos templates (`users/profile.html`, `org/profile.html`) tienen `<form>` real.
- **Excepciones:** Ninguna

---

## 4. Matriz RF ↔ Pantalla ↔ Actor

| RF | Pantalla | Actor | Estado |
|----|----------|-------|--------|
| RF-1 | `auth/register.html` | Invitado | Implementado |
| RF-2 | `auth/login.html` | Usuario, Organización | Implementado |
| RF-3 | `users/request-form.html` | Invitado, Usuario | Implementado |
| RF-4 | `users/track.html` | Invitado, Usuario | Implementado |
| RF-5 | `users/requests.html`, `users/request-detail.html`, `users/request-form.html` | Usuario | Implementado |
| RF-6 | `org/requests.html`, `org/dashboard.html` | Organización | Implementado |
| RF-7 | `org/complete-profile.html`, `org/profile.html` | Organización | Implementado |
| RF-8 | `org/catadores.html` | Organización | Implementado |
| — | `index.html` | Invitado, Usuario, Organización | Implementado (landing, no mapea a un RF específico) |
| — | `metrics.html` | Público | Implementado (métricas públicas, no mapea a un RF específico) |
| — | `users/dashboard.html` | Usuario | Implementado (dashboard, sub-función de RF-5) |
| — | `users/profile.html` | Usuario | Implementado (perfil editable, sub-función de RF-5) |
| — | `error/404.html` | Todos | Implementado |

---

## 5. Fuera de alcance (explícito)

- **No hay rol Admin.** El sistema no tiene un panel de administración general. Las funciones de gestión están distribuidas por rol (USER gestiona sus solicitudes, ORGANIZATION gestiona las suyas).
- **No hay blog/posts/categorías.** No existe contenido editorial ni CMS.
- **No hay mapas/geolocalización interactiva.** La selección de ciudad es por dropdown, no por mapa. (Nota: existió una implementación con Leaflet en `org/profile.html` pero no es parte de los RF core.)
- **No hay ruteo ni recurrencia fija.** La organización gestiona la logística de recolección por fuera del sistema.
- **No hay estimación de cantidad/volumen.** El sistema no solicita ni almacena peso/volumen de materiales. Eso se coordina por WhatsApp.
- **No hay chat/mensajería interna.** La comunicación entre usuario y organización es por WhatsApp.
- **No hay notificaciones push ni email.** Solo WhatsApp como canal de notificación.
- **No hay gestión de vehículos, EPP, uniformes, combustible.** Eso es logística física de la organización.
- **No hay capacitación de catadores.** Es un proceso humano/pedagógico, no digitalizable en este sistema.
- **No hay multi-tenant ni multi-organización jerárquica.** Cada organización es independiente.

**Origen de estas decisiones:** basadas en el Oficio n° 044/2023 (Proyecto Frontera de la Paz Sustentable) y en la auditoría de dominio realizada con stakeholders.

**Nota para la defensa de tesis:** estos puntos no son omisiones — son decisiones de alcance conscientes, justificadas porque exceden lo que una herramienta de software puede o debe resolver.

---

## 6. Criterio de alcance y backlog pendiente

**Criterio para decidir si algo nuevo entra al alcance** (chequear en este orden):
1. ¿Está en el oficio o surge de una necesidad real confirmada por el stakeholder (organización/usuario)?
2. ¿Es responsabilidad de un sistema de software, o es logística/inversión física/proceso humano?
3. ¿Se puede resolver con un campo o servicio simple, o requiere una entidad/módulo nuevo?

Si 1 es sí, 2 es "sí es del software" y 3 es "simple" → entra al backlog. Si no, se documenta como limitación consciente (sección 5).

**Backlog pendiente (no implementado):**
- 🟡 **Métricas privadas por organización + descarga PDF.** Renombrar `PublicMetricsController` → `OrgMetricsController` (sigue siendo público en `/metricas`, sin `@PreAuthorize`); nueva ruta protegida `/acopio/metricas`; endpoint `GET /acopio/metricas/pdf` (sugerido: OpenPDF o iText community) — ninguna de estas tres cosas está implementada.
- 🟡 **Consistencia de nombres** (baja prioridad): revisar que los nombres de métodos de `RequestQueryService`/`RequestOrgService`/`RequestMetricsService`/`CityOrgService` reflejen consistentemente su sub-dominio.
