# 🌱 Plataforma de Gestión de Residuos Sólidos

Sistema web para la gestión y coordinación de recolección de residuos reciclables, facilitando la comunicación entre ciudadanos y organizaciones recolectoras.

## ✨ Características

- Autenticación con roles diferenciados (Usuario, Organización) y onboarding forzado de perfil de organización
- Solicitudes de recolección por usuario registrado o invitado (sin cuenta), con rastreo por teléfono
- Selección de organización por ciudad y materiales reciclables a entregar
- Flujo de estados de solicitud (pendiente → en progreso/rechazada → completada) gestionado por la organización
- Notificación por WhatsApp en cambios de estado (configurable)
- Gestión de recolectores informales por organización
- Panel de estadísticas (dashboard de usuario y de organización)
- Rate limiting para solicitudes de invitados y bloqueo de cuenta tras intentos de login fallidos
- Internacionalización (es/pt)

## 🛠️ Stack Tecnológico

- **Backend:** Java 17, Spring Boot 3.2
- **Frontend:** Thymeleaf (SSR) + thymeleaf-layout-dialect, FontAwesome
- **Base de Datos:** MongoDB
- **Seguridad:** Spring Security (login con bloqueo por intentos fallidos, CSRF)
- **Almacenamiento de imágenes:** Local en disco (carpeta configurable vía `UPLOAD_DIR`, servida en `/uploads/**`)

## 📋 Requisitos

- Java 17 o superior
- Maven 3.8+
- MongoDB (local o Atlas)

## 🚀 Instalación y Ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/fedegonc/residuosolido.git
   cd residuosolido
   ```

2. **Configurar variables de entorno**

   Crear archivo `.env` en la raíz (o exportarlas en el entorno) con las siguientes variables:
   ```properties
   SPRING_DATA_MONGODB_URI=mongodb://localhost:27017
   MONGODB_DATABASE=residuosolido        # opcional, default: fedelabs
   UPLOAD_DIR=uploads                    # opcional, carpeta local para imágenes de solicitudes
   ```

3. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

   El perfil activo por defecto es `dev` (`spring.profiles.active` en `application.properties`). Para producción, usar `-Dspring-boot.run.profiles=prod` o `SPRING_PROFILES_ACTIVE=prod`.

4. **Acceder a la aplicación**

   Abrir navegador en `http://localhost:8080`

5. **Correr los tests**
   ```bash
   mvn test
   ```

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

##  Contribución

Las contribuciones son bienvenidas. Por favor, abre un issue o pull request para sugerencias y mejoras.
