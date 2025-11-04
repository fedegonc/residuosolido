# 🔍 Guía de Análisis de Performance

## Logs Habilitados

Se han agregado logs detallados para identificar cuellos de botella en múltiples endpoints.

### Ubicación de los Logs

Los logs aparecerán en la **consola de la aplicación** cuando accedas a:
- `http://localhost:8080/acopio/inicio` (Dashboard)
- `http://localhost:8080/acopio/requests` (Lista de solicitudes)
- `http://localhost:8080/acopio/requests/{id}` (Detalle de solicitud)

## Qué Buscar en los Logs

### 1. **Dashboard de Organización** (/acopio/inicio)
```
=== INICIO CARGA DASHBOARD ORGANIZACIÓN ===
⏱️ Tiempo autenticación: XXms
  🔍 Cargando solicitudes pendientes
  ✅ N solicitudes pendientes cargadas en: XXms
  🔍 Cargando solicitudes con estado: IN_PROGRESS
  ✅ N solicitudes cargadas en: XXms
  🔍 Cargando solicitudes con estado: COMPLETED
  ✅ N solicitudes cargadas en: XXms
⏱️ TIEMPO TOTAL DASHBOARD: XXms
=== FIN CARGA DASHBOARD ORGANIZACIÓN ===
```

### 2. **Detalle de Solicitud** (/acopio/requests/{id})
```
=== INICIO CARGA DETALLE SOLICITUD ID: 302 ===
  🔍 Ejecutando query findByIdWithDetails para ID: 302
  ✅ Query completada en: XXms
⏱️ Tiempo consulta DB: XXms
✓ Usuario cargado: username
✓ Organización cargada: org_name
✓ Materiales cargados: X items
⏱️ Tiempo preparación modelo: XXms
⏱️ TIEMPO TOTAL: XXms
=== FIN CARGA DETALLE SOLICITUD ===
```

### 3. **Logs SQL de Hibernate**

#### ✅ Query OPTIMIZADA (1 sola query con JOINs):
```sql
Hibernate: 
    select
        r1_0.id,
        -- campos de request
        u1_0.id,
        -- campos de user
        o1_0.id,
        -- campos de organization
        m1_0.request_id,
        m2_0.id
        -- campos de materials
    from
        request r1_0 
    left join
        users u1_0 on u1_0.id=r1_0.user_id 
    left join
        users o1_0 on o1_0.id=r1_0.organization_id 
    left join
        request_materials m1_0 on r1_0.id=m1_0.request_id 
    left join
        material m2_0 on m2_0.id=m1_0.material_id 
    where
        r1_0.id=?
```

#### ❌ Problema N+1 (EVITAR - múltiples queries):
Si ves esto, significa que el EntityGraph NO está funcionando:
```sql
-- Query 1: Cargar request
SELECT * FROM request WHERE id=?

-- Query 2: Cargar user (N+1)
SELECT * FROM users WHERE id=?

-- Query 3: Cargar organization (N+1)
SELECT * FROM users WHERE id=?

-- Query 4: Cargar materials (N+1)
SELECT * FROM request_materials WHERE request_id=?
```

## Análisis de Tiempos

### ✅ Tiempos Esperados (Optimizado)
- **Consulta DB**: < 100ms
- **Preparación modelo**: < 10ms
- **Tiempo total**: < 150ms

### ⚠️ Tiempos Problemáticos
- **Consulta DB**: > 500ms → Problema con índices o query
- **Preparación modelo**: > 50ms → Problema con lazy loading
- **Tiempo total**: > 1000ms → Optimización necesaria

## Optimizaciones Implementadas

### ✅ Ya Implementado
1. **EntityGraph en Repository** - Carga todas las relaciones en 1 sola query
2. **Método findByIdWithDetails()** - Evita problema N+1
3. **Logs de timing** - Identifica cuellos de botella

### 🔧 Si Aún Es Lento

#### Opción 1: Verificar Índices en BD
```sql
-- Verificar índices existentes
SELECT * FROM pg_indexes WHERE tablename = 'request';

-- Crear índices si faltan
CREATE INDEX IF NOT EXISTS idx_request_user_id ON request(user_id);
CREATE INDEX IF NOT EXISTS idx_request_organization_id ON request(organization_id);
CREATE INDEX IF NOT EXISTS idx_request_materials ON request_materials(request_id);
```

#### Opción 2: Cachear Resultados
Agregar `@Cacheable` en el servicio:
```java
@Cacheable(value = "requests", key = "#id")
public Optional<Request> findById(Long id) {
    // ...
}
```

#### Opción 3: Usar Proyecciones DTO
Si no necesitas todos los campos, crear un DTO ligero.

## Cómo Probar

1. **Reinicia la aplicación** para aplicar los cambios
2. **Accede a**: `http://localhost:8080/acopio/requests/302`
3. **Revisa la consola** y anota los tiempos
4. **Compara** con los tiempos esperados arriba

## Desactivar Logs SQL (Después de Debuggear)

En `application.properties`, cambiar:
```properties
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.use_sql_comments=false
```

## Ejemplo de Output Esperado

```
=== INICIO CARGA DETALLE SOLICITUD ID: 302 ===
  🔍 Ejecutando query findByIdWithDetails para ID: 302
Hibernate: 
    select ... (1 query con JOINs)
  ✅ Query completada en: 45ms
⏱️ Tiempo consulta DB: 47ms
✓ Usuario cargado: juan.perez
✓ Organización cargada: acopio_rivera
✓ Materiales cargados: 3 items
⏱️ Tiempo preparación modelo: 2ms
⏱️ TIEMPO TOTAL: 52ms
=== FIN CARGA DETALLE SOLICITUD ===
```

Si ves **1 sola query SQL** con JOINs → ✅ Optimización funcionando
Si ves **múltiples queries** (N+1) → ❌ Revisar EntityGraph
