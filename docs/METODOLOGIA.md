# Metodología — Modelo Iterativo Incremental

El sistema **Residuo Sólido** se desarrolló siguiendo un **modelo
iterativo incremental** adaptado a las restricciones de un MVP de
tesis (tecnólogo en Análisis y Desarrollo de Sistemas). Este documento
describe las 4 fases del modelo aplicado, su alcance y los artefactos
producidos en cada una.

---

## Por qué iterativo incremental

El proyecto tiene un plazo acotado (una semana para la entrega) y un
alcance definido por el oficio de la carrera. Un modelo en cascada puro
exigiría especificar todo antes de programar; un modelo ágil puro
(Scrum, sprints de 2 semanas) no encaja con un solo desarrollador y un
plazo tan corto.

El **iterativo incremental** permite:

- Entregar valor usable al final de cada fase (no esperar al final).
- Reducir riesgo temprano (validar arquitectura y seguridad antes de
  agregar features).
- Documentar y probar incrementalmente (cada fase deja tests que
  protegen a la siguiente).
- Ajustar alcance sin rehacer todo (las fases son acumulativas).

---

## Fase 1 — Fundación y seguridad

**Objetivo:** establecer la arquitectura base, autenticación, roles y
las reglas de seguridad más críticas.

**Artefactos producidos:**

- Stack: Java 21 + Spring Boot 3.2 + Thymeleaf + MongoDB.
- `User` con roles `USER` / `ORGANIZATION` (sin rol Admin).
- `SecurityConfig` con CSRF, login por formulario, bloqueo por intentos
  fallidos (`LoginAttemptService`).
- `RegistrationForm` (DTO) para evitar mass-assignment.
- Onboarding forzado de organización (`profileCompleted`).
- Validaciones server-side de email, username y contraseña.
- Rate limiting de invitados (`GuestRateLimiter`).
- 11 tests de seguridad crítica (`CriticalSecurityTest`).

**Cierre de fase:** el sistema autentica, autoriza por rol y rechaza
entradas maliciosas básicas. No hay flujo de solicitudes todavía.

---

## Fase 2 — Flujo principal de solicitudes

**Objetivo:** implementar el caso de uso central: un ciudadano (o
invitado) crea una solicitud y una organización la gestiona.

**Artefactos producidos:**

- `Request` con ciclo de estados (`PENDING → IN_PROGRESS / REJECTED →
  COMPLETED`), `@Version` para optimistic locking.
- `RequestCreateController` + `RequestService` (creación con imagen
  opcional).
- `CityOrgService` — resolución de organización por ciudad y materiales.
- `OrgRequestController` + `RequestTransitionService` (aceptar /
  rechazar / completar).
- Rastreo de invitados por teléfono + código privado de 8 caracteres.
- `InformalCollector` (CRUD interno de la organización).
- Tests de servicios (validación, transiciones, queries, métricas).

**Cierre de fase:** el flujo end-to-end funciona. Un usuario puede
registrarse, crear una solicitud, y una organización puede aceptarla y
completarla. 14 tests E2E (`EndToEndFlowsTest`) validan el flujo
completo.

---

## Fase 3 — Endurecimiento, métricas y usabilidad

**Objetivo:** cerrar brechas de seguridad, agregar métricas, mejorar
la experiencia de usuario y la consistencia visual.

**Artefactos producidos:**

- `PhoneNumber` con `CountryCode` (UY +598 / BR +55) y normalización
  E.164.
- Revalidación de materiales en edición de solicitudes.
- Borrado con optimistic locking (`delete(entity)`).
- `PublicMetricsController` + `PublicMetricsService` (métricas
  agregadas por ciudad).
- Dashboard de organización con estadísticas (faceted aggregation).
- i18n español / portugués con `data-i18n` en templates.
- Sistema de diseño canónico: variables CSS, BEM, dark mode,
  transiciones suaves, `prefers-reduced-motion`.
- PWA: `manifest.json`, service worker, iconos PNG.
- 218 tests, 0 failures.

**Cierre de fase:** el sistema es seguro, consistente, bilingüe y
instalable como PWA. Las métricas públicas funcionan.

---

## Fase 4 — Contenido, defensa y documentación

**Objetivo:** preparar el sistema para la defensa de tesis: contenido
público que transmita el propósito, documentación sincronizada,
diagramas, tradeoffs explícitos.

**Artefactos producidos:**

- Blog estático (`/blog`, `/blog/{slug}`) con 3 artículos:
  recolectores informales, galpones de acopio, Frontera de la Paz.
- Cards de blog en el index (con acentos binacionales azul/verde).
- Kanban integrado al dashboard de organización (no página aparte).
- "Mis solicitudes" — modal canónico con rastreo sin cuenta / login.
- Política de contraseña mínima de 8 caracteres (documentada en
  `docs/MEJORAS.md`).
- Documentación completa y sincronizada:
  - `README.md` — instalación, stack y mapa de documentos.
  - `docs/INDICE.md` — punto de entrada a toda la documentación.
  - `docs/ENDPOINTS.md` — rutas HTTP, OpenAPI y testing.
  - `docs/DIAGRAMAS.md` — arquitectura, diagramas UML y RF-RN en anexo.
  - `docs/DEFENSA.md` — guía de defensa, tradeoffs y limitaciones.
  - `docs/MEJORAS.md` — mejoras implementadas y correcciones.
  - `docs/METODOLOGIA.md` — este archivo (fases y copys en anexo).
- Diagramas draw.io: casos de uso, modelo lógico, clases, estados UML.

**Cierre de fase:** el sistema está listo para la defensa. La
documentación refleja el estado real del código. Los tradeoffs están
explícitos. Las limitaciones están documentadas como decisiones
conscientes, no como omisiones.

---

## Resumen de fases

| Fase | Foco | Entregable principal | Tests |
|---|---|---|---|
| 1 | Fundación y seguridad | Auth, roles, validación | ~30 |
| 2 | Flujo de solicitudes | CRUD Request, transiciones | ~100 |
| 3 | Endurecimiento y UX | Métricas, i18n, PWA, diseño | ~60 |
| 4 | Contenido y defensa | Blog, docs, diagramas | 218 total |

Cada fase es **incremental** (agrega funcionalidad nueva) e
**iterativa** (refina y endurece lo anterior). Los tests de cada fase
protegen a la siguiente contra regresiones.

---

## Relación con Design Science Research Methodology (DSRM)

El modelo iterativo incremental aplicado es compatible con el marco
DSRM de Peffers et al. (2007), usado aquí como marco de análisis
retrospectivo, no como metodología seguida desde el inicio.

| Etapa DSRM | Correspondencia en el proyecto |
|---|---|
| 1. Identificación del problema | Necesidad de coordinación entre ciudadanos y organizaciones |
| 2. Objetivos de la solución | MVP que gestiona solicitudes de recolección y su estado |
| 3. Diseño y desarrollo | Arquitectura, modelo de datos, reglas de negocio, interfaz |
| 4. Demostración | Demo con datos sintéticos (flujo end-to-end) |
| 5. Evaluación | 218 tests + verificación en navegador; usabilidad pendiente |
| 6. Comunicación | Documentación técnica, diagramas UML, esta defensa |

**Referencia:** Peffers, K., Tuunanen, T., Rothenberger, M. A., &
Chatterjee, S. (2007). A Design Science Research Methodology for
Information Systems Research. *MIS Quarterly*, 32(1), 77–105.
[DOI: 10.2753/MIS0742-1222240302](https://doi.org/10.2753/MIS0742-1222240302)

---

## Fuentes académicas y contexto

Ver `docs/DEFENSA.md` §8 para la lista completa de fuentes UTEC,
metodología, sistemas comparables y contexto territorial.

---

# Copys — Single Source of Truth (anexo)


> **Propósito:** centralizar todos los textos de interfaz del sistema
> en un único documento para detectar inconsistencias, copies mezclados
> y crecimientos innecesarios.
>
> **Fuente de verdad:** este documento. Los templates usan `data-i18n`
> con fallback en español. Los JSON de `i18n/common/` son para el
> cliente (JS). Los `messages_*.properties` son para el servidor
> (Thymeleaf).
>
> **Fecha:** commit `1e4d575` · **Tests:** 218, 0 failures

---

## Convenciones

- **Verbo único por flujo:** "Buscar" (no mezclar con "Rastrear")
- **Tono:** directo, profesional, austero. Sin aspiracional.
- **Sin slop:** no "futuro más limpio", "mejor destino", "sostenible",
  "contribuí a una ciudad", "sumate a la comunidad".
- **CTAs:** imperativo, concreto. "Pedir recolección", no "Enviar solicitud".
- **Estados vacíos:** cálido-moderado. "Acá van a aparecer...", no "No hay datos".

---

## 1. Público (sin login)

### 1.1 Index — Hero

| Clave | Texto (ES) |
|---|---|
| `badge` | Rivera · Sant'Ana do Livramento |
| `title` | Pedí que pasen a buscar tus reciclables |
| `subtitle` | Una cooperativa de tu ciudad los retira en el día y horario que coordinen. |
| `cta_request` | Pedir recolección |
| `cta_track` | Ver mi solicitud |

### 1.2 Index — Cómo funciona

| Clave | Texto (ES) |
|---|---|
| `how_step1` | Solicitá |
| `how_step1_desc` | Elegí tu ciudad y los materiales que tenés. |
| `how_step2` | Asignamos |
| `how_step2_desc` | Elegís una cooperativa de tu ciudad que acepte esos materiales. |
| `how_step3` | Retiran |
| `how_step3_desc` | Coordinan día y horario para el retiro. |
| `how_step4` | Impactamos |
| `how_step4_desc` | Seguís el estado con tu teléfono y código de rastreo. |

### 1.3 Index — Coop CTA

| Clave | Texto (ES) |
|---|---|
| `org_cta_title` | ¿Retirás reciclables en Rivera o Sant'Ana? |
| `org_cta_desc` | Registrate para recibir solicitudes de recolección en tu zona. |
| `org_cta_button` | Registrarme como organización |

### 1.4 Métricas

| Clave | Texto (ES) |
|---|---|
| `metrics_title` | Métricas públicas |
| `metrics_total` | Recolecciones completadas |
| `metrics_by_city` | Por ciudad |
| `metrics_empty` | Aún no hay datos disponibles |

### 1.5 Error 404

| Clave | Texto (ES) |
|---|---|
| `error_404_desc` | La página que buscás no existe o se movió de lugar. |
| `error_404_back` | Volver al inicio |

---

## 2. Navbar y layout

### 2.1 Navbar

| Clave | Texto (ES) |
|---|---|
| `nav_home` | Inicio |
| `nav_request` | Solicitar recolección |
| `nav_org_panel` | Panel acopio |
| `nav_my_requests` | Mis solicitudes |
| `nav_login` | Entrar |
| `nav_register` | Registrarse |
| `nav_logout` | Cerrar sesión |
| `nav_org_profile` | Perfil |
| `nav_user_panel` | Mi panel |
| `nav_user_profile` | Mi perfil |
| `nav_user_requests` | Mis solicitudes |
| `skip_content` | Saltar al contenido |
| `footer_rights` | Todos los derechos reservados. |

### 2.2 Modal "Mis solicitudes" (invitado)

| Clave | Texto (ES) |
|---|---|
| `modal_track_title` | ¿Cómo querés ver tus solicitudes? |
| `modal_track_subtitle` | Podés rastrear una solicitud sin tener cuenta usando tu teléfono y código, o entrar a tu cuenta para ver todas las que creaste. |
| `modal_track_guest_title` | Buscar sin cuenta |
| `modal_track_guest_desc` | Ingresá tu teléfono y el código que te dimos. |
| `modal_track_login_title` | Entrar a mi cuenta |
| `modal_track_login_desc` | Si ya tenés cuenta, entrá para ver todas tus solicitudes en un solo lugar. |

---

## 3. Auth

### 3.1 Login

| Clave | Texto (ES) |
|---|---|
| `auth_login_title` | Iniciar sesión |
| `auth_login_subtitle` | Entrá para ver y gestionar tus solicitudes. |
| `auth_login_button` | Entrar |
| `auth_login_link` | Iniciar sesión |
| `auth_no_account` | ¿No tenés cuenta? |
| `auth_register_link` | Registrarse |
| `auth_welcome_badge` | Rivera · Sant'Ana do Livramento |
| `auth_welcome_title` | ¡Bienvenido de nuevo! |
| `auth_welcome_subtitle` | Entrá para gestionar tus solicitudes de recolección. |

### 3.2 Register

| Clave | Texto (ES) |
|---|---|
| `auth_register_title` | Crear cuenta |
| `auth_register_subtitle` | Creá una cuenta para pedir recolección y seguir el estado. |
| `auth_register_button` | Registrarse |
| `auth_register_link` | Registrarse |
| `auth_have_account` | ¿Ya tenés cuenta? Iniciá sesión |
| `auth_join_title` | Sumate a la red de reciclaje |
| `auth_join_subtitle` | Creá una cuenta para pedir o recibir solicitudes de recolección. |
| `auth_welcome_badge` | Rivera · Sant'Ana do Livramento |

### 3.3 Campos de formulario

| Clave | Texto (ES) |
|---|---|
| `auth_username` | Usuario |
| `auth_email` | Email |
| `auth_password` | Contraseña |
| `auth_is_organization` | Soy una cooperativa o reciclador |
| `auth_is_organization_hint` | Marcá esta opción si querés recibir solicitudes de recolección. Podés gestionar tu perfil de organización después. |
| `auth_or` | o |

---

## 4. Track / Rastreo

| Clave | Texto (ES) |
|---|---|
| `track_title` | Buscar mi solicitud |
| `track_subtitle` | Si pediste recolección sin cuenta, ingresá tu teléfono y el código que te dimos. |
| `track_phone_label` | Teléfono |
| `track_code_label` | Código de rastreo |
| `track_search_btn` | Buscar |
| `track_no_results` | No encontramos solicitudes con esos datos |
| `track_no_results_desc` | Revisá el teléfono y el código. El código tiene 8 letras y números. |
| `track_empty_cta` | Crear una solicitud |
| `track_results_title` | Estas son tus solicitudes |
| `track_request_id` | Solicitud |
| `track_no_org` | Sin asignar |

---

## 5. Usuario (ROLE_USER)

### 5.1 Dashboard

| Clave | Texto (ES) |
|---|---|
| `dash_welcome` | Hola |
| `dash_subtitle` | Acá ves tus solicitudes de recolección y su estado. |
| `dash_new_request` | Nueva solicitud |
| `dash_recent` | Solicitudes recientes |
| `dash_view_all` | Ver lista completa |
| `dash_no_requests` | Acá van a aparecer tus solicitudes |
| `dash_no_requests_desc` | Cuando crees una solicitud de recolección, la vas a ver acá. |
| `dash_stat_total` | Total |
| `dash_stat_pending` | Pendientes |
| `dash_stat_progress` | En curso |
| `dash_stat_completed` | Completadas |
| `dash_footer_quote` | Pequeñas acciones, grandes cambios. |

### 5.2 Lista de solicitudes

| Clave | Texto (ES) |
|---|---|
| `req_list_title` | Mis solicitudes |
| `req_list_new` | Nueva solicitud |
| `req_list_empty` | Acá van a aparecer tus solicitudes |
| `req_list_empty_desc` | Cuando crees una solicitud de recolección, la vas a ver en esta lista. |
| `req_list_view` | Ver |
| `req_list_edit` | Editar |
| `req_list_delete` | Eliminar |

### 5.3 Formulario de solicitud

| Clave | Texto (ES) |
|---|---|
| `req_form_title_new` | Solicitar recolección |
| `req_form_title_edit` | Editar solicitud |
| `req_form_subtitle_new` | Decinos qué materiales tenés y en qué dirección retirarlos. |
| `req_form_subtitle_edit` | Modificá los datos de tu solicitud |
| `req_form_submit_new` | Enviar solicitud |
| `req_form_submit_edit` | Guardar cambios |
| `req_form_cancel` | Cancelar |
| `req_form_guest_name` | Tu nombre |
| `req_form_guest_phone` | Tu teléfono |
| `req_form_guest_hint` | Con estos datos podés rastrear tu solicitud sin cuenta. |
| `req_form_have_account` | ¿Ya tenés cuenta? Iniciá sesión |
| `req_form_logged_in_note` | Usaremos los datos de tu perfil para esta solicitud |
| `req_form_city` | Ciudad |
| `req_form_city_select` | Seleccioná una ciudad |
| `req_form_address` | Dirección de recolección |
| `req_form_address_ref` | Referencia (opcional) |
| `req_form_materials` | Materiales |
| `req_form_org` | Organización de acopio |
| `req_form_org_select` | Primero seleccioná una ciudad |
| `req_form_org_loading` | Cargando organizaciones... |
| `req_form_org_none` | No hay organizaciones en esta ciudad |
| `req_form_timeslot` | Franja horaria |
| `req_form_select_default` | — Sin especificar — |
| `req_form_weight` | Peso aproximado (opcional) |
| `req_form_weight_0_5` | Menos de 5 kg |
| `req_form_weight_5_20` | 5 a 20 kg |
| `req_form_weight_20_50` | 20 a 50 kg |
| `req_form_weight_50_plus` | Más de 50 kg |
| `req_form_volume` | Volumen aproximado (opcional) |
| `req_form_volume_bag` | Una bolsa |
| `req_form_volume_box` | Una caja |
| `req_form_volume_trunk` | Un baúl de auto |
| `req_form_volume_pickup` | Carga de camioneta |
| `req_form_image` | Foto (opcional) |
| `req_form_image_choose` | Click para subir |

### 5.4 Detalle de solicitud

| Clave | Texto (ES) |
|---|---|
| `req_detail_title` | Detalle de solicitud |
| `req_detail_back` | Volver |
| `req_detail_city` | Ciudad |
| `req_detail_address` | Dirección |
| `req_detail_materials` | Materiales |
| `req_detail_org` | Organización |
| `req_detail_no_org` | Sin asignar |
| `req_detail_date` | Fecha de creación |
| `req_detail_status` | Estado |
| `req_detail_slot` | Franja confirmada |
| `req_detail_no_slot` | Sin confirmar |
| `req_detail_ref` | Referencia |
| `req_detail_image` | Foto |
| `req_detail_edit` | Editar |
| `req_detail_delete` | Eliminar |

### 5.5 Éxito de solicitud

| Clave | Texto (ES) |
|---|---|
| `req_success_title` | ¡Solicitud creada! |
| `req_success_desc` | Tu solicitud fue enviada a la organización que elegiste. Te contactaremos para coordinar la recolección. |
| `req_success_id` | Solicitud |
| `req_success_track_code_label` | Guardá este código para rastrear tu solicitud: |
| `req_success_track_code_hint` | Lo vas a necesitar junto con tu teléfono para consultar el estado. Si lo perdés, contactá a la organización. |
| `req_success_track` | Rastrear mi solicitud |
| `req_success_new_request` | Nueva solicitud |
| `req_success_my_requests` | Mis solicitudes |
| `req_success_not_now` | Ahora no |
| `req_success_upsell_title` | ¿Querés hacer más fácil tus próximas solicitudes? |
| `req_success_upsell_prefill` | Datos precargados |
| `req_success_upsell_history` | Historial de solicitudes |
| `req_success_upsell_tracking` | Seguimiento en tiempo real |
| `req_success_create_account` | Crear cuenta |

### 5.6 Perfil

| Clave | Texto (ES) |
|---|---|
| `profile_title` | Mi perfil |
| `profile_edit` | Editar perfil |
| `profile_edit_title` | Editar datos |
| `profile_info` | Información personal |
| `profile_username` | Usuario |
| `profile_name` | Nombre |
| `profile_name_label` | Nombre |
| `profile_email` | Email |
| `profile_email_label` | Email |
| `profile_phone` | Teléfono |
| `profile_phone_label` | Teléfono |
| `profile_no_phone` | Sin teléfono |
| `profile_city` | Ciudad |
| `profile_city_label` | Ciudad |
| `profile_city_select` | Seleccioná una ciudad |
| `profile_no_city` | Sin ciudad |
| `profile_stats` | Estadísticas de solicitudes |
| `profile_save` | Guardar |
| `profile_cancel` | Cancelar |

### 5.7 Estados de solicitud

| Clave | Texto (ES) |
|---|---|
| `req_status_pending` | Pendientes |
| `req_status_in_progress` | En curso |
| `req_status_completed` | Completadas |
| `req_status_rejected` | Rechazadas |

---

## 6. Organización (ROLE_ORGANIZATION)

### 6.1 Dashboard

| Clave | Texto (ES) |
|---|---|
| `org_dash_title` | Panel de acopio |
| `org_dash_greeting` | ¡Hola |
| `org_dash_subtitle` | Solicitudes pendientes, en curso y completadas de tu organización. |
| `org_dash_requests` | Lista de solicitudes |
| `org_kanban_title` | Tablero de solicitudes |
| `org_kanban_empty` | Sin solicitudes por ahora |

### 6.2 Lista de solicitudes

| Clave | Texto (ES) |
|---|---|
| `org_req_title` | Solicitudes |
| `org_req_filter_all` | Todas |
| `org_req_type` | Tipo |
| `org_req_guest` | Invitado |
| `org_req_registered` | Registrado |
| `org_req_contact` | Contacto |
| `org_req_contact_name` | Nombre |
| `org_req_accept` | Aceptar |
| `org_req_accept_title` | Aceptar solicitud |
| `org_req_reject` | Rechazar |
| `org_req_reject_confirm` | ¿Confirmás el rechazo? |
| `org_req_reject_confirm_btn` | Sí, rechazar |
| `org_req_complete` | Marcar como completada |
| `org_req_complete_title` | Finalizar recolección |
| `org_req_select_slot` | Confirmar franja horaria |
| `org_req_back_dashboard` | Volver al panel |

### 6.3 Perfil

| Clave | Texto (ES) |
|---|---|
| `org_profile_title` | Perfil de organización |
| `org_profile_edit` | Editar perfil |
| `org_profile_edit_title` | Editar datos |
| `org_profile_info` | Información de la organización |
| `org_profile_name` | Nombre |
| `org_profile_name_label` | Nombre de la organización |
| `org_profile_materials` | Materiales que acepta |
| `org_profile_no_materials` | Sin materiales seleccionados |

### 6.4 Onboarding

| Clave | Texto (ES) |
|---|---|
| `onboard_title` | Completá tu perfil |
| `onboard_subtitle` | Necesitamos estos datos para que los ciudadanos puedan contactarte |
| `onboard_org_name` | Organización |
| `onboard_phone` | Teléfono de contacto |
| `onboard_city` | Ciudad |
| `onboard_city_select` | Seleccioná una ciudad |
| `onboard_submit` | Completar perfil |

### 6.5 Catadores (latente — sin sidebar)

| Clave | Texto (ES) |
|---|---|
| `cat_title` | Catadores |
| `cat_add` | Agregar catador |
| `cat_add_title` | Nuevo catador |
| `cat_edit_title` | Editar catador |
| `cat_name` | Nombre |
| `cat_notes` | Notas (opcional) |
| `cat_active` | Activo |
| `cat_active_yes` | Sí |
| `cat_active_no` | No |
| `cat_empty` | No hay catadores registrados |
| `cat_empty_desc` | Agregá catadores para gestionarlos |

---

## 7. Inconsistencias detectadas y resueltas

### 7.1 Copies mezclados (Rastrear vs Buscar)

**Problema:** el navbar decía "Rastrear solicitud" pero la página
decía "Buscar mi solicitud". El modal decía "Rastrear sin cuenta".

**Resolución:** unificado a "Buscar" en todo el flujo:
- Navbar CTA: "Ver mi solicitud" (no "Rastrear")
- Modal: "Buscar sin cuenta"
- Página: "Buscar mi solicitud"

### 7.2 Slop aspiracional eliminado

| Antes | Después |
|---|---|
| "Juntos por un futuro más limpio y sostenible" | "Pedí que pasen a buscar tus reciclables" |
| "Cuidemos nuestra ciudad" | "Rivera · Sant'Ana do Livramento" |
| "...tengan un mejor destino" | "...los retira en el día y horario que coordinen" |
| "Acompañá tus solicitudes y contribuí a una ciudad sustentable" | "Acá ves tus solicitudes de recolección y su estado" |
| "Contanos qué tenés y nos encargamos del resto" | "Decinos qué materiales tenés y en qué dirección retirarlos" |
| "Ingresá con tu cuenta para continuar" | "Entrá para ver y gestionar tus solicitudes" |
| "Registrate para empezar a usar Residuo Sólido" | "Creá una cuenta para pedir recolección y seguir el estado" |
| "¿Sos una cooperativa o reciclador?" | "¿Retirás reciclables en Rivera o Sant'Ana?" |
| "Sumate a la comunidad" | "Sumate a la red de reciclaje" |
| "Accedé a tu cuenta para seguir contribuyendo a un futuro más limpio" | "Entrá para gestionar tus solicitudes de recolección" |
| "Creá tu cuenta y ayudanos a construir un futuro más limpio" | "Creá una cuenta para pedir o recibir solicitudes de recolección" |

### 7.3 Pendientes

| Problema | Estado |
|---|---|
| `messages_es` tiene ~20 claves muertas (sin uso) | Pendiente |
| `messages_pt` tiene 27 claves menos que `messages_es` | Pendiente |
| `messages.properties` (108 claves) — verificar si se usa | Pendiente |
| `req_success_track` dice "Rastrear mi solicitud" | Debería ser "Buscar mi solicitud" |

---

## 8. Reglas para nuevos copies

1. **Un verbo por flujo:** "Buscar" para rastreo, "Pedir" para solicitud,
   "Entrar" para login, "Crear" para registro.
2. **Sin aspiracional:** describir qué hace el sistema, no qué debería lograr.
3. **CTAs concretos:** "Pedir recolección", no "Enviar solicitud".
4. **Estados vacíos cálidos-moderados:** "Acá van a aparecer...", no "No hay datos".
5. **Sin slop:** no "sostenible", "futuro más limpio", "mejor destino",
   "contribuí", "sumate a la comunidad".
6. **Badge geográfico:** "Rivera · Sant'Ana do Livramento" (no "Cuidemos nuestra ciudad").
7. **Tono:** directo, profesional, austero. No infantil, no excesivamente amistoso.
