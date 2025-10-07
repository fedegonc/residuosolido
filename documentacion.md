# 📚 Documentación General – EcoSolicitud

## 1. Panorama del Proyecto
- **Propósito:** Plataforma que conecta ciudadanos de Rivera (Uruguay) y Sant'Ana do Livramento (Brasil) con organizaciones recicladoras para coordinar recolecciones de residuos.
- **Tecnologías clave:** Java 17, Spring Boot 3.2, Thymeleaf, Tailwind CSS, Font Awesome 6, Spring Security (roles `USER`, `ORGANIZATION`, `ADMIN`), PostgreSQL, Cloudinary para imágenes.
- **Arquitectura:** MVC clásico con controladores en `com.residuosolido.app.controller`, servicios en `com.residuosolido.app.service`, entidades en `com.residuosolido.app.model` y plantillas Thymeleaf bajo `src/main/resources/templates/`.

## 2. Accesos Rápidos
- 🏠 Página principal: https://residuosolido.onrender.com/
- 🔐 Iniciar sesión: https://residuosolido.onrender.com/auth/login
- ✍️ Registro: https://residuosolido.onrender.com/auth/register
- 📚 Posts educativos (reciclables): https://residuosolido.onrender.com/posts/category/reciclables

## 3. Roles y Capacidades
- **Invitado:** Consulta posts públicos, organizaciones y usa el asistente virtual.
- **Usuario Ciudadano:** Todo lo anterior + crear solicitudes de recolección, adjuntar imágenes, ver y gestionar el estado de sus solicitudes.
- **Organización:** Revisa solicitudes pendientes, acepta/rechaza, agenda recolecciones y actualiza estados.
- **Administrador:** Gestiona entidades del sistema (usuarios, organizaciones, materiales, posts, estadísticas, configuración).

## 4. Flujos Clave de Usuario
### 4.1 Ciudadanos
1. **Registro:** Completar usuario, correo, contraseña y dirección.
2. **Crear solicitud:** Formular dirección, descripción, materiales (checklist), fotos (máx. 3, 5MB c/u) y fecha preferida.
3. **Estados de solicitud:** `PENDIENTE` → `ACEPTADA` → `EN_PROCESO` → `COMPLETADA` (con posibles `RECHAZADA` o `CANCELADA`).
4. **Edición:** Posible mientras el estado sea `PENDIENTE`.

### 4.2 Organizaciones
1. **Dashboard:** Resumen de métricas y solicitudes pendientes (`/org/dashboard`).
2. **Evaluar solicitud:** Revisar dirección, materiales, imágenes y fecha preferida.
3. **Actualizar estado:** Aceptar, poner en proceso, completar o rechazar con motivo.
4. **Materiales aceptados:** (Pendiente) Soporte para configurar materiales que recolecta cada organización.

## 5. Funciones Adicionales
- **Feedback:** Formulario público en `/feedback` para sugerencias, problemas o preguntas.
- **Compatibilidad:** Optimizado para Chrome, Firefox, Edge y Safari; uso fluido en desktop, tablet y móvil.
- **Soporte:** Asistente virtual 24/7 + formulario de feedback.

## 6. Preguntas Frecuentes
- **¿Hay límite de solicitudes por usuario?** No.
- **¿Se pueden editar solicitudes aceptadas?** No, solo pendientes.
- **¿Las imágenes son obligatorias?** No, pero mejoran la respuesta de las organizaciones.
- **¿Una organización puede cancelar una solicitud aceptada?** Sí, indicando motivo.

## 7. Guía de Estilos y UX
### 7.1 Colores
- **Primario:** `emerald-600` (hover `emerald-700`).
- **Secundario (información):** `blue-600`.
- **Éxito:** `green-600`.
- **Advertencia:** `yellow-600`.
- **Error:** `red-600`.
- **Fondos suaves:** `emerald-50`, bordes `emerald-100`.

### 7.2 Botones (Tailwind)
```html
<!-- Primario -->
class="bg-emerald-600 text-white px-4 py-2 rounded-lg hover:bg-emerald-700"

<!-- Secundario / navegación -->
class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"

<!-- Outline -->
class="border border-emerald-600 text-emerald-600 px-4 py-2 rounded-lg hover:bg-emerald-50"
```

### 7.3 Tarjetas y Contenedores
Usar `bg-white rounded-lg shadow-sm border border-gray-100 p-6` en todo el sistema para tarjetas, paneles y cajas de formularios.

### 7.4 Formularios
Entradas con `border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500`.

### 7.5 Iconografía
Estandarizar en **Font Awesome 6** (`fa-solid`). Se recomienda migrar los iconos existentes de Lucide/SVG gradualmente para mantener consistencia.

## 8. Plan de Homogeneización (Pendiente)
1. **Traducciones:** Verificar que todos los `messages_*.properties` incluyan las claves necesarias (`button.view`, `button.more`, etc.).
2. **Colores:** Reemplazar variantes `green-*` por `emerald-*` salvo casos de semántica específica.
3. **Iconos:** Migrar a Font Awesome en plantillas admin, user y org.
4. **Botones:** Alinear padding (`px-4 py-2`) y tamaño de fuente (`text-sm` o `text-base` según contexto).
5. **Tarjetas:** Aplicar el estilo estándar señalado en §7.3.
6. **Materiales por organización:** Implementar la relación Many-to-Many pendiente en `MaterialService` (`updateAcceptedMaterials()` y `toggleMaterialAcceptance()`).

## 9. Métricas y Estadísticas
- El dashboard administrativo (`/admin/statistics`) muestra usuarios por mes, crecimiento reciente, solicitudes por estado y materiales destacados.
- Asegurar que cualquier nueva tarjeta o gráfico respete los estilos descritos.

## 10. Diagramas y Recursos Visuales
- Los diagramas UML deben regenerarse a partir del repositorio histórico (`UML.md` fue archivado). Considerar migrarlos a una herramienta colaborativa (Draw.io/Lucidchart) y exportar imágenes a `docs/diagrams/`.

---
**Historial:** Documento consolidado en octubre 2025 a partir de `MANUAL.md`, `DIVERGENCIAS.md` y referencias de `UML.md`.
