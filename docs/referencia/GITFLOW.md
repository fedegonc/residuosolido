# Gitflow — Flujo de trabajo del repositorio

> **Archivo de referencia, no canónico** (ver `docs/INDICE.md` y `docs/MEJORAS.md`
> #173). Es evidencia de proceso, no argumento de defensa — `docs/METODOLOGIA.md`
> cubre el encuadre académico (DSRM). Contenido vigente, se sigue actualizando.

Cómo se organizan las ramas y cómo viaja un cambio desde el trabajo local
hasta producción. Diagrama visual en `docs/diagrams/figura5-gitflow.drawio`
(renderiza en `/docs/diagramas`).

## 1. Diagrama

```
                 ┌──────────────────────────────────────────────┐
                 │              LOCAL  (tu máquina)             │
                 │                                              │
                 │   git checkout -b feature/<slug>             │
                 │        │                                     │
                 │        ▼                                     │
                 │   commits convencionales ──► push            │
                 └──────────────────────────────┬───────────────┘
                                                 │
                                                 ▼
   feature/<slug> ──merge (tests OK)──► developer ──merge (mvn clean test)──► main ──push──► Render ──auto-deploy──► Producción
       rama de                          rama de                    rama estable          PaaS              residuosolido.onrender.com
       tópico                           integración                deployable
```

## 2. Roles de las ramas

| Rama | Rol | Regla |
|---|---|---|
| `main` | Estable y deployable | Render hace **auto-deploy on push**. Nunca pushear sin `mvn clean test` verde antes |
| `developer` | Integración | Punto de partida por defecto de todo trabajo nuevo. Acá conviven los cambios antes de ir a producción |
| `feature/<slug>` | Funcionalidad nueva | Se crea desde `developer`, se mergea de vuelta a `developer` |
| `fix/<slug>` | Corrección de bug | Idem |
| `ops/<slug>` | Infra, deploy, build | Idem |
| `sec/<slug>` | Seguridad, secretos | Idem |
| `test/<slug>` | Tests, cobertura | Idem |
| `docs/<slug>` | Documentación | Idem |

## 3. Regla de oro del flujo

```
tópico → developer → main     ✅
tópico → main                 ❌ nunca directo
```

`developer` es el filtro: todo pasa por integración antes de tocar la rama
que deploya sola.

## 4. Puerta de calidad antes de `main`

Antes de mergear a `main` (que dispara el deploy automático en Render):

```bash
mvn clean test   # debe terminar en BUILD SUCCESS, 0 fallos
```

Si los tests no pasan, el cambio se queda en `developer` hasta arreglarse.

## 5. Convención de commits

Conventional Commits corto:

```
<tipo>: <resumen en minúsculas, sin punto final>
```

Tipos: `feat`, `fix`, `docs`, `ops`, `sec`, `test`, `refactor`, `chore`, `style`.

El motivo extendido (qué y por qué) va en el **body** del commit, no amontonado
en el título.

```
sec: sacar token de la url del remote        ✅
fix varias cosas del login y el CSS          ❌
```

## 6. Qué no entra al repositorio

| Elemento | Regla |
|---|---|
| `.env` con credenciales | Gitignored — solo `.env.example` va versionado |
| Tokens/claves en URLs de remote | Prohibido — el remote es `https://github.com/fedegonc/residuosolido.git` sin credenciales embebidas |
| `git config` (remote, credential.helper) | Nunca se modifica desde agentes/CI — lo ejecuta el usuario en su terminal (ver `docs/SEGURIDAD.md`) |
| Archivos de IDE, `target/`, logs | Gitignored |

## 7. Ramas remotas

| Remoto | Estado |
|---|---|
| `origin/main` | Espejo de producción |
| `origin/developer` | Espejo de integración |
| `origin/dev`, `origin/merge` | Ramas históricas, no forman parte del flujo actual |

## 8. Resumen ejecutable

```bash
# Empezar trabajo nuevo
git checkout developer && git pull
git checkout -b feature/mi-cambio

# Trabajar y commitear
git add . && git commit -m "feat: mi cambio"

# Integrar
git checkout developer && git merge feature/mi-cambio

# Preparar deploy
mvn clean test                    # verde obligatorio
git checkout main && git merge developer
git push origin main              # Render deploya solo
```
