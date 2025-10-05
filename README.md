# 🌱 Plataforma de Gestión de Residuos Sólidos

Sistema web para la gestión y coordinación de recolección de residuos reciclables, facilitando la comunicación entre ciudadanos, organizaciones recolectoras y administradores.

## ✨ Características

- Sistema de autenticación con roles diferenciados (Admin, Usuario, Organización)
- Gestión de solicitudes de recolección de residuos
- Catálogo de materiales reciclables
- Panel de estadísticas y métricas
- Contenido educativo sobre reciclaje
- Interfaz responsive adaptada a dispositivos móviles

## 🛠️ Stack Tecnológico

- **Backend:** Java 17, Spring Boot 3.2
- **Frontend:** Thymeleaf, TailwindCSS, Lucide Icons
- **Base de Datos:** PostgreSQL
- **Seguridad:** Spring Security
- **Almacenamiento:** Cloudinary (opcional)

## 📋 Requisitos

- Java 17 o superior
- Maven 3.8+
- PostgreSQL 12+

## 🚀 Instalación y Ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/fedegonc/residuosolido.git
   cd residuosolido
   ```

2. **Configurar variables de entorno**
   
   Crear archivo `.env` o configurar las siguientes variables:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/residuosolido
   SPRING_DATASOURCE_USERNAME=tu_usuario
   SPRING_DATASOURCE_PASSWORD=tu_contraseña
   CLOUDINARY_URL=cloudinary://<key>:<secret>@<cloud_name>  # opcional
   ```

3. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

4. **Acceder a la aplicación**
   
   Abrir navegador en `http://localhost:8080`

## 📦 Despliegue

### Build de producción
```bash
mvn clean package -DskipTests
java -jar target/app-0.0.1-SNAPSHOT.jar
```

### Docker (opcional)
```bash
docker build -t residuosolido:latest .
docker run -p 8080:8080 --env-file .env residuosolido:latest
```

## 📄 Licencia

Este proyecto está bajo licencia MIT.

## 👥 Contribución

Las contribuciones son bienvenidas. Por favor, abre un issue o pull request para sugerencias y mejoras.
