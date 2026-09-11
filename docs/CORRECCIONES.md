# Correcciones y limpieza — Residuo Sólido

Fecha: 2026-09-11
Versión: post-cleanup (sin tag aún)
Tests: 218, 0 failures, 0 errors, 0 skipped
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

**Defecto:** `AccountInput.password()` aceptaba contraseñas de 3 caracteres. Insuficiente para un servicio público.

**Corrección:**
- Se subió el mínimo a 8 caracteres.
- Se actualizaron los mensajes de error en ES y PT.
- Se actualizó el test `I18nMessageResolutionTest`.
- Se actualizó el hint del template de registro.
- El demo password `12345678` (8 caracteres) sigue funcionando.

**Verificación:** Los tests existentes usan "12" (2 caracteres) y null, que siguen fallando la validación. Los tests pasan.

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

**Defecto:** `manifest.json` decía "EcoSolicitud" en lugar de "Residuo Sólido".

**Corrección:** Se actualizó el nombre y short_name.

---

## 2. Limitaciones pendientes (no corregidas)

### 2.1 CSP con `unsafe-inline`

El CSP usa `script-src 'unsafe-inline'` y `style-src 'unsafe-inline'`. Es necesario para:
- `window.uiCopies = [[${uiCopies}]]` (script inline en `base.html`).
- Estilos inline residuales.

**Para corregir:** Usar nonces o hashes CSP. Requiere cambio arquitectónico.

### 2.2 PWA sin offline completo

La PWA se puede instalar, pero HTML no está cacheado intencionalmente. Navegación offline no funciona. Solo CSS/JS/imágenes se cachean.

**No se corrige:** Es un tradeoff intencional para evitar contenido stale.

### 2.3 Tests E2E son MockMvc

`EndToEndFlowsTest` usa MockMvc con servicios simulados. No es un test de navegador real.

**Para corregir:** Agregar tests con Selenium o Playwright.

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

El sistema tiene tres fuentes de copies:

1. **`static/i18n/{page}/{lang}.json`** — fuente primaria. `UiCopyCatalog` los carga server-side y los inyecta en `window.uiCopies`.
2. **`messages_{es,pt}.properties`** — mensajes de validación del servidor (errores de formularios, flash messages).
3. **Fallback en templates** — texto visible si el JS falla. Debe coincidir con el JSON.

**Regla:** Cuando se cambia un copy, se actualizan las tres fuentes. `docs/COPIES.md` es el inventario, no la fuente de runtime.

---

## 5. Comando de tests

```bash
mvn test
```

Resultado actual:

```
Tests run: 218, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Requiere MongoDB en `localhost:27017`.

---

## 6. Comportamiento PWA/cache actual

- **Cache version:** `ecosolicitud-v19`
- **Pre-cacheados:** CSS, JS, manifest, icons, favicon
- **No pre-cacheados:** HTML, API
- **HTML/API:** network-first, sin guardar en cache
- **Actualización:** `updateViaCache: 'none'` + `reg.update()` en cada carga
- **Offline:** CSS/JS/imágenes disponibles; HTML no

---

## 7. Datos de demo (solo desarrollo)

El seed crea 10 usuarios y 12 solicitudes. Password: `12345678`.

- `juan` / `12345678` — usuario
- `coopverde` / `12345678` — organización

**Importante:** El seed solo se ejecuta si `app.seed=true` Y la base está vacía. No borra datos existentes.

---

## 8. Archivos nuevos

- `src/main/java/com/residuosolido/app/config/UiCopyCatalog.java` — carga JSON i18n server-side.
- `src/main/resources/static/js/theme.js` — aplica tema antes de pintar (sin inline).
- `src/test/java/com/residuosolido/app/config/SeedSafetyTest.java` — 3 tests de seguridad del seed.

## 9. Archivos eliminados

- `src/main/java/com/residuosolido/app/config/UiCopyDialect.java` — no necesario, eliminado.
