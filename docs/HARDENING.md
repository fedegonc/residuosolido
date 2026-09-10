# Endurecimiento del MVP — Cambios y limitaciones

Este documento describe las correcciones aplicadas al MVP y las limitaciones
que quedan fuera del alcance actual. Forma parte del análisis crítico del
sistema y debe consultarse junto con los diagramas de `docs/diagrams/`.

## Correcciones aplicadas

### 1. Registro de cuentas

- **Antes:** el controlador recibía directamente la entidad `User` mediante
  `@ModelAttribute` y la persistía. Un cliente podía enviar campos internos
  (`id`, `profileCompleted`, `acceptedMaterials`, `role`).
- **Ahora:** el controlador recibe un `RegistrationForm` (DTO) con solo
  `username`, `email` y `password`. El servicio construye una entidad nueva
  bajo control del servidor y asigna el rol según el checkbox de
  organización.
- **Validaciones:** username obligatorio y sin espacios; contraseña mínima
  de 8 caracteres; email válido y normalizado a minúsculas; unicidad de
  username y email con índices únicos en MongoDB y manejo de
  `DuplicateKeyException`.

### 2. Rastreo de solicitudes de invitado

- **Antes:** el rastreo público consultaba por teléfono solo. Cualquier
  persona que conociera el teléfono podía ver dirección, materiales y
  organización de solicitudes ajenas.
- **Ahora:** cada solicitud de invitado recibe un código privado de 8
  caracteres al crearse. El rastreo requiere teléfono + código. El teléfono
  solo no devuelve resultados.
- **UI:** la pantalla de éxito muestra el código al invitado; el formulario
  de rastreo pide ambos campos; los textos (es/pt) fueron actualizados.
- **Solicitudes antiguas sin código:** no son rastreables por el flujo
  público. La organización puede consultarlas desde su panel. No se habilitó
  acceso inseguro por compatibilidad.

### 3. Disponibilidad de organizaciones

- **Antes:** `getOrganizationsByCity` caía a organizaciones inactivas si no
  había activas. La asignación de una solicitud no verificaba `active`.
- **Ahora:** una organización recibe solicitudes solo si está activa, tiene
  rol `ORGANIZATION`, perfil completo, teléfono válido, ciudad coincidente
  y materiales aceptados no vacíos. El listado por ciudad nunca cae a
  organizaciones inactivas o incompletas.

### 4. Validación de materiales

- **Antes:** el navegador ocultaba los materiales no aceptados, pero el
  servidor no verificaba la compatibilidad.
- **Ahora:** el servidor valida que todos los materiales solicitados estén
  incluidos en `acceptedMaterials` de la organización, tanto en la creación
  como en la edición.

### 5. Persistencia de imagen

- **Antes:** `createRequestWithImage` persistía la solicitud y después
  subía la imagen. Si la imagen fallaba, la solicitud quedaba huérfana.
- **Ahora:** la imagen se valida (tipo, extensión, tamaño) antes de
  persistir la solicitud. Una imagen inválida no deja solicitud en la base.

### 6. Borrado concurrente

- **Antes:** el borrado hacía `deleteById` sin verificar versión. Entre la
  verificación de propiedad/estado y el borrado, una organización podía
  aceptar la solicitud.
- **Ahora:** el borrado usa `delete(entity)` sobre la entidad cargada con
  su `@Version`. Si una transición concurrente la modificó, se lanza
  `OptimisticLockingFailureException` con un mensaje controlado.

### 7. Edición de solicitudes

- **Antes:** la edición no revalidaba la compatibilidad de materiales con la
  organización.
- **Ahora:** la edición revalida ciudad, dirección, materiales,
  organización y compatibilidad de materiales. Solo las solicitudes
  `PENDING` pueden editarse.

### 8. Teléfono canónico

- **Antes:** los formatos `+598 99 123 456` y `+59899123456` se trataban
  como distintos.
- **Ahora:** `PhoneNumber` normaliza espacios y valida formato
  internacional. La misma canonicidad se aplica a usuarios, invitados,
  recolectores informales y consultas de rastreo.

### 9. Actualización de perfil

- **Antes:** `updateUser` confiaba en los campos de identidad del objeto
  enviado.
- **Ahora:** carga el usuario persistido, valida email duplicado, normaliza
  teléfono y actualiza solo los campos permitidos.

### 10. Promesa de "más cercana"

- **Antes:** la portada decía que la solicitud se enviaba a la cooperativa
  "más cercana", pero el sistema implementa elección explícita del usuario.
- **Ahora:** los textos (es/pt) describen elección explícita por ciudad y
  materiales.

## Limitaciones reconocidas (fuera del alcance del MVP)

### Arquitectura de cuenta/organización

- Una cuenta `User` con rol `ORGANIZATION` representa simultáneamente
  identidad de acceso y participante de negocio.
- **No soporta:** múltiples operadores por cooperativa, cambio de
  responsable sin cambiar la cuenta, ni una misma persona como ciudadano y
  operador de cooperativa.
- **Para soportarlo** haría falta separar `Cuenta`, `Organización` y su
  relación de pertenencia. No se hace en este MVP.

### Verificación de legitimidad

- El registro público permite autodeclararse organización. No verifica que
  la cooperativa exista ni que quien se registra la represente.
- **Para producción** se necesitaría validación humana o un entorno
  controlado.

### Trazabilidad del residuo

- `COMPLETED` significa "la organización declaró completada la solicitud".
  No verifica retiro físico, cantidad recibida, conformidad del ciudadano
  ni destino final.
- Las métricas cuentan solicitudes completadas, no kilos reciclados ni
  impacto ambiental medido.

### Asignación de recolector

- `InformalCollector` es una agenda interna de la organización. `Request`
  no tiene relación con el recolector responsable.
- No se puede responder "qué recolector atendió esta solicitud".

### Concurrencia

- MongoDB standalone no soporta transacciones multi-documento. Las
  operaciones que involucran varios documentos (crear solicitud + subir
  imagen) no son atómicas a nivel de base de datos.
- `@Version` protege las transiciones de estado y el borrado, pero no hay
  rollback automático si una operación parcial falla después de persistir.

### Historial de transiciones

- Se conserva el estado actual, no una historia de transiciones con fecha,
  responsable y motivo.

## Mejoras incrementales implementadas

### Selector de código de país en teléfono

- Se agregó un selector de país (`CountryCode`) con dos opciones: Uruguay
  (+598, 8 dígitos nacionales, primer dígito 9) y Brasil (+55, DDD de 2
  dígitos + 9 dígitos nacionales, primer dígito 9).
- `PhoneNumber.of(CountryCode, national, ddd)` normaliza el número al
  formato E.164, quitando el 0 inicial doméstico uruguayo y anteponiendo
  el DDD para Brasil.
- Los formularios de solicitud de invitado, perfil de usuario y perfil de
  organización usan el selector. La validación HTML5 (pattern) es solo
  visual; la validación real es del servidor.
- `PhoneNumber.of(String raw)` se mantiene para compatibilidad con datos
  existentes y tests previos.

### Tablero Kanban de solicitudes (CU-06)

- Nueva vista en `/acopio/kanban` que muestra las solicitudes asignadas
  agrupadas por estado en 4 columnas: Pendientes, En curso, Completadas,
  Rechazadas.
- Cada card muestra ciudad, materiales, solicitante y fecha. Los botones
  de acción llaman a los mismos endpoints de `OrgRequestController`
  (`/acopio/requests/{id}/transition`) — no duplica lógica de negocio.
- No implementa drag-and-drop; usa botones como acción intencional.

## Fuera de alcance — GPS y geolocalización interactiva

La sección 1.4 de la tesis establece explícitamente que "No hay mapas ni
geolocalización interactiva", fundamentado en el Oficio 044/2023. Esta
decisión se mantiene para el MVP.

Si después de la defensa se decide ampliar el alcance, la implementación
sería:

1. **Modelo:** agregar campo opcional `location: {lat: Double, lng: Double}`
   a `Request`. No obligatorio, no rompe RF-3.
2. **Frontend:** botón "Usar mi ubicación GPS" (Geolocation API del
   navegador) que muestra un mapa Leaflet con marcador arrastrable dentro
   del área de cobertura Rivera–Sant'Ana do Livramento.
3. **Validación server-side:** el punto lat/lng, si viene, debe caer dentro
   de un radio o polígono que cubra la zona fronteriza. Rechazar o ignorar
   si cae fuera.
4. **Documentación:** actualizar la sección 1.4 de la tesis y este archivo
   explicando el cambio de alcance y su justificación técnica.

Esta mejora queda registrada como posible evolución, no como deuda técnica.
