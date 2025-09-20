# 🌱 Sistema de Gestión de Residuos Sólidos
**Rivera - Sant'ana do Livramento**

## 🎯 Objetivo
El propósito de la app es facilitar la gestión de residuos sólidos urbanos:
- Registro y autenticación de usuarios con roles (ADMIN, ORGANIZATION, USER).
- Creación y gestión de solicitudes de recolección.
- Administración de materiales, posts y categorías.
- Panel de administración para seguimiento y configuración.

## ⚙️ Requisitos
- Java 17
- Maven 3.8+
- PostgreSQL 12+

## 🔧 Configuración
### Base de Datos
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/residuosolido
SPRING_DATASOURCE_USERNAME=<usuario_db>
SPRING_DATASOURCE_PASSWORD=<password_db>
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```
### Cloudinary (opcional)
```properties
cloudinary.cloud_name=<cloud_name>
cloudinary.api_key=<api_key>
cloudinary.api_secret=<api_secret>
```

- URL base local: http://localhost:8080

## 🚀 Ejecución Local
```bash
mvn spring-boot:run
```

## 📦 Build
```bash
mvn -DskipTests package
```

## 🐳 Docker
```bash
docker build -t residuosolido:latest .
docker run -p 8080:8080 --env-file .env residuosolido:latest
```

## 🧪 Perfiles
- Test con H2: `mvn spring-boot:run -Dspring.profiles.active=test`

## ☁️ Despliegue
Compatibles con proveedores como Render, Railway, Fly.io, Heroku (buildpack Java). Configurar variables de entorno para DB y Cloudinary.
