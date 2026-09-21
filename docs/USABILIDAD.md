# Evaluación de Usabilidad — Instrumento Likert por Funcionalidad

> Instrumento de evaluación: un formulario de escala Likert por cada
> funcionalidad del sistema, para aplicar a usuarios finales. Estado:
> **preparado, aún no aplicado** — la validación con usuarios reales es
> trabajo futuro declarado (`docs/DEFENSA.md` §7).

## 1. Modelo de la evaluación

La unidad de evaluación es la **funcionalidad**, no la pantalla. Cada
funcionalidad se descompone en casos de uso, y cada caso de uso se apoya en
endpoints reales del sistema (`src/main/java/com/residuosolido/app/config/Routes.java`).
Las funcionalidades se agrupan por **actor**, porque la experiencia — y por
lo tanto el cuestionario — es por actor:

```
Funcionalidad (RF-#) → Casos de uso → Endpoints → Formulario Likert
```

**Escala Likert de 5 puntos.** El usuario marca una opción por afirmación:

| 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|
| Totalmente en desacuerdo | En desacuerdo | Neutral | De acuerdo | Totalmente de acuerdo |

**Modalidad:** cada formulario se aplica inmediatamente después de que el
usuario ejecuta la funcionalidad (test moderado o uso real). Las afirmaciones
marcadas con **(R)** están redactadas en negativo para detectar respuestas
mecánicas — al puntuar se invierten (1↔5, 2↔4).

**Criterio de lectura:** media ≥ 4.0 por funcionalidad = usable; cualquier
ítem con media ≤ 3.0 señala un punto de fricción concreto y entra como
candidato a `docs/MEJORAS.md`.

---

## 2. Datos del encuestado (cabecera única)

- Franja etaria: 18–30 / 31–45 / 46–60 / 60+
- Dispositivo usado: celular / computadora
- ¿Usás apps o webs para trámites? seguido / a veces / casi nunca
- Idioma en que usaste la app: español / português
- Rol evaluado: vecino / organización

---

## 3. Funcionalidades por actor

### Actor: Invitado (sin cuenta)

#### F1 — Registrarse (RF-1)

| Casos de uso | Endpoints |
|---|---|
| Completar el formulario; recibir errores claros; confirmar alta | GET/POST `/registrarse` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Entendí qué datos me pedían y por qué | |
| 2 | Crear la cuenta me llevó poco tiempo | |
| 3 | El PIN de 4 dígitos me pareció fácil de recordar | |
| 4 | **(R)** Tuve que reintentar porque algo falló sin entender el motivo | |
| 5 | Me sentiría cómodo/a repitiendo este registro | |

#### F2 — Iniciar sesión (RF-2)

| Casos de uso | Endpoints |
|---|---|
| Ingresar usuario+PIN; recuperarse de credenciales inválidas; entender el bloqueo por intentos | GET/POST `/entrar`, POST `/salir` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Entrar con usuario + PIN fue rápido | |
| 2 | Cuando me equivoqué, el mensaje me ayudó a corregir | |
| 3 | **(R)** No quedó claro qué pasó después de varios intentos fallidos | |
| 4 | Entendí a qué pantalla me llevó el sistema al entrar | |

#### F3 — Pedir recolección (RF-3)

| Casos de uso | Endpoints |
|---|---|
| Completar formulario (ciudad, dirección, materiales, estimaciones); elegir organización; subir foto opcional; recibir código de seguimiento | GET/POST `/solicitar`, GET `/solicitudes/org-options`, GET `/organizaciones` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Pude pedir la recolección sin crear una cuenta | |
| 2 | Elegir ciudad, materiales y organización fue claro | |
| 3 | El campo de teléfono aceptó mi número como lo escribí (con espacios, guiones o paréntesis) | |
| 4 | El código de seguimiento quedó claro y sé dónde guardarlo | |
| 5 | **(R)** En algún punto del formulario no supe qué hacer | |
| 6 | Subir una foto (si la usé) funcionó sin problemas | |

> Nota técnica del ítem 3: el servidor normaliza separadores decorativos
> (`DECORATIVE_CHARS` en `PhoneNumber`: espacios, guiones, paréntesis) — el
> usuario *no debería* notar nada. Una media baja acá indicaría fricción real
> en la entrada de datos, no en la validación.

#### F4 — Rastrear solicitud (RF-4, invitado)

| Casos de uso | Endpoints |
|---|---|
| Consultar estado con teléfono + código; interpretar el resultado | GET `/rastrear` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Encontré dónde consultar el estado de mi pedido | |
| 2 | El estado mostrado se entiende (pendiente / en camino / retirado / rechazado) | |
| 3 | **(R)** Tuve que pedir ayuda para interpretar el resultado | |
| 4 | La información que muestra es la que me interesa | |

### Actor: Ciudadano registrado (`ROLE_USER`)

#### F5 — Gestionar solicitudes propias (RF-5)

| Casos de uso | Endpoints |
|---|---|
| Listar historial con estadísticas; editar; eliminar (solo `PENDING`, RN-4/RN-11) | GET `/mis-solicitudes`, GET `/solicitudes/{id}/editar`, PUT+DELETE `/solicitudes/{id}` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Veo todas mis solicitudes con su estado | |
| 2 | Pude corregir una solicitud pendiente sin rehacerla | |
| 3 | Entendí por qué una solicitud ya no se puede editar | |
| 4 | **(R)** Me da miedo tocar algo y romperlo | |

### Actor: Organización (`ROLE_ORGANIZATION`)

#### F6 — Gestionar solicitudes asignadas (RF-6)

| Casos de uso | Endpoints |
|---|---|
| Ver panel con estadísticas; filtrar por estado; ver detalle; aceptar con franja horaria; rechazar; completar (RN-1, RN-2) | GET `/acopio/solicitudes`, GET `/acopio/solicitudes/{id}`, POST `.../aceptar` `.../rechazar` `.../completar` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Distingo las solicitudes nuevas de las que ya gestioné | |
| 2 | Aceptar con franja horaria es un paso claro | |
| 3 | Rechazar o completar una solicitud es directo | |
| 4 | El detalle de la solicitud tiene todo lo que necesito para ir a buscar el material | |
| 5 | **(R)** A veces no sé en qué estado quedó una solicitud | |

#### F7 — Completar perfil (RF-7)

| Casos de uso | Endpoints |
|---|---|
| Configurar ciudad, teléfono y materiales aceptados; entender el onboarding forzado (RN-9) | GET/PUT `/mi-organizacion` |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Configurar ciudad, teléfono y materiales que aceptamos fue simple | |
| 2 | Entiendo que sin perfil completo no aparecemos para los vecinos | |
| 3 | **(R)** El formulario de perfil me resultó confuso | |

### Transversal (todos los actores)

#### F8 — Idioma, navegación y uso en celular

| Casos de uso | Endpoints |
|---|---|
| Cambiar español↔portugués; navegar por el menú; usar la app en pantalla chica; instalarla como app | GET `/change-language`, home `/`, navbar/footer |

| # | Afirmación | 1–5 |
|---|---|---|
| 1 | Pude cambiar entre español y portugués fácilmente | |
| 2 | La app se ve y funciona bien en mi celular | |
| 3 | Los botones y menús se ven y responden como espero | |
| 4 | En general, usar la app me dio confianza | |

### Fuera del instrumento

- **RF-8 (catadores):** funcionalidad latente — el CRUD existe pero no tiene
  acceso en la UI (`docs/TRADEOFFS.md` §6). No se encuesta lo que el usuario
  no puede tocar.
- `/seed`, `/api/**`, `/docs/**`, `/scratch/**`, actuator: superficies
  técnicas/de demo, no funcionalidades de usuario.

---

## 4. Preguntas abiertas (cierre, opcionales)

1. ¿Qué fue lo más difícil o confuso?
2. ¿Qué le cambiarías primero?
3. ¿Hay algo que esperabas que la app haga y no hace?

---

## 5. Aplicación sugerida

- **Muestra mínima razonable:** 5–8 usuarios por rol (los hallazgos de
  usabilidad saturan rápido — Nielsen: ~5 usuarios detectan ~85% de los
  problemas de una superficie).
- **Escenario:** vecino con celular → F1–F5 + F8; operador de cooperativa →
  F6–F8.
- **Resultados:** tabla de medias por ítem → los ≤3.0 entran como candidatos
  a `docs/MEJORAS.md`.

## 6. Trazabilidad y límite declarado

Cada funcionalidad F# cuelga de un RF de `docs/REQUISITOS.md` y de endpoints
reales de `Routes.java` — el instrumento cubre la especificación, no pregunta
en abstracto. Es el *instrumento*; su aplicación con usuarios reales y el
análisis de resultados quedan como trabajo futuro. No afirmar en la defensa
que la usabilidad fue validada — afirmar que el instrumento existe, está
mapeado 1:1 a los RF implementados, y es aplicable tal cual.
