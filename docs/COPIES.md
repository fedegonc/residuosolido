# Copys — Single Source of Truth

> **Propósito:** centralizar todos los textos de interfaz del sistema
> en un único documento para detectar inconsistencias, copies mezclados
> y crecimientos innecesarios.
>
> **Fuente de verdad:** este documento. Los templates usan `data-i18n`
> con fallback en español. Los JSON de `i18n/common/` son para el
> cliente (JS). Los `messages_*.properties` son para el servidor
> (Thymeleaf).
>
> **Fecha:** commit `1e4d575` · **Tests:** 193, 0 failures

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
