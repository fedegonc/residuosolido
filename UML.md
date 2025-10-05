# Diagrama de Casos de Uso - Sistema EcoSolicitud

## Actores del Sistema

### 1. **Usuario (USER)**
- Ciudadano registrado que solicita recolección de residuos
- Puede crear, ver y gestionar sus propias solicitudes

### 2. **Organización (ORGANIZATION)**
- Entidad encargada de recolectar residuos
- Gestiona solicitudes asignadas y calendario de recolección

### 3. **Administrador (ADMIN)**
- Gestor del sistema completo
- Control total sobre usuarios, organizaciones, contenido y configuración

### 4. **Invitado (GUEST)**
- Usuario no autenticado
- Acceso limitado a contenido público

---

## Casos de Uso por Actor

### 👤 USUARIO (USER)

#### **CU-U01: Gestionar Solicitudes de Recolección** ⭐ (Detallado)
- **Descripción:** Crear, ver, editar y cancelar solicitudes de recolección
- **Incluye:**
  - CU-U01.1: Crear Nueva Solicitud
  - CU-U01.2: Ver Mis Solicitudes
  - CU-U01.3: Ver Detalle de Solicitud
  - CU-U01.4: Cancelar Solicitud (solo si estado = PENDIENTE)

##### **CU-U01.1: Crear Nueva Solicitud** (Flujo Detallado)
1. Usuario accede a "Nueva Solicitud"
2. Sistema muestra formulario con:
   - Selección de materiales (múltiple)
   - Dirección de recolección
   - Fecha preferida
   - Notas adicionales
   - Carga de imágenes (opcional)
3. Usuario completa formulario
4. Sistema valida datos
5. Sistema crea solicitud con estado PENDIENTE
6. Sistema notifica al usuario
7. Sistema asigna solicitud a organizaciones disponibles

**Flujo Alternativo:**
- 4a. Datos inválidos → Sistema muestra errores
- 7a. No hay organizaciones → Solicitud queda en cola

##### **CU-U01.2: Ver Mis Solicitudes**
1. Usuario accede a "Mis Solicitudes"
2. Sistema muestra lista de solicitudes con:
   - Estado (PENDIENTE, ACEPTADA, EN_PROCESO, COMPLETADA, CANCELADA)
   - Fecha de creación
   - Materiales
   - Organización asignada (si existe)
3. Usuario puede filtrar por estado
4. Usuario puede ver detalles (→ CU-U01.3)

#### **CU-U02: Gestionar Perfil**
- **Incluye:**
  - CU-U02.1: Ver Perfil
  - CU-U02.2: Editar Información Personal
  - CU-U02.3: Cambiar Contraseña

#### **CU-U03: Ver Información Educativa**
- Acceder a posts sobre reciclaje
- Ver categorías de residuos
- Consultar guías

#### **CU-U04: Enviar Feedback**
- Reportar problemas
- Sugerencias
- Quejas

#### **CU-U05: Usar Chat de Ayuda**
- Consultar FAQs
- Asistente virtual

---

### 🏢 ORGANIZACIÓN (ORGANIZATION)

#### **CU-O01: Gestionar Solicitudes Asignadas** ⭐ (Detallado)
- **Descripción:** Ver, aceptar y gestionar solicitudes de recolección
- **Incluye:**
  - CU-O01.1: Ver Solicitudes Disponibles
  - CU-O01.2: Aceptar Solicitud
  - CU-O01.3: Cambiar Estado de Solicitud
  - CU-O01.4: Completar Recolección

##### **CU-O01.1: Ver Solicitudes Disponibles**
1. Organización accede a "Solicitudes"
2. Sistema muestra solicitudes filtradas por:
   - Estado (PENDIENTE, ACEPTADA, EN_PROCESO)
   - Zona geográfica
   - Tipo de material
3. Organización puede ver detalles completos

##### **CU-O01.2: Aceptar Solicitud** (Flujo Detallado)
1. Organización selecciona solicitud PENDIENTE
2. Sistema muestra detalles completos:
   - Dirección exacta
   - Materiales
   - Imágenes
   - Fecha preferida
3. Organización confirma aceptación
4. Sistema cambia estado a ACEPTADA
5. Sistema notifica al usuario
6. Sistema registra organización asignada

**Flujo Alternativo:**
- 3a. Organización rechaza → Solicitud vuelve a disponibles

##### **CU-O01.3: Cambiar Estado de Solicitud**
- ACEPTADA → EN_PROCESO (al iniciar recolección)
- EN_PROCESO → COMPLETADA (al finalizar)

##### **CU-O01.4: Completar Recolección**
1. Organización marca solicitud como COMPLETADA
2. Sistema solicita confirmación
3. Sistema registra fecha de completado
4. Sistema notifica al usuario
5. Sistema actualiza estadísticas

#### **CU-O02: Gestionar Calendario de Recolección**
- Ver solicitudes programadas
- Planificar rutas
- Gestionar disponibilidad

#### **CU-O03: Ver Dashboard Organizacional**
- Estadísticas de recolección
- Solicitudes activas
- Historial

#### **CU-O04: Gestionar Perfil de Organización**
- Editar información
- Actualizar zonas de cobertura
- Gestionar contacto

---

### 👨‍💼 ADMINISTRADOR (ADMIN)

#### **CU-A01: Gestionar Usuarios**
- Crear, editar, eliminar usuarios
- Activar/desactivar cuentas
- Asignar roles
- Ver actividad

#### **CU-A02: Gestionar Organizaciones**
- Aprobar/rechazar organizaciones
- Editar información
- Activar/desactivar
- Asignar zonas

#### **CU-A03: Gestionar Solicitudes (Global)**
- Ver todas las solicitudes
- Reasignar solicitudes
- Resolver conflictos
- Estadísticas globales

#### **CU-A04: Gestionar Contenido**
- **Incluye:**
  - CU-A04.1: Gestionar Posts
  - CU-A04.2: Gestionar Categorías
  - CU-A04.3: Gestionar Materiales

#### **CU-A05: Gestionar Feedback**
- Ver feedback de usuarios
- Responder
- Marcar como resuelto
- Estadísticas

#### **CU-A06: Configurar Sistema**
- Parámetros generales
- Imagen hero
- Textos del sistema
- Configuración de notificaciones

#### **CU-A07: Ver Dashboard Administrativo**
- Estadísticas completas
- Gráficos
- Reportes
- Actividad del sistema

---

### 🌐 INVITADO (GUEST)

#### **CU-G01: Ver Contenido Público**
- Ver posts educativos
- Ver categorías
- Ver organizaciones registradas

#### **CU-G02: Registrarse**
- Crear cuenta de usuario
- Validar email
- Completar perfil

#### **CU-G03: Ver Información del Sistema**
- Página principal
- Sobre nosotros
- Contacto

---

## Casos de Uso Compartidos (Todos los Actores Autenticados)

### **CU-S01: Autenticación** ⭐ (Detallado)

#### **CU-S01.1: Login** (Flujo Detallado)
1. Usuario accede a página de login
2. Sistema muestra formulario (username/email + password)
3. Usuario ingresa credenciales
4. Sistema valida credenciales
5. Sistema verifica estado de cuenta (activa)
6. Sistema crea sesión
7. Sistema redirige según rol:
   - USER → `/user/dashboard`
   - ORGANIZATION → `/organization/dashboard`
   - ADMIN → `/admin`

**Flujos Alternativos:**
- 4a. Credenciales inválidas → Mensaje de error
- 5a. Cuenta inactiva → Mensaje "Cuenta desactivada"
- 4b. Usuario bloqueado (3 intentos) → Bloqueo temporal

**Extensiones:**
- <<include>> CU-S01.3: Recordar Contraseña

#### **CU-S01.2: Logout** (Flujo Detallado)
1. Usuario autenticado hace clic en "Cerrar sesión"
2. Sistema solicita confirmación (opcional)
3. Usuario confirma
4. Sistema invalida sesión
5. Sistema limpia cookies/tokens
6. Sistema redirige a página principal

**Flujo Alternativo:**
- 2a. Usuario cancela → Permanece en sesión

#### **CU-S01.3: Recuperar Contraseña**
1. Usuario hace clic en "Olvidé mi contraseña"
2. Sistema solicita email
3. Usuario ingresa email
4. Sistema valida email existe
5. Sistema genera token temporal
6. Sistema envía email con link
7. Usuario hace clic en link
8. Sistema valida token (vigencia 24h)
9. Sistema muestra formulario nueva contraseña
10. Usuario ingresa nueva contraseña
11. Sistema valida requisitos
12. Sistema actualiza contraseña
13. Sistema invalida token
14. Sistema notifica cambio exitoso

**Flujos Alternativos:**
- 4a. Email no existe → Mensaje genérico (seguridad)
- 8a. Token expirado → Solicitar nuevo
- 11a. Contraseña débil → Mostrar requisitos

### **CU-S02: Cambiar Idioma**
- Seleccionar Español/Portugués
- Sistema actualiza interfaz
- Guarda preferencia

---

## Relaciones entre Casos de Uso

### **<<include>>** (Obligatorio)

1. **CU-U01.1: Crear Nueva Solicitud**
   - <<include>> CU-S03: Validar Sesión Activa
   - <<include>> CU-S04: Subir Imágenes (Cloudinary)

2. **CU-O01.2: Aceptar Solicitud**
   - <<include>> CU-S03: Validar Sesión Activa
   - <<include>> CU-S05: Enviar Notificación

3. **CU-S01.1: Login**
   - <<include>> CU-S06: Validar CSRF Token

4. **Todos los CU de modificación**
   - <<include>> CU-S03: Validar Sesión Activa
   - <<include>> CU-S06: Validar CSRF Token

### **<<extend>>** (Opcional)

1. **CU-U01.1: Crear Nueva Solicitud**
   - <<extend>> CU-U01.5: Programar Fecha Específica
   - <<extend>> CU-U01.6: Agregar Notas Especiales

2. **CU-O01.2: Aceptar Solicitud**
   - <<extend>> CU-O01.5: Sugerir Fecha Alternativa
   - <<extend>> CU-O01.6: Solicitar Información Adicional

3. **CU-S01.1: Login**
   - <<extend>> CU-S01.4: Login con Google (futuro)
   - <<extend>> CU-S01.5: Autenticación 2FA (futuro)

### **Generalización**

1. **CU-Base: Gestionar Entidad**
   - CU-A01: Gestionar Usuarios
   - CU-A02: Gestionar Organizaciones
   - CU-A04.1: Gestionar Posts
   - CU-A04.2: Gestionar Categorías
   - CU-A04.3: Gestionar Materiales

---

## Casos de Uso del Sistema (Internos)

### **CU-S03: Validar Sesión Activa**
- Verificar token de sesión
- Validar expiración
- Verificar permisos de rol

### **CU-S04: Subir Imágenes**
- Validar formato (jpg, png, webp)
- Validar tamaño (max 5MB)
- Subir a Cloudinary
- Retornar URL

### **CU-S05: Enviar Notificación**
- Crear notificación en BD
- Enviar email (opcional)
- Registrar en log

### **CU-S06: Validar CSRF Token**
- Verificar token en request
- Validar contra sesión
- Prevenir ataques CSRF

---

## Flujo Principal del Sistema (Ciclo Completo)

### **Flujo: Solicitud de Recolección Completa** ⭐

```
1. [USER] Crea solicitud (CU-U01.1)
   ↓
2. [SISTEMA] Valida y guarda con estado PENDIENTE
   ↓
3. [SISTEMA] Notifica a organizaciones disponibles
   ↓
4. [ORGANIZATION] Ve solicitud disponible (CU-O01.1)
   ↓
5. [ORGANIZATION] Acepta solicitud (CU-O01.2)
   ↓
6. [SISTEMA] Cambia estado a ACEPTADA
   ↓
7. [SISTEMA] Notifica a USER
   ↓
8. [ORGANIZATION] Inicia recolección → estado EN_PROCESO (CU-O01.3)
   ↓
9. [ORGANIZATION] Completa recolección → estado COMPLETADA (CU-O01.4)
   ↓
10. [SISTEMA] Notifica a USER
    ↓
11. [USER] Ve solicitud completada (CU-U01.2)
    ↓
12. [USER] (Opcional) Envía feedback (CU-U04)
```

**Flujos Alternativos:**
- Paso 5a: Ninguna organización acepta → ADMIN reasigna (CU-A03)
- Paso 8a: USER cancela → estado CANCELADA (CU-U01.4)
- Paso 9a: Problema en recolección → ORGANIZATION contacta USER

---

## Reglas de Negocio

### **RN-01: Estados de Solicitud**
- PENDIENTE → Solo puede ir a ACEPTADA o CANCELADA
- ACEPTADA → Solo puede ir a EN_PROCESO o CANCELADA
- EN_PROCESO → Solo puede ir a COMPLETADA
- COMPLETADA/CANCELADA → Estados finales (no cambian)

### **RN-02: Permisos de Cancelación**
- USER puede cancelar solo si estado = PENDIENTE o ACEPTADA
- ORGANIZATION puede rechazar solo si estado = PENDIENTE
- ADMIN puede cancelar en cualquier estado

### **RN-03: Asignación de Solicitudes**
- Una solicitud solo puede tener una organización asignada
- Organización debe estar activa
- Organización debe cubrir la zona geográfica

### **RN-04: Roles y Permisos**
- USER: Solo ve y gestiona sus propias solicitudes
- ORGANIZATION: Ve solicitudes de su zona o asignadas
- ADMIN: Ve y gestiona todas las solicitudes

### **RN-05: Validaciones de Solicitud**
- Mínimo 1 material seleccionado
- Dirección obligatoria
- Fecha preferida debe ser futura
- Máximo 5 imágenes por solicitud

---

## Prioridades de Implementación

### **Fase 1 - Core (Implementado)**
- ✅ CU-S01: Autenticación (Login/Logout)
- ✅ CU-G02: Registro
- ✅ CU-U01: Gestionar Solicitudes
- ✅ CU-O01: Gestionar Solicitudes Asignadas
- ✅ CU-A01: Gestionar Usuarios
- ✅ CU-A04: Gestionar Contenido

### **Fase 2 - Mejoras (En Progreso)**
- 🔄 CU-U04: Feedback
- 🔄 CU-U05: Chat de Ayuda
- 🔄 CU-O02: Calendario
- 🔄 CU-S05: Notificaciones

### **Fase 3 - Futuro**
- ⏳ CU-S01.4: Login Social
- ⏳ CU-S01.5: 2FA
- ⏳ CU-A07: Dashboard Avanzado
- ⏳ Reportes y Analytics

---

## Notas Técnicas

### **Seguridad**
- Todos los CU autenticados requieren validación de sesión
- CSRF protection en formularios
- Validación de permisos por rol
- Sanitización de inputs

### **Tecnologías**
- Spring Security para autenticación
- Thymeleaf para vistas
- Cloudinary para imágenes
- PostgreSQL para persistencia
- Gemini AI para chat de ayuda

### **Integraciones**
- Cloudinary API (subida de imágenes)
- Email service (notificaciones)
- Gemini API (asistente virtual)

---

# Diagrama Entidad-Relación (ER) - Modelo Conceptual

## Entidades Principales

### **1. Usuario (User)**
**Descripción:** Representa a cualquier persona registrada en el sistema

**Atributos:**
- id (PK)
- username (único)
- email (único)
- password (hash)
- firstName
- lastName
- phone
- address
- role (ADMIN, USER, ORGANIZATION)
- active (boolean)
- createdAt
- updatedAt

**Relaciones:**
- 1:N con Solicitud (un usuario puede crear muchas solicitudes)
- 1:N con Feedback (un usuario puede enviar múltiples feedbacks)
- 1:1 con Organización (si role = ORGANIZATION)

---

### **2. Organización (Organization)**
**Descripción:** Entidad que realiza recolección de residuos

**Atributos:**
- id (PK)
- userId (FK → User) (único)
- name
- description
- contactEmail
- contactPhone
- address
- coverageArea (zona geográfica)
- active (boolean)
- createdAt
- updatedAt

**Relaciones:**
- 1:1 con Usuario (una organización está vinculada a un usuario)
- 1:N con Solicitud (una organización puede aceptar muchas solicitudes)

---

### **3. Solicitud (Request)**
**Descripción:** Pedido de recolección de residuos

**Atributos:**
- id (PK)
- userId (FK → User)
- organizationId (FK → Organization, nullable)
- address
- preferredDate
- notes
- status (PENDIENTE, ACEPTADA, EN_PROCESO, COMPLETADA, CANCELADA)
- createdAt
- updatedAt
- acceptedAt
- completedAt
- cancelledAt

**Relaciones:**
- N:1 con Usuario (muchas solicitudes pertenecen a un usuario)
- N:1 con Organización (muchas solicitudes pueden ser asignadas a una organización)
- N:M con Material (una solicitud puede tener múltiples materiales)
- 1:N con Imagen (una solicitud puede tener múltiples imágenes)

---

### **4. Material**
**Descripción:** Tipo de residuo reciclable

**Atributos:**
- id (PK)
- name
- description
- category (RECICLABLE, NO_RECICLABLE, PELIGROSO, ORGÁNICO)
- imageUrl
- active (boolean)
- createdAt
- updatedAt

**Relaciones:**
- N:M con Solicitud (un material puede estar en muchas solicitudes)

---

### **5. SolicitudMaterial (Request_Material)** [Tabla Intermedia]
**Descripción:** Relación muchos a muchos entre Solicitud y Material

**Atributos:**
- requestId (FK → Request, PK)
- materialId (FK → Material, PK)
- quantity (opcional)
- estimatedWeight (opcional)

**Relaciones:**
- N:1 con Solicitud
- N:1 con Material

---

### **6. Imagen (Image)**
**Descripción:** Fotografías de los residuos

**Atributos:**
- id (PK)
- requestId (FK → Request)
- url (Cloudinary URL)
- publicId (Cloudinary ID)
- uploadedAt

**Relaciones:**
- N:1 con Solicitud (muchas imágenes pertenecen a una solicitud)

---

### **7. Post**
**Descripción:** Contenido educativo sobre reciclaje

**Atributos:**
- id (PK)
- categoryId (FK → Category)
- title
- content
- imageUrl
- sourceUrl
- sourceName
- active (boolean)
- createdAt
- updatedAt

**Relaciones:**
- N:1 con Categoría (muchos posts pertenecen a una categoría)

---

### **8. Categoría (Category)**
**Descripción:** Clasificación de posts educativos

**Atributos:**
- id (PK)
- name
- description
- iconClass
- displayOrder
- active (boolean)
- createdAt
- updatedAt

**Relaciones:**
- 1:N con Post (una categoría puede tener muchos posts)

---

### **9. Feedback**
**Descripción:** Comentarios y sugerencias de usuarios

**Atributos:**
- id (PK)
- userId (FK → User)
- name
- email
- comment
- adminResponse (nullable)
- isRead (boolean)
- createdAt
- respondedAt

**Relaciones:**
- N:1 con Usuario (muchos feedbacks pertenecen a un usuario)

---

### **10. Notificación (Notification)** [Conceptual - No implementada]
**Descripción:** Alertas y avisos para usuarios

**Atributos:**
- id (PK)
- userId (FK → User)
- type (SOLICITUD_ACEPTADA, SOLICITUD_COMPLETADA, etc.)
- title
- message
- isRead (boolean)
- createdAt

**Relaciones:**
- N:1 con Usuario

---

### **11. ConfiguraciónSistema (SystemConfig)** [Conceptual]
**Descripción:** Parámetros globales del sistema

**Atributos:**
- id (PK)
- key (único)
- value
- description
- updatedAt

---

## Diagrama ER en Texto

```
┌─────────────┐
│   Usuario   │
│  (User)     │
├─────────────┤
│ id (PK)     │
│ username    │
│ email       │
│ password    │
│ role        │
│ active      │
└──────┬──────┘
       │
       │ 1:1 (si role=ORG)
       │
       ├──────────────────────────┐
       │                          │
       │ 1:N (crea)               │ 1:N (envía)
       ↓                          ↓
┌──────────────┐           ┌──────────────┐
│ Organización │           │   Feedback   │
│(Organization)│           ├──────────────┤
├──────────────┤           │ id (PK)      │
│ id (PK)      │           │ userId (FK)  │
│ userId (FK)  │           │ comment      │
│ name         │           │ isRead       │
│ coverageArea │           └──────────────┘
└──────┬───────┘
       │
       │ 1:N (acepta)
       ↓
┌──────────────┐
│  Solicitud   │
│  (Request)   │
├──────────────┤
│ id (PK)      │
│ userId (FK)  │◄──────── N:1 (pertenece)
│ orgId (FK)   │
│ status       │
│ address      │
└──────┬───────┘
       │
       │ 1:N
       ↓
┌──────────────┐
│    Imagen    │
│   (Image)    │
├──────────────┤
│ id (PK)      │
│ requestId(FK)│
│ url          │
└──────────────┘

┌──────────────┐         ┌────────────────┐         ┌──────────────┐
│  Solicitud   │◄───N:M──┤SolicitudMaterial├───N:M──►│   Material   │
│  (Request)   │         │(Request_Material)│         ├──────────────┤
└──────────────┘         ├────────────────┤         │ id (PK)      │
                         │ requestId (FK) │         │ name         │
                         │ materialId(FK) │         │ category     │
                         │ quantity       │         │ active       │
                         └────────────────┘         └──────────────┘

┌──────────────┐         ┌──────────────┐
│  Categoría   │◄───1:N──┤     Post     │
│  (Category)  │         ├──────────────┤
├──────────────┤         │ id (PK)      │
│ id (PK)      │         │ categoryId(FK)│
│ name         │         │ title        │
│ displayOrder │         │ content      │
│ active       │         │ active       │
└──────────────┘         └──────────────┘
```

---

## Cardinalidades y Restricciones

### **Relaciones 1:1**
- Usuario ↔ Organización (un usuario puede ser una organización)
  - Restricción: Solo si `User.role = ORGANIZATION`

### **Relaciones 1:N**
- Usuario → Solicitud (un usuario crea muchas solicitudes)
- Usuario → Feedback (un usuario envía múltiples feedbacks)
- Organización → Solicitud (una organización acepta muchas solicitudes)
- Solicitud → Imagen (una solicitud tiene múltiples imágenes)
- Categoría → Post (una categoría agrupa múltiples posts)

### **Relaciones N:M**
- Solicitud ↔ Material (a través de SolicitudMaterial)
  - Una solicitud puede tener múltiples materiales
  - Un material puede estar en múltiples solicitudes

### **Restricciones de Integridad**
1. `Request.organizationId` puede ser NULL (solicitud pendiente)
2. `Request.status` solo puede tener valores del enum definido
3. `User.username` y `User.email` deben ser únicos
4. `Organization.userId` debe ser único (1:1)
5. Eliminación en cascada:
   - Usuario eliminado → Solicitudes marcadas como huérfanas (no eliminar)
   - Solicitud eliminada → Imágenes eliminadas
   - Categoría eliminada → Posts quedan sin categoría (nullable)

---

# Diagrama de Clases - Alto Nivel

## Paquetes Principales

### **1. com.residuosolido.app.model** (Entidades de Dominio)

```
┌─────────────────────────────────────────┐
│            <<abstract>>                 │
│           BaseEntity                    │
├─────────────────────────────────────────┤
│ - id: Long                              │
│ - createdAt: LocalDateTime              │
│ - updatedAt: LocalDateTime              │
├─────────────────────────────────────────┤
│ + getId(): Long                         │
│ + getCreatedAt(): LocalDateTime         │
└─────────────────────────────────────────┘
                    △
                    │ (herencia)
        ┌───────────┼───────────┬──────────────┐
        │           │           │              │
┌───────▼──────┐ ┌──▼────────┐ ┌▼──────────┐ ┌▼──────────┐
│    User      │ │  Request  │ │ Material  │ │   Post    │
├──────────────┤ ├───────────┤ ├───────────┤ ├───────────┤
│ - username   │ │ - address │ │ - name    │ │ - title   │
│ - email      │ │ - status  │ │ - category│ │ - content │
│ - password   │ │ - notes   │ │ - active  │ │ - active  │
│ - role       │ │ - user    │ └───────────┘ └───────────┘
│ - active     │ │ - org     │
├──────────────┤ │ - materials│
│ + login()    │ │ - images  │
│ + logout()   │ ├───────────┤
│ + hasRole()  │ │ + accept()│
└──────────────┘ │ + complete│
                 │ + cancel()│
                 └───────────┘
```

---

### **2. com.residuosolido.app.repository** (Acceso a Datos)

```
┌─────────────────────────────────────────┐
│      <<interface>>                      │
│   JpaRepository<T, ID>                  │
│      (Spring Data JPA)                  │
└─────────────────────────────────────────┘
                    △
                    │ (extends)
        ┌───────────┼───────────┬──────────────┐
        │           │           │              │
┌───────▼──────────┐ ┌─────▼────────┐ ┌──────▼────────┐
│ UserRepository   │ │RequestRepo   │ │MaterialRepo   │
├──────────────────┤ ├──────────────┤ ├───────────────┤
│ + findByEmail()  │ │ + findByUser │ │ + findActive()│
│ + findByRole()   │ │ + findByOrg  │ │ + findByName()│
│ + existsByEmail()│ │ + findByStatus│ └───────────────┘
└──────────────────┘ └──────────────┘
```

---

### **3. com.residuosolido.app.service** (Lógica de Negocio)

```
┌─────────────────────────────────────────┐
│         UserService                     │
├─────────────────────────────────────────┤
│ - userRepository: UserRepository        │
│ - passwordEncoder: PasswordEncoder      │
├─────────────────────────────────────────┤
│ + register(userData): User              │
│ + authenticate(credentials): User       │
│ + updateProfile(userId, data): User     │
│ + changePassword(userId, pwd): void     │
│ + deactivate(userId): void              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│       RequestService                    │
├─────────────────────────────────────────┤
│ - requestRepository: RequestRepository  │
│ - userService: UserService              │
│ - notificationService: NotificationSvc  │
├─────────────────────────────────────────┤
│ + createRequest(data): Request          │
│ + getUserRequests(userId): List<Req>    │
│ + acceptRequest(reqId, orgId): Request  │
│ + updateStatus(reqId, status): Request  │
│ + cancelRequest(reqId): void            │
│ + assignOrganization(reqId, orgId): Req │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│    OrganizationService                  │
├─────────────────────────────────────────┤
│ - orgRepository: OrganizationRepository │
│ - requestService: RequestService        │
├─────────────────────────────────────────┤
│ + getAvailableRequests(orgId): List<Req>│
│ + acceptRequest(orgId, reqId): Request  │
│ + getAssignedRequests(orgId): List<Req> │
│ + updateProfile(orgId, data): Org       │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│      MaterialService                    │
├─────────────────────────────────────────┤
│ - materialRepository: MaterialRepository│
├─────────────────────────────────────────┤
│ + getAllActive(): List<Material>        │
│ + create(data): Material                │
│ + update(id, data): Material            │
│ + toggleActive(id): Material            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│      NotificationService                │
├─────────────────────────────────────────┤
│ - emailService: EmailService            │
├─────────────────────────────────────────┤
│ + notifyRequestAccepted(request): void  │
│ + notifyRequestCompleted(request): void │
│ + sendEmail(to, subject, body): void    │
└─────────────────────────────────────────┘
```

---

### **4. com.residuosolido.app.controller** (Capa de Presentación)

```
┌─────────────────────────────────────────┐
│      AuthController                     │
├─────────────────────────────────────────┤
│ - userService: UserService              │
├─────────────────────────────────────────┤
│ + showLogin(): String                   │
│ + showRegister(): String                │
│ + register(form): String                │
│ + logout(): String                      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│      UserController                     │
├─────────────────────────────────────────┤
│ - requestService: RequestService        │
│ - materialService: MaterialService      │
├─────────────────────────────────────────┤
│ + showDashboard(): String               │
│ + showNewRequest(): String              │
│ + createRequest(form): String           │
│ + showMyRequests(): String              │
│ + cancelRequest(id): String             │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│    OrganizationController               │
├─────────────────────────────────────────┤
│ - orgService: OrganizationService       │
│ - requestService: RequestService        │
├─────────────────────────────────────────┤
│ + showDashboard(): String               │
│ + showAvailableRequests(): String       │
│ + acceptRequest(id): String             │
│ + updateRequestStatus(id, status): Str  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│       AdminController                   │
├─────────────────────────────────────────┤
│ - userService: UserService              │
│ - requestService: RequestService        │
│ - materialService: MaterialService      │
├─────────────────────────────────────────┤
│ + showDashboard(): String               │
│ + manageUsers(): String                 │
│ + manageRequests(): String              │
│ + manageMaterials(): String             │
│ + manageOrganizations(): String         │
└─────────────────────────────────────────┘
```

---

### **5. com.residuosolido.app.security** (Seguridad)

```
┌─────────────────────────────────────────┐
│      SecurityConfig                     │
├─────────────────────────────────────────┤
│ - userDetailsService: UserDetailsService│
│ - passwordEncoder: PasswordEncoder      │
├─────────────────────────────────────────┤
│ + securityFilterChain(): SecurityChain  │
│ + passwordEncoder(): PasswordEncoder    │
│ + configure(http): void                 │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│   CustomUserDetailsService              │
├─────────────────────────────────────────┤
│ - userRepository: UserRepository        │
├─────────────────────────────────────────┤
│ + loadUserByUsername(user): UserDetails │
└─────────────────────────────────────────┘
```

---

### **6. com.residuosolido.app.util** (Utilidades)

```
┌─────────────────────────────────────────┐
│      CloudinaryService                  │
├─────────────────────────────────────────┤
│ - cloudinary: Cloudinary                │
├─────────────────────────────────────────┤
│ + uploadImage(file): String             │
│ + deleteImage(publicId): void           │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│      ValidationUtil                     │
├─────────────────────────────────────────┤
│ + validateEmail(email): boolean         │
│ + validatePassword(pwd): boolean        │
│ + sanitizeInput(input): String          │
└─────────────────────────────────────────┘
```

---

## Relaciones entre Clases (Principales)

### **Dependencias**
```
Controller → Service → Repository → Entity
```

### **Composición**
```
Request ◆──→ Material (muchos)
Request ◆──→ Image (muchos)
User ◆──→ Request (muchos)
Organization ◆──→ Request (muchos)
```

### **Agregación**
```
Category ◇──→ Post (muchos)
User ◇──→ Feedback (muchos)
```

---

## Patrones de Diseño Aplicados

### **1. Repository Pattern**
- Abstrae el acceso a datos
- `UserRepository`, `RequestRepository`, etc.

### **2. Service Layer Pattern**
- Lógica de negocio centralizada
- `UserService`, `RequestService`, etc.

### **3. MVC (Model-View-Controller)**
- **Model:** Entidades JPA
- **View:** Templates Thymeleaf
- **Controller:** Controllers Spring

### **4. Dependency Injection**
- Spring gestiona todas las dependencias
- `@Autowired`, `@Service`, `@Repository`

### **5. DTO Pattern** (Conceptual)
- Transferencia de datos entre capas
- Evita exponer entidades directamente

### **6. Builder Pattern** (Lombok)
- Construcción de objetos complejos
- `@Builder` en entidades

### **7. Strategy Pattern** (Roles)
- Diferentes comportamientos según rol
- USER, ORGANIZATION, ADMIN

---

## Principios SOLID Aplicados

### **S - Single Responsibility**
- Cada servicio tiene una responsabilidad única
- `UserService` solo gestiona usuarios

### **O - Open/Closed**
- Extensible mediante herencia (`BaseEntity`)
- Cerrado para modificación

### **L - Liskov Substitution**
- Todas las entidades pueden sustituir a `BaseEntity`

### **I - Interface Segregation**
- Repositorios específicos por entidad
- No interfaces monolíticas

### **D - Dependency Inversion**
- Controllers dependen de abstracciones (Services)
- No de implementaciones concretas

---

## Diagrama de Secuencia (Ejemplo: Crear Solicitud)

```
Usuario → UserController → RequestService → RequestRepository → DB
   │            │                │                 │             │
   │──create()──→│                │                 │             │
   │            │──validate()────→│                 │             │
   │            │                │──save()─────────→│             │
   │            │                │                 │──INSERT────→│
   │            │                │                 │←────OK──────│
   │            │                │←────Request─────│             │
   │            │──notify()──────→NotificationSvc  │             │
   │            │←────view───────│                 │             │
   │←───redirect│                │                 │             │
```

---

## Notas de Arquitectura

### **Capas de la Aplicación**
1. **Presentación:** Controllers + Thymeleaf
2. **Negocio:** Services
3. **Persistencia:** Repositories + JPA
4. **Dominio:** Entities

### **Flujo de Datos**
```
HTTP Request → Controller → Service → Repository → Database
                    ↓
              Thymeleaf View ← Model
```

### **Transaccionalidad**
- `@Transactional` en métodos de servicio
- Rollback automático en excepciones

### **Validación**
- `@Valid` en controllers
- Bean Validation en entidades
- Validación custom en services
