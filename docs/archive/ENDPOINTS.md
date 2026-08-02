# 📍 Endpoints del Sistema - Documentación Completa

## 🎯 Propósito
Este documento mantiene un registro de todos los endpoints del sistema, sus mejoras y la evolución de las rutas hacia URLs en español más descriptivas.

---

## 📊 Tabla de Endpoints por Tipo de Usuario

### **👤 Usuarios (Ciudadanos)**
| Endpoint Original | Endpoint Actual | Descripción | Estado |
|-------------------|-----------------|-------------|--------|
| `/user/dashboard` | `/usuarios/inicio` | Dashboard principal del usuario | ✅ Actualizado |
| `/user/profile` | `/usuarios/perfil` | Perfil del usuario | ✅ Actualizado |
| `/users/requests` | `/usuarios/solicitudes` | Lista de solicitudes del usuario | ✅ Actualizado |
| `/users/requests/new` | `/usuarios/solicitudes/nueva` | Crear nueva solicitud | ✅ Actualizado |

### **🏢 Centros de Acopio (Organizaciones)**
| Endpoint Original | Endpoint Actual | Descripción | Estado |
|-------------------|-----------------|-------------|--------|
| `/org/dashboard` | `/acopio/inicio` | Dashboard del centro de acopio | ✅ Actualizado |
| `/org/profile` | `/acopio/perfil` | Perfil del centro | ✅ Actualizado |
| `/org/statistics` | `/acopio/estadisticas` | Estadísticas del centro | ✅ Actualizado |
| `/organization/requests` | `/acopio/solicitudes` | Gestión de solicitudes | ✅ Actualizado |
| `/organization/calendar` | `/acopio/calendario` | Calendario de recolecciones | ✅ Actualizado |

### **📝 Contenido Público**
| Endpoint Original | Endpoint Actual | Descripción | Estado |
|-------------------|-----------------|-------------|--------|
| `/posts` | `/posts` | Lista de posts educativos | ✅ Mantenido |
| `/posts/{id}` | `/posts/{id}` | Detalle de post | ✅ Mantenido |
| N/A | N/A | Nombre en UI: "Notas Educativas" | ✅ Actualizado |

### **🔧 Administración**
| Endpoint Original | Endpoint Actual | Descripción | Estado |
|-------------------|-----------------|-------------|--------|
| `/admin/users` | `/admin/users` | Gestión de usuarios | ✅ Mantenido |
| `/admin/organizations` | `/admin/organizations` | Gestión de organizaciones | ✅ Mantenido |
| `/admin/requests` | `/admin/requests` | Gestión de solicitudes | ✅ Mantenido |
| `/admin/materials` | `/admin/materials` | Gestión de materiales | ✅ Mantenido |
| `/admin/posts` | `/admin/posts` | Gestión de posts | ✅ Mantenido |
| `/admin/categories` | `/admin/categories` | Gestión de categorías | ✅ Mantenido |

---

## 🎨 Mejoras de Diseño Implementadas

### **1. Eliminación del "Look de IA"**
**Fecha:** 11/10/2025

**Cambios:**
- ✅ Eliminados emojis infantiles (🌱, 📚)
- ✅ Reemplazado fondo verde sólido por fondo blanco con barra decorativa
- ✅ Tipografía refinada: `font-semibold` en lugar de `font-bold`
- ✅ Colores diferenciados por función (verde, azul, morado)
- ✅ Gradientes sutiles (5-10% opacity)
- ✅ Sombras más sutiles y profesionales

**Archivos afectados:**
- `users/dashboard.html`
- `public/posts.html`

### **2. Unificación de Terminología**
**Fecha:** 11/10/2025

**Cambios:**
- ✅ "Posts" → "Notas" en toda la UI
- ✅ Mantenidas URLs `/posts` para SEO
- ✅ Actualizado en navbars, breadcrumbs y títulos

**Archivos afectados:**
- `navbar-user.html`
- `navbar-org.html`
- `navbar-guest.html`
- `navbar-admin.html`
- `BreadcrumbService.java`
- `public/posts.html`
- `users/dashboard.html`

### **3. URLs en Español**
**Fecha:** 11/10/2025

**Cambios:**
- ✅ `/users/` → `/usuarios/`
- ✅ `/dashboard` → `/inicio`
- ✅ `/profile` → `/perfil`
- ✅ `/requests` → `/solicitudes`
- ✅ `/requests/new` → `/solicitudes/nueva`
- ✅ `/org/` → `/acopio/`

**Beneficios:**
- Sin anglicismos
- Más intuitivo para usuarios hispanohablantes
- Mejor SEO en español

---

## 🔄 Próximas Mejoras Planificadas

### **Alta Prioridad**
- [x] Cambiar `/org/` a `/acopio/` en controladores
- [x] Actualizar navbar de organizaciones
- [x] Actualizar BreadcrumbInterceptor para "acopio"
- [ ] Actualizar templates de organizaciones (org/dashboard.html, etc.)

### **Media Prioridad**
- [ ] Implementar rutas en español para feedback
- [ ] Mejorar diseño de tablas de solicitudes
- [ ] Añadir más información contextual en dashboards

### **Baja Prioridad**
- [ ] Internacionalización completa (i18n)
- [ ] Aliases para URLs antiguas (redirects)
- [ ] Documentación de API REST

---

## 📝 Notas de Implementación

### **Convenciones de Nomenclatura**

**URLs:**
- Siempre en minúsculas
- Usar guiones para palabras compuestas (si es necesario)
- Preferir español sobre inglés
- Mantener consistencia en plurales/singulares

**Ejemplos:**
- ✅ `/usuarios/solicitudes/nueva`
- ✅ `/recolector/estadisticas`
- ❌ `/users/newRequest`
- ❌ `/org/stats`

### **Breadcrumbs**
- Usar nombres descriptivos en español
- Mantener jerarquía clara
- Máximo 4 niveles de profundidad

### **Navegación**
- Todos los navbars deben usar las mismas rutas
- Mantener consistencia entre roles
- Iconos Lucide para mejor UX

---

## 🔍 Búsqueda Rápida

**Por Rol:**
- Usuarios: `/usuarios/*`
- Centros de Acopio: `/acopio/*`
- Admin: `/admin/*`
- Público: `/posts`, `/`

**Por Funcionalidad:**
- Solicitudes: `*/solicitudes`
- Perfil: `*/perfil`
- Dashboard: `*/inicio`
- Estadísticas: `*/estadisticas`

---

## 📚 Referencias

- [ARQUITECTURA_GRAN_ESCALA.md](./ARQUITECTURA_GRAN_ESCALA.md)
- [DIAGRAMAS_FLUJO.md](./DIAGRAMAS_FLUJO.md)
- [FLUJOS_USUARIO.md](./FLUJOS_USUARIO.md)
- [MAPEO_HEADERS_NAVBARS.md](./MAPEO_HEADERS_NAVBARS.md)

---

**Última actualización:** 11/10/2025
**Versión:** 1.0.0
