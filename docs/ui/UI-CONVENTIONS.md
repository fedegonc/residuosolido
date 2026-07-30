# UI Conventions

## Reglas obligatorias

### 1. Nunca usar `style=""`

**Prohibido:**
```html
<div style="padding:1rem;margin-bottom:1rem;">
```

**En su lugar:**
```html
<div class="card mb-4">
```

**Excepción única:** `timeline__progress` donde el ancho es dinámico (calculado por Thymeleaf con `th:style`). Los colores de step-dot y step-label usan clases CSS (`.timeline__step-dot--yellow`, `.timeline__step-label--red`, etc.).

### 2. Nunca usar bloques `<style>`

**Prohibido:**
```html
<style>
.metric { background: var(--gray-50); }
</style>
```

**En su lugar:** Agregar la clase a `app.css` con su sección comentada.

### 3. Nunca usar Tailwind CDN

**Prohibido:**
```html
<script src="https://cdn.tailwindcss.com"></script>
```

El sistema usa `app.css` exclusivamente. Tailwind no está disponible.

### 4. Nunca usar Lucide icons

**Prohibido:**
```html
<i data-lucide="check"></i>
```

**En su lugar:**
```html
<i class="fa-solid fa-check"></i>
```

### 5. Nunca crear un botón sin `.btn`

**Prohibido:**
```html
<a class="bg-emerald-600 text-white px-4 py-2 rounded">Guardar</a>
```

**En su lugar:**
```html
<a class="btn btn--green">Guardar</a>
```

### 6. Toda página comienza con `.page`

**Prohibido:**
```html
<div class="max-w-4xl mx-auto px-4 py-8">
```

**En su lugar:**
```html
<div class="page page--narrow">
```

### 7. Toda sección usa `.card`

**Prohibido:**
```html
<div class="bg-white rounded-lg shadow border border-gray-200">
```

**En su lugar:**
```html
<div class="card">
```

### 8. No duplicar HTML — usar fragmentos

**Prohibido:**
```html
<div class="bg-green-50 border border-green-200 ...">
    <i data-lucide="check-circle"></i>
    <span th:text="${msg}"></span>
</div>
```

**En su lugar:**
```html
<div th:replace="~{fragments/components/alerts :: success(${msg})}"></div>
```

### 9. Iconos con FontAwesome

Usar `fa-solid` (solid style). No mezclar `fa-regular` o `fa-brands` salvo necesidad específica.

### 10. i18n en todo texto visible

Todo texto visible debe usar `th:text="#{key}"` con su entrada en `messages_es.properties` y `messages_pt.properties`.

**Prohibido:**
```html
<h1>Mis Solicitudes</h1>
```

**En su lugar:**
```html
<h1 th:text="#{user.requests.title}">Mis Solicitudes</h1>
```

### 11. CSRF en todo formulario POST

```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

### 12. No inventar clases

Si necesitas una clase que no existe en `app.css`, agrégala con su sección comentada al final del archivo. No uses clases ad-hoc en el HTML.

### 13. Páginas de auth usan `.auth-page` + `.auth-card`

**Prohibido:**
```html
<div class="min-h-[calc(100vh-9rem)] flex items-center justify-center px-4 py-6">
    <div class="w-full max-w-xl bg-white rounded-xl shadow-md border border-emerald-100 p-6">
```

**En su lugar:**
```html
<div class="auth-page">
    <div class="auth-card">
```

### 14. Password toggles usan `.password-field`

**Prohibido:**
```html
<div class="relative" data-password-container>
    <input ... class="w-full px-3 py-2 pr-10 border border-gray-300 rounded-md">
    <button class="absolute inset-y-0 right-3 ...">
```

**En su lugar:**
```html
<div class="password-field" data-password-container>
    <input ... class="input">
    <button class="password-field__toggle" ...>
```

## Nomenclatura

| Patrón         | Ejemplo              | Uso                    |
|----------------|----------------------|------------------------|
| `.component`   | `.card`, `.btn`      | Componente base        |
| `.component--variant` | `.card--flush`, `.btn--green` | Variante       |
| `.component__part` | `.card__body`, `.btn__icon` | Sub-elemento     |
| `.u-utility`   | (no usado aún)       | Utilidad pura          |

## Orden de clases

1. Clase base del componente (`.card`, `.btn`, `.badge`)
2. Variante (`--flush`, `--green`, `--sm`)
3. Utilidades (`mb-4`, `flex`, `gap-2`)

```html
<!-- Correcto -->
<div class="card card--flush mb-6">
<button class="btn btn--green btn--sm">
```

## Checklist para nueva pantalla

- [ ] Usa `layout:decorate="~{fragments/app-layout}"`
- [ ] Comienza con `.page` (o `.page--narrow` / `.page--wide`)
- [ ] Sin `style=""` (salvo `th:style` en `timeline__progress`)
- [ ] Sin `<style>` block
- [ ] Sin Tailwind
- [ ] Sin Lucide
- [ ] Usa fragmentos para alerts, headers, empty states
- [ ] Todo texto con `th:text="#{...}"`
- [ ] Forms con CSRF token
- [ ] Botones con `.btn` + variante
- [ ] Secciones con `.card`
- [ ] Si es auth: usa `.auth-page` + `.auth-card`
- [ ] Si tiene password: usa `.password-field` + `.password-field__toggle`
- [ ] Iconos con `aria-hidden="true"`
- [ ] Labels asociados a inputs con `for`/`id`
- [ ] Alertas con `role="status"` o `role="alert"` + `aria-live`
- [ ] Modales con `role="dialog"`, `aria-modal="true"`, Escape key, focus management
- [ ] Breadcrumbs con `nav th:replace="~{fragments/components/breadcrumbs :: breadcrumbs(${breadcrumbs})}"`
- [ ] SVGs decorativos con `aria-hidden="true"`

## Accesibilidad

### Iconos
Todos los iconos FontAwesome y SVGs decorativos deben llevar `aria-hidden="true"` para que los screen readers los ignoren.

### Alertas
- Success/warning/info: `role="status"` + `aria-live="polite"`
- Error: `role="alert"` + `aria-live="assertive"`

### Modales
- `role="dialog"` + `aria-modal="true"` + `aria-labelledby` apuntando al título
- Cerrar con Escape key
- Focus al botón principal al abrir
- Click en overlay cierra el modal

### Formularios
- Todo `<input>` debe tener un `<label>` asociado con `for`/`id`
- Botones de toggle (password) llevan `aria-pressed` y `aria-label`

### Skip link
El layout incluye un skip-link (`Saltar al contenido`) que aparece al recibir focus por teclado. No remover.
