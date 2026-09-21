# Correcciones y Hardening — Eco Solicitud

> **Archivo de referencia, no canónico** (ver `docs/INDICE.md` y `docs/MEJORAS.md`
> #173). Su contenido vigente ya está en `docs/MEJORAS.md` (fixes con entrada
> propia) y `docs/DEFENSA.md` §7 (limitaciones); la arquitectura de i18n que
> aportaba pasó a `docs/ARQUITECTURA.md`. Se conserva como historial fechado
> (2026-09-11 y anteriores), no se actualiza más.

Registro de correcciones aplicadas, hardening y limpieza del código. Extraído de los anexos de `MEJORAS.md` (que queda como tabla de superficies).

# Hardening — Correcciones aplicadas (anexo)


Este documento describe las correcciones aplicadas al MVP y las limitaciones
que quedan fuera del alcance actual. Forma parte del análisis crítico del
sistema y debe consultarse junto con los diagramas de `docs/diagrams/`.

## Correcciones aplicadas

### 1. Registro de cuentas

- **Antes:** el controlador recibía directamente la entidad `User` mediante
  `@ModelAttribute` y la persistía. Un cliente podía enviar campos internos
  (`id`, `profileCompleted`, `acceptedMaterials`, `role`).
- **Ahora:** el controlador recibe un `RegistrationForm` (DTO) con solo
  `username`, `email` y `password`. El servicio construye una entidad nueva
  bajo control del servidor y asigna el rol según el checkbox de
  organización.
- **Validaciones:** username obligatorio y sin espacios; contraseña mínima
  de 8 caracteres (ver sección 11); email válido y normalizado a
  minúsculas; unicidad de username y email con índices únicos en MongoDB
  y manejo de `DuplicateKeyException`.

### 2. Rastreo de solicitudes de invitado

- **Antes:** el rastreo público consultaba por teléfono solo. Cualquier
  persona que conociera el teléfono podía ver dirección, materiales y
  organización de solicitudes ajenas.
- **Ahora:** cada solicitud de invitado recibe un código privado de 8
  caracteres al crearse. El rastreo requiere teléfono + código. El teléfono
  solo no devuelve resultados.
- **UI:** la pantalla de éxito muestra el código al invitado; el formulario
  de rastreo pide ambos campos; los textos (es/pt) fueron actualizados.
- **Solicitudes antiguas sin código:** no son rastreables por el flujo
  público. La organización puede consultarlas desde su panel. No se habilitó
  acceso inseguro por compatibilidad.

### 3. Disponibilidad de organizaciones

- **Antes:** `getOrganizationsByCity` caía a organizaciones inactivas si no
  había activas. La asignación de una solicitud no verificaba `active`.
- **Ahora:** una organización recibe solicitudes solo si está activa, tiene
  rol `ORGANIZATION`, perfil completo, teléfono válido, ciudad coincidente
  y materiales aceptados no vacíos. El listado por ciudad nunca cae a
  organizaciones inactivas o incompletas.

### 4. Validación de materiales

- **Antes:** el navegador ocultaba los materiales no aceptados, pero el
  servidor no verificaba la compatibilidad.
- **Ahora:** el servidor valida que todos los materiales solicitados estén
  incluidos en `acceptedMaterials` de la organización, tanto en la creación
  como en la edición.

### 5. Persistencia de imagen

- **Antes:** `createRequestWithImage` persistía la solicitud y después
  subía la imagen. Si la imagen fallaba, la solicitud quedaba huérfana.
- **Ahora:** la imagen se valida (tipo, extensión, tamaño) antes de
  persistir la solicitud. Una imagen inválida no deja solicitud en la base.

### 6. Borrado concurrente

- **Antes:** el borrado hacía `deleteById` sin verificar versión. Entre la
  verificación de propiedad/estado y el borrado, una organización podía
  aceptar la solicitud.
- **Ahora:** el borrado usa `delete(entity)` sobre la entidad cargada con
  su `@Version`. Si una transición concurrente la modificó, se lanza
  `OptimisticLockingFailureException` con un mensaje controlado.

### 7. Edición de solicitudes

- **Antes:** la edición no revalidaba la compatibilidad de materiales con la
  organización.
- **Ahora:** la edición revalida ciudad, dirección, materiales,
  organización y compatibilidad de materiales. Solo las solicitudes
  `PENDING` pueden editarse.

### 8. Teléfono canónico

- **Antes:** los formatos `+598 99 123 456` y `+59899123456` se trataban
  como distintos.
- **Ahora:** `PhoneNumber` normaliza espacios y valida formato
  internacional. La misma canonicidad se aplica a usuarios, invitados,
  recolectores informales y consultas de rastreo.

### 9. Actualización de perfil

- **Antes:** `updateUser` confiaba en los campos de identidad del objeto
  enviado.
- **Ahora:** carga el usuario persistido, valida email duplicado, normaliza
  teléfono y actualiza solo los campos permitidos.

### 10. Promesa de "más cercana"

- **Antes:** la portada decía que la solicitud se enviaba a la cooperativa
  "más cercana", pero el sistema implementa elección explícita del usuario.
- **Ahora:** los textos (es/pt) describen elección explícita por ciudad y
  materiales.


# Correcciones y limpieza (anexo)


Fecha: 2026-09-11
Versión: post-cleanup (sin tag aún)
Tests: 181, 0 failures, 0 errors, 0 skipped
Build: SUCCESS

---

## 1. Defectos corregidos

### 1.1 Seed destructivo (CRÍTICO)

**Defecto:** `SeedDataFactory.seedAll()` ejecutaba `deleteAll()` sobre usuarios, solicitudes y catadores antes de cargar datos de prueba. Si `app.seed=true` estaba activo, borraba datos reales sin confirmación.

**Corrección:**
- Se eliminaron los tres `deleteAll()`.
- Se agregó un guard: si cualquier colección tiene datos, el seed se omite.
- `application-dev.properties` cambió a `app.seed=false`.
- `DataLoader` se restringió a `@Profile("dev & !prod & !test")`.
- Se agregó `SeedSafetyTest` con 3 tests que verifican que el guard funciona.

**Verificación:** `SeedSafetyTest` pasa. El seed no se ejecuta si hay datos existentes.

### 1.2 FOUC (flash de texto sin traducir)

**Defecto:** `app.js` hacía `fetch('/i18n/{page}/{lang}.json')` asíncrono y reemplazaba los `[data-i18n]` después de que la página se renderizaba. El usuario veía el texto fallback primero y luego el traducido.

**Corrección:**
- Se creó `UiCopyCatalog.java` (`@ControllerAdvice`) que carga los JSON i18n server-side y los inyecta en `window.uiCopies` en el `base.html`.
- `app.js` ahora usa `window.uiCopies` directamente, sin fetch asíncrono.
- Se eliminó el `visibility:hidden` del `<html>` que era un parche anterior.

**Verificación:** El HTML servido incluye `window.uiCopies` con todas las traducciones. El JS las aplica sincrónicamente.

### 1.3 Copies stale en JSON i18n

**Defecto:** Los templates tenían copies actualizados, pero los JSON i18n todavía contenían texto viejo ("Juntos por un futuro más limpio", "Rastrear solicitud", "Cuidemos nuestra ciudad"). El JS los cargaba y sobrescribía el texto correcto.

**Corrección:**
- Se actualizaron todos los JSON i18n: home, auth, track, common, requests (es y pt).
- Se unificó el verbo "Rastrear" → "Buscar" en toda la interfaz de usuario.
- Se unificó "Enviar solicitud" → "Pedir recolección" en el botón del formulario.
- Se actualizaron los fallbacks de los templates para que coincidan.
- Se corrigieron `messages.properties`, `messages_es.properties`, `messages_pt.properties`.

**Verificación:** `grep -rn "Rastrear\|Juntos\|Cuidemos" src/main/resources/` no encuentra copies activos (solo URLs `/rastrear` que son rutas del servidor).

### 1.4 Política de contraseña débil

**Defecto:** `UserService.validatePassword()` aceptaba contraseñas de 3 caracteres. Insuficiente para un servicio público.

**Corrección:**
- Se subió el mínimo a 8 caracteres.
- Se actualizaron los mensajes de error en ES y PT.
- Se actualizó el test `I18nMessageResolutionTest`.
- Se actualizó el hint del template de registro.
- El demo password `12345678` (8 caracteres) seguía funcionando en ese momento.

**Verificación:** Los tests existentes usan "12" (2 caracteres) y null, que siguen fallando la validación. Los tests pasan.

**Superseded (ver `docs/MEJORAS.md` #155):** esta validación de 8 caracteres resultó ser código muerto — nunca hubo un caller real que pasara un password no-nulo a `UserService.updateUser`. Se borró por completo; el sistema real usa PIN de 4 dígitos (`UserRegistrationService`), no esta política. El demo password del seed hoy es `1234`.

### 1.5 Recursos PWA no autorizados

**Defecto:** `SecurityConfig` no permitía acceso público a `/manifest.json`, `/sw.js`, `/icon-*.png`, `/icon-*.svg`. Un usuario no autenticado no podía instalar la PWA.

**Corrección:**
- Se agregaron `/manifest.json`, `/sw.js`, `/icon-*.png`, `/icon-*.svg` a los `permitAll()`.

**Verificación:** Los recursos se sirven sin redirección a login.

### 1.6 Service worker pre-cacheaba HTML stale

**Defecto:** El SW pre-cacheaba `'/'` en `STATIC_ASSETS`. Cuando se actualizaba el SW, cacheaba la página vieja. El usuario veía contenido stale por horas.

**Corrección:**
- `'/'` removido de `STATIC_ASSETS`.
- HTML/API: network-first puro, sin guardar en cache.
- Registro con `updateViaCache: 'none'`.
- `reg.update()` en cada carga.
- Cache version bumped a `v19`.

**Verificación:** El SW no pre-cachea HTML. Las páginas siempre van al server.

### 1.7 Navbar móvil sin botones de auth

**Defecto:** En móvil, `.navbar__actions` se ocultaba y los botones Entrar/Registrarse estaban dentro. Desaparecían.

**Corrección:**
- Se movieron los botones a `.dropdown__auth` separado.
- Registrarse usa `btn--primary`, Entrar usa `btn--outline`.
- Se agregó CSS para `.dropdown__auth`.

**Verificación:** Los botones son visibles en el dropdown móvil.

### 1.8 Manifest con nombre incorrecto

**Defecto:** `manifest.json` no coincidía con el branding vigente del sistema.

**Corrección:** Se actualizó el nombre y short_name.

---

## 2. Limitaciones pendientes (no corregidas)

### 2.1 CSP con `unsafe-inline`

El CSP usa `script-src 'unsafe-inline'` y `style-src 'unsafe-inline'`. Es necesario para:
- `window.uiCopies = [[${uiCopies}]]` (script inline en `base.html`).
- Estilos inline residuales.

**Para corregir:** Usar nonces o hashes CSP. Requiere cambio arquitectónico.

### 2.2 PWA descartada (histórico — ya no aplica)

**Obsoleto.** La PWA (manifest, install prompt, cache offline) se descartó
por completo (#11, #23). El único remanente en el código es `sw.js`, un
Service Worker "kill-switch" (#100) que se autodesregistra y limpia la
caché vieja en navegadores que habían instalado la PWA antes de que se
sacara — no cachea nada nuevo, no hay funcionalidad offline.

### 2.3 `EndToEndFlowsTest` sigue siendo MockMvc (parcialmente resuelto)

`EndToEndFlowsTest` usa MockMvc, no un navegador real. **Esto ya no es
toda la historia:** hoy existen 6 clases con Playwright/Chromium real
(`browser/CitizenBrowserTest`, `OrganizationBrowserTest`,
`GuestBrowserTest`, `FullLifecycleBrowserTest`, `HomePageBrowserTest`,
`TransversalBrowserTest`, sobre `PlaywrightBaseTest`) que cubren los
flujos de registro/login, creación de solicitud, aceptación/rechazo y
rastreo de invitado en un navegador real.

**Pendiente real:** `EndToEndFlowsTest` (11 tests) no se migró a
Playwright — sigue siendo MockMvc. No haría falta migrarlo si los
Playwright ya cubren los mismos flujos; si no coinciden 1:1, revisar cuál
de los dos suites tiene huecos.

### 2.4 Sin validación de usabilidad

No hay evaluación con usuarios reales. Los tests verifican correctitud funcional, no usabilidad.

**Para corregir:** Realizar una evaluación formativa con ciudadanos y organizaciones.

### 2.5 Sin medición de impacto

No se puede afirmar que el sistema aumenta el reciclaje o los ingresos. Eso requiere un piloto de campo.

---

## 3. Tradeoffs intencionales

| Decisión | Beneficio | Costo |
|---|---|---|
| HTML no cacheado en SW | Sin contenido stale | Sin offline |
| `window.uiCopies` inline | Sin FOUC, sin fetch asíncrono | CSP requiere `unsafe-inline` |
| Password mínimo 8 | Defendible para MVP | No incluye complejidad (mayús/números) |
| Render.com (PaaS) | Depliegue simple | Menos control que IaaS |
| MongoDB | Persistencia documental | PostgreSQL también válido |
| Sin GPS/mapas | MVP enfocado | Sin geolocalización |

---

## 4. Política de fuente de verdad de copies

El sistema tiene dos fuentes de copies (un `messages_{es,pt}.properties`
separado ya no existe — se unificó todo en JSON, ver `JsonMessageSource`):

1. **`static/i18n/{lang}.json`** (uno por idioma, no por página) — fuente
   única. `UiCopyCatalog` inyecta las claves cliente en `window.uiCopies`;
   `JsonMessageSource` resuelve las claves `_server_*` server-side
   (validación de formularios, flash messages) leyendo el mismo archivo.
2. **Fallback en templates** — texto visible si el JS falla. Debe coincidir con el JSON.

**Regla:** Cuando se cambia un copy, se actualizan ambas fuentes. `docs/METODOLOGIA.md` es el inventario, no la fuente de runtime.

---

## 5. Comando de tests

```bash
mvn test
```

Resultado actual:

```
Tests run: 181, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Requiere MongoDB en `localhost:27017`.

---

## 6. Comportamiento del Service Worker actual (post-descarte de PWA)

**Obsoleto — ya no hay cache real.** Esta sección describía la PWA activa
(`ecosolicitud-v19`, precacheo de CSS/JS/manifest/icons). Esa PWA se
descartó (#11, #23). El `sw.js` actual (#100) no precachea nada: solo se
instala, se autodesregistra (`self.registration.unregister()`) y limpia
cualquier caché vieja que haya quedado en el navegador del usuario. No
hay comportamiento offline de ningún tipo hoy.

---

## 7. Datos de demo (solo desarrollo)

El seed crea 10 usuarios y 12 solicitudes. Password: `1234`.

- `juan` / `1234` — usuario
- `coopverde` / `1234` — organización

**Importante:** El seed solo se ejecuta si `app.seed=true` Y la base está vacía. No borra datos existentes.

---

## 8. Archivos nuevos

- `src/main/java/com/residuosolido/app/config/UiCopyCatalog.java` — carga JSON i18n server-side.
- ~~`src/main/resources/static/js/theme.js`~~ — creado en esta iteración y **eliminado** después junto con el dark mode (SYNC-08).
- `src/test/java/com/residuosolido/app/config/SeedSafetyTest.java` — 3 tests de seguridad del seed.

## 9. Archivos eliminados

- `src/main/java/com/residuosolido/app/config/UiCopyDialect.java` — no necesario, eliminado.

---

