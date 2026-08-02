# Flujos de Usuario y Arquitectura de Templates

**Fecha:** 2025-10-10  
**Propósito:** Mapear flujos completos por rol y verificar consistencia de templates

---

## 🎯 Análisis de Consistencia: Página de Feedback

### **Estado Actual:**
✅ **CONSISTENTE** - La página de feedback sigue el patrón correcto

### **Verificación:**
- ✅ Usa `layout:decorate="~{fragments/layout}"` (layout para invitados/usuarios)
- ✅ Tiene breadcrumbs automáticos: `~{fragments/components/breadcrumbs :: auto}`
- ✅ Maneja autenticación correctamente (muestra formulario solo si está logueado)
- ✅ Mensajes de error/éxito con estilos estándar
- ✅ Diseño responsive con grid layout
- ✅ Footer y navbar heredados del layout

### **Características Especiales:**
- Lógica condicional para usuarios autenticados vs no autenticados
- Sidebar dinámico según estado de autenticación
- Redirección a login/register para usuarios no autenticados

---

## 🌳 Árbol de Flujo: Usuario Regular (ROLE_USER)

```
USUARIO REGULAR (ROLE_USER)
│
├─ 🔐 Autenticación
│  ├─ /auth/login → auth-layout ✅
│  ├─ /auth/register → auth-layout ✅
│  └─ /auth/forgot-password → auth-layout ✅
│
├─ 🏠 Inicio
│  └─ / (index.html) → fragmentos directos ⚠️
│
├─ 📋 Mis Solicitudes
│  ├─ /usuarios/inicio → user-layout ✅
│  │  └─ navbar-user + auth-user-footer
│  │
│  ├─ /users/requests → user-layout ✅
│  │  ├─ breadcrumbs automáticos ✅
│  │  └─ Lista de mis solicitudes
│  │
│  └─ /users/request-form → user-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     └─ Formulario nueva solicitud
│
├─ 📰 Contenido Público
│  ├─ /posts → layout (guest) ✅
│  │  └─ navbar-guest + footer condicional
│  │
│  ├─ /posts/{id} → layout (guest) ✅
│  │  └─ post-detail.html
│  │
│  ├─ /posts/category/{slug} → layout (guest) ✅
│  │  └─ category-posts.html
│  │
│  └─ /categories → layout (guest) ✅
│     └─ categories.html
│
├─ 💬 Feedback
│  ├─ /feedback → user-layout ✅
│  │  ├─ Formulario (solo autenticados)
│  │  └─ breadcrumbs automáticos ✅
│  │
│  └─ /feedback/my → user-layout ✅
│     └─ my-feedback.html
│
└─ 👤 Perfil
   └─ /users/profile → user-layout ✅
      └─ Editar información personal
```

---

## 🌳 Árbol de Flujo: Organización (ROLE_ORGANIZATION)

```
ORGANIZACIÓN (ROLE_ORGANIZATION)
│
├─ 🔐 Autenticación
│  ├─ /auth/login → auth-layout ✅
│  ├─ /auth/register → auth-layout ✅
│  └─ /auth/forgot-password → auth-layout ✅
│
├─ 🏢 Dashboard Organización
│  └─ /org/dashboard → org-layout ✅
│     ├─ navbar-org + auth-user-footer
│     └─ Vista de solicitudes asignadas
│
├─ 📋 Gestión de Solicitudes
│  └─ /org/requests → org-layout ✅
│     ├─ Ver solicitudes pendientes
│     ├─ Cambiar estado de solicitudes
│     └─ Programar recolecciones
│
├─ 📰 Contenido Público (mismo que usuarios)
│  ├─ /posts → layout (guest) ✅
│  ├─ /posts/{id} → layout (guest) ✅
│  ├─ /posts/category/{slug} → layout (guest) ✅
│  └─ /categories → layout (guest) ✅
│
├─ 💬 Feedback
│  ├─ /feedback → user-layout ✅
│  └─ /feedback/my → user-layout ✅
│
└─ 👤 Perfil
   └─ /org/profile → org-layout ✅
      └─ Editar información de la organización
```

---

## 🌳 Árbol de Flujo: Administrador (ROLE_ADMIN)

```
ADMINISTRADOR (ROLE_ADMIN)
│
├─ 🔐 Autenticación
│  ├─ /auth/login → auth-layout ✅
│  └─ /auth/forgot-password → auth-layout ✅
│
├─ 🏠 Dashboard Admin
│  └─ /admin/dashboard → admin-layout ✅
│     ├─ navbar-admin + admin-footer
│     ├─ breadcrumbs automáticos ✅
│     └─ Estadísticas y KPIs
│
├─ 👥 Gestión de Usuarios
│  └─ /admin/users → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     ├─ Lista con paginación front-end ✅
│     ├─ Buscador ✅
│     └─ CRUD completo
│
├─ 🏢 Gestión de Organizaciones
│  └─ /admin/organizations → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     ├─ Lista con paginación front-end ✅
│     ├─ Buscador ✅
│     └─ CRUD completo
│
├─ 📦 Gestión de Materiales
│  └─ /admin/materials → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     ├─ Lista con paginación front-end ✅
│     ├─ Buscador ✅
│     └─ CRUD completo
│
├─ 📋 Gestión de Solicitudes
│  └─ /admin/requests → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     ├─ Lista con paginación front-end ✅
│     ├─ Buscador ✅
│     └─ Gestión de estados
│
├─ 📰 Gestión de Contenido
│  ├─ /admin/posts → admin-layout ✅
│  │  ├─ breadcrumbs automáticos ✅
│  │  ├─ Lista con paginación front-end ✅
│  │  ├─ Buscador ✅
│  │  └─ CRUD completo
│  │
│  └─ /admin/categories → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     ├─ Lista con paginación front-end ✅
│     ├─ Buscador ✅
│     └─ CRUD completo
│
├─ 💬 Gestión de Feedback
│  └─ /admin/feedback → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     ├─ Lista con paginación front-end ✅
│     ├─ Buscador ✅
│     └─ Ver y responder feedbacks
│
├─ ⚙️ Configuración
│  ├─ /admin/config → admin-layout ✅
│  │  └─ Configuración de hero image
│  │
│  ├─ /admin/assistant-config → admin-layout ✅
│  │  └─ Configuración de asistente
│  │
│  └─ /admin/system → admin-layout ✅
│     └─ Información del sistema
│
├─ 📊 Estadísticas
│  └─ /admin/statistics → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     └─ Gráficos y métricas
│
├─ 🔑 Gestión de Contraseñas
│  └─ /admin/password-reset-requests → admin-layout ✅
│     ├─ breadcrumbs automáticos ✅
│     └─ Solicitudes de reset
│
└─ 📚 Documentación
   ├─ /admin/documentation → admin-layout ✅
   ├─ /admin/documentation/components → admin-layout ✅
   ├─ /admin/documentation/layouts → admin-layout ✅
   ├─ /admin/documentation/details → admin-layout ✅
   └─ /admin/uml → admin-layout ✅
```

---

## 📊 Resumen de Consistencia por Layout

| Layout | Páginas | Navbar | Footer | Breadcrumbs | Estado |
|--------|---------|--------|--------|-------------|--------|
| **admin-layout** | 19 | navbar-admin | admin-footer | ✅ Auto | ✅ 100% |
| **user-layout** | 6 | navbar-user | auth-user-footer | ✅ Auto | ✅ 100% |
| **org-layout** | 1 | navbar-org | auth-user-footer | ✅ Auto | ✅ 100% |
| **auth-layout** | 3 | Simple | Inline simple | ✅ Manual | ✅ 100% |
| **layout (guest)** | 4 | navbar-guest | Condicional | ✅ Auto | ⚠️ 90% |
| **index.html** | 1 | Fragmentos | Fragmentos | ❌ No | ⚠️ Especial |

---

## ⚠️ Inconsistencias Detectadas

### **1. `layout.html` (guest)**
- **Problema:** Referencia rota a `simple-footer` (línea 184)
- **Impacto:** Error potencial en páginas públicas
- **Solución:** Crear `simple-footer.html` o usar footer existente

### **2. `index.html`**
- **Problema:** No usa layout, usa fragmentos directos
- **Impacto:** Inconsistencia arquitectónica
- **Solución:** Evaluar si migrar a layout o mantener como página especial

### **3. Páginas de Documentación Admin**
- **Estado:** ✅ Todas usan `admin-layout` correctamente
- **Nota:** Una página (`documentation-layouts.html`) anteriormente usaba `base.html` (eliminado)

---

## ✅ Fortalezas de la Arquitectura Actual

1. **Separación clara por roles:**
   - Admin → `admin-layout`
   - Usuario → `user-layout`
   - Organización → `org-layout`
   - Auth → `auth-layout`
   - Público → `layout`

2. **Breadcrumbs automáticos:**
   - Implementados en todas las páginas admin ✅
   - Implementados en páginas de usuario ✅
   - Implementados en páginas públicas ✅

3. **Footers específicos:**
   - Admin: Footer simple con links de ayuda
   - User/Org: Footer completo con enlaces y feedback
   - Auth: Footer minimalista
   - Público: Footer condicional según autenticación

4. **Consistencia en páginas admin:**
   - Todas siguen el patrón estándar documentado
   - Paginación front-end implementada
   - Buscadores consistentes
   - Mensajes flash estandarizados

---

## 🎯 Recomendaciones

### **Prioridad Alta:**
1. ✅ **Arreglar referencia rota en `layout.html`**
   - Crear `fragments/simple-footer.html`
   - O eliminar lógica condicional

### **Prioridad Media:**
2. **Evaluar `index.html`**
   - Decidir si migrar a layout
   - O documentar como página especial

### **Prioridad Baja:**
3. **Documentar flujos en `ARQUITECTURA_GRAN_ESCALA.md`**
4. **Crear tests de integración para flujos críticos**

---

## ✅ Mejoras Aplicadas (2025-10-10 21:35)

### **Migración de Páginas de Feedback:**
- ✅ `public/feedback-form.html` → Migrado de `layout` a `user-layout`
- ✅ `public/my-feedback.html` → Migrado de `layout` a `user-layout`

**Razón:** Las páginas de feedback son para usuarios autenticados, deben usar `user-layout` para consistencia con el resto de páginas de usuario (requests, dashboard, etc.)

**Beneficio:** Ahora todas las páginas de usuario tienen el mismo navbar y footer, mejorando la experiencia de usuario.

**Resultado:** `user-layout` ahora tiene 5 páginas (antes 3):
- `/usuarios/inicio`
- `/users/requests`
- `/users/request-form`
- `/feedback` ✨ (nuevo)
- `/feedback/my` ✨ (nuevo)

---

**Última actualización:** 2025-10-10 21:35
