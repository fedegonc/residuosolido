# Guest Dropdown Component

Un componente de barra desplegable elegante y funcional para usuarios guest (no autenticados) en el sistema de gestión de residuos sólidos.

## Características

- ✅ **Responsive**: Se adapta perfectamente a dispositivos móviles y desktop
- ✅ **Animaciones suaves**: Transiciones elegantes con Tailwind CSS
- ✅ **Navegación rápida**: Acceso directo a categorías principales
- ✅ **Accesibilidad**: Soporte para teclado (Escape para cerrar)
- ✅ **Iconos SVG**: Interfaz visual atractiva y moderna
- ✅ **Auto-cierre**: Se cierra al hacer clic fuera o al navegar

## Estructura del Componente

El componente incluye tres fragmentos principales:

1. **`guest-dropdown`**: El HTML principal del componente
2. **`guest-dropdown-js`**: JavaScript para la funcionalidad
3. **`guest-dropdown-css`**: Estilos CSS adicionales

## Cómo usar

### 1. Incluir en el HTML

```html
<!-- Incluir el componente donde lo necesites -->
<div th:replace="fragments/guest-dropdown :: guest-dropdown"></div>
```

### 2. Incluir JavaScript

```html
<!-- Antes del cierre del body -->
<th:block th:replace="fragments/guest-dropdown :: guest-dropdown-js"></th:block>
```

### 3. Incluir CSS (opcional)

```html
<!-- En el head para estilos adicionales -->
<th:block th:replace="fragments/guest-dropdown :: guest-dropdown-css"></th:block>
```

## Ejemplo completo

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Mi Página</title>
    <script src="https://cdn.tailwindcss.com"></script>
    
    <!-- CSS del componente -->
    <th:block th:replace="fragments/guest-dropdown :: guest-dropdown-css"></th:block>
</head>
<body>
    <header class="bg-white shadow">
        <div class="max-w-7xl mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <h1>Mi Sitio</h1>
                
                <!-- Guest Dropdown -->
                <div th:replace="fragments/guest-dropdown :: guest-dropdown"></div>
            </div>
        </div>
    </header>
    
    <!-- Tu contenido aquí -->
    
    <!-- JavaScript del componente -->
    <th:block th:replace="fragments/guest-dropdown :: guest-dropdown-js"></th:block>
</body>
</html>
```

## Personalización

### Modificar categorías

Para cambiar las categorías mostradas, edita el archivo `guest-dropdown.html`:

```html
<!-- Agregar nueva categoría -->
<a href="#nueva-categoria" onclick="scrollToSection('nueva-categoria')" 
   class="group flex items-center px-4 py-3 text-sm text-gray-700 hover:bg-purple-50 hover:text-purple-800 transition-colors duration-150">
    <div class="flex-shrink-0 w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center mr-3 group-hover:bg-purple-200">
        <!-- Tu icono SVG aquí -->
    </div>
    <div>
        <div class="font-medium">Nueva Categoría</div>
        <div class="text-xs text-gray-500">Descripción</div>
    </div>
</a>
```

### Cambiar colores

El componente usa la paleta de colores de Tailwind CSS. Para personalizar:

```css
/* En guest-dropdown-css */
.custom-color {
    background-color: #tu-color;
}
```

### Modificar animaciones

Las animaciones se controlan mediante clases de Tailwind CSS:

```javascript
// Cambiar duración de animación
panel.classList.add('transition-all', 'duration-300'); // 300ms en lugar de 200ms
```

## Funciones JavaScript disponibles

- `openGuestDropdown()`: Abre el dropdown
- `closeGuestDropdown()`: Cierra el dropdown  
- `toggleGuestDropdown()`: Alterna entre abrir/cerrar
- `scrollToSection(sectionId)`: Hace scroll a una sección específica

## Dependencias

- **Tailwind CSS**: Para estilos y animaciones
- **Thymeleaf**: Para fragmentos y templating
- **JavaScript ES6+**: Para funcionalidad interactiva

## Compatibilidad

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 12+
- ✅ Edge 79+
- ✅ Dispositivos móviles iOS/Android

## Notas de desarrollo

- El componente es completamente independiente y reutilizable
- No requiere librerías externas además de Tailwind CSS
- Optimizado para rendimiento con event delegation
- Incluye manejo de errores básico

## Ejemplo en vivo

Consulta `examples/guest-dropdown-example.html` para ver el componente en acción.
