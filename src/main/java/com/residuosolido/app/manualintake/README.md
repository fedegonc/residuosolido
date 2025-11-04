# Módulo de Registro Manual de Materiales

## 📋 Descripción

Este módulo permite a las organizaciones de acopio registrar manualmente los materiales recibidos de forma independiente a las solicitudes formales del sistema. Es útil para registrar entregas directas de vecinos, donaciones espontáneas o recolecciones que no pasaron por el flujo normal de solicitudes.

## 🏗️ Estructura del Módulo

```
manualintake/
├── model/
│   └── ManualIntake.java          # Entidad JPA para registros manuales
├── repository/
│   └── ManualIntakeRepository.java # Repositorio con queries personalizadas
├── service/
│   └── ManualIntakeService.java    # Lógica de negocio
├── controller/
│   └── ManualIntakeController.java # Controlador REST/MVC
└── README.md                        # Este archivo
```

## 📊 Modelo de Datos

### Entidad `ManualIntake`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Identificador único |
| `organization` | User | Organización que registra el ingreso |
| `material` | Material | Material recibido |
| `quantityKg` | BigDecimal | Cantidad en kilogramos |
| `intakeDate` | LocalDate | Fecha del registro (puede ser retroactiva) |
| `notes` | String | Notas adicionales |
| `source` | String | Origen del material (ej: "Vecino", "Donación") |
| `createdAt` | LocalDateTime | Timestamp de creación |
| `updatedAt` | LocalDateTime | Timestamp de última actualización |

## 🔐 Seguridad

- **Acceso:** Solo usuarios con rol `ORGANIZATION`
- **Rutas protegidas:** `/acopio/registro-manual/**`
- **Validación:** Cada organización solo puede ver/editar sus propios registros

## 🎯 Funcionalidades

### 1. Crear Registro Manual
- Seleccionar material de la lista de materiales activos
- Ingresar cantidad en kilogramos (con decimales)
- Seleccionar fecha del registro (permite fechas pasadas)
- Agregar fuente/origen opcional
- Agregar notas adicionales opcionales

### 2. Listar Registros
- Vista paginada de todos los registros de la organización
- Ordenados por fecha descendente (más recientes primero)
- Muestra total acumulado de kg registrados
- Paginación front-end con localStorage

### 3. Buscar Registros
- Búsqueda por nombre de material
- Búsqueda por fuente/origen
- Búsqueda por notas

### 4. Editar Registro
- Modificar cualquier campo del registro
- Validación de permisos (solo la organización propietaria)

### 5. Eliminar Registro
- Eliminación con confirmación
- Validación de permisos

## 🚀 Uso

### Desde el Dashboard de Acopio

1. Acceder a `/acopio/inicio`
2. Click en el botón **"Registro Manual"** (color emerald)
3. Click en **"Nuevo Registro"**
4. Completar el formulario:
   - Material (requerido)
   - Cantidad en kg (requerido)
   - Fecha (requerido, por defecto hoy)
   - Fuente (opcional)
   - Notas (opcional)
5. Click en **"Crear Registro"**

### Desde el Navbar

1. Abrir menú hamburguesa
2. Click en **"Registro Manual"**

## 📱 Interfaz de Usuario

### Características del Formulario

- **Selector de Material:** Dropdown con todos los materiales activos
- **Input de Cantidad:** Acepta decimales (ej: 20.5 kg)
- **Selector de Fecha:** Calendario HTML5 nativo
  - Permite seleccionar fechas pasadas
  - Por defecto muestra la fecha actual
- **Campos Opcionales:** Fuente y Notas
- **Validación:** Campos requeridos marcados con asterisco rojo

### Características de la Lista

- **Tabla Responsive:** Se adapta a móviles
- **Paginación:** 10, 25, 50 o 100 registros por página
- **Búsqueda:** Filtro en tiempo real
- **Acciones:** Editar y Eliminar por fila
- **Total Acumulado:** Badge destacado con total de kg

## 🔄 Integración con el Sistema

### Breadcrumbs
- Automáticos vía `BreadcrumbService`
- Ruta: `Inicio > Registro Manual`

### Navbar
- Enlace en navbar de organizaciones
- Estilo diferenciado (emerald) para destacar

### Dashboard
- Card en sección "Acciones Rápidas"
- Icono: `clipboard-plus`
- Color: Emerald (verde esmeralda)

## 📈 Estadísticas

El servicio incluye métodos para:
- Contar registros por organización
- Calcular total de kg por organización
- Filtrar por rango de fechas

Estos métodos pueden ser utilizados para:
- Dashboard de estadísticas
- Reportes mensuales/anuales
- Gráficos de tendencias

## 🛠️ Tecnologías Utilizadas

- **Backend:**
  - Spring Boot 3.2
  - Spring Data JPA
  - Spring Security
  - Lombok
  - PostgreSQL

- **Frontend:**
  - Thymeleaf
  - TailwindCSS
  - Lucide Icons
  - JavaScript vanilla (paginación)

## 📝 Notas Técnicas

### Lazy Loading
El servicio fuerza la inicialización de relaciones lazy (`material`, `organization`) para evitar `LazyInitializationException` en las vistas Thymeleaf.

### Transacciones
- Operaciones de escritura: `@Transactional`
- Operaciones de lectura: `@Transactional(readOnly = true)`

### Validaciones
- Cantidad mínima: 0.01 kg
- Fecha: No puede ser futura (validación front-end)
- Material: Debe existir y estar activo
- Organización: Debe ser la autenticada

## 🔮 Futuras Mejoras

1. **Exportación de Datos**
   - Exportar a Excel/CSV
   - Generar reportes PDF

2. **Gráficos y Estadísticas**
   - Gráfico de barras por material
   - Tendencia temporal
   - Comparativa mensual

3. **Notificaciones**
   - Alertas cuando se alcancen metas
   - Resumen semanal/mensual por email

4. **Validaciones Adicionales**
   - Límite máximo de kg por registro
   - Validación de fechas futuras en backend

5. **Auditoría**
   - Registro de cambios (quién editó qué y cuándo)
   - Historial de modificaciones

## 📞 Soporte

Para dudas o problemas con este módulo, contactar al equipo de desarrollo.
