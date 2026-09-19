# Reglas mínimas de secretos y seguridad — Eco Solicitud

Checklist corto para no repetir el leak de credencial de Mongo Atlas que
motivó `docs/TRADEOFFS.md` §26. Para el detalle y el porqué de cada decisión,
ver las referencias cruzadas de cada punto.

## Secretos / credenciales de infraestructura

1. **Nunca hardcodear credenciales** en archivos versionados: `.properties`,
   código fuente, `render.yaml`, etc. Si una clase/config necesita una
   credencial, se lee de `${VARIABLE_DE_ENTORNO}`.
2. **Todo secreto vive en dos lugares, nunca en un tercero**:
   - local → `.env` en la raíz (gitignored, `spring.config.import` lo carga
     automáticamente — ver `application.properties`).
   - prod → variables de entorno del servicio en Render (Environment del
     dashboard).
3. **`.env.example` es la plantilla pública** (sí versionada): cuando se
   agrega una clave nueva a `.env`, se agrega también ahí con un valor de
   ejemplo, nunca el real.
4. **Si una credencial se filtra** (commit accidental, log, captura de
   pantalla compartida): se rota de inmediato en el proveedor (Atlas →
   Database Access → nuevo usuario/password) y se la considera comprometida
   aunque se borre del archivo — sigue viva en el historial de git.
5. **Tokens de acceso a proveedores (GitHub, etc.) nunca van embebidos en
   la URL del remote** (`git remote -v` lo expone en texto plano vía
   `.git/config`, y ese archivo puede terminar copiado/compartido sin
   querer). Usar un credential helper (`git config --global
   credential.helper store` como mínimo, o integración con el keyring del
   SO — `git-credential-libsecret` en Linux) y dejar la URL del remote
   limpia (`https://github.com/usuario/repo.git`, sin token).
6. **Antes de commitear**, revisar `git status`/`git diff` de cualquier
   `.properties`, `.env*` o archivo de config nuevo — no confiar en
   `git add -A` a ciegas.

## Contraseñas de usuarios de la app (no confundir con lo anterior)

7. El PIN de 4 dígitos del registro simplificado es una decisión de fricción
   mínima para el MVP, no un estándar de seguridad — ver
   `docs/TRADEOFFS.md` §4 y §24 para el tradeoff completo.

## Índices y config de Mongo

8. Cambios a `@Indexed` van también en `MongoIndexMigration.java` (no
   alcanza con la anotación, `auto-index-creation` está deshabilitado a
   propósito) — ver comentario en esa clase y `CLAUDE.md`.

## Referencias

- `docs/TRADEOFFS.md` §26 — centralización de secretos (`.env`/`.env.example`), motivo completo.
- `docs/TRADEOFFS.md` §4 — contraseña mínima, fase MVP.
- `docs/MEJORAS.md` #129 — registro del cambio de centralización.
