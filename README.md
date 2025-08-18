# 🌱 Sistema de Gestión de Residuos Sólidos
**Rivera - Sant'ana do Livramento**

Aplicación web full-stack para gestión integral de residuos sólidos urbanos. Arquitectura Spring Boot + PostgreSQL + Thymeleaf con sistema multi-rol y deployment optimizado para producción.

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

### Backend
- **Runtime**: Java 17 + Spring Boot 3.2.0
- **Security**: Spring Security 6 con autorización por roles
- **Data**: JPA/Hibernate + PostgreSQL + HikariCP
- **Architecture**: MVC en capas (Controller → Service → Repository)
- **Build**: Maven 3.8+

### Frontend
- **Templates**: Thymeleaf 3.1 con fragmentos reutilizables
- **Styles**: Tailwind CSS (CDN) - NO Bootstrap
- **JavaScript**: ES6+ vanilla para interactividad
- **UI/UX**: Responsive design, colores suaves, iconografía SVG

### DevOps & Deploy
- **Database**: PostgreSQL 12+ (producción) / H2 (testing)
- **Server**: Tomcat embebido (Spring Boot)
- **Profiles**: dev, test, prod configurables
- **Monitoring**: Logs estructurados + SQL debugging

## 🏗️ Arquitectura del Sistema

### Backend Architecture
```
src/main/java/com/residuosolido/app/
├── controller/
│   ├── admin/          # AdminUserController
│   ├── auth/           # AuthController (login/register)
│   ├── guest/          # Controladores públicos
│   ├── org/            # OrganizationController
│   └── user/           # UserController
├── service/            # Lógica de negocio
│   ├── AuthService     # Autenticación + index data
│   ├── UserService     # CRUD usuarios + validaciones
│   ├── PostService     # Gestión contenido
│   └── MaterialService # Gestión materiales
├── repository/         # JPA Repositories
├── model/              # Entidades JPA optimizadas
├── dto/                # Data Transfer Objects
└── config/             # Security + configuración
```

### Frontend Structure
```
src/main/resources/
├── templates/
│   ├── fragments/      # Componentes reutilizables
│   │   ├── layout.html      # Layout principal
│   │   ├── admin-layout.html # Layout admin
│   │   └── auth-layout.html  # Layout autenticación
│   ├── admin/          # Panel administración
│   ├── auth/           # Login/registro
│   ├── guest/          # Página pública
│   ├── org/            # Dashboard organizaciones
│   └── users/          # Dashboard usuarios
└── static/
    ├── js/             # JavaScript modular
    └── images/         # Assets estáticos
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

## 🚀 Quick Start

### Prerrequisitos
- **Java 17+** (OpenJDK recomendado)
- **PostgreSQL 12+** (producción) o H2 (desarrollo)
- **Maven 3.8+**
- **Git**

### Desarrollo Local

1. **Setup del proyecto**
```bash
git clone [repository-url]
cd residuosolido
cp .env.example .env  # Configurar variables
```

2. **Base de datos PostgreSQL**
```sql
CREATE DATABASE residuosolido;
CREATE USER residuo_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE residuosolido TO residuo_user;
```

3. **Variables de entorno (.env)**
```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/residuosolido
DB_USERNAME=residuo_user
DB_PASSWORD=dev_password
DB_DRIVER=org.postgresql.Driver

# Cloudinary (opcional)
CLOUDINARY_CLOUD_NAME=your_cloud
CLOUDINARY_API_KEY=your_key
CLOUDINARY_API_SECRET=your_secret
```

4. **Ejecutar aplicación**
```bash
# Desarrollo con PostgreSQL
mvn spring-boot:run

# Testing con H2 (en memoria)
mvn spring-boot:run -Dspring.profiles.active=test
```

5. **Acceso inicial**
- **URL**: http://localhost:8080
- **Admin**: username=`admin`, password=`12345`
- **Hot reload**: Templates se actualizan automáticamente

### Testing Rápido
```bash
# H2 en memoria (sin PostgreSQL)
mvn spring-boot:run -Dspring.profiles.active=test
# Puerto: 8081, datos se resetean al reiniciar
```

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

## 🔧 Configuración Avanzada

### Profiles de Aplicación

#### Development (default)
```properties
# application.properties
spring.profiles.active=dev
spring.jpa.hibernate.ddl-auto=update
spring.thymeleaf.cache=false
spring.devtools.restart.enabled=true
```

#### Testing
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
server.port=8081
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Production
```properties
# application-prod.properties
spring.jpa.hibernate.ddl-auto=validate
spring.thymeleaf.cache=true
logging.level.org.hibernate.SQL=WARN
```

### Optimizaciones de Performance

#### Database
- **Connection Pool**: HikariCP optimizado
- **N+1 Queries**: Resuelto con JOIN FETCH
- **Lazy Loading**: FetchType.LAZY por defecto
- **Índices**: En campos únicos (username, email)

#### Frontend
- **Hot Reload**: Templates sin restart completo
- **Tailwind**: CDN en dev, build optimizado en prod
- **Assets**: Servidos desde `/static/`

### Security Configuration
```java
// Estructura de autorización
@PreAuthorize("hasRole('ADMIN')")     // /admin/**
@PreAuthorize("hasRole('ORGANIZATION')") // /org/**
@PreAuthorize("hasRole('USER')")      // /users/**
// Públicas: /, /auth/**, /posts/**
```

## 🚀 Deployment

### Docker Deployment
```dockerfile
# Dockerfile incluido
FROM openjdk:17-jdk-slim
COPY target/app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build & Deploy
mvn clean package
docker build -t residuosolido .
docker run -p 8080:8080 --env-file .env residuosolido
```

### Production Checklist
- [ ] **Database**: PostgreSQL configurado y migrado
- [ ] **Environment**: Variables `.env` en servidor
- [ ] **Security**: HTTPS configurado
- [ ] **Monitoring**: Logs centralizados
- [ ] **Backup**: Base de datos automatizado
- [ ] **Performance**: Connection pool optimizado

### Cloud Deploy (Heroku/Railway/etc)
```bash
# Heroku example
heroku create residuosolido-app
heroku addons:create heroku-postgresql:hobby-dev
heroku config:set SPRING_PROFILES_ACTIVE=prod
git push heroku main
```

## 📊 API Endpoints

### Públicos
- `GET /` - Página principal
- `GET /auth/login` - Login
- `POST /auth/register` - Registro
- `GET /posts/**` - Contenido público

### Autenticados
- `GET /users/dashboard` - Dashboard usuario
- `GET /org/dashboard` - Dashboard organización
- `GET /admin/dashboard` - Panel admin

### Admin APIs
- `GET /admin/users` - Gestión usuarios
- `POST /admin/posts` - Crear contenido
- `PUT /admin/materials/{id}` - Actualizar materiales

## 🔍 Monitoring & Debug

### Logs SQL
```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Health Checks
- `GET /actuator/health` (si se habilita)
- Database connection status
- Application metrics

## 📈 Roadmap

### Backend
- [ ] API REST para móviles
- [ ] Microservicios (separar auth/content)
- [ ] Cache con Redis
- [ ] Message queues (RabbitMQ)

### Frontend
- [ ] PWA capabilities
- [ ] Real-time notifications
- [ ] Advanced analytics dashboard
- [ ] Mobile-first redesign

### DevOps
- [ ] CI/CD pipeline
- [ ] Kubernetes deployment
- [ ] Automated testing
- [ ] Performance monitoring

---

**Developed with ❤️ for sustainable waste management in Rivera - Sant'ana do Livramento**
