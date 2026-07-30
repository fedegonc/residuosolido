# UI Layouts

## Layout único: `app-layout.html`

El proyecto tiene **un solo layout**: `fragments/app-layout.html`.

### Qué provee

- `<head>`: meta tags, CSRF, fuentes Google (Inter), `app.css`, FontAwesome, flag-icons
- Navbar: `navbar-app` si autenticado, `navbar-guest` si anónimo
- `<main>`: contenedor flexible donde se inyecta el contenido
- Footer: diferenciado autenticado vs anónimo

### Cómo se usa

```html
<html xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{fragments/app-layout}">
<head>
    <title>Mi Página</title>
</head>
<body>
<main layout:fragment="content">
    <!-- contenido de la página -->
</main>
</body>
</html>
```

### Reglas

- **Toda página** debe usar `layout:decorate="~{fragments/app-layout}"`
- **No crear layouts adicionales**. Si necesitas una variante, usa clases en el contenido
- El `<title>` se setea en el `<head>` del template hijo
- El navbar se selecciona automáticamente según el estado de autenticación

### Páginas sin navbar

Las páginas de error (`error/error.html`) pueden no usar el layout si muestran contenido minimal. En ese caso, incluyen `app.css` manualmente.

### Páginas públicas vs autenticadas

No hay distinción a nivel de layout. La diferencia la da el navbar:
- **Autenticado**: `navbar-app` con menú de usuario, logout, dashboard
- **Anónimo**: `navbar-guest` con login, registro, links públicos

El layout maneja esto con `sec:authorize`:

```html
<th:block sec:authorize="isAuthenticated()" th:replace="~{fragments/navbar-app :: navbar}"></th:block>
<th:block sec:authorize="isAnonymous()" th:replace="~{fragments/navbar-guest :: navbar}"></th:block>
```
