# Sistema de Gestión de Residuos Sólidos

## Características de la Interfaz de Usuario

### Tablas de Entidades con Botones de Acción
Todas las tablas principales del sistema incluyen botones de acción para mejorar la experiencia de usuario:

- **Botón "Ver"**: Botones de color naranja que permiten acceder a los detalles de cada registro
  - Implementado en tablas de: Usuarios, Materiales, Organizaciones, Notificaciones y Solicitudes
  - Diseño consistente con Tailwind CSS (bg-orange-500, hover:bg-orange-600)

### Mensajes Informativos
La interfaz muestra mensajes amigables cuando no hay datos disponibles:

- **Mensajes personalizados** para cada tipo de entidad
- **Enlaces directos** para crear nuevos registros desde el mensaje
- **Estilo visual informativo** con iconos y colores distintivos


## Entidades del Sistema

### User
Representa a los usuarios del sistema con diferentes niveles de acceso.

**Atributos**:
- `id`: Identificador único del usuario
- `username`: Nombre de usuario para acceder al sistema
- `email`: Correo electrónico del usuario
- `password`: Contraseña almacenada de forma segura
- `role`: Rol asignado al usuario (ADMIN, ORGANIZATION, USER)
- `firstName`: Nombre real del usuario
- `lastName`: Apellido del usuario
- `phone`: Número de contacto
- `createdAt`: Fecha de creación de la cuenta
- `lastAccess`: Última vez que el usuario ingresó
- `active`: Indica si la cuenta está activa

### Role
Enum que define los posibles roles de usuario.

**Valores**:
- `ADMIN`: Administrador del sistema
- `ORGANIZATION`: Representante de una organización de gestión de residuos
- `USER`: Usuario normal que puede solicitar recolecciones

### Material
Representa los materiales reciclables que se pueden procesar.

**Atributos**:
- `id`: Identificador único del material
- `name`: Nombre del material
- `description`: Descripción detallada del material
- `category`: Categoría del material
- `active`: Indica si el material está disponible para recolección
- `organization`: Organización que gestiona este material

### Organization
Entidades responsables de la gestión de residuos.

**Atributos**:
- `id`: Identificador único de la organización
- `name`: Nombre de la organización
- `description`: Descripción de la organización
- `address`: Ubicación física
- `phoneNumber`: Número de contacto
- `email`: Correo electrónico
- `website`: Sitio web oficial
- `dailyCapacity`: Capacidad máxima diaria para procesar residuos
- `active`: Indica si la organización está activa
- `acceptedMaterials`: Lista de materiales que la organización acepta

### Request
Representa las solicitudes de recolección de residuos.

**Atributos**:
- `id`: Identificador único de la solicitud
- `user`: Usuario que realiza la solicitud
- `organization`: Organización asignada para la recolección
- `materials`: Lista de materiales incluidos en la solicitud
- `collectionAddress`: Dirección donde se recogerán los residuos
- `scheduledDate`: Fecha propuesta para la recolección
- `createdAt`: Fecha y hora de creación de la solicitud
- `updatedAt`: Fecha y hora de la última actualización
- `status`: Estado actual de la solicitud (PENDING, ACCEPTED, REJECTED, COMPLETED)
- `notes`: Comentarios adicionales

### RequestStatus
Enum que define los estados posibles de una solicitud.

**Valores**:
- `PENDING`: Solicitud creada pero no procesada aún
- `ACCEPTED`: Solicitud aprobada por la organización
- `REJECTED`: Solicitud rechazada por la organización
- `COMPLETED`: Recolección finalizada exitosamente

### Notification
Representa las notificaciones enviadas a los usuarios.

**Atributos**:
- `id`: Identificador único de la notificación
- `title`: Título de la notificación
- `message`: Contenido del mensaje
- `read`: Indica si la notificación fue leída
- `user`: Usuario que recibe la notificación
- `createdAt`: Fecha y hora de creación de la notificación