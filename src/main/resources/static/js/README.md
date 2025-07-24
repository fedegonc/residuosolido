# JavaScript Files Documentation

Este directorio contiene los archivos JavaScript organizados por funcionalidad para mejorar la mantenibilidad y reutilización del código.

## Estructura de Archivos

### `common.js`
**Propósito**: Funcionalidades JavaScript comunes que pueden ser reutilizadas en múltiples páginas.

**Funcionalidades incluidas**:
- **NotificationUtils**: Sistema de notificaciones toast
- **FormUtils**: Utilidades para validación de formularios
- **AnimationUtils**: Efectos de animación y scroll suave
- **DataUtils**: Manejo de peticiones HTTP con manejo de errores

**Uso**:
```javascript
// Mostrar notificación
NotificationUtils.show('Mensaje exitoso', 'success');

// Validar formulario
FormUtils.validate(document.getElementById('myForm'));

// Scroll suave
AnimationUtils.scrollTo('#section', 100);
```

### `categories.js`
**Propósito**: Manejo específico de las funcionalidades de categorías jerárquicas en la página de inicio.

**Funcionalidades incluidas**:
- Manejo de menús desplegables de categorías
- Carga dinámica de posts por categoría
- Animaciones de expansión/contracción
- Datos mock de posts (futuro: integración con API)

**Funciones principales**:
- `toggleCategory(categorySlug)`: Alterna la visibilidad de una categoría
- `loadPosts(categorySlug, container)`: Carga posts en el contenedor
- `createPostElement(post)`: Crea elementos HTML para posts

## Cómo Agregar Nuevos Archivos JavaScript

1. **Crear el archivo** en el directorio `/src/main/resources/static/js/`
2. **Seguir la estructura** de comentarios JSDoc para documentación
3. **Exponer funciones globalmente** si es necesario usando `window.nombreFuncion`
4. **Incluir el archivo** en las páginas HTML correspondientes
5. **Actualizar este README** con la nueva funcionalidad

## Buenas Prácticas

### Estructura del Código
```javascript
/**
 * Descripción del archivo
 */

// Constantes y configuración
const CONFIG = {};

// Funciones principales
function mainFunction() {
    // Implementación
}

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    // Código de inicialización
});
```

### Manejo de Errores
- Siempre incluir manejo de errores en funciones críticas
- Usar `console.error()` para logging de errores
- Mostrar mensajes amigables al usuario usando `NotificationUtils`

### Naming Conventions
- **Funciones**: camelCase (`toggleCategory`)
- **Constantes**: UPPER_SNAKE_CASE (`CATEGORY_POSTS`)
- **Variables**: camelCase (`categorySlug`)
- **Clases/Objetos**: PascalCase (`NotificationUtils`)

## Integración con Spring Boot

Los archivos JavaScript estáticos se sirven automáticamente desde `/src/main/resources/static/js/` y son accesibles en las plantillas Thymeleaf usando:

```html
<script src="/js/nombre-archivo.js"></script>
```

## Futuras Mejoras

1. **Integración con API REST**: Reemplazar datos mock con llamadas reales al backend
2. **Módulos ES6**: Migrar a módulos ES6 cuando sea apropiado
3. **Minificación**: Implementar proceso de build para minificar archivos en producción
4. **Testing**: Agregar tests unitarios para las funciones JavaScript
5. **TypeScript**: Considerar migración a TypeScript para mejor tipado

## Dependencias

Actualmente no se usan librerías externas de JavaScript. Todas las funcionalidades están implementadas en JavaScript vanilla para mantener el proyecto ligero y sin dependencias adicionales.
