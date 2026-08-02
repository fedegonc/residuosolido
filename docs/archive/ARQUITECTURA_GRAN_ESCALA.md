# 🏗️ Arquitectura a Gran Escala - Residuo Sólido

## 📋 Propósito
Este documento registra decisiones arquitectónicas, patrones de diseño y consideraciones de escalabilidad para el proyecto `residuosolido`.

---

## 🎯 Decisiones de Arquitectura

### **1. Sistema de Layouts y Navbars**

#### **Estado Actual (2025-10-10)**
- ✅ **4 Layouts Específicos por Rol:**
  - `admin-layout.html` → `navbar-admin` (Administradores)
  - `user-layout.html` → `navbar-user` (Usuarios)
  - `org-layout.html` → `navbar-org` (Organizaciones)
  - `fragments/layout.html` → `navbar-guest` (Invitados)

#### **Decisión:**
Mantener navbars separados por rol en lugar de un navbar unificado.

#### **Justificación:**
- Cada rol tiene necesidades de navegación específicas
- Facilita el mantenimiento por rol
- Evita lógica condicional compleja en un solo navbar
- Mejor separación de responsabilidades

#### **Alternativa Descartada:**
Navbar único con lógica `sec:authorize` para mostrar/ocultar opciones según rol.

**Razón del descarte:** Mayor complejidad, dificulta debugging, navbar más pesado.

---

## 📝 Notas para Futuras Decisiones

### **Áreas a Documentar:**
- [ ] Estrategia de paginación (front-end vs back-end)
- [ ] Gestión de imágenes (Cloudinary)
- [ ] Estrategia de caché
- [ ] Manejo de sesiones
- [ ] Estructura de controladores
- [ ] Patrón de servicios
- [ ] Validaciones y DTOs
- [ ] Manejo de errores global

---

## 🔄 Historial de Cambios

| Fecha | Cambio | Razón |
|-------|--------|-------|
| 2025-10-10 | Creación del documento | Registrar decisión de layouts separados |
| 2025-10-10 | Migración completa a layouts | Eliminar headers inline, consistencia 100% |

---

## 🎨 Patrones Establecidos

### **Thymeleaf Layout Dialect**
- Uso obligatorio de `layout:decorate` en todas las páginas
- Fragmento `content` para contenido específico
- Breadcrumbs automáticos con `fragments/components/breadcrumbs :: auto`
- Mensajes de success/error estandarizados

### **Estructura de Página Estándar**
```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout" 
      layout:decorate="~{fragments/[LAYOUT]}">
<head>
    <title>Título - Residuo Sólido</title>
</head>
<body>
<main layout:fragment="content">
    <!-- Breadcrumbs -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
        <th:block th:replace="~{fragments/components/breadcrumbs :: auto}"></th:block>
    </div>
    
    <!-- Contenido específico -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <!-- ... -->
    </div>
    
    <script>
        lucide.createIcons();
    </script>
</main>
</body>
</html>
```

---

## 📚 Referencias
- [MAPEO_HEADERS_NAVBARS.md](./MAPEO_HEADERS_NAVBARS.md) - Análisis completo de headers/navbars
- [Patrón Estándar de Páginas Admin](./memoria-admin-pattern.md) - Patrón para páginas admin

---

*Última actualización: 2025-10-10*
