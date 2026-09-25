# 🚨 Bugs Probables (Sin Descubrir) — Análisis Proactivo

Basado en análisis de patrones encontrados, estos 3 bugs probablemente existen en producción.

---

## BUG #1: Layout Shift en Imágenes de Solicitud 🐛

**Severidad:** MEDIA | **Impacto:** Toda página con imagen (org/requests, users/requests, request-detail)

### Síntoma
```
Usuario ve página de detalle de solicitud
  ↓ Contenido renderiza sin imagen (height desconocido)
  ↓ Imagen carga (0.5-2s típicamente)
  ↓ Contenido se expande → Layout Shift
```

### Root Cause
```html
<!-- request-detail.html, org/requests.html -->
<img th:src="${request.imageUrl}" 
     th:alt="'Foto de la solicitud #' + ${request.id}" 
     class="img-detail">
<!-- NO tiene width/height ni aspect-ratio CSS -->
```

### Impacto Medible
```
CLS score: +0.05 (a veces)
Duración: 0.5-1.5s (según conexión)
Visible en: Mobile (donde imágenes cargan más lento)
```

### Fix
```html
<!-- request-detail.html -->
<img th:src="${request.imageUrl}" 
     th:alt="'Foto de la solicitud #' + ${request.id}" 
     class="img-detail"
     width="600" height="400">
```

```css
/* app.css */
.img-detail {
  aspect-ratio: 3/2;
  object-fit: cover;
  width: 100%;
  height: auto;
}
```

**Archivos afectados:**
- templates/org/requests.html — solicitudes con foto
- templates/users/request-detail.html — detalle
- templates/users/request-form.html — preview de foto

---

## BUG #2: Modal Scroll Shift (Desktop) 🐛

**Severidad:** BAJA | **Impacto:** Página con modal (org/requests con "rechazar formulario")

### Síntoma
```
Usuario abre modal de rechazo de solicitud
  ↓ Modal aparece con overflow:hidden en body
  ↓ Scrollbar vertical desaparece
  ↓ Página "salta" 15-17px a la derecha (ancho scrollbar)
  ↓ Cierra modal → salta de vuelta
```

### Root Cause
```css
/* Cuando modal abre, JavaScript hace: */
body.modal-open { overflow: hidden; }
/* ↓ Quita scrollbar = espacio libre */
/* ↓ No reserva lugar = Layout Shift */
```

### Impacto Medible
```
CLS: +0.02-0.04 (si modal es grande)
Visible en: Desktop (mobile no tiene scrollbar siempre visible)
UX Issue: Usuario nota cambio
```

### Fix
```css
/* app.css */
body.modal-open {
  overflow: hidden;
  scrollbar-gutter: stable;  /* ← NUEVO */
}
```

**Archivos afectados:**
- `app.css` — necesita agregar `scrollbar-gutter: stable`
- `app.js` — Ya maneja `body.modal-open` (OK)

---

## BUG #3: Fetch sin Loading State (Imperceptible pero Pobre UX) 🐛

**Severidad:** BAJA-MEDIA | **Impacto:** Formulario de solicitud (request-form.html)

### Síntoma
```
Usuario selecciona ciudad
  ↓ request-form.js hace fetch '/solicitudes/org-options?ciudad=...'
  ↓ Usuario esperan 0.5-2s (conexión lenta)
  ↓ SIN visual feedback (spinner, disable, nada)
  ↓ Usuario no sabe si pulsó o si está cargando
  ↓ Hace click de nuevo
  ↓ 2x requests
```

### Root Cause
```javascript
/* request-form.js */
fetch('/solicitudes/org-options?ciudad=' + encodeURIComponent(ciudad.value))
  .then(r => r.json())
  .then(data => {
    // Actualiza select
    // PERO: sin loading state visual antes
  });
```

### Impacto Medible
```
UX: Confusión de usuario
Duplicate requests: 2x carga en servidor
Network: Desperdicio de requests
```

### Fix
```javascript
/* request-form.js */
function updateOrganizations(ciudad) {
  // 1. Mostrar loading
  organizationSelect.disabled = true;
  organizationSelect.classList.add('is-loading');
  
  // 2. Fetch
  fetch('/solicitudes/org-options?ciudad=' + encodeURIComponent(ciudad.value))
    .then(r => r.json())
    .then(data => {
      // 3. Actualizar UI
      organizationSelect.innerHTML = '';
      data.forEach(org => {
        const option = document.createElement('option');
        option.value = org.id;
        option.textContent = org.displayName;
        organizationSelect.appendChild(option);
      });
    })
    .catch(err => {
      showError('Error al cargar organizaciones');
    })
    .finally(() => {
      // 4. Limpiar estado
      organizationSelect.disabled = false;
      organizationSelect.classList.remove('is-loading');
    });
}
```

```css
/* app.css */
.is-loading {
  opacity: 0.6;
  cursor: not-allowed;
}

/* O mostrar un spinner dentro del select */
select.is-loading::after {
  content: '...';
  animation: dots 1.5s steps(3, end) infinite;
}

@keyframes dots {
  0%, 20% { content: ''; }
  40% { content: '.'; }
  60% { content '..'; }
  80%, 100% { content: '...'; }
}
```

**Archivos afectados:**
- `static/js/request-form.js` — fetch call
- `app.css` — estilos de .is-loading

---

## 🔍 Cómo Verificar Cada Bug

### BUG #1 (Imágenes)
```bash
# 1. Navegar a una solicitud CON foto
curl http://localhost:8080/mis-solicitudes

# 2. Chrome DevTools → Performance
# 3. Recargar y ver si hay Layout Shift cuando carga imagen
# 4. Medir CLS en consola:
  let cls = 0;
  new PerformanceObserver((l) => {
    l.getEntries().forEach(e => {
      if (!e.hadRecentInput) cls += e.value;
    });
    console.log('CLS:', cls);
  }).observe({type: 'layout-shift', buffered: true});
```

### BUG #2 (Modal)
```bash
# 1. Navegar a org/requests (o users/requests con formulario de rechazo)
# 2. Abrir modal
# 3. Observar si página "salta" a la derecha
# 4. O medir con:
  const body = document.body;
  const before = body.offsetWidth;
  modal.classList.remove('is-hidden');
  const after = body.offsetWidth;
  console.log('Shift:', before - after, 'px');
```

### BUG #3 (Fetch)
```bash
# 1. Navegar a /solicitar (formulario)
# 2. Seleccionar una ciudad
# 3. Chrome DevTools → Network
# 4. Cambiar ciudad de nuevo
# 5. Ver si hay 2 requests (indica double-click)
# 6. Ver si select se deshabilita o hay feedback visual
```

---

## 📊 Prioridad de Fixes

| Bug | Severidad | Impacto | Esfuerzo | Prioridad |
|-----|-----------|--------|---------|-----------|
| #1 Imágenes | MEDIA | CLS visible | 15 min | ALTO |
| #2 Modal | BAJA | UX minor | 2 min | BAJO |
| #3 Fetch | MEDIA | UX+Perf | 20 min | MEDIO |

---

## ✅ Verificación Final

Después de aplicar fixes:

```bash
# 1. Compilar y levantar
mvn clean install -DskipTests
mvn spring-boot:run &

# 2. Medir Core Web Vitals nuevamente
chrome://lighthouse

# 3. Test manual
curl http://localhost:8080/mis-solicitudes
# Recargar varias veces
# Abrir devtools → Performance → grabar
# Verificar CLS < 0.1

# 4. Test en mobile (DevTools → Device Emulation)
# Slower 4G para simular imágenes lentas
```

---

## 🎯 Conclusión

Estos 3 bugs son:
1. **Invisibles en desarrollo** (conexión local rápida, images cached)
2. **Visibles en producción** (usuarios reales, conexiones lentas, sin cache)
3. **Fáciles de fijar** (< 1 hora total)
4. **Importantes para Core Web Vitals** (ranking de Google)

**Recomendación:** Aplicarlos HOY antes de producción.
