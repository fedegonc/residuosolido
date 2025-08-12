# Sistema de Gestión de Residuos Sólidos
**Rivera - Sant'ana do Livramento**

Plataforma web integral para la gestión de residuos sólidos urbanos que conecta ciudadanos y organizaciones en un ecosistema colaborativo de reciclaje y manejo responsable de residuos.

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
- **Formularios validados** en tiempo real
- **Mensajes informativos** y confirmaciones
- **Layout adaptativo** para móviles

## ⚙️ Características No Funcionales

- **Seguridad**: Spring Security con autorización por roles (`USER`, `ORGANIZATION`, `ADMIN`).
- **Plantillas**: Thymeleaf 3.1 con fragmentos puros (`fragments/layout.html`, `fragments/master-layout.html`, `fragments/login-modal.html`, `fragments/guest-dropdown.html`, `fragments/footer.html`).
- **Caché de plantillas**: deshabilitada en desarrollo (`spring.thymeleaf.cache=false`).
- **Logging**: niveles reducidos a `WARN` para Thymeleaf y MVC; logs de acceso de Tomcat deshabilitados por defecto.
- **Estilos**: Tailwind CSS vía CDN en desarrollo; fuente global Nunito.
- **Sesiones**: política `ALWAYS` configurada para depuración.
- **HTTP/2 y compresión**: deshabilitados actualmente para evitar respuestas truncadas durante diagnóstico.
- **Recursos estáticos**: servidos desde `src/main/resources/static/` (incluye `favicon.svg`).

## 🌐 Endpoints y Rutas

### Públicas (sin autenticación)
- `GET /` y `GET /index` — Página pública inicial.
- `GET /invitados` y `GET /guest/**` — Secciones para visitantes/guest.
- `GET /auth/login`, `GET /auth/register` — Autenticación.
- `POST /auth/login` — Procesamiento de login.
- `GET /posts/**` — Contenido público (posts).
- `GET /categories/**` — Listado/categorías públicas.
- Recursos estáticos: `/css/**`, `/js/**`, `/images/**`, `/static/**`.

### Usuario (requiere rol USER)
- `GET /users/dashboard` — Dashboard de usuario.
- `GET /requests/**` — Gestión de solicitudes del usuario.

### Organización (requiere rol ORGANIZATION)
- `GET /org/dashboard` — Dashboard de organización.
- `GET /org/settings` — Configuración de organización (en preparación).

### Administración (requiere rol ADMIN)
- `GET /admin/**` — Paneles y páginas de administración.
- `GET /admin/users/**` — Gestión de usuarios.
- `GET /mapa/**` — Rutas de mapa administrativas (si se habilitan).

Notas:
- La seguridad está configurada en `SecurityConfig.java` usando `requestMatchers` por patrón.
- Tras autenticación, la app redirige según rol (handler de éxito de login).

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
