# Diagrama de Clases - Sistema ResiduoSólido

## 📋 Descripción General

Este diagrama de clases representa la estructura del sistema de gestión de residuos sólidos reciclables, mostrando las entidades principales, sus atributos, métodos y relaciones.

## 🎯 Clases Principales

### 1. **User** (Usuario)
Clase central del sistema que representa tres tipos de usuarios mediante el enum `Role`:
- **ADMIN**: Administrador del sistema
- **USER**: Usuario regular (ciudadano que solicita recolección)
- **ORGANIZATION**: Organización de reciclaje

**Atributos principales:**
- Información básica: `username`, `email`, `password`, `role`
- Perfil: `firstName`, `lastName`, `phone`, `profileImage`
- Ubicación: `address`, `latitude`, `longitude`, `addressReferences`
- Estado: `active`, `profileCompleted`, `createdAt`, `lastAccessAt`

**Métodos clave:**
- `getFullName()`: Retorna nombre completo del usuario
- `addRequest()`, `removeRequest()`: Gestión de solicitudes
- `addFeedback()`, `removeFeedback()`: Gestión de retroalimentación

### 2. **Request** (Solicitud de Recolección)
Representa una solicitud de recolección de residuos creada por un usuario.

**Atributos principales:**
- `description`: Descripción de los residuos
- `collectionAddress`: Dirección de recolección
- `scheduledDate`: Fecha programada
- `status`: Estado actual (enum `RequestStatus`)
- `notes`: Notas adicionales
- `imageUrl`: Imagen de los residuos

**Ciclo de vida (RequestStatus):**
```
PENDING → ACCEPTED → IN_PROGRESS → COMPLETED
                  ↘ REJECTED
```

**Relaciones:**
- Pertenece a un `User` (creador)
- Puede ser asignada a una `Organization` (User con role ORGANIZATION)
- Contiene múltiples `Material` (relación N:M)

### 3. **Material** (Material Reciclable)
Representa tipos de materiales reciclables disponibles en el sistema.

**Atributos:**
- `name`: Nombre del material (ej: "Plástico PET", "Vidrio")
- `description`: Descripción detallada
- `category`: Categoría del material
- `active`: Estado activo/inactivo

**Relaciones:**
- Asociado a múltiples `User` (organizaciones que lo aceptan) - N:M
- Asociado a múltiples `Request` (solicitudes que lo incluyen) - N:M

### 4. **Post** (Contenido Educativo)
Artículos informativos sobre reciclaje y medio ambiente.

**Atributos:**
- `title`, `content`: Título y contenido del post
- `imageUrl`: Imagen destacada
- `sourceUrl`, `sourceName`: Fuente de información
- `active`: Estado de publicación
- `createdAt`, `updatedAt`: Timestamps

**Relaciones:**
- Pertenece a una `Category` (N:1)

### 5. **Category** (Categoría)
Categorías para organizar posts educativos.

**Atributos:**
- `name`: Nombre de la categoría
- `description`: Descripción
- `displayOrder`: Orden de visualización
- `active`: Estado activo/inactivo

### 6. **Feedback** (Retroalimentación)
Comentarios y sugerencias de usuarios.

**Atributos:**
- `name`, `email`: Datos del remitente
- `comment`: Comentario del usuario
- `adminResponse`: Respuesta del administrador
- `isRead`: Estado de lectura
- `createdAt`, `respondedAt`: Timestamps

## 🔗 Relaciones Principales

### Relaciones de Composición (◆—)
Indican que el ciclo de vida del objeto contenido depende del contenedor:

1. **User ◆—→ Request** (1:N)
   - Un usuario puede crear múltiples solicitudes
   - Si se elimina el usuario, se eliminan sus solicitudes

2. **User ◆—→ Feedback** (1:N)
   - Un usuario puede enviar múltiples feedbacks
   - Si se elimina el usuario, se eliminan sus feedbacks

### Relaciones de Asociación (—)
Indican conexiones entre objetos independientes:

1. **User —— Material** (N:M)
   - **Tabla intermedia:** `user_materials`
   - **Significado:** Organizaciones (User con role ORGANIZATION) pueden aceptar múltiples materiales
   - **Multiplicidad:** Una organización acepta 0 o más materiales; un material puede ser aceptado por 0 o más organizaciones

2. **Request —— Material** (N:M)
   - **Tabla intermedia:** `request_materials`
   - **Significado:** Una solicitud puede contener múltiples tipos de materiales
   - **Multiplicidad:** Una solicitud contiene 1 o más materiales; un material puede estar en 0 o más solicitudes

3. **User ○—→ Request** (1:N - Organización gestiona)
   - **Significado:** Una organización puede gestionar múltiples solicitudes asignadas
   - **Multiplicidad:** Una solicitud puede tener 0 o 1 organización asignada; una organización puede gestionar 0 o más solicitudes
   - **Nota:** Esta es una segunda relación entre User y Request (diferente a la de creación)

4. **Post —→ Category** (N:1)
   - **Significado:** Múltiples posts pueden pertenecer a una categoría
   - **Multiplicidad:** Un post pertenece a 0 o 1 categoría; una categoría puede tener 0 o más posts

### Relaciones con Enumeraciones (→)
Asociaciones simples con tipos enumerados:

1. **User → Role** (1:1)
   - Cada usuario tiene exactamente un rol

2. **Request → RequestStatus** (1:1)
   - Cada solicitud tiene exactamente un estado

## 📊 Multiplicidades Explicadas

| Relación | Multiplicidad | Significado |
|----------|---------------|-------------|
| User → Request (crea) | 1:N | Un usuario crea 0 o más solicitudes |
| User → Request (gestiona) | 1:N | Una organización gestiona 0 o más solicitudes |
| User ↔ Material | N:M | Organizaciones aceptan múltiples materiales |
| Request ↔ Material | N:M | Solicitudes contienen múltiples materiales |
| User → Feedback | 1:N | Un usuario envía 0 o más feedbacks |
| Post → Category | N:1 | Múltiples posts en una categoría |

## 🎨 Diagrama PlantUML

Para visualizar el diagrama, puedes usar el archivo `class-diagram.puml` con cualquier visualizador de PlantUML:

- **Online:** [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/)
- **VS Code:** Extensión "PlantUML"
- **IntelliJ IDEA:** Plugin "PlantUML integration"

## 📝 Notas Importantes

### Patrón de Diseño: User como Polimorfismo
La clase `User` implementa un patrón de **polimorfismo basado en roles**:
- El atributo `role` determina el comportamiento y permisos
- Los usuarios regulares (USER) crean solicitudes
- Las organizaciones (ORGANIZATION) gestionan solicitudes y definen materiales aceptados
- Los administradores (ADMIN) tienen acceso completo al sistema

### Ciclo de Vida de una Solicitud
```
1. USER crea Request (status = PENDING)
2. ORGANIZATION acepta Request (status = ACCEPTED)
3. ORGANIZATION inicia recolección (status = IN_PROGRESS)
4. ORGANIZATION completa recolección (status = COMPLETED)
   O puede rechazar (status = REJECTED)
```

### Tablas Intermedias (Join Tables)
El sistema utiliza dos tablas intermedias para relaciones N:M:
- `user_materials`: Relaciona organizaciones con materiales que aceptan
- `request_materials`: Relaciona solicitudes con materiales a reciclar

## 🔍 Casos de Uso Principales

### 1. Creación de Solicitud
```
Usuario (USER) → Crea Request → Selecciona Materials → Sistema asigna status PENDING
```

### 2. Gestión por Organización
```
Organización (ORGANIZATION) → Ve Requests pendientes con sus Materials aceptados
→ Acepta Request → Cambia status a ACCEPTED → Programa scheduledDate
→ Inicia recolección (IN_PROGRESS) → Completa (COMPLETED)
```

### 3. Configuración de Materiales
```
Organización → Selecciona Materials que acepta → Se guarda en user_materials
→ Sistema filtra Requests que contengan esos Materials
```

## 📚 Referencias

- **JPA Annotations:** `@Entity`, `@ManyToOne`, `@ManyToMany`, `@OneToMany`
- **Lombok:** `@Data`, `@Getter`, `@Setter`, `@NoArgsConstructor`
- **Hibernate:** Lazy loading con `FetchType.LAZY`
- **Spring Security:** Roles y autenticación basada en `User.role`

---

**Última actualización:** 21 de octubre de 2025
**Versión del sistema:** 1.0
