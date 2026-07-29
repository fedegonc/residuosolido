EcoSolicitud — Marco de alcance y backlog técnico

Basado en el Oficio n° 044/2023 (Proyecto Frontera de la Paz Sustentable) y en la auditoría de dominio realizada con stakeholders. Este documento define qué le corresponde resolver al software y qué queda fuera por naturaleza (logística física, capacitación humana), más el backlog inmediato de funcionalidades chicas y bien acotadas.

1. Cumplimiento del oficio fundacional
Objetivo 1 — Agilizar comunicación y colecta de residuos reciclables
 Solicitud de recolección sin necesidad de registro (Invitado), con opción de registro para trackear su solicitud
 El invitado completa la solicitud igual que un usuario registrado; al final, si ya tiene cuenta (match por teléfono) se le pide login, si no tiene cuenta se le pide solo una contraseña + checkbox de consentimiento y se crea la cuenta automáticamente
 Selección manual de organización por parte del usuario (lista filtrada por ciudad, ya no auto-asignación única)
 CityOrganizationService devuelve lista de organizaciones por ciudad (findByRoleAndCity), no una sola
 Formulario conectado a OrgApiController (/api/organizations/by-city) para poblar el selector
 Validación server-side: la organización elegida debe pertenecer realmente a la ciudad de la solicitud (no confiar en el orgId que manda el form)
 Ciclo de estados de la solicitud (PENDING → IN_PROGRESS/REJECTED → COMPLETED), con nombres localizados (es/pt) en la vista
 Rastreo de solicitud sin login, por teléfono (GuestTrackingController)
 Notificación por WhatsApp cuando se acepta la solicitud, avisando que se aproxima el día y que prepare los materiales
 Notificación por WhatsApp solo en cambios de estado importantes (no en todos los estados)
 Cobertura binacional (City.RIVERA, City.LIVRAMENTO), con nombre de ciudad localizado en el template (ej. "Sant'Ana do Livramento" vs "Rivera") y organizaciones disponibles filtradas por esa ciudad
Objetivo 2 — Fortalecer la actividad de los recolectores informales y las organizaciones
 Registro de organizaciones y recolectores informales (censo simple, sin operativa asignada), pensado para informar a las autoridades y posiblemente servir para integrarlos
 Activar/desactivar organización (User con role=ORGANIZATION)
 Activar/desactivar recolector informal (InformalCollector), gestión de materiales que maneja
 Métrica de cantidad recolectada por organización/ciudad
 Reporte descargable en PDF para la organización
Fuera de alcance — responsabilidad no digitalizable, no del software
 Capacitación de catadores → proceso humano/pedagógico, fuera de una tesis de software (pero se puede incluir contenido informativo estático sobre seguridad, higiene y condiciones de trabajo, sin que sea un LMS)
 Adquisición de vehículos, EPP, uniformes, combustible, motoristas → logística/inversión física de la organización
 Ruteo o recurrencia fija de recolección (ej. "todos los viernes 18hs") → logística interna que la organización resuelve por su cuenta, no mencionada en el oficio
 Estimación de cantidad/volumen de material en la solicitud → se negocia por WhatsApp directamente antes de ir a levantar, no necesita campo en el sistema

Nota para la defensa de tesis: estos puntos no son omisiones — son decisiones de alcance conscientes, justificadas porque exceden lo que una herramienta de software puede o debe resolver. Documentar esto explícitamente en la sección de limitaciones.

2. Backlog inmediato (funcionalidades chicas, sin rediseño de arquitectura)
🟢 Notificaciones WhatsApp al Usuario
 Definir proveedor (API de WhatsApp Business / Twilio / similar)
 Servicio nuevo: NotificationService (o WhatsAppNotificationService)
 Disparar notificación en cada transición de estado: Request.accept(), complete(), reject()
 Mensaje debe indicar: nuevo estado + qué necesita preparar el usuario (si aplica)
 Usar el teléfono ya existente en User/Request (getContactPhone())
🟢 Métricas privadas por organización + descarga PDF
 Renombrar PublicMetricsController → OrgMetricsController (ya no es público)
 Nueva ruta protegida bajo /acopio/** (rol ORGANIZATION), ej. /acopio/metricas
 Nuevo método en RequestMetricsService: getMetricsByOrganization(User org) — cantidades por estado, por período
 Endpoint de descarga: GET /acopio/metricas/pdf (librería sugerida: OpenPDF o iText community)
 Retirar o restringir el endpoint público de métricas generales si ya no aplica
🟢 Métrica de cantidad recolectada
 Confirmar si ya existe conteo por estado COMPLETED en RequestMetricsService
 Si no existe: agregar conteo simple por organización/ciudad/período — sin desglose de comercialización, solo cantidad de solicitudes completadas (mantenido deliberadamente simple)
🟡 Consistencia de nombres (limpieza técnica, baja prioridad)
 Revisar y alinear nombres de controllers/servicios que ya no reflejan su alcance real (ej. el caso de PublicMetricsController de arriba)
 Confirmar que la separación por sub-dominio (RequestService / RequestOrganizationService / RequestMetricsService / CityOrganizationService) está reflejada consistentemente en los nombres de sus métodos
3. Criterio para decidir si algo entra al alcance

Antes de agregar cualquier funcionalidad nueva, chequear en este orden:

¿Está en el oficio o surge de una necesidad real confirmada por el stakeholder (organización/usuario)?
¿Es responsabilidad de un sistema de software, o es logística/inversión física/proceso humano?
¿Se puede resolver con un campo o servicio simple, o requiere una entidad/módulo nuevo?

Si la respuesta a 1 es sí, a 2 es "sí es del software", y a 3 es "simple" → entra al backlog inmediato. Si no, se documenta como limitación consciente y trabajo futuro.

4. Resumen ejecutivo (para no-técnicos)

EcoSolicitud resuelve el problema central que le corresponde a un sistema de software: que un vecino pueda pedir la recolección de material reciclable sin fricción, que la organización correcta lo reciba automáticamente, y que ambas partes sepan en qué estado está la solicitud — con aviso directo por WhatsApp. La capacitación de catadores y la provisión de equipamiento físico (vehículos, uniformes, combustible) siguen siendo responsabilidad de la organización y del proyecto mayor de cooperación — no de la aplicación.