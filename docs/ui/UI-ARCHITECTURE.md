# UI Architecture

> **Objetivo:** Un desarrollador sin conocer el proyecto puede crear una pantalla nueva en 20–30 minutos consultando esta guía y `/ui-showcase`, sin copiar código ni hacer preguntas.

## Filosofía

El sistema usa **una sola hoja de estilos** (`app.css`) con clases utilitarias y componentes. No hay Tailwind, no hay CSS inline, no hay bloques `<style>` en templates.

```
Application
    ↓
Layout          (app-layout.html)
    ↓
Page            (.page)
    ↓
Section         (.card)
    ↓
Fragment        (fragments/components/*)
    ↓
Control         (.btn, .input, .badge, .select)
```

## Capas

### 1. Application

El servidor Spring Boot renderiza templates Thymeleaf. Cada template declara su layout:

```html
<html xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{fragments/app-layout}">
```

### 2. Layout

Un solo layout: `fragments/app-layout.html`. Provee:
- `<head>` con metadatos, fuentes, `app.css`, FontAwesome
- Navbar (autenticado o guest según `sec:authorize`)
- `<main>` donde se inyecta el contenido
- Footer

El contenido se inyecta con:

```html
<main layout:fragment="content">
    <!-- tu página va aquí -->
</main>
```

### 3. Page

Toda página comienza con `.page`. Esto centra el contenido y aplica padding horizontal.

```html
<div class="page">
    <!-- secciones -->
</div>
```

Variantes:
- `.page` — ancho completo (80rem)
- `.page--narrow` — 40rem (formularios, páginas de detalle)
- `.page--wide` — 60rem (dashboards, listas)

### 4. Section

Las secciones son `.card`. Una card es un contenedor blanco con borde y sombra.

```html
<div class="card">
    <div class="card__header">
        <h2 class="card__title">Título</h2>
    </div>
    <div class="card__body">
        <!-- contenido -->
    </div>
</div>
```

Variantes:
- `.card` — padding 1rem
- `.card--flush` — sin padding (para listas, tablas)
- `.card--lg` — padding 1.5rem

### 5. Fragment

Componentes reutilizables en `fragments/components/`. Se invocan con `th:replace`:

```html
<div th:replace="~{fragments/components/alerts :: success(${msg})}"></div>
```

Catálogo completo en [UI-COMPONENTS.md](UI-COMPONENTS.md).

### 6. Control

Controles individuales: botones, inputs, selects, badges, links.

```html
<button class="btn btn--green btn--sm">Guardar</button>
<input class="input" type="text" />
<span class="badge badge--green">Activo</span>
```

## Qué puede contener una página

| Elemento           | Clase              | Obligatorio |
|--------------------|--------------------|-------------|
| Contenedor         | `.page`            | Sí          |
| Header de página   | `.page-header`     | Recomendado |
| Sección            | `.card`            | Sí          |
| Grid de secciones  | `.grid .grid--2`   | Opcional    |
| Alertas            | fragment `alerts`  | Opcional    |
| Formulario         | `.form`            | Opcional    |
| Tabla              | `.table`           | Opcional    |
| Empty state        | fragment `empty-state` | Opcional |

## Qué NO puede contener una página

| Prohibido                    | En su lugar usar         |
|------------------------------|--------------------------|
| `style="..."`                | Clase de `app.css`       |
| `<style>` block              | Clase en `app.css`       |
| Tailwind CDN (`tailwindcss`) | `app.css`                |
| Lucide icons (`data-lucide`) | FontAwesome (`fa-solid`) |
| Colores hex inline            | Variables CSS (`var(--green-600)`) |
| `<div class="bg-white ...">` | `.card`                  |

## Estructura de carpetas

```
templates/
├── fragments/
│   ├── app-layout.html          # Layout único
│   ├── navbar-app.html          # Navbar autenticado
│   ├── navbar-guest.html        # Navbar visitante
│   ├── lang-switch.html         # Selector de idioma
│   ├── auth/
│   │   └── auth-fragment.html   # Fragmentos de auth
│   └── components/
│       ├── alerts.html          # success, error, warning, info, flash
│       ├── empty-state.html     # state(icon, title, text)
│       ├── form-actions.html    # actions(submitText, submitIcon, cancelUrl, cancelText)
│       ├── info-box.html        # box(icon, title, text, variant), list(...)
│       ├── page-header.html     # header(title, subtitle)
│       └── status-badge.html    # badge(status)
├── auth/
│   ├── login.html
│   └── register.html
├── org/
│   ├── catadores.html
│   ├── complete-profile.html
│   ├── dashboard.html
│   ├── manual-intake.html
│   ├── profile.html
│   └── requests.html
├── users/
│   ├── dashboard.html
│   ├── profile.html
│   ├── request-detail.html
│   ├── request-form.html
│   ├── requests.html
│   └── track.html
├── error/
│   └── error.html
├── index.html
└── metrics.html

static/css/
└── app.css                      # Única hoja de estilos
```

## Template starter

Copiar este esqueleto para crear una página nueva:

```html
<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{fragments/app-layout}">
<head>
    <title th:text="#{page.title}">Título</title>
</head>
<body>
<main layout:fragment="content">
    <div class="page page--narrow">
        <div th:if="${successMessage}" th:replace="~{fragments/components/alerts :: success(${successMessage})}"></div>
        <div th:if="${errorMessage}" th:replace="~{fragments/components/alerts :: error(${errorMessage})}"></div>

        <div th:replace="~{fragments/components/page-header :: header('Título', 'Subtítulo')}"></div>

        <div class="card">
            <div class="card__body">
                <!-- contenido -->
            </div>
        </div>
    </div>
</main>
</body>
</html>
```
