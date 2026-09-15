# Residuo Sólido — Plataforma de Gestión de Reciclaje

Sistema web para la gestión y coordinación de recolección de residuos
reciclables en la Frontera de la Paz (Rivera, Uruguay — Sant'Ana do
Livramento, Brasil).

## Características

- Autenticación con roles diferenciados (Usuario, Organización) y
  onboarding forzado de perfil de organización
- Solicitudes de recolección por usuario registrado o invitado (sin
  cuenta), con rastreo por teléfono + código privado
- Selector de código de país (Uruguay +598 / Brasil +55) con
  normalización E.164 server-side
- Selección de organización por ciudad y materiales reciclables
- Flujo de estados de solicitud (pendiente → en curso/rechazada →
  completada) gestionado por la organización
- Tablero Kanban integrado en el panel de organización
- Blog estático con historias del reciclaje (recolectores informales,
  galpones de acopio, Frontera de la Paz)
- Rate limiting para solicitudes de invitados y bloqueo de cuenta tras
  intentos de login fallidos
- PWA instalable (manifest, service worker, iconos)
- Diseño canónico homogéneo (variables CSS, BEM, 0 estilos inline)
- Internacionalización (es/pt)

## Stack Tecnológico

- **Backend:** Java 21, Spring Boot 3.2, Spring Security 6
- **Frontend:** Thymeleaf 3 (SSR) + thymeleaf-layout-dialect,
  FontAwesome 6
- **Base de Datos:** MongoDB (spring-data-mongodb)
- **Seguridad:** Spring Security (login con bloqueo por intentos
  fallidos, CSRF, rate limiting)
- **Almacenamiento de imágenes:** Local en disco (carpeta configurable
  vía `UPLOAD_DIR`, servida en `/uploads/**`)
- **PWA:** manifest.json, service worker, iconos PNG

## Requisitos

- Java 21 o superior
- Maven 3.8+
- MongoDB (local o Atlas)

## Instalación y Ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/fedegonc/residuosolido.git
   cd residuosolido
   ```

2. **Configurar variables de entorno**

   Crear archivo `.env` en la raíz con:
   ```properties
   SPRING_DATA_MONGODB_URI=mongodb://localhost:27017
   UPLOAD_DIR=uploads
   ```

3. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

4. **Acceder** a `http://localhost:8080`

5. **Correr los tests**
   ```bash
   mvn test
   ```

## Documentación

- `docs/INDICE.md` — Punto de entrada y mapa de toda la documentación
- `docs/ENDPOINTS.md` — Rutas HTTP del sistema, acceso por rol y OpenAPI
- `docs/DIAGRAMAS.md` — Diagramas UML, arquitectura y requisitos en anexo
- `docs/DEFENSA.md` — Guía de defensa, tradeoffs y limitaciones
- `docs/MEJORAS.md` — Mejoras implementadas, diferidas y correcciones
- `docs/METODOLOGIA.md` — Modelo iterativo incremental (4 fases)
- `docs/diagrams/` — Diagramas draw.io (casos de uso, modelo lógico,
  clases, estados)

Los anexos (arquitectura, RF-RN, testing, hardening, tradeoffs, copys)
están como secciones dentro de estos archivos. Ver `docs/INDICE.md`.

## Build de producción

```bash
mvn clean package -DskipTests
java -jar target/app-0.0.1-SNAPSHOT.jar
```

## Contribución

Las contribuciones son bienvenidas. Abrí un issue o pull request para
sugerencias y mejoras.
