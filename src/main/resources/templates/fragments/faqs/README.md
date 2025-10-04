# Chat Widget - FAQ Component

## 📋 Descripción

Componente de chat flotante para asistencia y preguntas frecuentes. El botón aparece en la esquina inferior derecha de todas las páginas y permite a los usuarios interactuar con un asistente virtual.

## 🎨 Características

### Diseño Responsive
- **Desktop**: Ventana de chat de 384px de ancho (1/3 de pantalla aprox)
- **Mobile**: Modal que ocupa toda la pantalla con bordes redondeados superiores

### Funcionalidades Actuales
- ✅ Botón flotante en esquina inferior derecha
- ✅ Animación de hover con escala
- ✅ Ventana de chat con header personalizado
- ✅ Área de mensajes con scroll
- ✅ Input para escribir mensajes
- ✅ Mensaje de bienvenida automático
- ✅ Respuesta simulada del bot
- ✅ Escape de HTML para prevenir XSS

## 📁 Estructura de Archivos

```
src/main/resources/templates/fragments/faqs/
├── chat-widget.html          # Componente principal del widget
└── README.md                  # Esta documentación

src/main/java/com/residuosolido/app/controller/
└── FaqController.java         # Controlador para endpoints del chat
```

## 🔌 Endpoints Disponibles

### POST `/faq/chat/message`
Procesa mensajes del chat.

**Request Body:**
```json
{
  "message": "¿Cómo funciona el reciclaje?"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Respuesta del bot",
  "timestamp": 1234567890
}
```

### GET `/faq/chat/status`
Verifica el estado del servicio de chat.

**Response:**
```json
{
  "available": true,
  "message": "Servicio de chat disponible",
  "version": "1.0.0"
}
```

### GET `/faq/questions`
Obtiene preguntas frecuentes (en desarrollo).

## 🚀 Uso

### Incluir en un Layout

El widget ya está incluido en los siguientes layouts:
- `fragments/layout.html` (Layout público)
- `fragments/user-layout.html` (Layout de usuario)
- `fragments/admin-layout.html` (Layout de admin)

Para incluirlo en otros templates:

```html
<!-- Chat Widget -->
<div th:replace="~{fragments/faqs/chat-widget :: chatWidget}"></div>
```

## 🎯 Próximas Funcionalidades

- [ ] Integración con backend real para respuestas
- [ ] Sistema de preguntas frecuentes predefinidas
- [ ] Historial de conversación persistente
- [ ] Indicador de "escribiendo..."
- [ ] Notificaciones de nuevos mensajes
- [ ] Integración con IA para respuestas automáticas
- [ ] Panel de administración para gestionar FAQs
- [ ] Estadísticas de uso del chat
- [ ] Soporte para archivos adjuntos
- [ ] Modo offline con respuestas en caché

## 🔧 Personalización

### Colores
El widget usa la paleta de colores verde de TailwindCSS:
- Botón: `bg-green-600` / `hover:bg-green-700`
- Header: `bg-green-600`
- Mensajes del usuario: `bg-green-600`

Para cambiar los colores, busca y reemplaza las clases de Tailwind en `chat-widget.html`.

### Tamaño
Ajusta las siguientes clases en el div `#chatWindow`:
- Desktop: `md:w-96` (cambiar a `md:w-80`, `md:w-[500px]`, etc.)
- Mobile: `w-full` (mantener para ocupar toda la pantalla)

### Posición
El botón usa clases de posicionamiento fijo:
```html
class="fixed bottom-6 right-6 ..."
```

Cambia `bottom-6` y `right-6` según necesites.

## 🐛 Debugging

### El widget no aparece
1. Verifica que el fragmento esté incluido en el layout
2. Revisa la consola del navegador para errores JavaScript
3. Asegúrate de que TailwindCSS esté cargado

### Los mensajes no se envían
1. Verifica que el endpoint `/faq/chat/message` esté accesible
2. Revisa la configuración de seguridad en `SecurityConfig.java`
3. Comprueba los logs del servidor para errores

### Problemas de estilo
1. Verifica que TailwindCSS esté cargado correctamente
2. Revisa que no haya conflictos con otros estilos
3. Usa las herramientas de desarrollo del navegador para inspeccionar

## 📝 Notas de Desarrollo

- El widget usa JavaScript vanilla (no requiere jQuery)
- Los mensajes se escapan automáticamente para prevenir XSS
- El scroll se ajusta automáticamente al agregar mensajes
- El estado del chat (abierto/cerrado) se maneja con clases CSS
- Por ahora, las respuestas son simuladas localmente

## 🔐 Seguridad

- ✅ Escape de HTML en mensajes
- ✅ Rutas públicas configuradas en Spring Security
- ✅ CSRF habilitado (requiere token para POST)
- ⚠️ Validación de entrada pendiente en backend
- ⚠️ Rate limiting pendiente

## 📚 Referencias

- [TailwindCSS Documentation](https://tailwindcss.com/docs)
- [Spring Security Configuration](https://docs.spring.io/spring-security/reference/index.html)
- [Thymeleaf Fragments](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#template-layout)
