# 🌱 Plataforma de Gestión de Residuos Sólidos
**Municipios Rivera (UY) y Sant'Ana do Livramento (BR)**

## 📌 Contexto y Propósito
El proyecto digitaliza la coordinación entre ciudadanos, organizaciones y el área administrativa para mejorar la recolección de residuos reciclables. La plataforma centraliza solicitudes, materiales disponibles y contenidos educativos para apoyar campañas locales.

## 🧭 Visión de Alto Nivel
- **Actores principales:** ciudadanos que generan solicitudes, organizaciones recolectoras y administradores municipales.
- **Cobertura:** recepción de pedidos de retiro, seguimiento del estado, comunicación de materiales aceptados y difusión de información ambiental.
- **Alcance geográfico:** ciudades fronterizas Rivera y Sant'Ana do Livramento, con posibilidad de extenderse a otras comunidades.

## 🗂️ Casos de Uso Clave
- **Solicitar recolección:** un vecino describe materiales, dirección y fecha deseada.
- **Gestionar solicitudes:** operadores revisan, actualizan estado (pendiente, en curso, completada, cancelada) y registran notas.
- **Administrar materiales y categorías:** se mantiene un catálogo claro de materiales aceptados y su clasificación.
- **Comunicar novedades:** se publican posts educativos sobre separación y eventos ambientales.
- **Monitorear indicadores:** panel administrativo con métricas de solicitudes, usuarios y materiales más demandados.

## ✨ Principales Características
- Autenticación con roles diferenciados (`ADMIN`, `USER`, `ORGANIZATION`).
- Formularios guiados para crear y dar seguimiento a solicitudes.
- Módulos de administración para materiales, categorías, posts informativos y usuarios.
- Estadísticas visuales para la toma de decisiones (tendencias, estados, top de materiales).
- Interfaz adaptada a escritorio y dispositivos móviles.

## 🧾 Requisitos Técnicos
- Java 17
- Maven 3.8 o superior
- PostgreSQL 12 o superior
- Cuenta opcional en Cloudinary para gestionar imágenes (puede deshabilitarse si no se usa).

## 🚀 Puesta en Marcha Local
1. **Clonar el repositorio** y entrar al directorio del proyecto.
2. **Configurar variables de entorno** o un archivo `.env` con credenciales propias de base de datos y, si se desea, Cloudinary. Ejemplo:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/residuosolido
   SPRING_DATASOURCE_USERNAME=tu_usuario
   SPRING_DATASOURCE_PASSWORD=tu_contraseña
   CLOUDINARY_URL=cloudinary://<key>:<secret>@<cloud_name>   # opcional
   ```
3. **Ejecutar la aplicación**:
   ```bash
   mvn spring-boot:run
   ```
4. **Acceder** en `http://localhost:8080`.

## 🔁 Flujo de Despliegue
- **Build manual:**
  ```bash
  mvn -DskipTests package
  java -jar target/app-0.0.1-SNAPSHOT.jar
  ```
- **Docker (opcional):**
  ```bash
  docker build -t residuosolido:latest .
  docker run -p 8080:8080 --env-file .env residuosolido:latest
  ```
- **Proveedores recomendados:** Render, Railway, Fly.io o Heroku (buildpack Java). Sólo se requieren las variables de entorno anteriores adaptadas al servicio.

## 🔄 Buenas Prácticas Operativas
- Mantener actualizada la base de datos PostgreSQL con respaldos periódicos.
- Revisar el panel de estadísticas para detectar materiales con mayor demanda y planificar campañas.
- Actualizar la sección de posts con información relevante para incentivar la participación ciudadana.

---
Este README resume la funcionalidad clave para usuarios y equipos que despliegan la plataforma. Para detalles técnicos adicionales puede consultarse el código fuente y la documentación interna del área de TI.
