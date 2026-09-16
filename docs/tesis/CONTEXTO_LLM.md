# Contexto consolidado para asistencia con la tesis

**Estado:** borrador parcial  
**Última actualización:** 2026-09-15  
**Fuente de verdad:** no (es un ensamblado derivado)  
**Relacionado con:** `README.md`, `docs/INDICE.md`, `docs/METODOLOGIA.md`, `docs/DIAGRAMAS.md`, `docs/ENDPOINTS.md`, `docs/DEFENSA.md`, `docs/MEJORAS.md` y secciones canónicas de despliegue/observabilidad

> Este archivo organiza contenido existente en los documentos canónicos. No reemplaza esas fuentes.

## 1. Descripción del problema

Eco Solicitud es un sistema web para gestionar y coordinar la recolección de residuos reciclables en la Frontera de la Paz: Rivera, Uruguay, y Sant'Ana do Livramento, Brasil.

El problema abordado es la necesidad de centralizar la información necesaria para solicitar una recolección y conocer su estado. La solución convierte esa necesidad de coordinación en un proceso de software explícito, implementado y verificable, con reglas, permisos y limitaciones documentadas.

La necesidad se presenta como una observación del contexto territorial. No se afirma que se hayan realizado entrevistas con organizaciones ni que el sistema haya demostrado aumentar el reciclaje o los ingresos. Esas afirmaciones requieren validación mediante un piloto.

**Estado:** problema identificado y proceso de coordinación implementado; impacto territorial no validado.

## 2. Objetivos general y específicos

### Objetivo general

Implementar un MVP que gestione solicitudes de recolección y su estado, adaptado al contexto de Rivera y Sant'Ana do Livramento.

### Objetivos específicos

[FALTA: formulación explícita y canónica de los objetivos específicos; debería originarse en `docs/METODOLOGIA.md` o en un documento académico canónico enlazado desde `docs/INDICE.md`.]

## 3. Alcance

### Dentro del alcance

- **Implementado:** registro e inicio de sesión con roles `USER` y `ORGANIZATION`.
- **Implementado:** onboarding forzado del perfil de organización.
- **Implementado:** creación de solicitudes por usuario registrado o invitado.
- **Implementado:** selección explícita de organización según ciudad y materiales aceptados.
- **Implementado:** edición y eliminación de solicitudes propias mientras están en `PENDING`.
- **Implementado:** gestión de estados por la organización: aceptar, rechazar y completar.
- **Implementado:** seguimiento de solicitudes de invitados mediante teléfono y código privado.
- **Implementado:** dashboards para usuario y organización.
- **Implementado:** tablero Kanban integrado al dashboard de organización.
- **Implementado:** métricas públicas agregadas por ciudad.
- **Descartado:** blog estático (no se implementó).
- **Implementado:** internacionalización español/portugués.
- **Implementado:** PWA instalable con caché de recursos estáticos.
- **Parcialmente implementado / latente:** gestión de recolectores informales; el CRUD existe, pero no tiene acceso visible desde el sidebar y una solicitud no registra al recolector responsable.

### Fuera del alcance

- **Descartado para el MVP:** optimización de rutas, mapas y GPS.
- **Descartado para el MVP:** pagos.
- **Descartado para el MVP:** notificaciones reales por WhatsApp o SMS.
- **Descartado para el MVP:** panel general con rol `ADMIN`.
- **Descartado para el MVP:** blog estático (no se implementó).
- **Descartado para el MVP:** medición de impacto ambiental.
- **Limitación conocida:** no existe validación de usabilidad con usuarios reales.

## 4. Requisitos

Los documentos canónicos declaran 8 requisitos funcionales, 14 reglas de negocio y tres actores principales: invitado, usuario registrado y organización.

### Casos de uso documentados

#### Invitado

- **Implementado — RF-3:** crear una solicitud sin cuenta.
- **Implementado — RF-4:** consultar una solicitud mediante teléfono y código privado.

#### Usuario registrado

- **Implementado — RF-1:** registrarse.
- **Implementado — RF-2:** iniciar sesión.
- **Implementado — RF-3:** crear una solicitud.
- **Implementado — RF-4:** consultar una solicitud.
- **Implementado — RF-5:** ver dashboard e historial.
- **Implementado — RF-5:** editar una solicitud propia pendiente.
- **Implementado — RF-5:** eliminar una solicitud propia pendiente.
- **Implementado — RF-7:** editar el perfil.

#### Organización

- **Implementado — RF-1:** registrarse.
- **Implementado — RF-2:** iniciar sesión.
- **Implementado — RF-7:** completar y editar el perfil.
- **Implementado — RF-6:** consultar solicitudes asignadas y filtrarlas por estado.
- **Implementado — RF-6:** aceptar una solicitud con franja horaria.
- **Implementado — RF-6:** rechazar una solicitud.
- **Implementado — RF-6:** completar una solicitud.
- **Parcialmente implementado / latente — RF-8:** gestionar recolectores informales mediante CRUD sin acceso visible desde el sidebar.

### Reglas documentadas explícitamente

- Una organización no puede modificar solicitudes ajenas.
- Una solicitud no puede completarse directamente desde `PENDING`.
- El seguimiento de invitados requiere teléfono y código privado; el teléfono por sí solo no devuelve resultados.
- Solo una solicitud `PENDING` puede editarse o eliminarse.
- Una organización asignable debe estar activa, tener rol `ORGANIZATION`, perfil completo, teléfono válido, ciudad coincidente y materiales aceptados no vacíos.
- Todos los materiales de una solicitud deben estar incluidos entre los aceptados por la organización.
- Las transiciones y el borrado utilizan optimistic locking mediante `@Version`.

[FALTA: catálogo canónico completo y numerado de los 8 RF y las 14 RN con descripción, precondiciones, postcondiciones y criterios de aceptación; debería originarse en la sección `Requisitos y Reglas de Negocio` de `docs/DIAGRAMAS.md`.]

## 5. Arquitectura

El sistema aplica una arquitectura en capas:

```text
Presentación
Controllers → Templates Thymeleaf → Fragments y JavaScript
                         ↓
Negocio
Services → validación → lógica de dominio
                         ↓
Datos
Repositories → MongoDB
```

### Presentación

Incluye controllers Spring MVC, templates Thymeleaf renderizados en servidor, fragments reutilizables y recursos estáticos. `Routes.java` centraliza las rutas utilizadas por controllers, seguridad y tests. `OrgApiController` expone el endpoint JSON de organizaciones por ciudad.

### Negocio

- `UserService`: usuarios y perfiles.
- `UserRegistrationService`: registro.
- `RequestService`: creación de solicitudes.
- `RequestQueryService`: consultas y verificación de propiedad.
- `RequestTransitionService`: transiciones de estado.
- `RequestMetricsService`: estadísticas de dashboards.
- `PublicMetricsService`: métricas públicas.
- `CityOrgService`: resolución de organizaciones por ciudad.
- `LocalImageService`: imágenes locales.

### Datos

La persistencia utiliza Spring Data MongoDB. `Request.user` y `Request.organization` son referencias documentales a `User`. Los materiales se almacenan como una lista del enum `MaterialCategory`; no existe una entidad separada para materiales.

[FALTA: sincronización canónica de cantidades y componentes arquitectónicos después de las consolidaciones y eliminaciones recientes; debería originarse en `docs/DIAGRAMAS.md` §Núcleo del Sistema.]

## 6. Modelo de dominio

### `User`

Representa tanto usuarios como organizaciones, diferenciados mediante `Role`.

Datos documentados: identificador, username, email, contraseña, rol, nombre, teléfono, ciudad, estado activo, estado del perfil, materiales aceptados y fecha de creación.

La validación de email, nombre y teléfono se realiza inline en los setters de `User`. `PhoneNumber` es una utility class con métodos estáticos que admite Uruguay `+598` y Brasil `+55`, y normaliza a formato E.164.

### `Request`

Representa una solicitud de recolección. Puede pertenecer a un usuario registrado o contener datos de invitado. Registra organización, ciudad, dirección, referencia, materiales, peso y volumen estimados, imagen opcional, franja horaria, estado, código de seguimiento, fecha y versión de optimistic locking.

Ciclo de estados:

```text
PENDING → IN_PROGRESS → COMPLETED
    └───────────────→ REJECTED
```

- `PENDING`: estado inicial.
- `IN_PROGRESS`: la organización aceptó la solicitud y confirmó una franja.
- `REJECTED`: estado final.
- `COMPLETED`: estado final.

Las operaciones de dominio incluyen `accept(TimeSlot)`, `reject()`, `complete()`, `canBeEdited()`, `isGuest()`, `hasMaterials()` y `assignOrganization(User)`.

### Enums

- `Role`: `USER`, `ORGANIZATION`.
- `RequestStatus`: `PENDING`, `IN_PROGRESS`, `REJECTED`, `COMPLETED`.
- `City`: `RIVERA`, `LIVRAMENTO`.
- `TimeSlot`: `MANANA`, `TARDE`, `NOCHE`.
- `MaterialCategory`: `PLASTICO`, `PAPEL`, `CARTON`, `VIDRIO`, `METAL`, `MADERA`, `ESCOMBROS`.

### Recolectores informales

**Parcialmente implementado / latente:** la documentación describe `InformalCollector` como agenda interna vinculada a una organización. No existe relación entre una solicitud y el recolector responsable.

## 7. Tecnologías

- **Implementado — Backend:** Java 21, Spring Boot 3.2.0, Spring Security 6.
- **Implementado — Presentación:** Thymeleaf 3 con renderizado server-side y `thymeleaf-layout-dialect`.
- **Implementado — Persistencia:** MongoDB con Spring Data MongoDB.
- **Implementado — Interfaz:** HTML, JavaScript, CSS con variables y convención BEM, FontAwesome.
- **Implementado — Seguridad:** Spring Security, CSRF, control por roles, bloqueo de login y rate limiting.
- **Implementado — PWA:** manifest, service worker e iconos.
- **Implementado — Testing:** JUnit 5, Mockito, Spring Boot Test, Spring Security Test y MockMvc.
- **Implementado — Calidad:** JaCoCo y PMD 7.
- **Implementado — Empaquetado:** Maven y Docker multi-stage.
- **Implementado — Plataforma:** Render.com y MongoDB Atlas.
- **Propuesto:** migración primero a Spring Boot 3.5.x y luego a 4.x.

## 8. Seguridad

### Controles implementados

- Autenticación mediante formulario.
- Autorización por roles `USER` y `ORGANIZATION`.
- CSRF en formularios.
- DTO `RegistrationForm` para evitar mass assignment durante el registro.
- Validación server-side de username, email y contraseña.
- Índices únicos y manejo de duplicados para username y email.
- Contraseña mínima de ocho caracteres.
- Bloqueo durante 15 minutos después de cinco intentos fallidos de login.
- Rate limiting para solicitudes de invitados mediante ventana deslizante por IP.
- Seguimiento de invitado mediante teléfono y código privado de ocho caracteres.
- Verificación de propiedad para solicitudes de usuarios y organizaciones.
- Optimistic locking mediante `@Version`.
- Validación de tipo, extensión y tamaño de imágenes antes de persistir la solicitud.
- Seed de desarrollo condicionado a `app.seed=true`, base vacía y perfiles no productivos.

### Limitaciones de seguridad

- **Limitación conocida:** CSP utiliza `unsafe-inline`.
- **Limitación conocida:** el registro permite que una cuenta se autodeclare organización sin validación humana.
- **Limitación conocida:** contraseña de ocho caracteres sin requisito de complejidad ni comprobación contra listas de credenciales filtradas.
- **Limitación conocida:** rate limiting y bloqueo de login se almacenan en memoria, no en un sistema distribuido.
- **Limitación conocida:** MongoDB sin transacciones multi-documento.
- **Propuesto:** CSP con nonces o hashes.
- **Propuesto:** política de contraseña alineada con OWASP.
- **Propuesto:** verificación de legitimidad de organizaciones.
- **Propuesto:** rate limiting distribuido con Redis.

## 9. Pruebas

La documentación canónica registra:

```text
Tests run: 176, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Estrategia

- Tests unitarios de servicios con repositorios simulados mediante Mockito.
- Tests de integración y seguridad con `@SpringBootTest` y MockMvc.
- Tests de autorización con `@WithMockUser`.
- Tests de regresión asociados a correcciones de defectos.
- `MongoAggregationUtilsIntegrationTest` utiliza una instancia real de MongoDB.
- JaCoCo genera el reporte de cobertura en `target/site/jacoco/index.html`.

### Cobertura cualitativa documentada

- Alta para creación, edición, transición y borrado de solicitudes.
- Alta para seguridad por rol.
- Alta para rate limiting y bloqueo de login.
- Alta para las agregaciones MongoDB de dashboards.
- Cobertura agregada para métricas públicas.

### Limitaciones

- **Limitación conocida:** `EndToEndFlowsTest` usa MockMvc con servicios simulados y no constituye una prueba de navegador real según la documentación canónica.
- **Limitación conocida:** no existe un reporte JaCoCo versionado.
- **Limitación conocida:** el test de integración de MongoDB necesita `localhost:27017`.

## 10. Despliegue

### Estrategia documentada

- **Implementado:** despliegue en Render.com como PaaS.
- **Implementado:** persistencia productiva mediante MongoDB Atlas.
- **Implementado:** HTTPS y dominio público proporcionados por Render.
- **Implementado:** Docker multi-stage con Java 21.
- **Implementado:** despliegue automático desde GitHub después de un push.
- **Descartado para el MVP:** AWS/IaaS, VPN y MCP de AWS.

El build de producción documentado es:

```bash
mvn clean package -DskipTests
java -jar target/app-0.0.1-SNAPSHOT.jar
```

Render se eligió para reducir configuración operativa y mantener el foco en el sistema. Las consecuencias documentadas son menor control de infraestructura, ausencia de VPC e IAM propios, escalado limitado en el plan gratuito y falta de acceso directo a servicios AWS.

[FALTA: sección canónica de observabilidad que documente Spring Actuator, UptimeRobot, endpoint monitoreado, fuentes de métricas, política de alertas y separación entre salud interna y disponibilidad externa; debería originarse en una sección de Despliegue/Observabilidad enlazada desde `docs/INDICE.md`.]

[FALTA: evidencia temporal canónica del despliegue actualmente publicado —fecha, commit, duración, estado y health check—; debería originarse en la sección de Despliegue/Observabilidad y registrarse como “evidencia observada el <fecha>”.]

## 11. Resultados

### Resultados técnicos documentados

- **Implementado:** flujo completo desde la creación de una solicitud hasta su aceptación, rechazo o finalización por una organización.
- **Implementado:** control de acceso por actor y propiedad de la solicitud.
- **Implementado:** seguimiento para invitados sin exponer solicitudes por teléfono solamente.
- **Implementado:** métricas públicas agregadas por ciudad.
- **Implementado:** interfaz bilingüe español/portugués.
- **Implementado:** PWA instalable con caché de CSS, JavaScript, imágenes, manifest e iconos.
- **Implementado:** documentación técnica, endpoints catalogados y cuatro diagramas UML editables.
- **Implementado:** suite documentada de 176 tests sin fallos.

La evaluación realizada demuestra correctitud funcional y controles técnicos. No demuestra usabilidad ni impacto ambiental.

[FALTA: resultados de una evaluación con usuarios reales, métricas de usabilidad y resultados de un piloto territorial; deberían originarse en `docs/METODOLOGIA.md` y `docs/DEFENSA.md` después de realizar la evaluación.]

## 12. Limitaciones

- **Limitación conocida:** no hay evaluación de usabilidad con usuarios reales.
- **Limitación conocida:** no hay medición de impacto ambiental ni aumento de ingresos.
- **Limitación conocida:** `COMPLETED` significa que la organización declaró completada la solicitud; no verifica la recolección física, el peso recibido, la conformidad del usuario ni el destino final.
- **Limitación conocida:** se conserva el estado actual, no un historial de transiciones con fecha, responsable y motivo.
- **Limitación conocida:** una cuenta `ORGANIZATION` representa simultáneamente identidad de acceso y participante de negocio; no admite múltiples operadores.
- **Limitación conocida:** no se verifica la legitimidad de una organización registrada.
- **Limitación conocida:** imágenes almacenadas localmente, sin almacenamiento distribuido, backup o CDN.
- **Limitación conocida:** PWA instalable sin navegación HTML offline.
- **Limitación conocida:** CSP con `unsafe-inline`.
- **Limitación conocida:** MongoDB sin transacciones multi-documento.
- **Limitación conocida:** asignación por ciudad, no por proximidad geográfica.
- **Limitación conocida:** blog no implementado en el MVP.
- **Limitación conocida:** sin rol o panel `ADMIN`.
- **Descartado:** notificaciones automáticas en el MVP.

## 13. Decisiones y justificaciones

### Arquitectura y dominio

- **Mono-modelo `User`:** reduce modelo, repositorios y flujo de autenticación; limita múltiples operadores y roles simultáneos.
- **Arquitectura en capas:** separa presentación, reglas y persistencia.
- **MongoDB:** permite persistencia documental y materiales como array embebido; PostgreSQL también se reconoce como alternativa válida.
- **Optimistic locking:** detecta conflictos concurrentes sin bloqueo pesimista.
- **State Pattern en `RequestStatus`:** encapsula las transiciones permitidas.
- **Rutas centralizadas en `Routes.java`:** evita URLs dispersas en controllers, seguridad y tests.

### Interfaz y contenido

- **Thymeleaf SSR:** reduce piezas de despliegue.
- **Un solo CSS con variables y BEM:** evita tooling adicional de frontend y mantiene coherencia visual.
- **Dark mode con variables CSS:** evita dependencias y respeta preferencia persistida y del sistema operativo.
- **Sin blog:** elimina complejidad de CMS y riesgo de edición dinámica.
- **Kanban integrado al dashboard:** reduce navegación y reutiliza endpoints existentes.
- **HTML fuera del caché del service worker:** evita contenido desactualizado a costa de navegación offline.

### Operación

- **Render PaaS en lugar de AWS IaaS:** despliegue simple, HTTPS y menor carga operativa para el MVP; sacrifica control de red y escalado avanzado.
- **Imágenes locales:** evita proveedor y configuración externa; no permite escalado horizontal.
- **Sin notificaciones externas:** reduce código y acoplamiento con proveedores.

## 14. Trabajo futuro

### Priorizado en la defensa

1. **Propuesto:** piloto con usuarios y organizaciones de Rivera y Sant'Ana do Livramento.
2. **Propuesto:** notificaciones reales mediante WhatsApp Business API o SMS, condicionadas a validación con usuarios.
3. **Propuesto:** pruebas reales de navegador con Selenium o Playwright.
4. **Propuesto:** CSP con nonces para eliminar `unsafe-inline`.
5. **Propuesto:** política de contraseña más fuerte.

### Backlog técnico y funcional

- **Propuesto:** métricas privadas por organización y descarga PDF.
- **Propuesto:** separar `Cuenta` de `Organización` si se necesitan múltiples operadores.
- **Propuesto:** asignar un recolector responsable a cada solicitud o eliminar el subsistema latente.
- **Propuesto:** historial auditable de transiciones.
- **Propuesto:** almacenamiento de imágenes en S3, Cloudinary o equivalente.
- **Propuesto:** MongoDB replica set y transacciones multi-documento.
- **Propuesto:** panel `ADMIN` limitado para validación y moderación.
- **Propuesto:** CMS si el contenido editorial requiere gestión frecuente.
- **Propuesto:** métricas con Prometheus/Grafana.
- **Propuesto:** logs estructurados en JSON.
- **Propuesto:** rate limiting distribuido mediante Redis.
- **Propuesto:** migración gradual de Spring Boot 3.2 a 3.5 y posteriormente a 4.x.
- **Propuesto:** infraestructura IaaS cuando existan requisitos concretos de VPC, IAM, escalado horizontal, multirregión, backups o compliance.

## 15. Glosario

[FALTA: glosario canónico aprobado con definiciones estables de solicitud, usuario, invitado, organización, recolección, seguimiento, código privado, material, franja horaria, estado, PWA, optimistic locking, health check y actor; debería originarse en `docs/METODOLOGIA.md` o en un anexo enlazado desde `docs/INDICE.md`.]

## 16. Referencias a código y diagramas

### Código

- Aplicación principal: `src/main/java/com/residuosolido/app/ResiduoSolidoApplication.java`.
- Rutas canónicas: `src/main/java/com/residuosolido/app/config/Routes.java`.
- Seguridad: `src/main/java/com/residuosolido/app/config/SecurityConfig.java`.
- Modelo de usuario: `src/main/java/com/residuosolido/app/model/User.java`.
- Modelo de solicitud: `src/main/java/com/residuosolido/app/model/Request.java`.
- Estados: `src/main/java/com/residuosolido/app/enums/RequestStatus.java`.
- Creación y validación de solicitudes: `src/main/java/com/residuosolido/app/service/RequestService.java`.
- Consultas de solicitudes: `src/main/java/com/residuosolido/app/service/RequestQueryService.java`.
- Transiciones: `src/main/java/com/residuosolido/app/service/RequestTransitionService.java`.
- Usuarios y perfiles: `src/main/java/com/residuosolido/app/service/UserService.java`.
- Copys server-side: `src/main/java/com/residuosolido/app/config/UiCopyCatalog.java`.
- Endpoints HTTP: `docs/ENDPOINTS.md`.

### Diagramas

- Casos de uso: `docs/diagrams/figura1-casos-uso.drawio`.
- Modelo lógico: `docs/diagrams/figura2-modelo-logico.drawio`.
- Diagrama de clases: `docs/diagrams/figura3-clases.drawio`.
- Diagrama de secuencia UML 2.5: `docs/diagrams/figura4-secuencia.drawio`.
- Índice visual de figuras: `docs/diagrams/figuras.html`.
- Explicación textual y arquitectura: `docs/DIAGRAMAS.md`.

[FALTA: diagrama canónico de despliegue/infraestructura que relacione GitHub, Render, contenedor Docker, Spring Boot, MongoDB Atlas, Actuator y monitorización externa; debería originarse en `docs/DIAGRAMAS.md` y guardarse en `docs/diagrams/`.]

## Resumen de huecos

1. **Sección 2 — Objetivos:** formulación explícita de objetivos específicos.
2. **Sección 4 — Requisitos:** catálogo completo de 8 RF y 14 RN con precondiciones, postcondiciones y criterios de aceptación.
3. **Sección 5 — Arquitectura:** cantidades y componentes sincronizados después de las consolidaciones y eliminaciones recientes.
4. **Sección 10 — Despliegue:** sección canónica de observabilidad con Actuator, UptimeRobot, métricas y alertas.
5. **Sección 10 — Despliegue:** evidencia temporal del despliegue actual.
6. **Sección 11 — Resultados:** evaluación con usuarios, métricas de usabilidad y piloto territorial.
7. **Sección 15 — Glosario:** glosario canónico aprobado.
8. **Sección 16 — Referencias:** diagrama de despliegue e infraestructura.
