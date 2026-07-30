# UI Components

Catálogo de componentes disponibles en `app.css` y fragmentos Thymeleaf.

---

## Botones

### Clases

| Clase          | Descripción              |
|----------------|--------------------------|
| `.btn`         | Base (requerida)         |
| `.btn--primary`| Azul                     |
| `.btn--green`  | Verde (acción principal) |
| `.btn--amber`  | Ámbar                    |
| `.btn--danger` | Rojo (eliminar)          |
| `.btn--ghost`  | Blanco con borde         |
| `.btn--sm`     | Tamaño pequeño           |
| `.btn--lg`     | Tamaño grande            |
| `.btn--block`  | Ancho completo           |

### Ejemplo

```html
<button class="btn btn--green">Guardar</button>
<a class="btn btn--ghost btn--sm" href="/cancel">Cancelar</a>
<button class="btn btn--danger btn--sm">
    <i class="fa-solid fa-trash"></i>
    <span>Eliminar</span>
</button>
```

### Reglas

- Todo botón usa `.btn` + variante de color
- Iconos con `fa-solid` dentro del botón
- Texto del botón en `<span>` (para spacing correcto)

---

## Cards

### Clases

| Clase           | Descripción                    |
|-----------------|--------------------------------|
| `.card`         | Base con padding 1rem          |
| `.card--flush`  | Sin padding (listas, tablas)   |
| `.card--lg`     | Padding 1.5rem                 |
| `.card__header` | Header con borde inferior      |
| `.card__title`  | Título dentro del header       |
| `.card__body`   | Body con padding 1.5rem        |
| `.card__bar`    | Barra superior verde            |
| `.card__bar--blue` | Barra superior azul         |

### Ejemplo

```html
<div class="card">
    <div class="card__header">
        <h2 class="card__title">Título de la card</h2>
        <button class="btn btn--ghost btn--sm">Acción</button>
    </div>
    <div class="card__body">
        <p>Contenido de la card.</p>
    </div>
</div>
```

---

## Formularios

### Clases

| Clase            | Descripción                    |
|------------------|--------------------------------|
| `.form`          | Grid vertical con gap 1rem     |
| `.form__row`     | Fila de campos                 |
| `.form__row--2`  | Fila de 2 columnas (responsive)|
| `.field`         | Campo con label + input        |
| `.field__label`  | Label del campo                |
| `.field__hint`   | Texto de ayuda                 |
| `.field__error`  | Mensaje de error               |
| `.input`         | Input de texto                 |
| `.select`        | Select                         |
| `.textarea`      | Textarea                       |
| `.input--sm`     | Input pequeño                  |
| `.input--file`   | Input type=file                |
| `.input--readonly` | Input readonly              |
| `.input--invalid`  | Input con error              |

### Alias compatibles

`.form__group`, `.form__label`, `.form__input`, `.form__select`, `.form__textarea` — equivalentes a `.field`, `.field__label`, `.input`, `.select`, `.textarea`.

### Ejemplo

```html
<form class="form" method="post">
    <div class="field">
        <label class="field__label">Nombre</label>
        <input class="input" type="text" name="name" required />
        <p class="field__hint">Tu nombre completo.</p>
    </div>
    <div class="form__row form__row--2">
        <div class="field">
            <label class="field__label">Email</label>
            <input class="input" type="email" name="email" />
        </div>
        <div class="field">
            <label class="field__label">Teléfono</label>
            <input class="input" type="tel" name="phone" />
        </div>
    </div>
    <button type="submit" class="btn btn--green">Guardar</button>
</form>
```

---

## Tablas

### Clases

| Clase                | Descripción              |
|----------------------|--------------------------|
| `.table`             | Base                     |
| `.table--striped`    | Filas alternadas         |
| `.table-wrapper`     | Scroll horizontal        |
| `.table__row--total` | Fila de totales (negrita)|
| `.text-right`        | Alinear celda a la derecha|

### Ejemplo

```html
<div class="table-wrapper">
    <table class="table">
        <thead>
            <tr>
                <th>Material</th>
                <th class="text-right">Cantidad</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Plástico</td>
                <td class="text-right">120 kg</td>
            </tr>
            <tr class="table__row--total">
                <td>Total</td>
                <td class="text-right">120 kg</td>
            </tr>
        </tbody>
    </table>
</div>
```

---

## Badges

### Clases

| Clase             | Color  |
|-------------------|--------|
| `.badge`          | Base   |
| `.badge--green`   | Verde  |
| `.badge--blue`    | Azul   |
| `.badge--yellow`  | Ámbar  |
| `.badge--red`     | Rojo   |
| `.badge--gray`    | Gris   |
| `.badge--indigo`  | Índigo |

### Alias de estado

| Clase              | Equivale a        |
|--------------------|--------------------|
| `.badge--success`  | `.badge--green`   |
| `.badge--info`     | `.badge--blue`    |
| `.badge--warning`  | `.badge--yellow`  |
| `.badge--danger`   | `.badge--red`     |

### Ejemplo

```html
<span class="badge badge--green">
    <i class="fa-solid fa-check"></i>
    <span>Activo</span>
</span>
```

---

## Alertas (fragmento)

### Fragmentos disponibles

| Fragmento  | Uso                                  |
|------------|--------------------------------------|
| `success`  | `alerts :: success(${message})`      |
| `error`    | `alerts :: error(${message})`        |
| `warning`  | `alerts :: warning(${message})`      |
| `info`     | `alerts :: info(${message})`         |
| `flash`    | `alerts :: flash(${message}, ${type})` (auto-dismiss 5s) |

### Ejemplo

```html
<div th:if="${successMessage}"
     th:replace="~{fragments/components/alerts :: success(${successMessage})}"></div>
```

---

## Page Header (fragmento)

### Parámetros

| Parámetro | Tipo   | Obligatorio |
|-----------|--------|-------------|
| `title`   | String | Sí          |
| `subtitle`| String | No          |

### Ejemplo

```html
<div th:replace="~{fragments/components/page-header :: header('Mis Solicitudes', 'Gestiona tus recolecciones')}"></div>
```

---

## Info Box (fragmento)

### Parámetros

| Parámetro | Tipo         | Obligatorio | Valores                    |
|-----------|--------------|-------------|----------------------------|
| `icon`    | String       | Sí          | Nombre FA sin `fa-solid`   |
| `title`   | String       | No          | —                          |
| `text`    | String       | No          | —                          |
| `variant` | String       | No          | `blue`, `green`, `amber`, `gray` |

### Fragmentos

- `box(icon, title, text, variant)` — texto simple
- `list(icon, title, items, variant)` — lista de items

### Ejemplo

```html
<div th:replace="~{fragments/components/info-box :: box('circle-info', 'Importante', 'Los datos se guardan automáticamente.', 'blue')}"></div>
```

---

## Empty State (fragmento)

### Parámetros

| Parámetro | Tipo   | Obligatorio |
|-----------|--------|-------------|
| `icon`    | String | Sí          |
| `title`   | String | No          |
| `text`    | String | No          |

### Ejemplo

```html
<div th:replace="~{fragments/components/empty-state :: state('inbox', 'Sin solicitudes', 'No hay solicitudes para mostrar.')}"></div>
```

---

## Status Badge (fragmento)

### Parámetros

| Parámetro | Tipo   | Valores                                    |
|-----------|--------|--------------------------------------------|
| `status`  | Enum   | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `REJECTED` |

### Ejemplo

```html
<th:block th:replace="~{fragments/components/status-badge :: badge(${request.status})}"></th:block>
```

---

## Form Actions (fragmento)

### Parámetros

| Parámetro     | Tipo   | Default    |
|---------------|--------|------------|
| `submitText`  | String | "Guardar"  |
| `submitIcon`  | String | —          |
| `cancelUrl`   | String | —          |
| `cancelText`  | String | "Cancelar" |

### Ejemplo

```html
<div th:replace="~{fragments/components/form-actions :: actions('Guardar', 'floppy-disk', '/cancelar', 'Cancelar')}"></div>
```

---

## Stat Card

### Clases

| Clase              | Descripción           |
|--------------------|------------------------|
| `.stat`            | Card centrada          |
| `.stat__value`     | Número grande          |
| `.stat__label`     | Etiqueta en uppercase  |
| `.stat__icon`      | Icono circular         |
| `.stat__icon--green` | Variante verde       |
| `.stat__icon--blue`  | Variante azul        |
| `.stat__icon--amber` | Variante ámbar       |
| `.stat__icon--red`   | Variante rojo        |
| `.stat__icon--yellow`| Variante amarillo    |

### Ejemplo

```html
<div class="stat">
    <div class="stat__icon stat__icon--green"><i class="fa-solid fa-recycle"></i></div>
    <p class="stat__value">42</p>
    <p class="stat__label">Solicitudes</p>
</div>
```

---

## Grid

### Clases

| Clase       | Columnas (desktop) |
|-------------|---------------------|
| `.grid`     | 1                   |
| `.grid--2`  | 2                   |
| `.grid--3`  | 3                   |
| `.grid--4`  | 4                   |

Todas son responsive: 1 columna en móvil, N columnas desde 640px/768px.

---

## Hero Header

### Clases

| Clase                    | Descripción              |
|--------------------------|--------------------------|
| `.hero-header`           | Base                     |
| `.hero-header--green`    | Gradiente verde          |
| `.hero-header--blue`     | Gradiente azul           |
| `.hero-header__icon`     | Icono circular grande    |
| `.hero-header__body`     | Contenedor de título     |
| `.hero-header__title`    | Título 1.5rem            |
| `.hero-header__subtitle` | Subtítulo                |
| `.hero-header__stats`    | Contenedor de estadística|
| `.hero-header__stat-value` | Número grande         |
| `.hero-header__stat-label` | Etiqueta              |

---

## Timeline

### Clases

| Clase                      | Descripción              |
|----------------------------|--------------------------|
| `.timeline`                | Contenedor               |
| `.timeline__track`         | Línea de fondo           |
| `.timeline__progress`      | Línea de progreso        |
| `.timeline__step`          | Paso (25% ancho)         |
| `.timeline__step-dot`      | Círculo del paso         |
| `.timeline__step-dot--done`| Completado (verde)       |
| `.timeline__step-dot--active`| Activo (con color variant) |
| `.timeline__step-dot--pending`| Pendiente (gris)      |
| `.timeline__step-dot--yellow`| Fondo amarillo (pendiente) |
| `.timeline__step-dot--red`| Fondo rojo (rechazado) |
| `.timeline__step-dot--indigo`| Fondo índigo (en progreso) |
| `.timeline__step-dot--green`| Fondo verde (completado) |
| `.timeline__step-label`    | Texto del paso           |
| `.timeline__step-label--yellow`| Texto amarillo       |
| `.timeline__step-label--red`| Texto rojo             |
| `.timeline__step-label--gray`| Texto gris claro       |
| `.timeline__step-label--gray-dark`| Texto gris oscuro |
| `.timeline__step-label--indigo`| Texto índigo         |
| `.timeline__step-label--green`| Texto verde           |
| `.timeline__progress--demo`| Demo: width 33%, margin-left 8% |

### Ejemplo

```html
<div class="timeline">
    <div class="timeline__track"></div>
    <div class="timeline__progress timeline__progress--demo"></div>
    <div class="timeline__step">
        <div class="timeline__step-dot timeline__step-dot--done"><i class="fa-solid fa-check"></i></div>
        <span class="timeline__step-label">Creada</span>
    </div>
    <!-- más pasos... -->
</div>
```

> **Nota:** El `th:style` en `timeline__progress` es la única excepción permitida porque el ancho es dinámico (calculado por Thymeleaf). Los colores de los step-dot y step-label usan clases CSS, no inline styles.

---

## Modal

### Clases

| Clase              | Descripción                    |
|--------------------|--------------------------------|
| `.modal`           | Base (hidden por defecto)      |
| `.modal.is-open`   | Visible                        |
| `.modal__overlay`  | Fondo oscuro                   |
| `.modal__dialog`   | Caja del modal                 |
| `.modal__header`   | Header con título              |
| `.modal__title`    | Título                         |
| `.modal__close`    | Botón cerrar                   |
| `.modal__body`     | Contenido                      |
| `.modal__footer`   | Footer con botones             |

### Confirm modal simple

| Clase                | Descripción              |
|----------------------|--------------------------|
| `.modal-overlay`     | Overlay fijo             |
| `.modal-box`         | Caja centrada            |
| `.modal-body`        | Contenido centrado       |
| `.modal-icon`        | Icono circular           |
| `.modal-icon--danger`| Variante rojo            |

---

## Utilidades

### Display

| Clase          | Equivale a                      |
|----------------|---------------------------------|
| `.flex`        | `display:flex`                  |
| `.flex-between`| `flex + justify + align center` |
| `.flex-col`    | `flex-direction:column`         |
| `.flex-wrap`   | `flex-wrap:wrap`                |
| `.flex-1`      | `flex:1`                        |
| `.items-start` | `align-items:flex-start`        |
| `.min-w-0`     | `min-width:0`                   |
| `.is-hidden`   | `display:none !important`       |

### Spacing

| Clase   | Valor    |
|---------|----------|
| `.gap-1`| 0.25rem  |
| `.gap-2`| 0.5rem   |
| `.gap-3`| 0.75rem  |
| `.gap-4`| 1rem     |
| `.mt-1` | 0.25rem  |
| `.mt-2` | 0.5rem   |
| `.mt-4` | 1rem     |
| `.mb-2` | 0.5rem   |
| `.mb-4` | 1rem     |
| `.mb-6` | 1.5rem   |
| `.pb-4` | 1rem     |
| `.pb-6` | 1.5rem   |
| `.pt-6` | 1.5rem   |

### Tipografía

| Clase            | Valor           |
|------------------|-----------------|
| `.text-xs`       | 0.75rem         |
| `.text-sm`       | 0.875rem        |
| `.text-lg`       | 1.125rem        |
| `.text-xl`       | 1.25rem         |
| `.text-2xl`      | 1.5rem          |
| `.font-medium`   | font-weight:500 |
| `.font-semibold` | font-weight:600 |
| `.font-bold`     | font-weight:700 |
| `.text-center`   | text-align:center |
| `.text-right`    | text-align:right  |

### Colores de texto

| Clase            | Color           |
|------------------|-----------------|
| `.text-gray-500` | `var(--gray-500)` |
| `.text-gray-600` | `var(--gray-600)` |
| `.text-gray-900` | `var(--gray-900)` |

### Otros

| Clase              | Descripción              |
|--------------------|--------------------------|
| `.link`            | Link pequeño azul        |
| `.link--green`     | Link verde               |
| `.link--blue`      | Link azul                |
| `.link--danger`    | Link rojo                |
| `.section-divider` | Separador horizontal     |
| `.section-divider--lg` | Separador con más padding |
| `.filter-bar`      | Barra de filtros flex    |
| `.content-grid`    | Grid vertical gap 1rem   |
| `.content-grid--md`| Grid vertical gap 1.25rem |
| `.content-grid--sm`| Grid vertical gap 0.75rem |
| `.avatar`          | Avatar circular          |
| `.avatar--sm`      | Avatar pequeño           |
| `.avatar--lg`      | Avatar grande            |
| `.req`             | Asterisco rojo (requerido) |
| `.sr-only`         | Oculto para screen readers |
| `.font-normal`     | font-weight:400          |
| `.icon-green`      | color:var(--green-500)   |
| `.icon-green-dark` | color:var(--green-600)   |
| `.icon-amber`      | color:var(--yellow-800)  |
| `.text-green-dark` | color:var(--green-700)   |
| `.page--padded`    | padding-top/bottom:1rem  |
| `.page--padded-lg` | padding-top/bottom:3rem  |
| `.page--gap-sm`    | gap:0.75rem              |
| `.page--center`    | Página centrada (landing) |
| `.card--lg`        | padding:1.5rem           |
| `.card--shadow-lg` | box-shadow:var(--shadow-lg) |
| `.card__body--lg`  | padding:1.5rem           |
| `.grid--gap-lg`    | gap:1.5rem               |
| `.grid--gap-md`    | gap:1rem                 |
| `.grid--gap-sm`    | gap:0.5rem               |
| `.empty-state--lg` | padding:3rem             |
| `.empty-state--xl` | padding:4rem             |
| `.auth-page`       | Contenedor centrado auth |
| `.auth-card`       | Card para auth (max 36rem) |
| `.auth-card--narrow`| Card auth estrecha (28rem) |
| `.auth-card__header`| Header auth centrado    |
| `.auth-card__title`| Título auth 1.25rem      |
| `.auth-card__subtitle`| Subtítulo auth         |
| `.password-field`  | Contenedor input+toggle  |
| `.password-field__toggle`| Botón mostrar/ocultar |
| `.hero-header--flush`| Sin margin/border-radius |
| `.page-header__title--xl`| Título XL 2rem/800 |
| `.page-header__subtitle--lg`| Subtítulo LG 1.125rem |
| `.skip-link`       | Skip-to-content link (a11y) |
| `.alert--fade-out` | Animación de fade para flash alerts |

---

## Breadcrumbs

### Clases

| Clase                    | Descripción              |
|--------------------------|--------------------------|
| `.breadcrumbs`           | Contenedor nav           |
| `.breadcrumbs__item`     | Item individual          |
| `.breadcrumbs__link`     | Link a página anterior   |
| `.breadcrumbs__separator`| Separador (chevron)      |
| `.breadcrumbs__current`  | Página actual (no link)  |

### Uso

```html
<nav th:replace="~{fragments/components/breadcrumbs :: breadcrumbs(${breadcrumbs})}"></nav>
```

Controller: `model.addAttribute("breadcrumbs", breadcrumbService.addCurrent(breadcrumbService.home(), "Mi página"))`

---

## Loading States

### Clases

| Clase              | Descripción                    |
|--------------------|--------------------------------|
| `.skeleton`        | Base shimmer animation         |
| `.skeleton--text`  | Skeleton para texto            |
| `.skeleton--title` | Skeleton para título           |
| `.skeleton--card`  | Skeleton para card             |
| `.skeleton--avatar`| Skeleton para avatar           |
| `.btn--loading`    | Botón en estado loading        |
| `.btn__spinner`    | Spinner del botón (hidden por defecto) |

---

## Form Validation

### Clases

| Clase             | Descripción                    |
|-------------------|--------------------------------|
| `.input--error`   | Borde rojo en input con error  |
| `.field__error`   | Mensaje de error (hidden por defecto) |
| `.field--error`   | Contenedor de campo con error  |
