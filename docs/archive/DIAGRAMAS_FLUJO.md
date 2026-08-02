# 📊 Diagramas de Flujo y Casos de Uso - Sistema de Gestión de Residuos Sólidos

## 🎯 Propósito
Este documento especifica los flujos de usuario, casos de uso y diagramas de proceso del sistema. Sirve como referencia para entender el comportamiento del sistema desde la perspectiva de cada tipo de usuario.

---

## 👥 Actores del Sistema

### **1. Ciudadano (USER)**
Usuario final que genera residuos reciclables y solicita su recolección.

**Objetivos:**
- Solicitar recolección de residuos
- Consultar estado de solicitudes
- Aprender sobre reciclaje

### **2. Centro de Acopio (ORGANIZATION)**
Organización que gestiona la recolección y procesamiento de residuos.

**Objetivos:**
- Gestionar solicitudes de recolección
- Programar rutas de recolección
- Reportar estadísticas

### **3. Administrador (ADMIN)**
Gestor del sistema con acceso completo.

**Objetivos:**
- Gestionar usuarios y organizaciones
- Supervisar operaciones
- Configurar el sistema

---

## 📋 Casos de Uso por Actor

### **👤 Casos de Uso - Ciudadano**

#### **CU-01: Registrarse en el Sistema**
**Actor:** Visitante (no autenticado)
**Precondiciones:** Ninguna
**Flujo Principal:**
1. Usuario accede a `/auth/register`
2. Completa formulario con datos personales
3. Selecciona tipo de usuario (Ciudadano)
4. Sistema valida datos
5. Sistema crea cuenta
6. Sistema envía a página de login

**Flujo Alternativo:**
- 4a. Datos inválidos → Muestra errores
- 4b. Email ya existe → Muestra mensaje de error

**Postcondiciones:** Usuario registrado en el sistema

---

#### **CU-02: Iniciar Sesión**
**Actor:** Ciudadano
**Precondiciones:** Usuario registrado
**Flujo Principal:**
1. Usuario accede a `/auth/login`
2. Ingresa credenciales (email, contraseña)
3. Sistema valida credenciales
4. Sistema redirige a `/usuarios/inicio`

**Flujo Alternativo:**
- 3a. Credenciales incorrectas → Muestra error
- 3b. Usuario inactivo → Muestra mensaje

**Postcondiciones:** Usuario autenticado, sesión activa

---

#### **CU-03: Crear Solicitud de Recolección**
**Actor:** Ciudadano
**Precondiciones:** Usuario autenticado
**Flujo Principal:**
1. Usuario accede a `/usuarios/solicitudes/nueva`
2. Completa formulario:
   - Descripción de residuos
   - Dirección de recolección
   - Materiales a recolectar
   - Fecha preferida (opcional)
3. Sistema valida datos
4. Sistema crea solicitud con estado "PENDING"
5. Sistema notifica al usuario
6. Sistema redirige a `/usuarios/solicitudes`

**Flujo Alternativo:**
- 3a. Datos incompletos → Muestra errores
- 3b. Dirección inválida → Solicita corrección

**Postcondiciones:** Solicitud creada, visible para centros de acopio

**Diagrama de Flujo:**
```
[Inicio] 
   ↓
[Acceder a formulario]
   ↓
[Completar datos]
   ↓
[¿Datos válidos?] --No--> [Mostrar errores] → [Volver a formulario]
   ↓ Sí
[Crear solicitud]
   ↓
[Estado: PENDING]
   ↓
[Notificar usuario]
   ↓
[Redirigir a lista]
   ↓
[Fin]
```

---

#### **CU-04: Consultar Estado de Solicitudes**
**Actor:** Ciudadano
**Precondiciones:** Usuario autenticado
**Flujo Principal:**
1. Usuario accede a `/usuarios/solicitudes`
2. Sistema carga solicitudes del usuario
3. Sistema muestra lista con:
   - Descripción
   - Estado actual
   - Fecha de creación
   - Dirección
   - Materiales
4. Usuario puede filtrar/buscar

**Postcondiciones:** Usuario visualiza sus solicitudes

---

#### **CU-05: Ver Dashboard Personal**
**Actor:** Ciudadano
**Precondiciones:** Usuario autenticado
**Flujo Principal:**
1. Usuario accede a `/usuarios/inicio`
2. Sistema carga estadísticas:
   - Total de solicitudes
   - Solicitudes completadas
   - Tipos de materiales reciclados
   - Solicitudes del mes
3. Sistema muestra solicitudes recientes (últimas 5)
4. Sistema muestra accesos rápidos

**Postcondiciones:** Usuario visualiza su actividad

---

#### **CU-06: Consultar Notas Educativas**
**Actor:** Cualquier usuario (público)
**Precondiciones:** Ninguna
**Flujo Principal:**
1. Usuario accede a `/posts`
2. Sistema carga posts activos
3. Sistema muestra lista de notas
4. Usuario puede:
   - Ver detalle de nota
   - Filtrar por categoría
   - Buscar contenido

**Postcondiciones:** Usuario accede a información educativa

---

### **🏢 Casos de Uso - Centro de Acopio**

#### **CU-07: Ver Solicitudes Pendientes**
**Actor:** Centro de Acopio
**Precondiciones:** Organización autenticada
**Flujo Principal:**
1. Centro accede a `/acopio/inicio`
2. Sistema carga solicitudes con estado "PENDING"
3. Sistema muestra:
   - Cantidad de pendientes
   - Lista de últimas 5 solicitudes
   - Información de contacto
   - Ubicación
4. Centro puede ver detalles

**Postcondiciones:** Centro visualiza trabajo pendiente

**Diagrama de Flujo:**
```
[Inicio]
   ↓
[Cargar dashboard]
   ↓
[Consultar BD: solicitudes PENDING]
   ↓
[¿Hay solicitudes?] --No--> [Mostrar mensaje vacío]
   ↓ Sí                           ↓
[Mostrar lista]                [Fin]
   ↓
[Ordenar por fecha]
   ↓
[Limitar a 5 más recientes]
   ↓
[Renderizar en UI]
   ↓
[Fin]
```

---

#### **CU-08: Aceptar Solicitud**
**Actor:** Centro de Acopio
**Precondiciones:** Solicitud en estado "PENDING"
**Flujo Principal:**
1. Centro visualiza solicitud
2. Centro hace clic en "Aceptar"
3. Sistema cambia estado a "ACCEPTED"
4. Sistema registra fecha de aceptación
5. Sistema notifica al ciudadano
6. Sistema actualiza estadísticas

**Flujo Alternativo:**
- 2a. Centro rechaza → Estado "REJECTED"

**Postcondiciones:** Solicitud aceptada, ciudadano notificado

---

#### **CU-09: Programar Recolección**
**Actor:** Centro de Acopio
**Precondiciones:** Solicitud aceptada
**Flujo Principal:**
1. Centro accede a `/acopio/calendario`
2. Visualiza solicitudes aceptadas
3. Asigna fecha y hora de recolección
4. Sistema cambia estado a "IN_PROGRESS"
5. Sistema notifica al ciudadano
6. Sistema actualiza calendario

**Postcondiciones:** Recolección programada

---

#### **CU-10: Completar Recolección**
**Actor:** Centro de Acopio
**Precondiciones:** Recolección en progreso
**Flujo Principal:**
1. Centro realiza recolección física
2. Centro accede al sistema
3. Marca solicitud como completada
4. Sistema cambia estado a "COMPLETED"
5. Sistema registra:
   - Fecha de completado
   - Peso recolectado (opcional)
   - Observaciones
6. Sistema actualiza estadísticas
7. Sistema notifica al ciudadano

**Postcondiciones:** Recolección completada, estadísticas actualizadas

**Diagrama de Flujo:**
```
[Inicio]
   ↓
[Recolección física realizada]
   ↓
[Acceder a solicitud]
   ↓
[Marcar como completada]
   ↓
[Registrar datos adicionales]
   ↓
[Actualizar estado: COMPLETED]
   ↓
[Actualizar estadísticas]
   ↓
[Notificar ciudadano]
   ↓
[Fin]
```

---

#### **CU-11: Ver Estadísticas del Centro**
**Actor:** Centro de Acopio
**Precondiciones:** Centro autenticado
**Flujo Principal:**
1. Centro accede a `/acopio/estadisticas`
2. Sistema calcula métricas:
   - Solicitudes por estado
   - Total de kg reciclados
   - Materiales más comunes
   - Tendencias temporales
3. Sistema genera gráficos
4. Sistema muestra datos

**Postcondiciones:** Centro visualiza su desempeño

---

### **⚙️ Casos de Uso - Administrador**

#### **CU-12: Gestionar Usuarios**
**Actor:** Administrador
**Precondiciones:** Admin autenticado
**Flujo Principal:**
1. Admin accede a `/admin/users`
2. Sistema muestra lista de usuarios
3. Admin puede:
   - Crear nuevo usuario
   - Editar usuario existente
   - Activar/desactivar usuario
   - Eliminar usuario
   - Buscar/filtrar usuarios
4. Sistema valida operación
5. Sistema ejecuta acción
6. Sistema muestra confirmación

**Postcondiciones:** Usuarios gestionados

---

#### **CU-13: Gestionar Organizaciones**
**Actor:** Administrador
**Precondiciones:** Admin autenticado
**Flujo Principal:**
1. Admin accede a `/admin/organizations`
2. Sistema muestra lista de organizaciones
3. Admin puede:
   - Crear nueva organización
   - Editar organización
   - Asignar materiales aceptados
   - Activar/desactivar
   - Eliminar
4. Sistema valida operación
5. Sistema ejecuta acción

**Postcondiciones:** Organizaciones gestionadas

---

#### **CU-14: Gestionar Materiales**
**Actor:** Administrador
**Precondiciones:** Admin autenticado
**Flujo Principal:**
1. Admin accede a `/admin/materials`
2. Sistema muestra catálogo de materiales
3. Admin puede:
   - Crear nuevo material
   - Editar material
   - Activar/desactivar
   - Asignar a categorías
4. Sistema valida datos
5. Sistema guarda cambios

**Postcondiciones:** Catálogo actualizado

---

#### **CU-15: Gestionar Contenido Educativo**
**Actor:** Administrador
**Precondiciones:** Admin autenticado
**Flujo Principal:**
1. Admin accede a `/admin/posts`
2. Sistema muestra lista de notas
3. Admin puede:
   - Crear nueva nota
   - Editar nota existente
   - Asignar categoría
   - Subir imagen
   - Publicar/despublicar
   - Eliminar
4. Sistema procesa contenido
5. Sistema guarda cambios

**Postcondiciones:** Contenido actualizado

---

## 🔄 Flujos de Proceso Principales

### **Flujo 1: Ciclo de Vida de una Solicitud**

```
[Ciudadano crea solicitud]
         ↓
    [PENDING]
         ↓
[Centro de Acopio revisa] → [Rechaza] → [REJECTED] → [Fin]
         ↓ Acepta
    [ACCEPTED]
         ↓
[Centro programa recolección]
         ↓
  [IN_PROGRESS]
         ↓
[Centro realiza recolección]
         ↓
   [COMPLETED]
         ↓
      [Fin]
```

**Estados Posibles:**
- **PENDING**: Solicitud creada, esperando revisión
- **ACCEPTED**: Centro acepta la solicitud
- **IN_PROGRESS**: Recolección programada/en curso
- **COMPLETED**: Recolección finalizada
- **REJECTED**: Solicitud rechazada por el centro

---

### **Flujo 2: Registro y Autenticación**

```
[Usuario nuevo]
      ↓
[Accede a /auth/register]
      ↓
[Completa formulario]
      ↓
[¿Datos válidos?] --No--> [Mostrar errores]
      ↓ Sí                      ↓
[Crear cuenta]            [Corregir datos]
      ↓                         ↓
[Redirigir a login] <-----------+
      ↓
[Ingresar credenciales]
      ↓
[¿Válidas?] --No--> [Mostrar error]
      ↓ Sí                ↓
[Crear sesión]      [Reintentar]
      ↓
[Redirigir según rol]
      ↓
[Dashboard correspondiente]
```

---

### **Flujo 3: Gestión de Contenido Educativo**

```
[Admin crea/edita nota]
         ↓
[Completa información]
         ↓
[¿Incluye imagen?] --Sí--> [Subir a Cloudinary]
         ↓ No                      ↓
[Asignar categoría] <--------------+
         ↓
[¿Publicar ahora?] --No--> [Guardar como borrador]
         ↓ Sí                      ↓
[Marcar como activo]          [Estado: inactivo]
         ↓                         ↓
[Guardar en BD] <-----------------+
         ↓
[Disponible en /posts]
         ↓
[Ciudadanos pueden ver]
```

---

## 📊 Diagramas de Secuencia

### **Secuencia 1: Crear Solicitud de Recolección**

```
Ciudadano          Sistema          BD          Notificaciones
    |                 |              |                |
    |--GET /usuarios/solicitudes/nueva-->|         |
    |                 |              |                |
    |<--Formulario----|              |                |
    |                 |              |                |
    |--POST (datos)-->|              |                |
    |                 |--Validar---->|                |
    |                 |              |                |
    |                 |--INSERT----->|                |
    |                 |<--ID---------|                |
    |                 |                                |
    |                 |--Enviar notificación---------->|
    |                 |                                |
    |<--Redirect------|                                |
    |                 |                                |
```

---

### **Secuencia 2: Centro Acepta Solicitud**

```
Centro          Sistema          BD          Ciudadano
  |                |              |              |
  |--Ver solicitud->|              |              |
  |                |--Query------>|              |
  |<--Detalles-----|<--Datos------|              |
  |                |              |              |
  |--Aceptar------>|              |              |
  |                |--UPDATE----->|              |
  |                |  (estado)    |              |
  |                |<--OK---------|              |
  |                |                              |
  |                |--Notificar------------------>|
  |<--Confirmación-|                              |
  |                |                              |
```

---

## 🎨 Diagramas de Estado

### **Estados de Solicitud**

```
     ┌─────────┐
     │ PENDING │ (Estado inicial)
     └────┬────┘
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
┌──────────┐  ┌──────────┐
│ ACCEPTED │  │ REJECTED │ (Estado final)
└────┬─────┘  └──────────┘
     │
     ▼
┌──────────────┐
│ IN_PROGRESS  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  COMPLETED   │ (Estado final)
└──────────────┘
```

---

### **Estados de Usuario**

```
┌──────────┐
│  ACTIVE  │ ←→ Usuario puede acceder al sistema
└────┬─────┘
     │
     ↕ (Admin activa/desactiva)
     │
┌────┴─────┐
│ INACTIVE │ ←→ Usuario bloqueado
└──────────┘
```

---

## 🔐 Matriz de Permisos

| Acción | Ciudadano | Centro Acopio | Admin |
|--------|-----------|---------------|-------|
| Ver posts públicos | ✅ | ✅ | ✅ |
| Crear solicitud | ✅ | ❌ | ✅ |
| Ver propias solicitudes | ✅ | ❌ | ✅ |
| Ver todas solicitudes | ❌ | ✅ | ✅ |
| Aceptar/rechazar solicitud | ❌ | ✅ | ✅ |
| Completar recolección | ❌ | ✅ | ✅ |
| Ver estadísticas propias | ✅ | ✅ | ✅ |
| Ver estadísticas globales | ❌ | ❌ | ✅ |
| Gestionar usuarios | ❌ | ❌ | ✅ |
| Gestionar organizaciones | ❌ | ❌ | ✅ |
| Gestionar materiales | ❌ | ❌ | ✅ |
| Gestionar contenido | ❌ | ❌ | ✅ |

---

## 📝 Reglas de Negocio

### **RN-01: Creación de Solicitudes**
- Un ciudadano puede crear múltiples solicitudes
- Cada solicitud debe tener al menos un material
- La dirección de recolección es obligatoria
- El estado inicial siempre es "PENDING"

### **RN-02: Aceptación de Solicitudes**
- Solo centros de acopio pueden aceptar solicitudes
- Una solicitud solo puede ser aceptada por un centro
- No se puede aceptar una solicitud ya aceptada por otro centro
- Al aceptar, se debe programar fecha de recolección

### **RN-03: Materiales**
- Solo materiales activos están disponibles para solicitudes
- Un centro de acopio solo puede aceptar solicitudes con materiales que gestiona
- Los materiales deben estar categorizados

### **RN-04: Usuarios**
- Solo usuarios activos pueden acceder al sistema
- El email debe ser único
- La contraseña debe cumplir requisitos mínimos
- Cada usuario tiene un único rol

### **RN-05: Contenido Educativo**
- Solo posts activos son visibles públicamente
- Cada post debe tener una categoría
- Las imágenes son opcionales pero recomendadas
- El contenido debe ser apropiado y educativo

---

## 🔄 Flujos de Excepción

### **Excepción 1: Solicitud sin Centros Disponibles**
```
[Ciudadano crea solicitud]
         ↓
[Sistema busca centros compatibles]
         ↓
[¿Hay centros?] --No--> [Notificar: "No hay centros disponibles"]
         ↓ Sí                    ↓
[Crear solicitud]         [Guardar como PENDING]
                                 ↓
                          [Notificar admin]
```

### **Excepción 2: Usuario Inactivo Intenta Acceder**
```
[Usuario ingresa credenciales]
         ↓
[Sistema valida]
         ↓
[¿Usuario activo?] --No--> [Mostrar: "Cuenta desactivada"]
         ↓ Sí                      ↓
[Permitir acceso]           [Contactar admin]
```

---

## 📚 Referencias

- [ENDPOINTS.md](./ENDPOINTS.md) - Documentación de rutas
- [ARQUITECTURA_GRAN_ESCALA.md](./ARQUITECTURA_GRAN_ESCALA.md) - Arquitectura del sistema
- [FLUJOS_USUARIO.md](./FLUJOS_USUARIO.md) - Flujos detallados de usuario

---

**Última actualización:** 11/10/2025  
**Versión:** 1.0.0
