# Sistema de Gestión de Residuos Sólidos
**Rivera - Sant'ana do Livramento**

Plataforma web integral para la gestión de residuos sólidos urbanos que conecta ciudadanos, organizaciones y administradores en un ecosistema colaborativo de reciclaje y manejo responsable de residuos.

## 🚀 Características Principales

### 🎭 Sistema Multi-Rol
- **GUEST**: Acceso público al contenido informativo
- **USER**: Ciudadanos que solicitan recolecciones
- **ORGANIZATION**: Entidades que gestionan materiales y recolecciones
- **ADMIN**: Administración completa del sistema

### 📊 Dashboards Especializados
- **Admin Dashboard**: Control total del sistema, gestión de usuarios y contenido
- **Organization Dashboard**: Gestión de materiales, solicitudes y recolecciones
- **User Dashboard**: Solicitud y seguimiento de recolecciones
- **Página Pública**: Información y recursos para visitantes

### 🗂️ Gestión Dinámica de Contenido
- **Secciones de Residuos**: Configurables desde panel admin (Reciclables, No Reciclables, etc.)
- **Posts y Categorías**: Sistema CRUD completo para contenido educativo
- **Materiales**: Gestión de tipos de residuos reciclables

### 🔐 Seguridad y Autenticación
- Registro y login con validación robusta
- Recuperación de contraseña con flujo administrativo
- Autorización basada en roles con Spring Security
- Redirección automática según perfil de usuario

## 🛠️ Stack Tecnológico

- **Backend**: Java 17, Spring Boot 3.x, Spring Security, JPA/Hibernate
- **Base de Datos**: PostgreSQL con HikariCP
- **Frontend**: Thymeleaf, Tailwind CSS, JavaScript ES6+
- **Mapas**: Leaflet.js para geolocalización interactiva
- **Build**: Maven

## 📁 Arquitectura del Proyecto

```
src/main/
├── java/com/residuosolido/app/
│   ├── controller/     # Controladores por dominio
│   ├── service/        # Lógica de negocio
│   ├── repository/     # Acceso a datos
│   ├── model/          # Entidades JPA
│   ├── dto/           # Objetos de transferencia
│   └── config/        # Configuración y seguridad
└── resources/
    ├── templates/     # Vistas Thymeleaf
    │   ├── admin/     # Páginas de administración
    │   ├── auth/      # Login, registro
    │   ├── guest/     # Página pública
    │   ├── org/       # Dashboard organizaciones
    │   └── users/     # Dashboard usuarios
    └── static/        # CSS, JS, imágenes
```

## 🗃️ Modelo de Datos

### User
Entidad principal que maneja todos los tipos de usuario según rol.

**Campos principales**:
- Información personal: `username`, `email`, `firstName`, `lastName`
- Seguridad: `password` (encriptada), `role`, `active`
- Geolocalización: `latitud`, `longitud` para mapas
- Auditoría: `createdAt`, `lastAccessAt`

### WasteSection
Secciones dinámicas de residuos configurables desde admin.

**Campos**:
- `title`: Título de la sección (ej: "Reciclables")
- `description`: Descripción detallada
- `icon`: Icono para la interfaz
- `displayOrder`: Orden de visualización
- `active`: Estado de la sección
- `actionText`: Texto del botón de acción

### Post & Category
Sistema de contenido educativo e informativo.

**Post**:
- `title`, `content`, `imageUrl`
- `category`: Relación con Category
- `author`: Relación con User
- `createdAt`, `updatedAt`

### Material
Tipos de residuos gestionados por organizaciones.

**Campos**:
- `name`, `description`, `category`
- `active`: Estado del material
- `recyclingInstructions`: Instrucciones específicas
- `organization`: Organización responsable

### Request
Solicitudes de recolección de residuos.

**Campos**:
- `user`: Usuario solicitante
- `materials`: Materiales a recolectar
- `collectionAddress`: Dirección de recolección
- `scheduledDate`: Fecha programada
- `status`: Estado (PENDING, ACCEPTED, REJECTED, COMPLETED)
- `notes`: Comentarios adicionales

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 17+
- PostgreSQL 12+
- Maven 3.8+

### Instalación

1. **Clonar el repositorio**
```bash
git clone [repository-url]
cd residuosolido
```

2. **Configurar base de datos**
```sql
CREATE DATABASE residuosolido;
CREATE USER residuo_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE residuosolido TO residuo_user;
```

3. **Configurar application.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/residuosolido
spring.datasource.username=residuo_user
spring.datasource.password=your_password
```

4. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

5. **Acceder a la aplicación**
- URL: http://localhost:8080
- Admin por defecto: username=admin, password=admin123

## 📋 Funcionalidades por Rol

### 👤 Usuario (USER)
- ✅ Registro y perfil personal
- ✅ Solicitar recolecciones
- ✅ Ver estado de solicitudes
- ✅ Mapa interactivo para ubicación

### 🏢 Organización (ORGANIZATION)
- ✅ Dashboard de gestión
- ✅ Administrar materiales
- ✅ Procesar solicitudes
- ✅ Coordinar recolecciones

### ⚙️ Administrador (ADMIN)
- ✅ Panel de control completo
- ✅ Gestión de usuarios y roles
- ✅ Configuración de secciones
- ✅ Gestión de contenido (posts/categorías)
- ✅ Recuperación de contraseñas
- ✅ Configuración del sitio

## 🎨 Características de UX/UI

- **Diseño responsivo** con Tailwind CSS
- **Colores suaves** (verde menta, grises claros)
- **Iconografía consistente** con SVG
- **Navegación intuitiva** por roles
- **Mapas interactivos** con Leaflet
- **Formularios validados** en tiempo real
- **Mensajes informativos** y confirmaciones
- **Layout adaptativo** para móviles

## 🔧 Configuración de Desarrollo

### Variables de Entorno
```properties
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/residuosolido
DB_USERNAME=residuo_user
DB_PASSWORD=your_password

# Logs SQL (desarrollo)
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### Estructura de Seguridad
- **Rutas públicas**: `/`, `/auth/**`, `/posts/**`
- **Rutas de admin**: `/admin/**`
- **Rutas de organización**: `/org/**`
- **Rutas de usuario**: `/users/**`

## 📈 Roadmap

- [ ] Sistema de notificaciones en tiempo real
- [ ] API REST para aplicaciones móviles
- [ ] Reportes y estadísticas avanzadas
- [ ] Integración con servicios de mapas externos
- [ ] Sistema de gamificación para usuarios

---

**Developed with ❤️ for sustainable waste management in Rivera - Sant'ana do Livramento**