Documento de Requerimientos  
Sistema ResiduoSolido

Versión: 1.1  
Fecha: 25 de septiembre de 2025  
Autor: Federico Goncalvez  
Estado: Versión revisada con mejoras

---

## Tabla de contenidos
1. Introducción  
2. Visión General del Sistema / Proyecto  
3. Glosario  
4. Requerimientos Funcionales (RF)  
5. Requerimientos No Funcionales (RNF)  
6. Requisitos de Usuario / Roles  
7. Restricciones  
8. Casos de Uso / Historias de Usuario  
9. Prioridades  
10. Trazabilidad de Requerimientos  
11. Criterios de Aceptación  
12. Métricas de Éxito  
13. Plan de Entregas  
14. Arquitectura y Diseño  
15. Gestión de Riesgos  
16. Anexos

---

## 1. Introducción
Este documento define los requerimientos funcionales y no funcionales del sistema ResiduoSolido, una aplicación web para la gestión integral de residuos reciclables. Su objetivo es alinear expectativas entre stakeholders, servir como contrato con el equipo de desarrollo y facilitar la validación de entregables.

### 1.1 Alcance
El sistema cubre el ciclo de vida de la recolección de residuos reciclables:
- Registro y autenticación de usuarios (residentes, organizaciones, administradores).
- Gestión de materiales reciclables disponibles y su categorización.
- Creación, seguimiento y operación de solicitudes de recolección.
- Coordinación de organizaciones responsables de recolección.
- Panel administrativo con métricas y configuraciones clave.
- Portal público informativo y sistema de feedback.

### 1.2 Fuera del alcance
- Integraciones con sistemas externos de logística o transporte.
- Analítica avanzada, gamificación o programas de recompensas.
- Aplicaciones móviles nativas.
- Pagos online u otras transacciones económicas.

---

## 2. Visión General del Sistema / Proyecto
ResiduoSolido es una plataforma web construida con Spring Boot, que permite conectar a residentes con organizaciones recolectoras bajo la supervisión de un administrador. Ofrece una experiencia pública informativa y áreas autenticadas para ejecutar operaciones.

### 2.1 Contexto de operación
- **Entorno**: Navegadores modernos (desktop, tablet, mobile).
- **Infraestructura**: Backend Java 17 + Spring Boot 3.2, base de datos PostgreSQL, almacenamiento de imágenes en Cloudinary.
- **Usuarios clave**: Residentes urbanos, organizaciones recicladoras, administradores municipales o del programa.

### 2.2 Objetivos principales
- **Eficiencia operacional**: Coordinar solicitudes y recolecciones de forma transparente.
- **Participación ciudadana**: Facilitar la adopción del reciclaje mediante información y notificaciones oportunas.
- **Control administrativo**: Brindar métricas, auditorías y capacidades de gestión centralizadas.

---

## 3. Glosario
- **RF**: Requerimiento Funcional.
- **RNF**: Requerimiento No Funcional.
- **CRUD**: Create, Read, Update, Delete.
- **Thymeleaf**: Motor de plantillas para Java utilizado en la capa de presentación.
- **CSP**: Content Security Policy.
- **Usuario**: Residente registrado que crea solicitudes.
- **Organización**: Entidad que ejecuta recolecciones.
- **Administrador**: Usuario con permisos de gestión total.

---

## 4. Requerimientos Funcionales (RF)

### 4.1 Gestión de usuarios y autenticación
- **RF-01**: Registro de usuarios residentes con validación de email y contraseña robusta.
- **RF-02**: Autenticación con email/contraseña, bloqueo tras 5 intentos fallidos y redirección según rol.
- **RF-03**: Recuperación de contraseña mediante enlace temporal con caducidad de 24 h.
- **RF-04**: Edición de perfil (nombre, apellido, dirección, teléfono) y consulta de historial de solicitudes.
- **RF-05**: Administración completa de usuarios (CRUD, asignación de roles, activación/desactivación).

### 4.2 Gestión de materiales y catálogo
- **RF-06**: Creación de materiales con nombre, descripción, categoría, imagen y estado.
- **RF-07**: Edición integral de materiales con trazabilidad de cambios.
- **RF-08**: Activación/desactivación de materiales sin afectar solicitudes previas.
- **RF-09**: Filtrado y búsqueda de materiales activos por nombre o categoría en formularios de solicitud.

### 4.3 Gestión de solicitudes de recolección
- **RF-10**: Creación de solicitudes seleccionando múltiples materiales, cantidades (0.1-1000 kg) y fecha preferida (1-30 días).
- **RF-11**: Seguimiento de solicitudes con estados PENDIENTE, APROBADA, PROGRAMADA, COMPLETADA, CANCELADA y timeline de eventos.
- **RF-12**: Administración de estados por parte del administrador con reglas de transición y notificaciones automáticas.
- **RF-13**: Asignación de solicitudes a organizaciones considerando criterios geográficos/capacidad y posibilidad de reasignación.
- **RF-14**: Panel para organizaciones con acceso solo a solicitudes asignadas, confirmación de recolección e incidencias.
- **RF-15**: Sistema de notificaciones internas y por email para usuarios y organizaciones.

### 4.4 Contenido, organizaciones y experiencia
- **RF-16**: Gestión de posts informativos (CRUD, estado, imagen, fuente).
- **RF-17**: Gestión de categorías de posts para navegación temática.
- **RF-18**: Publicación de contenido educativo en el portal público sin autenticación.
- **RF-19**: Gestión de secciones públicas clave (hero, llamados a la acción, preguntas frecuentes).
- **RF-20**: Registro y administración de organizaciones con contacto, cobertura y estado.
- **RF-21**: Configuración de capacidad y zonas de servicio por organización.
- **RF-22**: Captura de feedback de usuarios (comentarios, calificaciones).
- **RF-23**: Gestión de feedback desde el panel admin (respuestas, seguimiento).
- **RF-24**: Dashboard administrativo con métricas en tiempo real (solicitudes, usuarios, materiales).
- **RF-25**: Configuración global del sistema (idiomas soportados, texto institucional, enlaces útiles).

---

## 5. Requerimientos No Funcionales (RNF)
- **RNF-01 – Seguridad de acceso**: Autenticación Spring Security, contraseñas con BCrypt ≥10 rondas, bloqueo tras 5 intentos y cumplimiento regulatorio.
- **RNF-02 – Rendimiento**: Tiempo de respuesta <2 s en el 95% de solicitudes.
- **RNF-03 – Disponibilidad**: Uptime ≥99% mensual, reinicio automático vía contenedores <5 min.
- **RNF-04 – Usabilidad**: Diseño responsive, soporte español/portugués, mensajes claros.
- **RNF-05 – Accesibilidad**: Cumplimiento WCAG 2.1 nivel AA en vistas clave.
- **RNF-06 – Mantenibilidad**: Cobertura de pruebas ≥85%, JavaDoc en servicios/repositorios, estilo consistente.
- **RNF-07 – Portabilidad**: Soporte Chrome, Firefox, Edge, Safari; despliegue portable en Docker.
- **RNF-08 – Escalabilidad**: Arquitectura modular que permita nuevos módulos sin cambios estructurales.
- **RNF-09 – Observabilidad**: Logs centralizados, trazas de auditoría y alertas básicas.
- **RNF-10 – Configurabilidad**: Uso de variables de entorno y profiles por entorno.
- **RNF-11 – Internacionalización**: Soporte completo a i18n (es/pt) en UI y emails.
- **RNF-12 – Resiliencia**: Mecanismos de reintento en operaciones críticas y backups programados.
- **RNF-13 – Interoperabilidad**: APIs internas siguiendo prácticas REST para futuras integraciones.
- **RNF-14 – Transaccionalidad**: Consistencia en operaciones críticas (creación de solicitud, cambio de estado).

---

## 6. Requisitos de Usuario / Roles
- **Administrador**: Acceso total a gestión de usuarios, materiales, organizaciones, solicitudes, contenido, dashboard y configuración.
- **Organización**: Acceso restringido a solicitudes asignadas, estados operativos, incidencias y reportes propios.
- **Usuario residente**: Registro, perfil, creación/seguimiento de solicitudes, preferencias de notificación y envío de feedback.
- **Visitante**: Acceso a portal público, documentación y recursos educativos sin autenticación.

---

## 7. Restricciones

### 7.1 Tecnológicas
- Spring Boot 3.2 y Java 17 obligatorios.
- Base de datos PostgreSQL.
- Motor de plantillas Thymeleaf con Tailwind CSS.
- Spring Security para autenticación/autoridad.
- Cloudinary para imágenes.
- HTMX para interacciones progresivas.

### 7.2 De negocio
- Cumplimiento con leyes locales de protección de datos.
- Uso preferente de herramientas open source.
- MVP funcional en 3 meses desde inicio del desarrollo.

### 7.3 Arquitecturales
- Patrón MVC con capas controller/service/repository.
- Preparación para despliegue contenedorizado.
- Configuración externa mediante variables de entorno.

---

## 8. Casos de Uso / Historias de Usuario
- **CU-01**: “Como usuario residente quiero registrarme para solicitar recolecciones.”
- **CU-02**: “Como usuario quiero crear una solicitud indicando materiales y fecha preferida.”
- **CU-03**: “Como usuario quiero revisar el estado y evolución de mis solicitudes.”
- **CU-04**: “Como administrador quiero aprobar o rechazar solicitudes.”
- **CU-05**: “Como administrador quiero asignar solicitudes a organizaciones específicas.”
- **CU-06**: “Como organización quiero ver y gestionar solo las solicitudes asignadas.”
- **CU-07**: “Como administrador quiero publicar contenido educativo.”
- **CU-08**: “Como visitante quiero acceder a información sobre reciclaje sin registrarme.”
- **CU-09**: “Como administrador quiero visualizar estadísticas para tomar decisiones.”
- **CU-10**: “Como usuario quiero enviar feedback sobre el servicio.”

---

## 9. Prioridades

| Prioridad | Descripción | Requerimientos asociados |
|-----------|-------------|--------------------------|
| Must Have (v0.1) | Funcionalidades mínimas viables para operar el servicio | RF-01 a RF-15, RNF-01 a RNF-06, RNF-08 |
| Should Have (v0.1.1) | Mejoras que elevan la experiencia y el control | RF-16 a RF-23, RNF-07, RNF-09 a RNF-12 |
| Nice to Have (futuro) | Extensiones estratégicas | RF-24, RF-25, RNF-13, RNF-14, integraciones externas, notificaciones avanzadas, app móvil |

---

## 10. Trazabilidad de Requerimientos

| RF | Caso de uso relacionado | RNF asociado | Criterio de aceptación principal |
|----|-------------------------|--------------|----------------------------------|
| RF-01 | CU-01 | RNF-01, RNF-04 | Validación de email/contraseña, confirmación por correo |
| RF-02 | CU-01, CU-03 | RNF-01, RNF-04 | Bloqueo tras 5 intentos, timeout 30 min |
| RF-03 | CU-01 | RNF-01, RNF-12 | Enlace temporal 24 h, invalida links previos |
| RF-04 | CU-03 | RNF-04 | Edición de perfil y listado de solicitudes recientes |
| RF-05 | CU-04, CU-05 | RNF-06, RNF-10 | CRUD completo con roles y activación/desactivación |
| RF-06 | CU-04 | RNF-02, RNF-06 | Validación de nombres únicos, impacto en solicitudes |
| RF-07 | CU-04 | RNF-06 | Registro de cambios y actualización inmediata |
| RF-08 | CU-02 | RNF-02, RNF-04 | Materiales inactivos no visibles en formularios |
| RF-09 | CU-02 | RNF-02, RNF-04 | Filtros por nombre/categoría con resultados <2 s |
| RF-10 | CU-02 | RNF-02, RNF-04, RNF-14 | Rango válido de fecha/cantidad, confirmación con ID |
| RF-11 | CU-03 | RNF-04, RNF-08 | Timeline con estados y timestamps auditables |
| RF-12 | CU-04 | RNF-01, RNF-12, RNF-14 | Flujo de transición validado y notificaciones |
| RF-13 | CU-05 | RNF-08, RNF-12 | Asignación y reasignación con criterios y alertas |
| RF-14 | CU-06 | RNF-05, RNF-07 | Acceso segmentado y registro de acciones |
| RF-15 | CU-02, CU-05, CU-06 | RNF-01, RNF-09 | Notificaciones internas/email configurables |
| RF-16 | CU-07 | RNF-04, RNF-06 | CRUD de posts con estado y preview |
| RF-17 | CU-07 | RNF-04, RNF-08 | Categorías visibles en portal y formularios |
| RF-18 | CU-08 | RNF-04, RNF-05 | Contenido público accesible sin autenticación |
| RF-19 | CU-08 | RNF-04, RNF-05 | Edición de secciones públicas con auditoría |
| RF-20 | CU-05 | RNF-06, RNF-08 | CRUD de organizaciones con validaciones geográficas |
| RF-21 | CU-05 | RNF-08, RNF-12 | Definición de capacidades/zonas con cálculo de asignación |
| RF-22 | CU-10 | RNF-01, RNF-04 | Formulario de feedback con seguimiento |
| RF-23 | CU-10 | RNF-06, RNF-09 | Respuesta y cierre de feedback con registro |
| RF-24 | CU-09 | RNF-02, RNF-09 | Dashboard con métricas actualizadas y filtros |
| RF-25 | CU-09 | RNF-10, RNF-12 | Configuración centralizada con auditoría |

---

## 11. Criterios de Aceptación (extracto)

- **RF-01 – Registro de usuarios**
  - Validación de email único y contraseña ≥8 caracteres.
  - Confirmación por correo y alta con rol `USER`.
- **RF-02 – Autenticación**
  - Bloqueo después de 5 intentos fallidos.
  - Redirección según rol (USER/ORGANIZATION/ADMIN).
- **RF-03 – Recuperación de contraseña**
  - Enlace caduca a las 24 h y solo puede usarse una vez.
- **RF-04 – Perfil de usuario**
  - Edición de datos personales y listado de últimas 5 solicitudes.
- **RF-06 – Creación de materiales**
  - No permite duplicados (nombre+categoría).
  - Imagen opcional, estado activo por defecto.
- **RF-08 – Activación/desactivación**
  - Material inactivo no aparece en formularios nuevos.
- **RF-10 – Creación de solicitudes**
  - Permite múltiples materiales activos, validación de rangos y muestra número de solicitud.
- **RF-12 – Administración de estados**
  - Cambio exige justificación en cancelaciones y envía notificación.
- **RF-14 – Panel de organizaciones**
  - Acceso solo a solicitudes asignadas con registro de cambios.
- **RF-16 – Gestión de posts**
  - Publicación requiere título, categoría, contenido y estado activo.
- **RF-22 – Feedback**
  - Formulario obligatorio: nombre, comentario; email opcional.
- **RF-24 – Dashboard**
  - Métricas de solicitudes, usuarios y materiales con filtros por fecha.

(El documento completo preserva los criterios para cada RF crítico.)

---

## 12. Métricas de Éxito

- **KPIs de rendimiento**
  - Disponibilidad: ≥99% mensual.
  - Tiempo de respuesta: 95% de operaciones <2 s.
  - Procesamiento operativo: 80% de solicitudes completadas <48 h.

- **KPIs de negocio**
  - Adopción: 500 usuarios registrados en el primer trimestre.
  - Satisfacción: Calificación media ≥4.5/5.
  - Eficiencia: Reducción del 30% en reclamos por recolección fallida.

- **KPIs técnicos**
  - Cobertura de pruebas ≥85%.
  - Cero vulnerabilidades críticas relevantes.
  - Tiempo medio de resolución de bugs <4 h.

---

## 13. Plan de Entregas

| Fase | Duración | Actividades | Entregables |
|------|----------|-------------|-------------|
| 1 – Análisis & Diseño | Semanas 1-2 | Definición final RF/RNF, arquitectura, diagramas | Documento v1.2, diagramas UML |
| 2 – Desarrollo inicial | Semanas 3-6 | Usuarios, autenticación, materiales | MVP v0.1 |
| 3 – Desarrollo avanzado | Semanas 7-10 | Solicitudes, notificaciones, dashboard | Versión 0.1.1 |
| 4 – Pruebas & ajustes | Semanas 11-12 | QA, integración, feedback | Release candidate |
| 5 – Despliegue & cierre | Semana 13 | Deploy productivo, capacitación | Versión 1.0 estable |

---

## 14. Arquitectura y Diseño
- **Capa de presentación**: Controladores Thymeleaf, HTMX para dinámicas ligeras.
- **Capa de servicio**: 12 servicios con lógica de negocio y anotaciones `@Transactional`.
- **Capa de datos**: 8 repositorios JPA, 10 entidades con asociaciones ManyToOne/ManyToMany.
- **Dependencias clave**: Spring Boot starters (web, data-jpa, security, thymeleaf), Lombok, Cloudinary, PostgreSQL driver, Thymeleaf Layout Dialect, Spring Security extras.

---

## 15. Gestión de Riesgos

| Riesgo | Impacto | Probabilidad | Mitigación |
|--------|----------|--------------|------------|
| Dependencia de Cloudinary | Alto | Medio | Fallback local y script de migración. |
| Curva de aprendizaje de Thymeleaf/HTMX | Medio | Alto | Capacitación interna y guías de estilo ([templates/fragments/](cci:7://file:///c:/Users/gonca/OneDrive/Documentos/GitHub/residuosolido/src/main/resources/templates/fragments:0:0-0:0)). |
| Falta de pruebas automatizadas | Alto | Medio | Implementar suite obligatoria antes de cada release. |
| Cambios en regulaciones de datos | Alto | Bajo | Monitoreo legal trimestral y ajustes de políticas. |
| Sobrecarga de organizaciones | Medio | Medio | Alertas en dashboard cuando capacidad se supera. |
| Integración futura con otros sistemas | Medio | Medio | Diseñar APIs REST siguiendo RNF-13. |

---

## 16. Anexos
- **A.1 Diagramas UML**: Diagrama de clases y casos de uso (ver carpeta de documentación).
- **A.2 Referencias**: Enlaces a normativas locales, políticas de privacidad y guías de estilo.