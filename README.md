# 🌱 Sistema de Gestión de Residuos Sólidos
**Rivera - Sant'ana do Livramento**

Sistema web para gestión de residuos sólidos urbanos con arquitectura Spring Boot + PostgreSQL + Thymeleaf. Incluye sistema multi-rol, internacionalización (ES/PT) y panel de administración consolidado.

## 🎯 Funcionalidades Principales

- **Gestión de usuarios** (Admin/Organización/Usuario)
- **Solicitudes de recolección** de residuos
- **Administración de materiales** reciclables
- **Sistema de posts y categorías** educativas
- **Panel de administración** completo

## ⚙️ Stack Tecnológico

- **Spring Boot 3.2** + Java 17
- **PostgreSQL** + Spring Security 6
- **Thymeleaf** + Tailwind CSS
- **Cloudinary** para imágenes

## 🔐 Roles del Sistema

- **⚙️ ADMIN**: Acceso completo al sistema, gestión de usuarios y configuración
- **🏢 ORGANIZATION**: Gestión de materiales y procesamiento de solicitudes
- **👤 USER**: Creación de solicitudes y gestión de perfil

## 🏗️ Arquitectura Actual

**Controladores:** 6 admin consolidados  
**Servicios:** 1:1 por entidad  
**Templates:** Organizados por dominio

### Estructura Backend
```
controller/admin/
├── AdminController        # Dashboard, Categories, Posts, Documentation
├── AdminUserController    # Gestión de usuarios
├── AdminMaterialController # Materiales reciclables
├── AdminRequestController # Solicitudes de recolección
├── AdminFeedbackController # Comentarios
└── AdminPasswordResetController # Reset contraseñas
```

## 📋 Guías de Uso

### Gestión de Usuarios
Crear, editar y asignar roles a usuarios del sistema

### Administración de Contenido  
Gestionar posts, categorías y materiales reciclables

### Solicitudes de Recolección
Supervisar y procesar solicitudes de recolección de residuos

## 🚀 Instalación

### Prerrequisitos
- Java 17+
- PostgreSQL 12+
- Maven 3.8+

### Desarrollo Local
```bash
# 1. Clonar proyecto
git clone [repository-url]
cd residuosolido

# 2. Configurar base de datos
CREATE DATABASE residuosolido;

# 3. Ejecutar aplicación
mvn spring-boot:run

# 4. Acceso
# URL: http://localhost:8080
# Admin: admin / 12345
```

### Testing Rápido
```bash
# H2 en memoria (sin PostgreSQL)
mvn spring-boot:run -Dspring.profiles.active=test
```

---

**Desarrollado para la gestión sostenible de residuos en Rivera - Sant'ana do Livramento**
