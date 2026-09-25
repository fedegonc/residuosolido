# Patrones de Bugs Visuales/Performance — Guía de Prevención

Análisis de categorías de bugs visuales y performance que típicamente ocurren en aplicaciones web.
Este documento busca PREVENIR descubrimientos casuales de bugs en producción.

---

## 🎯 Categorías de Bugs Visuales (CWV — Core Web Vitals)

### 1. **Layout Shift (CLS)** — Cumulative Layout Shift

**Síntoma:** Elementos se mueven/redimensionan durante la carga.

#### Patrón A: Fuentes web cargando async (✅ FIJO)
```
❌ ANTES:
  font-display: swap → cambio abrupto cuando llega
  Duración: 0.5-2s visible

✅ DESPUÉS:
  font-display: fallback + size-adjust
  + preload en <head>
```
**Ubicación:** `fonts.css`, `base.html`
**Severidad:** CRÍTICA (vista cada reload)

---

#### Patrón B: Imágenes sin dimensiones reservadas
```
❌ RIESGO ACTUAL:
  <img src="..." class="img-detail">
  sin width/height → navegador no sabe tamaño

✅ SOLUCIÓN:
  <!-- Opción 1: Inline en HTML -->
  <img src="..." class="img-detail" width="800" height="600">
  
  <!-- Opción 2: CSS aspect-ratio (moderno) -->
  .img-detail { aspect-ratio: 4/3; object-fit: cover; }
```

**Archivos con riesgo:**
- `templates/org/requests.html` — `<img th:src="${request.imageUrl}">`
- `templates/users/request-detail.html` — idem
- `templates/users/request-form.html` — idem
- `templates/public/index.html` — `<img class="hero__img">`

**Severidad:** MEDIA (solo si imágenes cargan lento)

---

#### Patrón C: classList.toggle/remove/add en startup
```
❌ RIESGO:
  document.querySelectorAll('.x').forEach(el => {
    el.classList.remove('is-hidden');  // ← modificación immediate
  });

✅ SOLUCIÓN:
  Use requestAnimationFrame o batch updates:
  requestAnimationFrame(() => {
    document.querySelectorAll('.x').forEach(el => {
      el.classList.remove('is-hidden');
    });
  });
```

**Ubicación:** `app.js` — PWA install button toggle
**Severidad:** BAJA (elementos específicos)
**Estado:** ✅ Ya documentado en código

---

#### Patrón D: Modales/Overlays que afectan scroll
```
❌ RIESGO:
  .modal-open { overflow: hidden; }  // ← Quita scrollbar
  Cuando modal abre, página "salta" porque scrollbar desaparece

✅ SOLUCIÓN:
  .modal-open {
    overflow: hidden;
    scrollbar-gutter: stable;  /* Reserva espacio para scrollbar */
  }
```

**Ubicación:** `app.css` — modal handling
**Severidad:** BAJA (UX, no contenido)
**Estado:** ⚠️ SIN FIX (revisar si .modal-open existe)

---

### 2. **Largest Contentful Paint (LCP)** — Tiempo del contenido principal

**Síntoma:** Página tarda en mostrar contenido principal.

#### Patrón A: Imágenes hero sin lazy loading
```
❌ RIESGO:
  <img src="/images/hero/plaza.jpg" class="hero__img">
  Es la imagen GRANDE principal, debe cargar prioridad

✅ SOLUCIÓN:
  <img src="/images/hero/plaza.jpg" class="hero__img" 
       fetchpriority="high">
```

**Ubicación:** `templates/public/index.html`
**Severidad:** MEDIA (home page = más tráfico)

---

#### Patrón B: CSS critical path bloqueante
```
❌ RIESGO:
  <link rel="stylesheet" href="/css/app.css">
  <link rel="stylesheet" href="/css/fonts.css">
  Ambos cargan en serie

✅ SOLUCIÓN:
  <!-- CSS crítico inline en <head> -->
  <style>
    /* Reset + layout base — 2KB */
    :root { ... }
    * { box-sizing: border-box; }
    body { ... }
  </style>
  <link rel="stylesheet" href="/css/app.css">
```

**Ubicación:** `base.html`
**Severidad:** MEDIA (puede diferir)
**Estado:** ⏳ Puede optimizarse futuramente

---

#### Patrón C: Fonts que bloquean LCP
```
✅ YA FIJO:
  preload + font-display: fallback
```

---

### 3. **First Input Delay (FID) / Interaction to Next Paint (INP)**

**Síntoma:** Página lenta en responder a clicks/inputs.

#### Patrón A: Event listeners que bloquean (long tasks)
```
❌ RIESGO:
  document.querySelectorAll('.modal').forEach(el => {
    el.addEventListener('click', function() {
      // Código pesado: DOM manipulation, calculations
      for (let i = 0; i < 100000; i++) { ... }
    });
  });

✅ SOLUCIÓN:
  el.addEventListener('click', function() {
    setTimeout(() => {
      // Pesado en background
    }, 0);
  });
  // O usar requestIdleCallback para tareas no-urgentes
```

**Ubicación:** `app.js`, `request-form.js`, `org-profile.js`
**Severidad:** BAJA (scripts están optimizados)

---

#### Patrón B: Fetch sin loading state
```
❌ RIESGO:
  fetch('/solicitudes/org-options?ciudad=' + ciudad.value)
    .then(r => r.json())
    .then(data => {
      // Usuario espera sin feedback visual
    });

✅ SOLUCIÓN:
  function fetchOrgs(ciudad) {
    showLoadingSpinner();  // Visual feedback
    return fetch(...)
      .then(r => r.json())
      .then(data => {
        updateUI(data);
        hideLoadingSpinner();
      })
      .catch(err => showError(err));
  }
```

**Ubicación:** `request-form.js` — org options fetch
**Severidad:** MEDIA (UX feedback)
**Estado:** ⚠️ Revisar si hay loading state visual

---

### 4. **Cumulative Layout Shift (CLS)** — Cambios inesperados

**Síntoma:** Ya cubierto en Patrón A.

#### Patrón: Z-index stacking context confusion
```
❌ RIESGO:
  .modal { z-index: 100; }
  .navbar { z-index: 100; }  ← Conflicto
  .modal__close { z-index: 101; }
  
Orden en HTML determina apariencia, no z-index

✅ SOLUCIÓN:
  /* Definir jerarquía clara */
  .navbar { z-index: 100; }
  .modal { z-index: 200; }
  .modal__close { z-index: 300; }
  .toast { z-index: 400; }
```

**Ubicación:** `app.css`
**Severidad:** BAJA (visual, no funcional)
**Estado:** ✅ Parece OK (navbar 100, modal 400, toast 400)

---

## 🛠️ Checklist de Prevención

### Para cada página nueva:

```
RECURSOS:
  [ ] ¿Hay <img>? → Tiene width/height o aspect-ratio CSS
  [ ] ¿Hay <iframe>? → Tiene width/height
  [ ] ¿Hay <video>? → Tiene preload/poster
  [ ] ¿CSS externo? → Carga en <head>, antes que HTML
  [ ] ¿Fuentes web? → Usa preload + font-display fallback

JAVASCRIPT:
  [ ] ¿Modifica DOM al startup? → Usa requestAnimationFrame
  [ ] ¿classList.toggle/remove? → Batched en un frame
  [ ] ¿Fetch/API call? → Tiene loading state visual
  [ ] ¿Event listener pesado? → Usa setTimeout/requestIdleCallback

LAYOUTS:
  [ ] ¿Modal/overlay? → Reserva scrollbar-gutter: stable
  [ ] ¿Animaciones? → No causan reflow (usa transform/opacity)
  [ ] ¿Grid/flexbox dinámico? → Dimensiones predefinidas

Z-INDEX:
  [ ] ¿Múltiples z-index? → Jerarquía documentada
  [ ] ¿Elemento se ve tapado? → Revisar contexto de stacking
```

---

## 🔍 Cómo Detectar Estos Bugs

### En Development:

```bash
# 1. Chrome DevTools → Performance
mvn spring-boot:run &
open http://localhost:8080
# Recargar página, ver recordings en DevTools Performance

# 2. Lighthouse (auditoría integrada)
chrome://lighthouse

# 3. WebPageTest
https://www.webpagetest.org

# 4. Core Web Vitals en real
chrome://flags → Enable Core Web Vitals display
```

### En Producción:

```javascript
// Medir CLS en real
let cls = 0;
const observer = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    if (!entry.hadRecentInput) {
      cls += entry.value;
    }
  }
  console.log('CLS:', cls);
});
observer.observe({type: 'layout-shift', buffered: true});
```

---

## 📋 Auditoría de Archivos Actuales

### ✅ Estado Actual (Post-Fixes)

| Patrón | Ubicación | Estado | Acción |
|--------|-----------|--------|--------|
| Font loading | `fonts.css`, `base.html` | ✅ FIJO | Preload + fallback |
| Traducciones sync | `app.js` | ✅ FIJO | requestAnimationFrame |
| Imágenes sin dims | `*.html` | ⚠️ RIESGO | Ver Patrón B |
| PWA button toggle | `app.js` | ✅ OK | Ya batched |
| Modal scroll shift | `app.css` | ⏳ REVISAR | scrollbar-gutter |
| Fetch sin loading | `request-form.js` | ⏳ REVISAR | Agregar spinner |

---

## 🚀 Próximos Pasos (Orden de Impacto)

### 1. INMEDIATO (Riesgo CRÍTICA)
```
[x] font-display fallback — HECHO
[ ] scrollbar-gutter en modales — RÁPIDO, 2 líneas CSS
```

### 2. CORTO PLAZO (MEDIA)
```
[ ] aspect-ratio en imágenes — 5 min CSS
[ ] Fetch loading state — 10 min JS
[ ] Revisar z-index conflicts — 5 min audit
```

### 3. FUTURO (OPTIMIZACIÓN)
```
[ ] Critical CSS inline — análisis de impacto
[ ] LCP optimization — medir actual primero
[ ] Long task detection — monitoring
```

---

## 📚 Referencias

- **Core Web Vitals:** https://web.dev/vitals/
- **Layout Shift:** https://web.dev/cls/
- **Font Loading:** https://web.dev/fonts/
- **Image Optimization:** https://web.dev/image-optimization/
- **Performance:** https://web.dev/performance/

---

## 🎯 Conclusión

**Regla de Oro:**
```
Cualquier recurso que carga async (fuente, imagen, script, API)
DEBE tener:
  1. Fallback inmediato (sistema font, placeholder, etc.)
  2. Dimensión reservada (width/height o aspect-ratio)
  3. Loading state visual (si es fetch)
  4. Transición suave (sin Layout Shift)
```

**Patrón Simple:**
```
Lo que el usuario ve primero:
  ↓ NO cambia tamaño/posición cuando llegan recursos async
  ↓ Tiene feedback visual si hay espera
  ↓ ES accesible sin JS
```
