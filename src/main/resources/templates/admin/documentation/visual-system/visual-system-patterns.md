# Patrón Estándar de Páginas Admin - Documentación Completa

## 🎯 **Propósito**
Esta documentación establece el patrón estándar para todas las páginas de administración del sistema, asegurando consistencia visual, funcional y de experiencia de usuario.

## 📋 **Estructura General de Página Admin**

### 1. **Header HTML (Obligatorio)**
```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Título de la Página - Admin</title>
    <meta name="_csrf" th:if="${_csrf != null}" th:content="${_csrf.token}"/>
    <meta name="_csrf_header" th:if="${_csrf != null}" th:content="${_csrf.headerName}"/>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest/dist/umd/lucide.js"></script>
    <script src="https://unpkg.com/htmx.org@1.9.12"></script>
</head>
<body th:replace="~{fragments/admin-layout :: layout(~{::main})}">
```

### 2. **Body Structure (Obligatorio)**
```html
<body th:replace="~{fragments/admin-layout :: layout(~{::main})}">
<main>
    <!-- Breadcrumbs -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
        <th:block th:replace="~{fragments/components/breadcrumbs :: auto}"></th:block>
    </div>

    <!-- Messages -->
    <div th:if="${successMessage}" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
        <div class="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg">
            <div class="flex items-center">
                <i data-lucide="check-circle" class="w-5 h-5 mr-2"></i>
                <span th:text="${successMessage}"></span>
            </div>
        </div>
    </div>

    <div th:if="${errorMessage}" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-4">
        <div class="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg">
            <div class="flex items-center">
                <i data-lucide="alert-circle" class="w-5 h-5 mr-2"></i>
                <span th:text="${errorMessage}"></span>
            </div>
        </div>
    </div>

    <!-- Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <!-- CONTENIDO ESPECÍFICO DE LA PÁGINA -->
    </div>

    <script>
        // JavaScript específico de la página
        lucide.createIcons();
    </script>
</main>
</body>
</html>
```

## 🏗️ **Estructura del Contenedor Blanco (List View)**

### **Patrón Estándar:**
```html
<!-- List View -->
<div th:if="${viewType == 'list'}" class="bg-white rounded-lg shadow">
    <div class="px-6 py-4 border-b border-gray-200 space-y-3">
        <!-- Título Principal -->
        <h2 class="text-lg font-semibold text-gray-900">
            Gestión de [Entidad]
        </h2>

        <!-- Texto Descriptivo -->
        <p class="text-sm text-gray-600">
            [Descripción específica de la funcionalidad de la página].
            <span class="block mt-1 text-xs text-gray-500">
                [Consejo específico sobre el uso del buscador].
            </span>
        </p>

        <!-- Subtítulo -->
        <h3 class="text-lg font-semibold text-gray-900">
            Lista de [Entidad]
        </h3>

        <!-- Buscador -->
        <form th:action="@{/admin/[entidad]}" method="get" class="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 w-full">
            <input type="text" name="q" th:value="${query}" placeholder="[Placeholder específico]"
                   class="w-full sm:w-96 h-9 px-3 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"/>
            <div class="flex gap-2">
                <button type="submit" class="h-9 px-3 text-sm bg-emerald-600 text-white rounded-md hover:bg-emerald-700">Buscar</button>
                <a th:href="@{/admin/[entidad]}" class="h-9 px-3 text-sm border border-gray-300 rounded-md hover:bg-gray-50 flex items-center">Limpiar</a>
            </div>
        </form>
    </div>

    <!-- Tabla -->
    <div class="overflow-x-auto px-6 py-4">
        <table class="min-w-full divide-y divide-gray-200">
            <!-- Header de tabla -->
            <thead class="bg-gray-50 border-b border-gray-200">
                <tr>
                    <!-- Columnas específicas -->
                </tr>
            </thead>
            <!-- Body de tabla -->
            <tbody id="[entidad]TableBody" class="bg-white divide-y divide-gray-200">
                <!-- Filas de datos -->
            </tbody>
        </table>
    </div>

    <!-- Controles de paginación -->
    <div class="px-6 py-4 border-t border-gray-200 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div class="flex items-center gap-2 text-sm text-gray-600">
            <span>Filas por página:</span>
            <select id="[entidad]PageSize" class="border border-gray-300 rounded-md px-2 py-1 focus:outline-none focus:ring-2 focus:ring-green-500">
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
            </select>
            <span id="[entidad]PageInfo" class="ml-2">Mostrando 0–0 de 0</span>
        </div>
        <div id="[entidad]PaginationControls" class="flex items-center gap-1"></div>
    </div>

    <!-- Botón de Nueva Entidad -->
    <div class="px-6 pb-4 flex justify-start">
        <a th:if="${viewType == 'list'}" th:href="@{/admin/[entidad](action='new')}"
           class="bg-green-600 text-white px-3 py-1.5 rounded-md hover:bg-green-700 flex items-center space-x-2 text-sm">
            <i data-lucide="plus" class="w-4 h-4"></i>
            <span>Nueva [Entidad]</span>
        </a>
    </div>
</div>
```

## 📝 **Patrones de Texto Descriptivo por Entidad**

### **Materials:**
- **Principal:** "Gestiona el catálogo de materiales reciclables disponibles en el sistema. Puedes crear nuevos materiales, editar información existente y gestionar el estado de cada material. Los materiales activos estarán disponibles para las solicitudes de recolección."
- **Consejo:** "Usa el buscador para filtrar por nombre, descripción o categoría. Los materiales marcados como inactivos no estarán disponibles para nuevas solicitudes."

### **Requests:**
- **Principal:** "Gestiona todas las solicitudes de recolección de residuos en el sistema. Puedes ver detalles de cada solicitud, cambiar su estado, programar fechas de recolección y gestionar su ciclo de vida completo desde creación hasta finalización."
- **Consejo:** "Usa el buscador para filtrar por usuario, materiales, dirección, estado o fecha. Las solicitudes pendientes requieren atención inmediata para su asignación."

### **Users:**
- **Principal:** "Administra todos los usuarios registrados en el sistema. Puedes gestionar perfiles, permisos, información de contacto y el estado de activación de cada usuario."
- **Consejo:** "Usa el buscador para filtrar por nombre, apellido, email o rol. Los usuarios inactivos no podrán acceder al sistema."

### **Organizations:**
- **Principal:** "Gestiona las organizaciones registradas en la plataforma. Puedes administrar información de contacto, direcciones, y gestionar su estado de activación en el sistema."
- **Consejo:** "Usa el buscador para filtrar por nombre, dirección o estado. Las organizaciones inactivas no podrán crear solicitudes."

### **Posts:**
- **Principal:** "Gestiona el contenido informativo del sistema. Puedes crear, editar y organizar posts con información relevante sobre reciclaje y medio ambiente. Los posts publicados estarán disponibles en el portal público para educar a los usuarios."
- **Consejo:** "Usa el buscador para filtrar por título, categoría o contenido. Los posts organizados por categorías facilitan la navegación de los usuarios."

### **Feedback:**
- **Principal:** "Gestiona los comentarios y sugerencias de los usuarios del sistema. Puedes revisar feedback de usuarios registrados y anónimos, responder consultas y mantener un registro de todas las comunicaciones. El feedback es fundamental para mejorar la experiencia del usuario y la calidad del servicio."
- **Consejo:** "Usa el buscador para filtrar por usuario, mensaje o fecha. Los comentarios recientes requieren atención prioritaria para mantener la satisfacción del usuario."

## 🎨 **Estilos y Clases CSS Estándar**

### **Botones:**
- **Buscar:** `bg-emerald-600 text-white rounded-md hover:bg-emerald-700`
- **Limpiar:** `border border-gray-300 rounded-md hover:bg-gray-50`
- **Nuevo:** `bg-green-600 text-white px-3 py-1.5 rounded-md hover:bg-green-700`
- **Editar:** `text-green-700 border-green-200`
- **Eliminar:** `text-red-700 border-red-200`

### **Formularios:**
- **Input:** `border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-green-500`
- **Select:** `border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500`
- **Textarea:** `border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500`

### **Estados:**
- **Activo:** `bg-green-100 text-green-800`
- **Inactivo:** `bg-red-100 text-red-800`

## 🔧 **JavaScript Patterns**

### **Paginación Front-end (Obligatorio):**
```javascript
(function() {
    const tbody = document.getElementById('[entidad]TableBody');
    if (!tbody) return;
    const rows = Array.from(tbody.querySelectorAll('tr'));
    const pageSizeSelect = document.getElementById('[entidad]PageSize');
    const pageInfo = document.getElementById('[entidad]PageInfo');
    const controls = document.getElementById('[entidad]PaginationControls');

    let pageSize = parseInt(localStorage.getItem('[entidad]_page_size') || '10', 10);
    if (![10,25,50,100].includes(pageSize)) pageSize = 10;
    pageSizeSelect.value = String(pageSize);
    let currentPage = 1;

    function renderPage() {
        const total = rows.length;
        const totalPages = Math.max(1, Math.ceil(total / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const endIdx = Math.min(startIdx + pageSize, total);

        rows.forEach((tr, idx) => {
            tr.style.display = (idx >= startIdx && idx < endIdx) ? '' : 'none';
        });

        if (total === 0) {
            pageInfo.textContent = 'Mostrando 0–0 de 0';
        } else {
            pageInfo.textContent = `Mostrando ${startIdx + 1}–${endIdx} de ${total}`;
        }

        buildControls(totalPages);
    }

    function buildControls(totalPages) {
        controls.innerHTML = '';
        const btnClass = 'px-3 py-1 border rounded-md text-sm hover:bg-gray-50';
        const activeClass = 'bg-emerald-600 text-white border-emerald-600 hover:bg-emerald-700';

        function addButton(label, disabled, onClick, extraClass = '') {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.textContent = label;
            btn.className = `${btnClass} ${extraClass}`;
            btn.disabled = disabled;
            btn.onclick = onClick;
            controls.appendChild(btn);
        }

        addButton('«', currentPage === 1, () => { currentPage = 1; renderPage(); });
        addButton('‹', currentPage === 1, () => { currentPage--; renderPage(); });

        const maxBtns = 7;
        let start = Math.max(1, currentPage - Math.floor(maxBtns/2));
        let end = Math.min(totalPages, start + maxBtns - 1);
        if (end - start + 1 < maxBtns) {
            start = Math.max(1, end - maxBtns + 1);
        }
        for (let p = start; p <= end; p++) {
            addButton(String(p), false, () => { currentPage = p; renderPage(); }, p === currentPage ? activeClass : '');
        }

        addButton('›', currentPage === totalPages, () => { currentPage++; renderPage(); });
        addButton('»', currentPage === totalPages, () => { currentPage = totalPages; renderPage(); });
    }

    pageSizeSelect.addEventListener('change', () => {
        pageSize = parseInt(pageSizeSelect.value, 10);
        localStorage.setItem('[entidad]_page_size', String(pageSize));
        currentPage = 1;
        renderPage();
    });

    renderPage();
})();
```

## ✅ **Checklist de Implementación**

### **Antes de crear una nueva página admin:**
- [ ] ¿Usa `th:replace="~{fragments/components/breadcrumbs :: auto}"`?
- [ ] ¿Tiene mensajes de success/error con los estilos estándar?
- [ ] ¿El contenedor blanco tiene la jerarquía correcta de títulos?
- [ ] ¿Incluye texto descriptivo específico de la entidad?
- [ ] ¿El buscador sigue el patrón estándar?
- [ ] ¿El botón "Nuevo" está al final de la lista?
- [ ] ¿Implementa paginación front-end?
- [ ] ¿Usa las clases CSS estándar?
- [ ] ¿Tiene JavaScript para `lucide.createIcons()`?

### **Entidades implementadas:**
- [x] Materials
- [x] Requests  
- [x] Users
- [x] Organizations
- [x] Posts
- [x] Categories
- [x] Feedback

## 🔄 **Proceso de Homogenización**

Para homogenizar una página existente:
1. **Eliminar** header personalizado y breadcrumbs manuales
2. **Agregar** breadcrumbs automáticos
3. **Implementar** estructura de mensajes estándar
4. **Crear** jerarquía de títulos dentro del contenedor blanco
5. **Agregar** texto descriptivo específico
6. **Mover** botón principal al final de la lista
7. **Corregir** cualquier inconsistencia de estilos

Esta documentación garantiza que todas las páginas de administración mantengan consistencia perfecta y una experiencia de usuario homogénea.
